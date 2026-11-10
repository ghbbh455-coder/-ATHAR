package com.example.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_verses WHERE reviewStatus = 'APPROVED' ORDER BY surahNumber ASC, verseNumber ASC")
    fun getApprovedVerses(): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE reviewStatus = 'APPROVED' AND topic = :topic ORDER BY surahNumber ASC, verseNumber ASC")
    fun getApprovedVersesByTopic(topic: String): Flow<List<QuranVerseEntity>>

    @Query("""
        SELECT * FROM quran_verses 
        WHERE reviewStatus = 'APPROVED' 
        AND (surahName LIKE '%' || :query || '%' OR textUthmani LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%')
        ORDER BY surahNumber ASC, verseNumber ASC
    """)
    fun searchApprovedVerses(query: String): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses ORDER BY surahNumber ASC, verseNumber ASC")
    fun getAllVerses(): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE reviewStatus = 'PENDING'")
    fun getPendingVerses(): Flow<List<QuranVerseEntity>>

    @Query("SELECT * FROM quran_verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: String): QuranVerseEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVerses(verses: List<QuranVerseEntity>): List<Long>

    @Query("UPDATE quran_verses SET reviewStatus = :status, reviewer = :reviewer, reviewDate = :reviewDate WHERE id = :id")
    suspend fun updateReviewStatus(id: String, status: ReviewStatus, reviewer: String, reviewDate: String)

    @Query("SELECT COUNT(*) FROM quran_verses WHERE reviewStatus = 'APPROVED'")
    suspend fun getApprovedCount(): Int

    @Query("SELECT COUNT(*) FROM quran_verses")
    suspend fun getTotalCount(): Int
}

@Dao
interface HadithDao {
    @Query("SELECT * FROM hadiths WHERE reviewStatus = 'APPROVED' ORDER BY id ASC")
    fun getApprovedHadiths(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE reviewStatus = 'APPROVED' AND topic = :topic ORDER BY id ASC")
    fun getApprovedHadithsByTopic(topic: String): Flow<List<HadithEntity>>

    @Query("""
        SELECT * FROM hadiths 
        WHERE reviewStatus = 'APPROVED' 
        AND (arabicText LIKE '%' || :query || '%' OR collectionName LIKE '%' || :query || '%' OR chapterName LIKE '%' || :query || '%' OR topic LIKE '%' || :query || '%')
        ORDER BY id ASC
    """)
    fun searchApprovedHadiths(query: String): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths ORDER BY id ASC")
    fun getAllHadiths(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE reviewStatus = 'PENDING'")
    fun getPendingHadiths(): Flow<List<HadithEntity>>

    @Query("SELECT * FROM hadiths WHERE id = :id LIMIT 1")
    suspend fun getHadithById(id: String): HadithEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHadiths(hadiths: List<HadithEntity>): List<Long>

    @Query("UPDATE hadiths SET reviewStatus = :status, reviewer = :reviewer, reviewDate = :reviewDate WHERE id = :id")
    suspend fun updateReviewStatus(id: String, status: ReviewStatus, reviewer: String, reviewDate: String)

    @Query("SELECT COUNT(*) FROM hadiths WHERE reviewStatus = 'APPROVED'")
    suspend fun getApprovedCount(): Int

    @Query("SELECT COUNT(*) FROM hadiths")
    suspend fun getTotalCount(): Int
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM adhkar WHERE reviewStatus = 'APPROVED' ORDER BY id ASC")
    fun getApprovedDhikr(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar WHERE reviewStatus = 'APPROVED' AND category = :category ORDER BY id ASC")
    fun getApprovedDhikrByCategory(category: String): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar ORDER BY id ASC")
    fun getAllDhikr(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar WHERE reviewStatus = 'PENDING'")
    fun getPendingDhikr(): Flow<List<DhikrEntity>>

    @Query("SELECT * FROM adhkar WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: String): DhikrEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDhikr(dhikrList: List<DhikrEntity>): List<Long>

    @Query("UPDATE adhkar SET reviewStatus = :status, reviewer = :reviewer, reviewDate = :reviewDate WHERE id = :id")
    suspend fun updateReviewStatus(id: String, status: ReviewStatus, reviewer: String, reviewDate: String)

    @Query("SELECT COUNT(*) FROM adhkar WHERE reviewStatus = 'APPROVED'")
    suspend fun getApprovedCount(): Int

    @Query("SELECT COUNT(*) FROM adhkar")
    suspend fun getTotalCount(): Int
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE contentType = :contentType ORDER BY createdAt DESC")
    fun getFavoritesByType(contentType: String): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId)")
    fun isFavorite(contentId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE contentId = :contentId)")
    suspend fun isFavoriteSync(contentId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE contentId = :contentId")
    suspend fun deleteFavoriteByContentId(contentId: String)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun getFavoritesCount(): Int
}

@Dao
interface ContentSettingsDao {
    @Query("SELECT * FROM content_settings")
    fun getAllSettings(): Flow<List<ContentSettingsEntity>>

    @Query("SELECT * FROM content_settings WHERE contentType = :contentType LIMIT 1")
    suspend fun getSettingsForType(contentType: String): ContentSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: ContentSettingsEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertInitialSettings(settings: List<ContentSettingsEntity>)
}
