package com.example.data.repository

import com.example.data.local.db.ContentSettingsDao
import com.example.data.local.db.ContentSettingsEntity
import com.example.data.local.db.DhikrDao
import com.example.data.local.db.DhikrEntity
import com.example.data.local.db.FavoriteDao
import com.example.data.local.db.FavoriteEntity
import com.example.data.local.db.HadithDao
import com.example.data.local.db.HadithEntity
import com.example.data.local.db.QuranDao
import com.example.data.local.db.QuranVerseEntity
import com.example.data.local.db.ReviewStatus
import com.example.data.sources.SunnahComIntegration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * 1. QuranRepository
 * Strictly exposes APPROVED verses to the UI from local Room database on Dispatchers.IO
 * with a fast thread-safe In-Memory Cache for 0ms overlay/UI reads.
 */
class QuranRepository(
    private val quranDao: QuranDao
) {
    @Volatile
    private var cachedApprovedVerses: List<QuranVerseEntity>? = null

    fun getApprovedVerses(): Flow<List<QuranVerseEntity>> = quranDao.getApprovedVerses()
        .onEach { cachedApprovedVerses = it }
        .flowOn(Dispatchers.IO)

    fun getApprovedVersesByTopic(topic: String): Flow<List<QuranVerseEntity>> =
        (if (topic == "الكل") quranDao.getApprovedVerses() else quranDao.getApprovedVersesByTopic(topic))
            .flowOn(Dispatchers.IO)

    fun searchApprovedVerses(query: String): Flow<List<QuranVerseEntity>> =
        (if (query.isBlank()) quranDao.getApprovedVerses() else quranDao.searchApprovedVerses(query.trim()))
            .flowOn(Dispatchers.IO)

    suspend fun getApprovedVersesCached(): List<QuranVerseEntity> {
        cachedApprovedVerses?.takeIf { it.isNotEmpty() }?.let { return it }
        return withContext(Dispatchers.IO) {
            quranDao.getApprovedVerses().first().also { cachedApprovedVerses = it }
        }
    }

    suspend fun getVerseById(id: String): QuranVerseEntity? = withContext(Dispatchers.IO) {
        quranDao.getVerseById(id)
    }

    suspend fun importTanzilVerses(verses: List<QuranVerseEntity>): Int = withContext(Dispatchers.IO) {
        val inserted = quranDao.insertVerses(verses)
        cachedApprovedVerses = null
        inserted.count { it != -1L }
    }

    suspend fun getApprovedCount(): Int = withContext(Dispatchers.IO) {
        quranDao.getApprovedCount()
    }
}

/**
 * 2. HadithRepository
 * Strictly exposes APPROVED prophetic hadiths from local Room database on Dispatchers.IO
 * with In-Memory Cache.
 */
class HadithRepository(
    private val hadithDao: HadithDao,
    private val sunnahIntegration: SunnahComIntegration = SunnahComIntegration()
) {
    @Volatile
    private var cachedApprovedHadiths: List<HadithEntity>? = null

    fun getApprovedHadiths(): Flow<List<HadithEntity>> = hadithDao.getApprovedHadiths()
        .onEach { cachedApprovedHadiths = it }
        .flowOn(Dispatchers.IO)

    fun getApprovedHadithsByTopic(topic: String): Flow<List<HadithEntity>> =
        (if (topic == "الكل") hadithDao.getApprovedHadiths() else hadithDao.getApprovedHadithsByTopic(topic))
            .flowOn(Dispatchers.IO)

    fun searchApprovedHadiths(query: String): Flow<List<HadithEntity>> =
        (if (query.isBlank()) hadithDao.getApprovedHadiths() else hadithDao.searchApprovedHadiths(query.trim()))
            .flowOn(Dispatchers.IO)

    suspend fun getApprovedHadithsCached(): List<HadithEntity> {
        cachedApprovedHadiths?.takeIf { it.isNotEmpty() }?.let { return it }
        return withContext(Dispatchers.IO) {
            hadithDao.getApprovedHadiths().first().also { cachedApprovedHadiths = it }
        }
    }

    suspend fun getHadithById(id: String): HadithEntity? = withContext(Dispatchers.IO) {
        hadithDao.getHadithById(id)
    }

    suspend fun insertHadiths(hadiths: List<HadithEntity>): Int = withContext(Dispatchers.IO) {
        val inserted = hadithDao.insertHadiths(hadiths)
        cachedApprovedHadiths = null
        inserted.count { it != -1L }
    }

    suspend fun fetchAndImportFromSunnahCom(
        collectionName: String,
        apiKey: String?,
        page: Int = 1
    ): Result<Int> {
        val fetchResult = sunnahIntegration.fetchCollectionHadiths(collectionName, apiKey, page)
        return fetchResult.map { hadiths ->
            insertHadiths(hadiths)
        }
    }

    suspend fun getApprovedCount(): Int = withContext(Dispatchers.IO) {
        hadithDao.getApprovedCount()
    }
}

