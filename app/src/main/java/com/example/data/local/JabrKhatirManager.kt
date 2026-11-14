package com.example.data.local

import android.content.Context
import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Immutable
data class JabrKhatirItem(
    val id: String,
    val ayahText: String,
    val surahAndVerse: String,
    val reflection: String
)

object JabrKhatirManager {

    val messages = listOf(
        JabrKhatirItem(
            id = "jabr_1",
            ayahText = "وَلَسَوْفَ يُعْطِيكَ رَبُّكَ فَتَرْضَىٰ",
            surahAndVerse = "سورة الضحى: ٥",
            reflection = "بشارة ربانية عظيمة بأن العوض الإلهي قادم لا محالة، ولن يرضيك فحسب، بل سيُدهش قلبك حتى تفيض عيناك حمداً وامتناناً."
        ),
        JabrKhatirItem(
            id = "jabr_2",
            ayahText = "لَا تَدْرِي لَعَلَّ اللَّهَ يُحْدِثُ بَعْدَ ذَٰلِكَ أَمْرًا",
            surahAndVerse = "سورة الطلاق: ١",
            reflection = "كل باب أُغلق في وجهك إنما هو حكمة، ليدبر الله لك أمراً أعظم وأجمل في خفاء لطفه ورحمته الواسعة."
        ),
        JabrKhatirItem(
            id = "jabr_3",
            ayahText = "فَنَادَىٰ فِي الظُّلُمَاتِ أَن لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ إِنِّي كُنتُ مِنَ الظَّالِمِينَ ۝ فَاسْتَجَبْنَا لَهُ وَنَجَّيْنَاهُ مِنَ الْغَمِّ ۚ وَكَذَٰلِكَ نُنجِي الْمُؤْمِنِينَ",
            surahAndVerse = "سورة الأنبياء: ٨٧ - ٨٨",
            reflection = "كما شق الله ظلمات البحر والليل وبطن الحوت ليونس عليه السلام، قادر سبحانه أن يبدد كل غمّ وألم يثقل صدرك في هذه اللحظة."
        ),
        JabrKhatirItem(
            id = "jabr_4",
            ayahText = "وَاصْبِرْ لِحُكْمِ رَبِّكَ فَإِنَّكَ بِأَعْيُنِنَا",
            surahAndVerse = "سورة الطور: ٤٨",
            reflection = "ما أعظم هذا الأمان! أنت محاط برعاية الله وعنايته الخاصة؛ لا يضيع منك دمع ولا يغيب عنه حزن، فاستبشر خيراً."
        ),
        JabrKhatirItem(
            id = "jabr_5",
            ayahText = "إِنَّ مَعَ الْعُسْرِ يُسْرًا ۝ إِنَّ مَعَ الْعُسْرِ يُسْرًا",
            surahAndVerse = "سورة الشرح: ٥ - ٦",
            reflection = "لم يقل بعد العسر، بل مع العسر؛ فالفرج يولد دائماً في رحم الشدة، ولن يغلب عسرٌ يسرين أبداً."
        ),
        JabrKhatirItem(
            id = "jabr_6",
            ayahText = "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
            surahAndVerse = "سورة الرعد: ٢٨",
            reflection = "إذا اضطربت عليك الدنيا وأثقلتك مشاغل الحياة، ففي معية الله وحده السكينة والقرار وطمأنينة الروح."
        ),
        JabrKhatirItem(
            id = "jabr_7",
            ayahText = "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ ۖ أُجِيبُ دَعْوَةَ الدَّاعِ إِذَا دَعَانِ",
            surahAndVerse = "سورة البقرة: ١٨٦",
            reflection = "بينك وبين الإجابة والفرج دعوة خاشعة بقلب صادق؛ الله أقرب إليك من حبل الوريد ويسمع نداء فؤادك في خلوتك."
        ),
        JabrKhatirItem(
            id = "jabr_8",
            ayahText = "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنفُسِهِمْ لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا",
            surahAndVerse = "سورة الزمر: ٥٣",
            reflection = "مهما تعاظمت ذنوبك أو زلت قدمك، فرحمة الله أوسع ومغفرته سابغة؛ اقبل عليه بصدق يفتح لك كل أبواب الرضا."
        ),
        JabrKhatirItem(
            id = "jabr_9",
            ayahText = "وَمَن يَتَّقِ اللَّهَ يَجْعَل لَّهُ مَخْرَجًا ۝ وَيَرْزُقْهُ مِنْ حَيْثُ لَا يَحْتَسِبُ",
            surahAndVerse = "سورة الطلاق: ٢ - ٣",
            reflection = "أبواب الرزق والتيسير تأتيك من دروب لم تخطر على بالك قط، ما دام قلبك معلقاً بالتقوى والتوكل على الله."
        ),
        JabrKhatirItem(
            id = "jabr_10",
            ayahText = "قَالَ لَا تَخَافَا ۖ إِنَّنِي مَعَكُمَا أَسْمَعُ وَأَرَىٰ",
            surahAndVerse = "سورة طه: ٤٦",
            reflection = "حينما تكون في معية الله، تسقط كل المخاوف وتتضاءل كل الصعاب؛ فالله يسمع ضعفك ويرى تضرعك وسينصرك."
        ),
        JabrKhatirItem(
            id = "jabr_11",
            ayahText = "وَأُفَوِّضُ أَمْرِي إِلَى اللَّهِ ۚ إِنَّ اللَّهَ بَصِيرٌ بِالْعِبَادِ",
            surahAndVerse = "سورة غافر: ٤٤",
            reflection = "التفويض راحة للبال وسكينة للقلب؛ سلّم أمرك لمن يدبر ملكوت السماوات والأرض، واسترح من عناء التفكير."
        ),
        JabrKhatirItem(
            id = "jabr_12",
            ayahText = "إِنَّ رَبِّي لَطِيفٌ لِّمَا يَشَاءُ ۚ إِنَّهُ هُوَ الْعَلِيمُ الْحَكِيمُ",
            surahAndVerse = "سورة يوسف: ١٠٠",
            reflection = "بعد سنوات البئر والسجن والفرقة جاء التمكين والبشرى؛ فثق بلطف الله في أقدارك مهما طال الانتظار وصعبت الأيام."
        ),
        JabrKhatirItem(
            id = "jabr_13",
            ayahText = "فَسَيَكْفِيكَهُمُ اللَّهُ ۚ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            surahAndVerse = "سورة البقرة: ١٣٧",
            reflection = "كفاية الله تكفيك من كل خوف وتغنيك عن الخلق أجمعين، وهو السميع لنجواك العليم بحالك."
        ),
        JabrKhatirItem(
            id = "jabr_14",
            ayahText = "وَقَالَ رَبُّكُمُ ادْعُونِي أَسْتَجِبْ لَكُمْ",
            surahAndVerse = "سورة غافر: ٦٠",
            reflection = "وعد كريم من رب كريم لا يُخلف الميعاد؛ ما ألهمك الدعاء إلا وهو يريد أن يجود عليك بالعطاء والإكرام."
        ),
        JabrKhatirItem(
            id = "jabr_15",
            ayahText = "رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا",
            surahAndVerse = "سورة البقرة: ٢٨٦",
            reflection = "الله أرحم بك من نفسك، لا يحملك فوق وسعك؛ اطلب منه العفو والرحمة واستند إلى فضله العظيم."
        )
    )

    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    /**
     * Determines whether today's first Fajr "Jabr Al-Khatir" message should be shown in overlay
     * Triggered if time is after 04:00 AM and not yet shown today.
     */
    fun shouldShowFajrJabrKhatir(context: Context): Boolean {
        val prefs = AtharPreferences(context)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        // From 04:00 AM onwards
        if (hour < 4) return false

        val today = getTodayDateString()
        return prefs.getLastJabrKhatirDate() != today
    }

    /**
     * Retrieves today's designated Jabr Al-Khatir message based on day of year
     */
    fun getTodayJabrKhatir(): JabrKhatirItem {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1) % messages.size
        return messages[index]
    }

    fun markJabrKhatirShownToday(context: Context) {
        val prefs = AtharPreferences(context)
        prefs.setLastJabrKhatirDate(getTodayDateString())
    }
}
