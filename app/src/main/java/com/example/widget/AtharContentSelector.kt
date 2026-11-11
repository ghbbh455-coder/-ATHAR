package com.example.widget

import android.content.Context
import com.example.data.local.AtharPreferences
import com.example.data.local.AuthenticData
import com.example.data.local.db.AtharDatabase
import com.example.data.model.ContentType
import kotlinx.coroutines.flow.firstOrNull

data class AtharSelectableItem(
    val id: String,
    val type: ContentType,
    val badge: String,
    val text: String,
    val reference: String,
    val targetDestination: String, // "quran", "hadith", "adhkar"
    val isFavorite: Boolean = false
)

object AtharContentSelector {

    suspend fun getNextRandomItem(context: Context): AtharSelectableItem {
        val prefs = AtharPreferences(context)
        val candidates = mutableListOf<AtharSelectableItem>()
        var isDbAvailable = false

        try {
            val db = AtharDatabase.getDatabase(context)
            val verses = db.quranDao().getApprovedVerses().firstOrNull() ?: emptyList()
            val hadiths = db.hadithDao().getApprovedHadiths().firstOrNull() ?: emptyList()
            val adhkar = db.dhikrDao().getApprovedDhikr().firstOrNull() ?: emptyList()

            if (verses.isNotEmpty() || hadiths.isNotEmpty() || adhkar.isNotEmpty()) {
                isDbAvailable = true
                verses.forEach { v ->
                    candidates.add(
                        AtharSelectableItem(
                            id = v.id,
                            type = ContentType.QURAN,
                            badge = "${v.surahName} (الآية ${v.verseNumber})",
                            text = v.textUthmani,
                            reference = "${v.surahName}: ${v.verseNumber}",
                            targetDestination = "quran"
                        )
                    )
                }

                hadiths.forEach { h ->
                    candidates.add(
                        AtharSelectableItem(
                            id = h.id,
                            type = ContentType.HADITH,
                            badge = h.collectionName,
                            text = h.arabicText,
                            reference = h.collectionName,
                            targetDestination = "hadith"
                        )
                    )
                }

                adhkar.forEach { d ->
                    val isDua = d.category.contains("دعاء") || d.category.contains("أدعية")
                    candidates.add(
                        AtharSelectableItem(
                            id = d.id,
                            type = if (isDua) ContentType.DUA else ContentType.DHIKR,
                            badge = d.category,
                            text = d.arabicText,
                            reference = d.reference,
                            targetDestination = "adhkar"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback to static AuthenticData if DB query yielded no items yet
        if (candidates.isEmpty()) {
            AuthenticData.quranVerses.forEach { v ->
                candidates.add(
                    AtharSelectableItem(
                        id = v.id,
                        type = ContentType.QURAN,
                        badge = "${v.surahName} (الآية ${v.ayahNumber})",
                        text = v.textWithTashkeel,
                        reference = "${v.surahName}: ${v.ayahNumber}",
                        targetDestination = "quran"
                    )
                )
            }
            AuthenticData.hadiths.forEach { h ->
                candidates.add(
                    AtharSelectableItem(
                        id = h.id,
                        type = ContentType.HADITH,
                        badge = h.source,
                        text = h.matnWithTashkeel,
                        reference = h.source,
                        targetDestination = "hadith"
                    )
                )
            }
            AuthenticData.adhkar.forEach { d ->
                candidates.add(
                    AtharSelectableItem(
                        id = d.id,
                        type = ContentType.DHIKR,
                        badge = d.category.title,
                        text = d.textWithTashkeel,
                        reference = d.reference,
                        targetDestination = "adhkar"
                    )
                )
            }
        }

        val shownHistory = prefs.getShownOverlayIds()
        var unshown = candidates.filter { !shownHistory.contains(it.id) }

        if (unshown.isEmpty()) {
            prefs.resetShownHistory()
            unshown = candidates
        }

        val selected = unshown.randomOrNull() ?: candidates.first()
        prefs.recordShownContentId(selected.id)

        // Check favorite status synchronously
        var isFav = false
        try {
            val db = AtharDatabase.getDatabase(context)
            isFav = db.favoriteDao().isFavoriteSync(selected.id)
        } catch (_: Exception) {
            isFav = prefs.isFavorite(selected.id)
        }

        return selected.copy(isFavorite = isFav)
    }
}