/**
 * 3. DhikrRepository
 * Strictly exposes APPROVED Fortress of the Muslim (حصن المسلم) supplications on Dispatchers.IO
 * with In-Memory Cache.
 */
class DhikrRepository(
    private val dhikrDao: DhikrDao
) {
    @Volatile
    private var cachedApprovedDhikr: List<DhikrEntity>? = null

    fun getApprovedDhikr(): Flow<List<DhikrEntity>> = dhikrDao.getApprovedDhikr()
        .onEach { cachedApprovedDhikr = it }
        .flowOn(Dispatchers.IO)

    fun getApprovedDhikrByCategory(category: String): Flow<List<DhikrEntity>> =
        (if (category == "الكل") dhikrDao.getApprovedDhikr() else dhikrDao.getApprovedDhikrByCategory(category))
            .flowOn(Dispatchers.IO)

    suspend fun getApprovedDhikrCached(): List<DhikrEntity> {
        cachedApprovedDhikr?.takeIf { it.isNotEmpty() }?.let { return it }
        return withContext(Dispatchers.IO) {
            dhikrDao.getApprovedDhikr().first().also { cachedApprovedDhikr = it }
        }
    }

    suspend fun getDhikrById(id: String): DhikrEntity? = withContext(Dispatchers.IO) {
        dhikrDao.getDhikrById(id)
    }

    suspend fun insertDhikr(dhikrList: List<DhikrEntity>): Int = withContext(Dispatchers.IO) {
        val inserted = dhikrDao.insertDhikr(dhikrList)
        cachedApprovedDhikr = null
        inserted.count { it != -1L }
    }

    suspend fun getApprovedCount(): Int = withContext(Dispatchers.IO) {
        dhikrDao.getApprovedCount()
    }
}

/**
 * 4. FavoriteRepository
 * Offline-first local management of user favorites on Dispatchers.IO.
 */
class FavoriteRepository(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites().flowOn(Dispatchers.IO)

    fun getFavoritesByType(contentType: String): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavoritesByType(contentType).flowOn(Dispatchers.IO)

    fun isFavorite(contentId: String): Flow<Boolean> = favoriteDao.isFavorite(contentId).flowOn(Dispatchers.IO)

    suspend fun toggleFavorite(contentId: String, contentType: String) = withContext(Dispatchers.IO) {
        val exists = favoriteDao.isFavoriteSync(contentId)
        if (exists) {
            favoriteDao.deleteFavoriteByContentId(contentId)
        } else {
            val entity = FavoriteEntity(
                id = UUID.randomUUID().toString(),
                contentId = contentId,
                contentType = contentType,
                createdAt = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(entity)
        }
    }

    suspend fun removeFavorite(contentId: String) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavoriteByContentId(contentId)
    }
}

/**
 * 5. ContentApprovalRepository
 * Manages the reliability system on Dispatchers.IO.
 */
class ContentApprovalRepository(
    private val quranDao: QuranDao,
    private val hadithDao: HadithDao,
    private val dhikrDao: DhikrDao,
    private val contentSettingsDao: ContentSettingsDao
) {
    fun getPendingVerses(): Flow<List<QuranVerseEntity>> = quranDao.getPendingVerses().flowOn(Dispatchers.IO)
    fun getPendingHadiths(): Flow<List<HadithEntity>> = hadithDao.getPendingHadiths().flowOn(Dispatchers.IO)
    fun getPendingDhikr(): Flow<List<DhikrEntity>> = dhikrDao.getPendingDhikr().flowOn(Dispatchers.IO)

    suspend fun approveQuranVerse(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        quranDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectQuranVerse(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        quranDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    suspend fun approveHadith(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        hadithDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectHadith(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        hadithDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    suspend fun approveDhikr(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        dhikrDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectDhikr(id: String, reviewer: String, date: String) = withContext(Dispatchers.IO) {
        dhikrDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    fun getContentSettings(): Flow<List<ContentSettingsEntity>> = contentSettingsDao.getAllSettings().flowOn(Dispatchers.IO)

    suspend fun updateContentSetting(contentType: String, enabled: Boolean, frequency: String) = withContext(Dispatchers.IO) {
        contentSettingsDao.upsertSettings(
            ContentSettingsEntity(
                contentType = contentType,
                enabled = enabled,
                displayFrequency = frequency
            )
        )
    }
}
