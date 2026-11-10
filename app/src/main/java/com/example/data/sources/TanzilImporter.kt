package com.example.data.sources

import com.example.data.local.db.QuranVerseEntity
import com.example.data.local.db.ReviewStatus

/**
 * Tanzil Project Importer
 * Standards:
 * 1. Preserves literal Uthmani script including aya pause marks and Quranic symbols.
 * 2. Mandatory source attribution to "Tanzil Project (tanzil.net)".
 * 3. Default reviewStatus = PENDING for newly imported external files until certified by reviewer.
 */
object TanzilImporter {

    const val TANZIL_SOURCE = "Tanzil Project (tanzil.net)"
    const val TANZIL_VERSION = "Uthmani Script v1.0.2"
    const val TANZIL_COPYRIGHT_NOTICE = "نص القرآن الكريم برسم المصحف العثماني مستورد من مشروع تنزيل (tanzil.net) بترخيص الاستخدام غير التجاري."

    data class TanzilRawEntry(
        val surahNumber: Int,
        val verseNumber: Int,
        val surahName: String,
        val textUthmani: String,
        val topic: String = "عام",
        val tafsirSnippet: String = ""
    )

    /**
     * Parses standard Tanzil text export lines in format: surah|ayah|text
     */
    fun parseTanzilPipeSeparated(rawLines: List<String>, surahNamesMap: Map<Int, String> = emptyMap()): List<QuranVerseEntity> {
        val result = mutableListOf<QuranVerseEntity>()
        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
            val parts = trimmed.split("|")
            if (parts.size >= 3) {
                val surah = parts[0].toIntOrNull() ?: continue
                val ayah = parts[1].toIntOrNull() ?: continue
                val text = parts[2].trim()
                val surahName = surahNamesMap[surah] ?: "سورة رقم $surah"
                
                result.add(
                    QuranVerseEntity(
                        id = "tanzil_${surah}_$ayah",
                        surahNumber = surah,
                        surahName = surahName,
                        verseNumber = ayah,
                        textUthmani = text,
                        source = TANZIL_SOURCE,
                        sourceVersion = TANZIL_VERSION,
                        topic = "تدبر قرآني",
                        tafsirSnippet = "نص قرآني معتمد برسم المصحف العثماني من مشروع تنزيل.",
                        reviewStatus = ReviewStatus.PENDING, // Newly imported files are PENDING by default
                        reviewer = "بانتظار مراجعة وتدقيق هيئة المصاحف",
                        reviewDate = ""
                    )
                )
            }
        }
        return result
    }

    /**
     * Converts a verified Tanzil entry into a QuranVerseEntity
     */
    fun convertEntry(entry: TanzilRawEntry, isPreApproved: Boolean = false): QuranVerseEntity {
        return QuranVerseEntity(
            id = "tanzil_${entry.surahNumber}_${entry.verseNumber}",
            surahNumber = entry.surahNumber,
            surahName = entry.surahName,
            verseNumber = entry.verseNumber,
            textUthmani = entry.textUthmani,
            source = TANZIL_SOURCE,
            sourceVersion = TANZIL_VERSION,
            topic = entry.topic,
            tafsirSnippet = entry.tafsirSnippet,
            reviewStatus = if (isPreApproved) ReviewStatus.APPROVED else ReviewStatus.PENDING,
            reviewer = if (isPreApproved) "هيئة مراجعة المصاحف وتدقيق Tanzil" else "غير مراجع",
            reviewDate = if (isPreApproved) "2024-01-15" else ""
        )
    }
}
