package com.example.overlay

import android.content.Context
import com.example.data.local.AtharPreferences
import com.example.data.local.JabrKhatirManager
import com.example.data.model.ContentType
import java.util.Calendar

object ContentSelector {

    enum class TimePeriod {
        FAJR_MORNING, // 04:00 - 11:30
        MIDDAY,       // 11:30 - 15:30
        EVENING,      // 15:30 - 19:30
        NIGHT         // 19:30 - 03:59
    }

    fun getCurrentTimePeriod(): TimePeriod {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        return when {
            totalMinutes in (4 * 60)..(11 * 60 + 30) -> TimePeriod.FAJR_MORNING
            totalMinutes in (11 * 60 + 31)..(15 * 60 + 30) -> TimePeriod.MIDDAY
            totalMinutes in (15 * 60 + 31)..(19 * 60 + 30) -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }

    /**
     * Checks if current time is within Friday's special blessing window:
     * from Thursday evening (17:30) to Friday sunset (18:00).
     */
    fun isFridayWindow(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val totalMinutes = hour * 60 + minute

        val isThursdayEvening = dayOfWeek == Calendar.THURSDAY && totalMinutes >= (17 * 60 + 30)
        val isFridayDay = dayOfWeek == Calendar.FRIDAY && totalMinutes <= (18 * 60)

        return isThursdayEvening || isFridayDay
    }

    /**
     * Contextual content selection matching time of day, Friday status, and smart unshown rotation.
     */
    fun selectContextualContent(
        context: Context,
        allCandidates: List<OverlayContentItem>
    ): OverlayContentItem {
        if (allCandidates.isEmpty()) {
            // Ultimate fallback to first Jabr Al-Khatir message
            val fallback = JabrKhatirManager.messages.first()
            return OverlayContentItem(
                id = fallback.id,
                contentType = ContentType.QURAN,
                badgeText = "بشارة فجرية 🌿 | جبر خاطر",
                mainText = fallback.ayahText,
                referenceText = fallback.surahAndVerse,
                topic = "جبر الخواطر والسكينة",
                benefitOrTafsir = fallback.reflection
            )
        }

        val prefs = AtharPreferences(context)
        val shownIds = prefs.getShownOverlayIds()

        // Filter unshown candidates to prevent immediate repetition
        var availablePool = allCandidates.filter { !shownIds.contains(it.id) }
        if (availablePool.isEmpty()) {
            prefs.resetShownHistory()
            availablePool = allCandidates
        }

        val timePeriod = getCurrentTimePeriod()
        val isFriday = isFridayWindow()

        // 1. Friday Priority: Prioritize prayers upon Prophet ﷺ and Al-Kahf
        if (isFriday) {
            val fridayItems = availablePool.filter { item ->
                item.mainText.contains("صَلِّ") ||
                        item.mainText.contains("النَّبِيّ") ||
                        item.mainText.contains("مُحَمَّد") ||
                        item.topic.contains("الجمعة") ||
                        item.badgeText.contains("الكهف") ||
                        item.referenceText.contains("الكهف")
            }
            if (fridayItems.isNotEmpty()) {
                return fridayItems.random()
            }
        }

        // 2. Time-of-Day Contextual Prioritization
        val prioritizedPool = when (timePeriod) {
            TimePeriod.FAJR_MORNING -> {
                availablePool.filter { item ->
                    item.badgeText.contains("الصباح") ||
                            item.badgeText.contains("استيقاظ") ||
                            item.topic.contains("الصباح") ||
                            item.topic.contains("انشراح") ||
                            item.topic.contains("السكينة") ||
                            item.topic.contains("الرزق")
                }
            }
            TimePeriod.EVENING -> {
                availablePool.filter { item ->
                    item.badgeText.contains("المساء") ||
                            item.topic.contains("المساء") ||
                            item.topic.contains("الاستغفار") ||
                            item.topic.contains("الحمد")
                }
            }
            TimePeriod.NIGHT -> {
                availablePool.filter { item ->
                    item.badgeText.contains("النوم") ||
                            item.topic.contains("النوم") ||
                            item.topic.contains("الاستغفار") ||
                            item.topic.contains("التهجد") ||
                            item.topic.contains("الطمأنينة")
                }
            }
            TimePeriod.MIDDAY -> emptyList()
        }

        if (prioritizedPool.isNotEmpty()) {
            return prioritizedPool.random()
        }

        // Standard smart rotation if no specific contextual group matched
        return availablePool.random()
    }
}
