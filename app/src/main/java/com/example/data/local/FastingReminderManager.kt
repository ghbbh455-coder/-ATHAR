package com.example.data.local

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar

@Immutable
data class FastingOccasion(
    val id: String,
    val title: String,
    val occasionName: String,
    val dateDisplay: String,
    val hadithText: String,
    val source: String,
    val virtue: String,
    val targetDateString: String
)

object FastingReminderManager {

    private val hijriMonthNames = listOf(
        "المحرّم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    fun getHijriMonthName(monthNumber: Int): String {
        return if (monthNumber in 1..12) hijriMonthNames[monthNumber - 1] else ""
    }

    /**
     * Determines whether we are currently in the eve of fasting (from 19:00 / 7:00 PM until 04:00 AM Fajr).
     */
    fun isFastingEveNow(hourOfDay: Int? = null): Boolean {
        val hour = hourOfDay ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 19 || hour < 4
    }

    /**
     * Checks if tomorrow (or today if after midnight before dawn) is an authentic recommended fasting day.
     * Returns the FastingOccasion if true, or null if no fasting occasion is upcoming.
     */
    fun getActiveFastingOccasion(context: Context): FastingOccasion? {
        val prefs = AtharPreferences(context)
        if (!prefs.isFastingRemindersEnabled()) {
            return null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return null
        }

        return calculateFastingOccasionApi26()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateFastingOccasionApi26(): FastingOccasion? {
        val now = LocalDateTime.now()
        val hour = now.hour

        // Must be in fasting eve (from 19:00 until 03:59 AM)
        if (!isFastingEveNow(hour)) {
            return null
        }

        // Target fasting date:
        // If between 19:00 and 23:59: tomorrow is the fast.
        // If between 00:00 and 03:59: today (starting in a few hours at dawn) is the fast.
        val targetDate: LocalDate = if (hour >= 19) {
            now.toLocalDate().plusDays(1)
        } else {
            now.toLocalDate()
        }

        val targetDateStr = targetDate.toString() // "yyyy-MM-dd"
        val dayOfWeek = targetDate.dayOfWeek

        val hijrahDate = try {
            HijrahDate.from(targetDate)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        val hYear = hijrahDate.get(ChronoField.YEAR)
        val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
        val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
        val monthName = getHijriMonthName(hMonth)

        // Do not remind for voluntary fasting in Ramadan (month 9) or on Eid days (1 Shawwal, 10-13 Dhul-Hijjah)
        if (hMonth == 9) return null
        if (hMonth == 10 && hDay == 1) return null // Eid al-Fitr
        if (hMonth == 12 && hDay in 10..13) return null // Eid al-Adha & Days of Tashreeq

        // 1. Day of Arafah (9 Dhul-Hijjah)
        if (hMonth == 12 && hDay == 9) {
            return FastingOccasion(
                id = "fasting_arafah_$targetDateStr",
                title = "غداً صيام يوم عَرَفَة 🌙",
                occasionName = "يوم عرفة",
                dateDisplay = "٩ ذو الحجة $hYear هـ",
                hadithText = "«صِيَامُ يَوْمِ عَرَفَةَ ، أَحْتَسِبُ عَلَى اللَّهِ أَنْ يُكَفِّرَ السَّنَةَ الَّتِي قَبْلَهُ ، وَالسَّنَةَ الَّتِي بَعْدَهُ»",
                source = "صحيح مسلم، برقم 1162",
                virtue = "يكفّر ذنوب سنتين كاملتين؛ سنة ماضية وسنة مقبلة، وهو أفضل أيام الدعاء.",
                targetDateString = targetDateStr
            )
        }

        // 2. Ashura (10 Muharram)
        if (hMonth == 1 && hDay == 10) {
            return FastingOccasion(
                id = "fasting_ashura_$targetDateStr",
                title = "غداً صيام يوم عَاشُورَاء 🌙",
                occasionName = "يوم عاشوراء",
                dateDisplay = "١٠ المحرّم $hYear هـ",
                hadithText = "«صِيَامُ يَوْمِ عَاشُورَاءَ ، أَحْتَسِبُ عَلَى اللَّهِ أَنْ يُكَفِّرَ السَّنَةَ الَّتِي قَبْلَهُ»",
                source = "صحيح مسلم، برقم 1162",
                virtue = "يوم شرفه الله بنجاة موسى وقومه، وصيامه كفارة لسنة كاملة مضت.",
                targetDateString = targetDateStr
            )
        }

        // 3. Tasu'a (9 Muharram)
        if (hMonth == 1 && hDay == 9) {
            return FastingOccasion(
                id = "fasting_tasua_$targetDateStr",
                title = "غداً صيام يوم تَاسُوعَاء 🌙",
                occasionName = "تاسوعاء",
                dateDisplay = "٩ المحرّم $hYear هـ",
                hadithText = "«لَئِنْ بَقِيتُ إِلَى قَابِلٍ لَأَصُومَنَّ التَّاسِعَ»",
                source = "صحيح مسلم، برقم 1134",
                virtue = "استحباب صوم اليوم التاسع مع العاشر استكمالاً للأجر ومخالفة لأهل الكتاب.",
                targetDateString = targetDateStr
            )
        }

        // 4. Ayyam al-Beed (13, 14, 15 Hijri)
        if (hDay in 13..15) {
            val dayLabel = when (hDay) {
                13 -> "أول الأيام البيض (١٣ $monthName)"
                14 -> "ثاني الأيام البيض (١٤ $monthName)"
                else -> "ثالث الأيام البيض (١٥ $monthName)"
            }
            return FastingOccasion(
                id = "fasting_beed_${hDay}_$targetDateStr",
                title = "غداً صيام $dayLabel 🌙",
                occasionName = "الأيام البيض",
                dateDisplay = "$hDay $monthName $hYear هـ",
                hadithText = "«صَوْمُ ثَلَاثَةِ أَيَّامٍ مِنْ كُلِّ شَهْرٍ : صَوْمُ الدَّهْرِ كُلِّهِ»",
                source = "متفق عليه (البخاري 1979، مسلم 1159)",
                virtue = "صيام ثلاثة أيام من كل شهر يعدل صيام الدهر كاملاً؛ الحسنة بعشر أمثالها.",
                targetDateString = targetDateStr
            )
        }

        // 5. Monday and Thursday
        if (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.THURSDAY) {
            val dayArabicName = if (dayOfWeek == DayOfWeek.MONDAY) "الاثنين" else "الخميس"
            return FastingOccasion(
                id = "fasting_sunnah_${dayArabicName}_$targetDateStr",
                title = "غداً صيام يوم $dayArabicName 🌙",
                occasionName = "سُنّة $dayArabicName",
                dateDisplay = "$dayArabicName ($hDay $monthName)",
                hadithText = "«تُعْرَضُ الأَعْمَالُ يَوْمَ الاثْنَيْنِ وَالخَمِيسِ ، فَأُحِبُّ أَنْ يُعْرَضَ عَمَلِي وَأَنَا صَائِمٌ»",
                source = "جامع الترمذي (747) وصححه الألباني",
                virtue = "يومان تُعرض فيهما أعمال العباد على الله، وسنة نبوية داوم عليها رسول الله ﷺ.",
                targetDateString = targetDateStr
            )
        }

        return null
    }

    /**
     * Determines whether the floating overlay fasting reminder should be displayed.
     * Condition: is fasting eve, fasting occasion is active, and hasn't been shown yet for this target date.
     */
    fun shouldShowFastingOverlay(context: Context): Boolean {
        val occasion = getActiveFastingOccasion(context) ?: return false
        val prefs = AtharPreferences(context)
        return prefs.getLastFastingReminderShownDate() != occasion.targetDateString
    }

    /**
     * Marks the fasting reminder as shown for this specific occasion/target date.
     */
    fun markFastingOverlayShown(context: Context, occasion: FastingOccasion) {
        val prefs = AtharPreferences(context)
        prefs.setLastFastingReminderShownDate(occasion.targetDateString)
    }
}
