package com.example.data.model

import androidx.compose.runtime.Immutable

enum class ContentType(val titleArabic: String) {
    QURAN("القرآن الكريم"),
    HADITH("الحديث النبوي"),
    DHIKR("الأذكار"),
    DUA("الأدعية")
}

@Immutable
data class QuranVerse(
    val id: String,
    val surahName: String,
    val ayahNumber: Int,
    val textWithTashkeel: String,
    val textWithoutTashkeel: String,
    val topic: String,
    val tafsirSnippet: String
)

@Immutable
data class HadithItem(
    val id: String,
    val title: String,
    val matnWithTashkeel: String,
    val matnWithoutTashkeel: String,
    val source: String,
    val narrator: String,
    val topic: String,
    val benefit: String
)

enum class DhikrCategory(val id: String, val title: String) {
    SABAH("sabah", "أذكار الصباح"),
    MASAA("masaa", "أذكار المساء"),
    NAWM("nawm", "أذكار النوم"),
    ISTIQAZ("istiqaz", "أذكار الاستيقاظ"),
    SALAH("salah", "أذكار بعد الصلاة"),
    MANZIL("manzil", "أذكار دخول وخروج المنزل"),
    MASJID("masjid", "أذكار المسجد"),
    TAAM("taam", "أذكار الطعام والشراب"),
    SAFAR("safar", "أذكار السفر"),
    KARB("karb", "أذكار الكرب والهم"),
    ISTIGHFAR("istighfar", "أذكار الاستغفار والتوبة"),
    TASBIH("tasbih", "أذكار التسبيح والحمد"),
    DUA_QURAN("dua_quran", "أدعية من القرآن الكريم"),
    DUA_ANBIYA("dua_anbiya", "أدعية الأنبياء عليهم السلام"),
    DUA_SUNNAH("dua_sunnah", "أدعية من السنة النبوية"),
    WUDU("wudu", "أذكار الوضوء"),
    DUA_RIZQ("dua_rizq", "أدعية الرزق والبركة"),
    MUTANAWIA("mutanawia", "أذكار وأدعية متنوعة"),
    JAWAMI("jawami", "جوامع الاستغفار والتسبيح") // Backward compatible
}

@Immutable
data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val textWithTashkeel: String,
    val textWithoutTashkeel: String,
    val targetCount: Int,
    val virtue: String,
    val reference: String
)

@Immutable
data class FavoriteItem(
    val id: String,
    val type: ContentType,
    val title: String,
    val contentText: String,
    val referenceText: String,
    val categoryOrTopic: String
)

enum class AtharThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AtharFontSize(val label: String, val scaleFactor: Float) {
    SMALL("صغير", 0.85f),
    MEDIUM("متوسط", 1.0f),
    LARGE("كبير", 1.2f)
}

enum class OverlayPosition(val label: String) {
    TOP("أعلى الشاشة"),
    CENTER("منتصف الشاشة"),
    BOTTOM("أسفل الشاشة")
}
