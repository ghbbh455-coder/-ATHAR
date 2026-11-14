package com.example.data.local.db

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

enum class ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}

class DatabaseConverters {
    @TypeConverter
    fun fromReviewStatus(status: ReviewStatus): String = status.name

    @TypeConverter
    fun toReviewStatus(value: String): ReviewStatus = try {
        ReviewStatus.valueOf(value)
    } catch (_: Exception) {
        ReviewStatus.PENDING
    }
}

/**
 * 1. QuranVerse Entity
 * Follows Tanzil source attribution standards and literal Uthmani script preservation.
 */
@Immutable
@Entity(tableName = "quran_verses")
data class QuranVerseEntity(
    @PrimaryKey
    val id: String,
    val surahNumber: Int,
    val surahName: String,
    val verseNumber: Int,
    val textUthmani: String,
    val source: String = "مصحف مجمع الملك فهد (الرسم العثماني)",
    val sourceVersion: String = "مصحف المدينة النبوية الشريف",
    val topic: String,
    val tafsirSnippet: String,
    val reviewStatus: ReviewStatus = ReviewStatus.PENDING,
    val reviewer: String = "هيئة مراجعة المصاحف والتحقيق",
    val reviewDate: String = "2024-01-01"
)

/**
 * 2. Hadith Entity
 * Separates authentic prophetic text from scholarly grading, preserving collection, book, chapter and reviewer audit.
 */
@Immutable
@Entity(tableName = "hadiths")
data class HadithEntity(
    @PrimaryKey
    val id: String,
    val arabicText: String,
    val collectionName: String,
    val bookName: String,
    val chapterName: String,
    val hadithNumber: String,
    val grading: String,
    val gradingSource: String,
    val sourceUrl: String,
    val reviewStatus: ReviewStatus = ReviewStatus.PENDING,
    val reviewer: String = "لجنة مراجعة السنة النبوية",
    val reviewDate: String = "2024-01-01",
    val topic: String,
    val benefit: String
)

/**
 * 3. Dhikr Entity
 * Fortress of the Muslim (حصن المسلم) and verified prophetic supplications with exact references.
 */
@Immutable
@Entity(tableName = "adhkar")
data class DhikrEntity(
    @PrimaryKey
    val id: String,
    val arabicText: String,
    val category: String,
    val repetitionCount: Int,
    val sourceBook: String = "حصن المسلم من أذكار الكتاب والسنة",
    val reference: String,
    val authenticity: String = "صحيح",
    val reviewStatus: ReviewStatus = ReviewStatus.PENDING,
    val virtue: String,
    val reviewer: String = "قسم التحقيق والمراجعة",
    val reviewDate: String = "2024-01-01"
)

/**
 * 4. Favorite Entity
 * Stores user saved contents with timestamps locally.
 */
@Immutable
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val contentId: String,
    val contentType: String, // QURAN, HADITH, DHIKR
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 5. ContentSettings Entity
 * Local offline controls for content display frequencies and enablement.
 */
@Immutable
@Entity(tableName = "content_settings")
data class ContentSettingsEntity(
    @PrimaryKey
    val contentType: String, // QURAN, HADITH, DHIKR
    val enabled: Boolean = true,
    val displayFrequency: String = "DAILY" // DAILY, ALWAYS, HOURLY
)
