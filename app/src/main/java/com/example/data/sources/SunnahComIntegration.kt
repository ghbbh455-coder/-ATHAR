package com.example.data.sources

import com.example.data.local.db.HadithEntity
import com.example.data.local.db.ReviewStatus
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Sunnah.com API Data Transfer Objects (Moshi compatible)
 */
@JsonClass(generateAdapter = true)
data class SunnahCollectionItem(
    @Json(name = "name") val name: String,
    @Json(name = "title") val title: String,
    @Json(name = "hasBooks") val hasBooks: Boolean = true,
    @Json(name = "totalHadith") val totalHadith: Int = 0
)

@JsonClass(generateAdapter = true)
data class SunnahGrading(
    @Json(name = "grade") val grade: String,
    @Json(name = "graded_by") val gradedBy: String?
)

@JsonClass(generateAdapter = true)
data class SunnahHadithDetail(
    @Json(name = "collection") val collection: String,
    @Json(name = "bookNumber") val bookNumber: String?,
    @Json(name = "chapterId") val chapterId: String?,
    @Json(name = "hadithNumber") val hadithNumber: String,
    @Json(name = "hadith") val hadithContent: List<SunnahHadithLang>,
    @Json(name = "grades") val grades: List<SunnahGrading>?
)

@JsonClass(generateAdapter = true)
data class SunnahHadithLang(
    @Json(name = "lang") val lang: String,
    @Json(name = "body") val body: String,
    @Json(name = "chapterTitle") val chapterTitle: String?
)

@JsonClass(generateAdapter = true)
data class SunnahHadithListResponse(
    @Json(name = "data") val data: List<SunnahHadithDetail>?,
    @Json(name = "total") val total: Int = 0
)

/**
 * Retrofit interface for Sunnah.com API
 * Note: API Key header `x-api-key` is dynamically passed; no key is ever hardcoded.
 */
interface SunnahApiService {

    @GET("v1/collections")
    suspend fun getCollections(
        @Header("x-api-key") apiKey: String
    ): List<SunnahCollectionItem>

    @GET("v1/collections/{collectionName}/hadiths")
    suspend fun getHadithsByCollection(
        @Path("collectionName") collectionName: String,
        @Header("x-api-key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): SunnahHadithListResponse

    @GET("v1/collections/{collectionName}/hadiths/{hadithNumber}")
    suspend fun getHadithByNumber(
        @Path("collectionName") collectionName: String,
        @Path("hadithNumber") hadithNumber: String,
        @Header("x-api-key") apiKey: String
    ): SunnahHadithDetail
}

/**
 * Integration layer with Sunnah.com API
 * Strictly enforces:
 * - reviewStatus = PENDING by default
 * - Separation between text and grading
 * - Dynamic API key injection with graceful offline fallback
 */
class SunnahComIntegration(
    private val apiService: SunnahApiService? = null
) {
    companion object {
        const val BASE_URL = "https://api.sunnah.com/"

        fun createClient(): SunnahApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(SunnahApiService::class.java)
        }
    }

    /**
     * Converts Sunnah.com DTO into HadithEntity
     * Separates arabicText from grading, and tags status as PENDING
     */
    fun convertToEntity(detail: SunnahHadithDetail): HadithEntity {
        val arabicLang = detail.hadithContent.find { it.lang == "ar" }
        val rawArabic = arabicLang?.body ?: ""
        // Strip HTML tags if returned by API
        val cleanArabic = rawArabic.replace(Regex("<.*?>"), "").trim()
        val chapter = arabicLang?.chapterTitle ?: "باب في السنة النبوية"

        val firstGrade = detail.grades?.firstOrNull()
        val grading = firstGrade?.grade ?: "موقوف على المراجعة"
        val gradingSource = firstGrade?.gradedBy ?: "بانتظار تدقيق المحدث المختص"

        return HadithEntity(
            id = "sunnah_${detail.collection}_${detail.hadithNumber}",
            arabicText = cleanArabic,
            collectionName = detail.collection,
            bookName = "كتاب رقم ${detail.bookNumber ?: "-"}",
            chapterName = chapter,
            hadithNumber = detail.hadithNumber,
            grading = grading,
            gradingSource = gradingSource,
            sourceUrl = "https://sunnah.com/${detail.collection}:${detail.hadithNumber}",
            reviewStatus = ReviewStatus.PENDING, // Mandate: Must be PENDING by default
            reviewer = "بانتظار مراجعة لجنة التحقيق",
            reviewDate = "",
            topic = "حديث مستورد من Sunnah.com",
            benefit = "لم يعتمد للنشر بعد - يخضع لإجراءات التحقيق والاعتماد الشرعي."
        )
    }

    /**
     * Fetches hadiths from Sunnah.com API if user provided an API key.
     * If no API key is provided, returns failure gracefully without failing offline app operations.
     */
    suspend fun fetchCollectionHadiths(
        collectionName: String,
        apiKey: String?,
        page: Int = 1
    ): Result<List<HadithEntity>> {
        if (apiKey.isNullOrBlank()) {
            return Result.failure(
                IllegalStateException("مفتاح API الخاص بـ Sunnah.com غير مهيأ. التطبيق يعمل بوضع عدم الاتصال بالكامل من قاعدة البيانات المعتمدة محلياً.")
            )
        }

        return try {
            val service = apiService ?: createClient()
            val response = service.getHadithsByCollection(collectionName, apiKey, page = page)
            val entities = response.data?.map { convertToEntity(it) } ?: emptyList()
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
