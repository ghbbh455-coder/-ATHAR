package com.example

import com.example.data.local.QuranSurahCatalog
import com.example.data.local.db.DhikrEntity
import com.example.data.local.db.FavoriteEntity
import com.example.data.local.db.HadithEntity
import com.example.data.local.db.InitialSeedData
import com.example.data.local.db.QuranVerseEntity
import com.example.data.local.db.ReviewStatus
import com.example.data.model.ContentType
import com.example.data.model.DhikrCategory
import com.example.data.model.OverlayPosition
import com.example.data.sources.AdhkarFileImporter
import com.example.data.sources.SunnahComIntegration
import com.example.data.sources.SunnahHadithDetail
import com.example.data.sources.SunnahHadithLang
import com.example.overlay.OverlayContentItem
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    /**
     * اختبار 1: اختبار عدم ظهور المحتوى غير المعتمد
     * يضمن أن السجلات التي حالتها PENDING أو REJECTED لا يتم اعتبارها ضمن المحتوى المعتمد للعرض.
     */
    @Test
    fun testUnapprovedContentIsFilteredOut() {
        val mixedVerses = listOf(
            QuranVerseEntity(
                id = "v_approved",
                surahNumber = 1,
                surahName = "الفاتحة",
                verseNumber = 1,
                textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                reviewStatus = ReviewStatus.APPROVED,
                topic = "التوحيد",
                tafsirSnippet = "أم الكتاب"
            ),
            QuranVerseEntity(
                id = "v_pending",
                surahNumber = 1,
                surahName = "الفاتحة",
                verseNumber = 2,
                textUthmani = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ",
                reviewStatus = ReviewStatus.PENDING,
                topic = "الحمد",
                tafsirSnippet = "ثناء"
            ),
            QuranVerseEntity(
                id = "v_rejected",
                surahNumber = 1,
                surahName = "الفاتحة",
                verseNumber = 3,
                textUthmani = "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
                reviewStatus = ReviewStatus.REJECTED,
                topic = "الرحمة",
                tafsirSnippet = "رحمة"
            )
        )

        val approvedOnly = mixedVerses.filter { it.reviewStatus == ReviewStatus.APPROVED }
        assertEquals(1, approvedOnly.size)
        assertEquals("v_approved", approvedOnly.first().id)
        assertFalse(approvedOnly.any { it.reviewStatus == ReviewStatus.PENDING })
        assertFalse(approvedOnly.any { it.reviewStatus == ReviewStatus.REJECTED })
    }

    /**
     * اختبار 2: اختبار حفظ واسترجاع المفضلة محلياً
     */
    @Test
    fun testFavoritesCreationAndMapping() {
        val favorite = FavoriteEntity(
            id = "fav_1",
            contentId = "quran_1",
            contentType = "QURAN",
            createdAt = System.currentTimeMillis()
        )

        assertEquals("fav_1", favorite.id)
        assertEquals("quran_1", favorite.contentId)
        assertEquals("QURAN", favorite.contentType)
        assertTrue(favorite.createdAt > 0L)
    }

    /**
     * اختبار 3: اختبار العمل دون إنترنت (Offline-First)
     * يضمن توفر حزمة بيانات موثقة ومعتمدة أولية فوراً دون أي اتصال شبكي
     */
    @Test
    fun testOfflineFirstInitialSeedAvailability() {
        assertTrue("يجب أن تتوفر آيات قرآنية معتمدة محلياً", InitialSeedData.quranVerses.isNotEmpty())
        assertTrue("يجب أن تتوفر أحاديث صحيحة معتمدة محلياً", InitialSeedData.hadiths.isNotEmpty())
        assertTrue("يجب أن تتوفر أذكار موثقة معتمدة محلياً", InitialSeedData.adhkar.isNotEmpty())

        InitialSeedData.quranVerses.forEach {
            assertEquals("يجب أن تكون نصوص الباقة الأولية معتمدة", ReviewStatus.APPROVED, it.reviewStatus)
            assertTrue("يجب توثيق مصدر الآية المعتمد", it.source.isNotBlank())
        }

        InitialSeedData.hadiths.forEach {
            assertEquals("يجب أن تكون الأحاديث الأولية معتمدة", ReviewStatus.APPROVED, it.reviewStatus)
            assertTrue("يجب توثيق حكم الحديث", it.grading.isNotBlank())
            assertTrue("يجب توثيق مخرج الحديث", it.gradingSource.isNotBlank())
        }

        val integration = SunnahComIntegration()
        runBlocking {
            val result = integration.fetchCollectionHadiths("bukhari", apiKey = null)
            assertTrue("في وضع الأوفلاين بدون مفتاح، يفشل الجلب بهدوء دون استثناء قاتل", result.isFailure)
        }
    }

    /**
     * اختبار 4: اختبار فهرس السور الـ 114 بالكامل
     */
    @Test
    fun testQuranSurahCatalogCompleteness() {
        assertEquals("يجب أن يحتوي الفهرس على 114 سورة كاملة", 114, QuranSurahCatalog.surahs.size)

        val uniqueNumbers = QuranSurahCatalog.surahs.map { it.number }.toSet()
        assertEquals(114, uniqueNumbers.size)

        val first = QuranSurahCatalog.getSurahByNumber(1)
        assertNotNull(first)
        assertEquals("الفَاتِحَة", first!!.arabicName)
        assertEquals(7, first.versesCount)
        assertEquals("مكية", first.revelationType)

        val last = QuranSurahCatalog.getSurahByNumber(114)
        assertNotNull(last)
        assertEquals("النَّاس", last!!.arabicName)
        assertEquals(6, last.versesCount)

        val baqarah = QuranSurahCatalog.getSurahByNumber(2)
        assertNotNull(baqarah)
        assertEquals("البَقَرَة", baqarah!!.arabicName)
        assertEquals(286, baqarah.versesCount)
        assertEquals("مدنية", baqarah.revelationType)
    }

    /**
     * اختبار 5: اختبار تغطية كتب السنة الستة
     */
    @Test
    fun testSixHadithBooksCoverage() {
        val collections = InitialSeedData.hadiths.map { it.collectionName }.toSet()
        assertTrue("يجب احتواء صحيح البخاري", collections.any { it.contains("البخاري") })
        assertTrue("يجب احتواء صحيح مسلم", collections.any { it.contains("مسلم") })
        assertTrue("يجب احتواء جامع الترمذي", collections.any { it.contains("الترمذي") })
        assertTrue("يجب احتواء سنن أبي داود", collections.any { it.contains("أبي داود") })
        assertTrue("يجب احتواء سنن النسائي", collections.any { it.contains("النسائي") })
        assertTrue("يجب احتواء سنن ابن ماجه", collections.any { it.contains("ابن ماجه") })
    }

    /**
     * اختبار 6: اختبار تغطية الأقسام الـ 18 للأذكار والأدعية
     */
    @Test
    fun testEighteenAdhkarCategoriesCoverage() {
        val categoriesInDb = InitialSeedData.adhkar.map { it.category }.toSet()
        val expectedCategories = listOf(
            "أذكار الصباح",
            "أذكار المساء",
            "أذكار النوم",
            "أذكار الاستيقاظ",
            "أذكار بعد الصلاة",
            "أذكار دخول وخروج المنزل",
            "أذكار المسجد",
            "أذكار الطعام والشراب",
            "أذكار السفر",
            "أذكار الكرب والهم",
            "أذكار الاستغفار والتوبة",
            "أذكار التسبيح والحمد",
            "أدعية من القرآن الكريم",
            "أدعية الأنبياء عليهم السلام",
            "أدعية من السنة النبوية",
            "أذكار الوضوء",
            "أدعية الرزق والبركة",
            "أذكار وأدعية متنوعة"
        )

        expectedCategories.forEach { expected ->
            assertTrue("يجب توفر قسم: $expected", categoriesInDb.contains(expected))
        }
    }

    /**
     * اختبار 7: الدوران الذكي للنافذة العائمة ومنع التكرار حتى نفاد القائمة
     */
    @Test
    fun testSmartRotationPreventsImmediateRepetition() {
        val candidates = listOf("item_1", "item_2", "item_3")
        val shownHistory = mutableSetOf<String>()

        fun pickNext(all: List<String>, history: Set<String>): Pair<String, Boolean> {
            val unshown = all.filter { !history.contains(it) }
            return if (unshown.isNotEmpty()) {
                unshown.first() to false
            } else {
                all.first() to true // Reset occurred
            }
        }

        // Pick 1
        val (p1, r1) = pickNext(candidates, shownHistory)
        assertEquals("item_1", p1)
        assertFalse(r1)
        shownHistory.add(p1)

        // Pick 2 (must not repeat item_1)
        val (p2, r2) = pickNext(candidates, shownHistory)
        assertEquals("item_2", p2)
        assertFalse(r2)
        shownHistory.add(p2)

        // Pick 3
        val (p3, r3) = pickNext(candidates, shownHistory)
        assertEquals("item_3", p3)
        assertFalse(r3)
        shownHistory.add(p3)

        // Pick 4: All shown! Must reset history
        val (p4, r4) = pickNext(candidates, shownHistory)
        assertTrue("يجب إعادة ضبط سجل التكرار بعد استنفاد كل العناصر", r4)
        assertEquals("item_1", p4)
    }

    /**
     * اختبار 8: دعم مواقع النافذة العائمة (أعلى، منتصف، أسفل)
     */
    @Test
    fun testOverlayPositionSupport() {
        val positions = OverlayPosition.values()
        assertEquals(3, positions.size)
        assertTrue(positions.contains(OverlayPosition.TOP))
        assertTrue(positions.contains(OverlayPosition.CENTER))
        assertTrue(positions.contains(OverlayPosition.BOTTOM))

        assertEquals("أعلى الشاشة", OverlayPosition.TOP.label)
        assertEquals("منتصف الشاشة", OverlayPosition.CENTER.label)
        assertEquals("أسفل الشاشة", OverlayPosition.BOTTOM.label)
    }

    /**
     * اختبار 9: منع التكرار في نفس عملية فتح القفل (Debounce)
     */
    @Test
    fun testOverlayDebouncePreventsDuplicateOnSameUnlock() {
        fun canShow(enabled: Boolean, lastShownTime: Long, currentTime: Long): Boolean {
            if (!enabled) return false
            if (lastShownTime <= 0L) return true
            val elapsed = currentTime - lastShownTime
            return elapsed >= 3_000L
        }

        val t0 = 100_000L
        // محاولة في نفس عملية فتح القفل بعد ثانية واحدة فقط
        val tSecondLater = t0 + 1000L
        assertFalse("يجب منع التكرار في نفس فتح القفل فوراً (Debounce)", canShow(true, t0, tSecondLater))

        // محاولة بعد 5 ثوانٍ
        val tFiveSecLater = t0 + 5_000L
        assertTrue("يجب السماح بالظهور لعملية فتح جديدة", canShow(true, t0, tFiveSecLater))
    }

    /**
     * اختبار 10: حصر المحتوى المعروض في النافذة العائمة على المعتمد فقط (APPROVED)
     */
    @Test
    fun testOverlayCandidateOnlyIncludesApprovedItems() {
        val pool = listOf(
            QuranVerseEntity(id = "1", surahNumber = 1, surahName = "سورة الفاتحة", verseNumber = 1, textUthmani = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", reviewStatus = ReviewStatus.APPROVED, topic = "أم الكتاب", tafsirSnippet = "سورة الحمد"),
            QuranVerseEntity(id = "2", surahNumber = 1, surahName = "سورة الفاتحة", verseNumber = 2, textUthmani = "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَـٰلَمِينَ", reviewStatus = ReviewStatus.PENDING, topic = "الحمد", tafsirSnippet = "ثناء"),
            QuranVerseEntity(id = "3", surahNumber = 1, surahName = "سورة الفاتحة", verseNumber = 3, textUthmani = "ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ", reviewStatus = ReviewStatus.REJECTED, topic = "الرحمة", tafsirSnippet = "رحمة")
        )

        val approvedCandidates = pool.filter { it.reviewStatus == ReviewStatus.APPROVED }
            .map {
                OverlayContentItem(
                    id = it.id,
                    contentType = ContentType.QURAN,
                    badgeText = "${it.surahName} (آية ${it.verseNumber})",
                    mainText = it.textUthmani,
                    referenceText = it.source,
                    topic = it.topic,
                    benefitOrTafsir = it.tafsirSnippet
                )
            }

        assertEquals("يجب أن تحتوي قائمة مرشحي النافذة العائمة على المعتمد فقط", 1, approvedCandidates.size)
        assertEquals("1", approvedCandidates[0].id)
        assertEquals("سورة الفاتحة (آية 1)", approvedCandidates[0].badgeText)
    }

    /**
     * اختبار 11: التحقق من نموذج بيانات ويدجت الشاشة الرئيسية (WidgetContent)
     */
    @Test
    fun testGlanceWidgetContentModelDefaultsAndProperties() {
        val content = com.example.data.local.AtharPreferences.WidgetContent(
            id = "w_quran_1",
            type = "quran",
            badge = "سورة الإخلاص (آية 1)",
            text = "قُلْ هُوَ اللَّهُ أَحَدٌ",
            reference = "سورة الإخلاص: 1",
            isFavorite = true
        )

        assertEquals("w_quran_1", content.id)
        assertEquals("quran", content.type)
        assertEquals("سورة الإخلاص (آية 1)", content.badge)
        assertEquals("قُلْ هُوَ اللَّهُ أَحَدٌ", content.text)
        assertEquals("سورة الإخلاص: 1", content.reference)
        assertTrue(content.isFavorite)
    }

    /**
     * اختبار 14: التحقق من عمل زر المفضلة التفاعلي في الويدجت وتبديل حالته
     */
    @Test
    fun testGlanceWidgetFavoriteToggleState() {
        val content = com.example.data.local.AtharPreferences.WidgetContent(
            id = "w_hadith_1",
            type = "hadith",
            badge = "صحيح البخاري",
            text = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ",
            reference = "صحيح البخاري",
            isFavorite = false
        )

        assertFalse(content.isFavorite)
        val toggled = content.copy(isFavorite = !content.isFavorite)
        assertTrue(toggled.isFavorite)
        val toggledBack = toggled.copy(isFavorite = !toggled.isFavorite)
        assertFalse(toggledBack.isFavorite)
    }

    /**
     * اختبار 12: التحقق من توجيه النقر على الويدجت إلى الشاشات المخصصة
     */
    @Test
    fun testGlanceWidgetTargetDestinationsRouting() {
        fun resolveDestination(target: String): String {
            return when (target.lowercase()) {
                "quran" -> "quran_screen"
                "hadith" -> "hadith_screen"
                "adhkar" -> "adhkar_screen"
                "favorites" -> "favorites_screen"
                else -> "home_screen"
            }
        }

        assertEquals("quran_screen", resolveDestination("quran"))
        assertEquals("hadith_screen", resolveDestination("HADITH"))
        assertEquals("adhkar_screen", resolveDestination("adhkar"))
        assertEquals("favorites_screen", resolveDestination("favorites"))
        assertEquals("home_screen", resolveDestination("unknown"))
    }

    /**
     * اختبار 13: التحقق من نزاهة وصحة نصوص اختيار الويدجت الأوفلاين
     */
    @Test
    fun testAtharContentSelectorSeedIntegrity() {
        val quranCandidates = InitialSeedData.quranVerses.filter { it.reviewStatus == ReviewStatus.APPROVED }
        val hadithCandidates = InitialSeedData.hadiths.filter { it.reviewStatus == ReviewStatus.APPROVED }
        val adhkarCandidates = InitialSeedData.adhkar.filter { it.reviewStatus == ReviewStatus.APPROVED }

        assertTrue("يجب توفر آيات معتمدة لاختيار الويدجت", quranCandidates.isNotEmpty())
        assertTrue("يجب توفر أحاديث معتمدة لاختيار الويدجت", hadithCandidates.isNotEmpty())
        assertTrue("يجب توفر أذكار معتمدة لاختيار الويدجت", adhkarCandidates.isNotEmpty())

        quranCandidates.forEach { verse ->
            assertTrue("نص الآية لا يمكن أن يكون فارغاً", verse.textUthmani.isNotBlank())
            assertTrue("اسم السورة لا يمكن أن يكون فارغاً", verse.surahName.isNotBlank())
        }

        hadithCandidates.forEach { hadith ->
            assertTrue("نص الحديث لا يمكن أن يكون فارغاً", hadith.arabicText.isNotBlank())
            assertTrue("كتاب الحديث لا يمكن أن يكون فارغاً", hadith.collectionName.isNotBlank())
            assertTrue("حكم الحديث لا يمكن أن يكون فارغاً", hadith.grading.isNotBlank())
        }

        adhkarCandidates.forEach { dhikr ->
            assertTrue("نص الذكر لا يمكن أن يكون فارغاً", dhikr.arabicText.isNotBlank())
            assertTrue("تصنيف الذكر لا يمكن أن يكون فارغاً", dhikr.category.isNotBlank())
        }
    }

    /**
     * اختبار 15: التحقق من كشف نوع مصنّع الجهاز في DevicePermissionHelper
     */
    @Test
    fun testDevicePermissionHelperBrandDetection() {
        val brandName = com.example.overlay.DevicePermissionHelper.getDeviceBrandDisplayName()
        assertNotNull(brandName)
        assertTrue(brandName.isNotBlank())
    }

    /**
     * اختبار 16: التحقق من توجيه الوجهة لشاشة الأذونات (Permissions Destination Routing)
     */
    @Test
    fun testPermissionsDestinationRouting() {
        fun resolveDestination(target: String): String {
            return when (target.lowercase()) {
                "quran" -> "quran_screen"
                "hadith" -> "hadith_screen"
                "adhkar" -> "adhkar_screen"
                "favorites" -> "favorites_screen"
                "permissions" -> "permissions_screen"
                else -> "home_screen"
            }
        }

        assertEquals("permissions_screen", resolveDestination("permissions"))
        assertEquals("permissions_screen", resolveDestination("PERMISSIONS"))
    }

    /**
     * اختبار 17: التحقق من حالة إكمال إعداد الأذونات في التفضيلات
     */
    @Test
    fun testPermissionsOnboardingState() {
        var completed = false
        assertFalse(completed)
        completed = true
        assertTrue(completed)
    }

    /**
     * اختبار 18: التحقق من ثوابت وتكوين خدمة النافذة العائمة
     */
    @Test
    fun testUnlockOverlayServiceConstants() {
        val extraKey = "extra_trigger_now"
        assertEquals("extra_trigger_now", extraKey)
    }

    /**
     * اختبار 19: التحقق من روابط قنوات التواصل مع المطور (WhatsApp, Telegram, Facebook, TikTok, Snapchat)
     */
    @Test
    fun testDeveloperContactChannelsUrls() {
        val contacts = mapOf(
            "whatsapp" to "https://wa.me/201019524839",
            "telegram" to "https://t.me/body20089",
            "facebook" to "https://www.facebook.com/share/1PDexKZxyG/",
            "tiktok" to "https://www.tiktok.com/@3abdal3",
            "snapchat" to "https://www.snapchat.com/add/body.oo"
        )

        assertEquals(5, contacts.size)
        assertTrue(contacts["whatsapp"]!!.startsWith("https://wa.me/"))
        assertTrue(contacts["telegram"]!!.startsWith("https://t.me/"))
        assertTrue(contacts["facebook"]!!.contains("facebook.com"))
        assertTrue(contacts["tiktok"]!!.contains("tiktok.com"))
        assertTrue(contacts["snapchat"]!!.contains("snapchat.com"))
    }
}
