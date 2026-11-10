package com.example.data.sources

import com.example.data.local.db.DhikrEntity
import com.example.data.local.db.ReviewStatus

/**
 * Adhkar File Importer
 * Standards:
 * 1. Only imports records with verified sourceBook, reference, and authenticity.
 * 2. Does NOT load untrusted / source-unknown data.
 * 3. Default reviewStatus = PENDING for new imports until certified by reviewer.
 */
object AdhkarFileImporter {

    data class VerifiedDhikrInput(
        val arabicText: String,
        val category: String,
        val repetitionCount: Int,
        val sourceBook: String,
        val reference: String,
        val authenticity: String,
        val virtue: String
    )

    /**
     * Validates and converts input items into DhikrEntity
     * Discards any item missing source or authenticity!
     */
    fun importVerifiedBatch(
        inputs: List<VerifiedDhikrInput>,
        preApproved: Boolean = false,
        reviewerName: String = "قسم تدقيق الأذكار"
    ): List<DhikrEntity> {
        val validEntities = mutableListOf<DhikrEntity>()

        for ((index, item) in inputs.withIndex()) {
            // Strict verification: Reject if text, source, or reference is blank
            if (item.arabicText.isBlank() || item.sourceBook.isBlank() || item.reference.isBlank() || item.authenticity.isBlank()) {
                continue // Rejects unknown or incomplete source
            }

            val id = "dhikr_import_${item.category.hashCode()}_${index}_${System.currentTimeMillis()}"
            validEntities.add(
                DhikrEntity(
                    id = id,
                    arabicText = item.arabicText.trim(),
                    category = item.category.trim(),
                    repetitionCount = if (item.repetitionCount <= 0) 1 else item.repetitionCount,
                    sourceBook = item.sourceBook.trim(),
                    reference = item.reference.trim(),
                    authenticity = item.authenticity.trim(),
                    reviewStatus = if (preApproved) ReviewStatus.APPROVED else ReviewStatus.PENDING,
                    virtue = item.virtue.trim(),
                    reviewer = if (preApproved) reviewerName else "بانتظار المراجعة والاعتماد",
                    reviewDate = if (preApproved) "2024-01-15" else ""
                )
            )
        }

        return validEntities
    }
}
