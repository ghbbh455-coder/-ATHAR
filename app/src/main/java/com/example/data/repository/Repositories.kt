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
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 1. QuranRepository
 * Strictly exposes APPROVED verses to the UI from local Room database.
 */
class QuranRepository(
    private val quranDao: QuranDao
) {
    fun getApprovedVerses(): Flow<List<QuranVerseEntity>> = quranDao.getApprovedVerses()

    fun getApprovedVersesByTopic(topic: String): Flow<List<QuranVerseEntity>> =
        if (topic == "الكل") quranDao.getApprovedVerses() else quranDao.getApprovedVersesByTopic(topic)

    fun searchApprovedVerses(query: String): Flow<List<QuranVerseEntity>> =
        if (query.isBlank()) quranDao.getApprovedVerses() else quranDao.searchApprovedVerses(query.trim())

    suspend fun getVerseById(id: String): QuranVerseEntity? = quranDao.getVerseById(id)

    suspend fun importTanzilVerses(verses: List<QuranVerseEntity>): Int {
        val inserted = quranDao.insertVerses(verses)
        return inserted.count { it != -1L }
    }

    suspend fun getApprovedCount(): Int = quranDao.getApprovedCount()
}

/**
 * 2. HadithRepository
 * Strictly exposes APPROVED prophetic hadiths from local Room database.
 */
class HadithRepository(
    private val hadithDao: HadithDao,
    private val sunnahIntegration: SunnahComIntegration = SunnahComIntegration()
) {
    fun getApprovedHadiths(): Flow<List<HadithEntity>> = hadithDao.getApprovedHadiths()

    fun getApprovedHadithsByTopic(topic: String): Flow<List<HadithEntity>> =
        if (topic == "الكل") hadithDao.getApprovedHadiths() else hadithDao.getApprovedHadithsByTopic(topic)

    fun searchApprovedHadiths(query: String): Flow<List<HadithEntity>> =
        if (query.isBlank()) hadithDao.getApprovedHadiths() else hadithDao.searchApprovedHadiths(query.trim())

    suspend fun getHadithById(id: String): HadithEntity? = hadithDao.getHadithById(id)

    suspend fun insertHadiths(hadiths: List<HadithEntity>): Int {
        val inserted = hadithDao.insertHadiths(hadiths)
        return inserted.count { it != -1L }
    }

    /**
     * Imports hadiths from Sunnah.com API with default reviewStatus = PENDING
     * Does NOT display them until vetted and approved by ContentApprovalRepository.
     */
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

    suspend fun getApprovedCount(): Int = hadithDao.getApprovedCount()
}

/**
 * 3. DhikrRepository
 * Strictly exposes APPROVED Fortress of the Muslim (حصن المسلم) supplications.
 */
class DhikrRepository(
    private val dhikrDao: DhikrDao
) {
    fun getApprovedDhikr(): Flow<List<DhikrEntity>> = dhikrDao.getApprovedDhikr()

    fun getApprovedDhikrByCategory(category: String): Flow<List<DhikrEntity>> =
        if (category == "الكل") dhikrDao.getApprovedDhikr() else dhikrDao.getApprovedDhikrByCategory(category)

    suspend fun getDhikrById(id: String): DhikrEntity? = dhikrDao.getDhikrById(id)

    suspend fun insertDhikr(dhikrList: List<DhikrEntity>): Int {
        val inserted = dhikrDao.insertDhikr(dhikrList)
        return inserted.count { it != -1L }
    }

    suspend fun getApprovedCount(): Int = dhikrDao.getApprovedCount()
}

/**
 * 4. FavoriteRepository
 * Offline-first local management of user favorites.
 */
class FavoriteRepository(
    private val favoriteDao: FavoriteDao
) {
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    fun getFavoritesByType(contentType: String): Flow<List<FavoriteEntity>> =
        favoriteDao.getFavoritesByType(contentType)

    fun isFavorite(contentId: String): Flow<Boolean> = favoriteDao.isFavorite(contentId)

    suspend fun toggleFavorite(contentId: String, contentType: String) {
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

    suspend fun removeFavorite(contentId: String) {
        favoriteDao.deleteFavoriteByContentId(contentId)
    }
}

/**
 * 5. ContentApprovalRepository
 * Manages the reliability system: review status audits, approvals, rejections, and offline content settings.
 * Ensures:
 * - reviewStatus = PENDING by default
 * - No content displayed unless APPROVED
 * - Retains source and reviewer audit trail
 */
class ContentApprovalRepository(
    private val quranDao: QuranDao,
    private val hadithDao: HadithDao,
    private val dhikrDao: DhikrDao,
    private val contentSettingsDao: ContentSettingsDao
) {
    fun getPendingVerses(): Flow<List<QuranVerseEntity>> = quranDao.getPendingVerses()
    fun getPendingHadiths(): Flow<List<HadithEntity>> = hadithDao.getPendingHadiths()
    fun getPendingDhikr(): Flow<List<DhikrEntity>> = dhikrDao.getPendingDhikr()

    suspend fun approveQuranVerse(id: String, reviewer: String, date: String) {
        quranDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectQuranVerse(id: String, reviewer: String, date: String) {
        quranDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    suspend fun approveHadith(id: String, reviewer: String, date: String) {
        hadithDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectHadith(id: String, reviewer: String, date: String) {
        hadithDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    suspend fun approveDhikr(id: String, reviewer: String, date: String) {
        dhikrDao.updateReviewStatus(id, ReviewStatus.APPROVED, reviewer, date)
    }

    suspend fun rejectDhikr(id: String, reviewer: String, date: String) {
        dhikrDao.updateReviewStatus(id, ReviewStatus.REJECTED, reviewer, date)
    }

    fun getContentSettings(): Flow<List<ContentSettingsEntity>> = contentSettingsDao.getAllSettings()

    suspend fun updateContentSetting(contentType: String, enabled: Boolean, frequency: String) {
        contentSettingsDao.upsertSettings(
            ContentSettingsEntity(
                contentType = contentType,
                enabled = enabled,
                displayFrequency = frequency
            )
        )
    }
}
