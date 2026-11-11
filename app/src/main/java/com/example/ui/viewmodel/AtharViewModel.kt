package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AtharPreferences
import com.example.data.local.QuranSurahCatalog
import com.example.data.local.SurahMetadata
import com.example.data.local.db.AtharDatabase
import com.example.data.local.db.ContentSettingsEntity
import com.example.data.local.db.DhikrEntity
import com.example.data.local.db.FavoriteEntity
import com.example.data.local.db.HadithEntity
import com.example.data.local.db.QuranVerseEntity
import com.example.data.model.AtharFontSize
import com.example.data.model.AtharThemeMode
import com.example.data.model.ContentType
import com.example.data.model.DhikrCategory
import com.example.data.model.FavoriteItem
import com.example.data.model.OverlayPosition
import com.example.data.repository.ContentApprovalRepository
import com.example.data.repository.DhikrRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.HadithRepository
import com.example.data.repository.QuranRepository
import com.example.overlay.OverlayContentItem
import com.example.overlay.UnlockOverlayManager
import com.example.overlay.UnlockOverlayService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AtharDestination(val route: String, val titleArabic: String) {
    object Splash : AtharDestination("splash", "أثر")
    object Permissions : AtharDestination("permissions", "أذونات التشغيل")
    object Home : AtharDestination("home", "الرئيسية")
    object Quran : AtharDestination("quran", "القرآن الكريم")
    object Hadith : AtharDestination("hadith", "الحديث النبوي")
    object Adhkar : AtharDestination("adhkar", "الأذكار والأدعية")
    object AllContent : AtharDestination("all_content", "كل المحتوى")
    object Favorites : AtharDestination("favorites", "المفضلة")
    object Settings : AtharDestination("settings", "الإعدادات")
}

data class UnifiedContentItem(
    val id: String,
    val type: ContentType,
    val badge: String,
    val title: String,
    val text: String,
    val reference: String,
    val topic: String,
    val benefit: String
)

class AtharViewModel(application: Application) : AndroidViewModel(application) {
    val prefs = AtharPreferences(application)
    private val database = AtharDatabase.getDatabase(application)

    // Room Repositories
    val quranRepository = QuranRepository(database.quranDao())
    val hadithRepository = HadithRepository(database.hadithDao())
    val dhikrRepository = DhikrRepository(database.dhikrDao())
    val favoriteRepository = FavoriteRepository(database.favoriteDao())
    val approvalRepository = ContentApprovalRepository(
        database.quranDao(),
        database.hadithDao(),
        database.dhikrDao(),
        database.contentSettingsDao()
    )

    private val _currentDestination = MutableStateFlow<AtharDestination>(
        if (prefs.hasSeenWelcome.value) AtharDestination.Home else AtharDestination.Splash
    )
    val currentDestination: StateFlow<AtharDestination> = _currentDestination.asStateFlow()

    // Quran State
    private val _quranSearch = MutableStateFlow("")
    val quranSearch: StateFlow<String> = _quranSearch.asStateFlow()

    private val _selectedQuranTopic = MutableStateFlow<String?>("الكل")
    val selectedQuranTopic: StateFlow<String?> = _selectedQuranTopic.asStateFlow()

    private val _selectedSurah = MutableStateFlow<SurahMetadata?>(null)
    val selectedSurah: StateFlow<SurahMetadata?> = _selectedSurah.asStateFlow()

    // Hadith State
    private val _hadithSearch = MutableStateFlow("")
    val hadithSearch: StateFlow<String> = _hadithSearch.asStateFlow()

    private val _selectedHadithTopic = MutableStateFlow<String?>("الكل")
    val selectedHadithTopic: StateFlow<String?> = _selectedHadithTopic.asStateFlow()

    // Adhkar State
    private val _selectedDhikrCategory = MutableStateFlow("أذكار الصباح")
    val selectedDhikrCategory: StateFlow<String> = _selectedDhikrCategory.asStateFlow()

    // All Content Screen State
    private val _allContentSearch = MutableStateFlow("")
    val allContentSearch: StateFlow<String> = _allContentSearch.asStateFlow()

    private val _allContentFilter = MutableStateFlow<ContentType?>(null)
    val allContentFilter: StateFlow<ContentType?> = _allContentFilter.asStateFlow()

    // Favorites Filter
    private val _favoriteTypeFilter = MutableStateFlow<ContentType?>(null)
    val favoriteTypeFilter: StateFlow<ContentType?> = _favoriteTypeFilter.asStateFlow()

    // Observables from Room
    val approvedVerses: StateFlow<List<QuranVerseEntity>> = quranRepository.getApprovedVerses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedHadiths: StateFlow<List<HadithEntity>> = hadithRepository.getApprovedHadiths()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedDhikr: StateFlow<List<DhikrEntity>> = dhikrRepository.getApprovedDhikr()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawFavorites: StateFlow<List<FavoriteEntity>> = favoriteRepository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentSettings: StateFlow<List<ContentSettingsEntity>> = approvalRepository.getContentSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified Content Aggregation
    val unifiedContentItems: StateFlow<List<UnifiedContentItem>> = combine(
        approvedVerses,
        approvedHadiths,
        approvedDhikr,
        _allContentFilter,
        _allContentSearch
    ) { verses, hadiths, adhkar, filter, query ->
        val list = mutableListOf<UnifiedContentItem>()

        verses.forEach { v ->
            list.add(
                UnifiedContentItem(
                    id = v.id,
                    type = ContentType.QURAN,
                    badge = "${v.surahName} (الآية ${v.verseNumber})",
                    title = v.topic,
                    text = v.textUthmani,
                    reference = "${v.surahName}: ${v.verseNumber}",
                    topic = v.topic,
                    benefit = v.tafsirSnippet
                )
            )
        }

        hadiths.forEach { h ->
            list.add(
                UnifiedContentItem(
                    id = h.id,
                    type = ContentType.HADITH,
                    badge = "${h.collectionName} - رقم ${h.hadithNumber}",
                    title = h.chapterName,
                    text = h.arabicText,
                    reference = h.collectionName,
                    topic = h.topic,
                    benefit = h.benefit
                )
            )
        }

        adhkar.forEach { d ->
            val isDua = d.category.contains("دعاء") || d.category.contains("أدعية")
            list.add(
                UnifiedContentItem(
                    id = d.id,
                    type = if (isDua) ContentType.DUA else ContentType.DHIKR,
                    badge = d.category,
                    title = if (d.repetitionCount > 1) "التكرار: ${d.repetitionCount} مرات" else "مرة واحدة",
                    text = d.arabicText,
                    reference = d.reference,
                    topic = d.category,
                    benefit = d.virtue
                )
            )
        }

        list.filter { item ->
            val matchesFilter = filter == null || item.type == filter
            val matchesQuery = if (query.isBlank()) true else {
                item.text.contains(query, ignoreCase = true) ||
                        item.badge.contains(query, ignoreCase = true) ||
                        item.title.contains(query, ignoreCase = true) ||
                        item.topic.contains(query, ignoreCase = true) ||
                        item.reference.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Favorites mapped to full UI display items offline
    val favoriteDisplayItems: StateFlow<List<FavoriteItem>> = combine(
        rawFavorites,
        approvedVerses,
        approvedHadiths,
        approvedDhikr,
        _favoriteTypeFilter
    ) { favs, verses, hadiths, adhkar, filter ->
        val list = mutableListOf<FavoriteItem>()
        val favMap = favs.associateBy { it.contentId }

        verses.forEach { verse ->
            if (favMap.containsKey(verse.id)) {
                list.add(
                    FavoriteItem(
                        id = verse.id,
                        type = ContentType.QURAN,
                        title = "${verse.surahName} (الآية ${verse.verseNumber})",
                        contentText = verse.textUthmani,
                        referenceText = "${verse.surahName}: ${verse.verseNumber}",
                        categoryOrTopic = verse.topic
                    )
                )
            }
        }

        hadiths.forEach { hadith ->
            if (favMap.containsKey(hadith.id)) {
                list.add(
                    FavoriteItem(
                        id = hadith.id,
                        type = ContentType.HADITH,
                        title = "${hadith.collectionName} - رقم ${hadith.hadithNumber}",
                        contentText = hadith.arabicText,
                        referenceText = hadith.collectionName,
                        categoryOrTopic = hadith.topic
                    )
                )
            }
        }

        adhkar.forEach { dhikr ->
            if (favMap.containsKey(dhikr.id)) {
                val isDua = dhikr.category.contains("دعاء") || dhikr.category.contains("أدعية")
                list.add(
                    FavoriteItem(
                        id = dhikr.id,
                        type = if (isDua) ContentType.DUA else ContentType.DHIKR,
                        title = dhikr.category,
                        contentText = dhikr.arabicText,
                        referenceText = dhikr.reference,
                        categoryOrTopic = dhikr.virtue
                    )
                )
            }
        }

        if (filter == null) list else list.filter { it.type == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteIdsSet: StateFlow<Set<String>> = rawFavorites.combine(prefs.favoriteIds) { dbFavs, prefFavs ->
        dbFavs.map { it.contentId }.toSet() + prefFavs
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val themeMode: StateFlow<AtharThemeMode> = prefs.themeMode
    val fontSize: StateFlow<AtharFontSize> = prefs.fontSize
    val showTashkeel: StateFlow<Boolean> = prefs.showTashkeel
    val dhikrCounters: StateFlow<Map<String, Int>> = prefs.dhikrCounters
    val overlayEnabled: StateFlow<Boolean> = prefs.overlayEnabled
    val overlayPosition: StateFlow<OverlayPosition> = prefs.overlayPosition
    val showQuranInOverlay: StateFlow<Boolean> = prefs.showQuranInOverlay
    val showHadithInOverlay: StateFlow<Boolean> = prefs.showHadithInOverlay
    val showDhikrInOverlay: StateFlow<Boolean> = prefs.showDhikrInOverlay
    val showDuaInOverlay: StateFlow<Boolean> = prefs.showDuaInOverlay

    init {
        // Ensure initial offline seed package is inserted in Room on launch
        viewModelScope.launch(Dispatchers.IO) {
            if (quranRepository.getApprovedCount() == 0) {
                AtharDatabase.seedInitialVerifiedData(database)
            }
        }
    }

    fun navigateTo(dest: AtharDestination) {
        if (dest == AtharDestination.Home && !prefs.hasSeenWelcome.value) {
            prefs.setHasSeenWelcome(true)
        }
        _currentDestination.value = dest
    }

    fun setQuranSearch(query: String) {
        _quranSearch.value = query
    }

    fun selectQuranTopic(topic: String?) {
        _selectedQuranTopic.value = topic
    }

    fun selectSurah(surah: SurahMetadata?) {
        _selectedSurah.value = surah
    }

    fun setHadithSearch(query: String) {
        _hadithSearch.value = query
    }

    fun selectHadithTopic(topic: String?) {
        _selectedHadithTopic.value = topic
    }

    fun selectDhikrCategory(category: String) {
        _selectedDhikrCategory.value = category
    }

    fun setAllContentSearch(query: String) {
        _allContentSearch.value = query
    }

    fun setAllContentFilter(type: ContentType?) {
        _allContentFilter.value = type
    }

    fun setFavoriteTypeFilter(type: ContentType?) {
        _favoriteTypeFilter.value = type
    }

    fun toggleFavorite(contentId: String, contentType: ContentType) {
        viewModelScope.launch(Dispatchers.IO) {
            favoriteRepository.toggleFavorite(contentId, contentType.name)
            prefs.toggleFavorite(contentId)
        }
    }

    fun isFavorite(id: String): Boolean {
        return favoriteIdsSet.value.contains(id)
    }

    fun incrementDhikr(id: String, max: Int) {
        prefs.incrementDhikrCount(id, max)
    }

    fun resetDhikr(id: String) {
        prefs.resetDhikrCount(id)
    }

    fun setThemeMode(mode: AtharThemeMode) {
        prefs.setThemeMode(mode)
    }

    fun setFontSize(size: AtharFontSize) {
        prefs.setFontSize(size)
    }

    fun setShowTashkeel(show: Boolean) {
        prefs.setShowTashkeel(show)
    }

    fun setOverlayEnabled(context: Context, enabled: Boolean) {
        prefs.setOverlayEnabled(enabled)
        if (enabled) {
            if (UnlockOverlayManager.canDrawOverlays(context)) {
                UnlockOverlayService.startService(context)
            }
        } else {
            UnlockOverlayService.stopService(context)
        }
    }

    fun setOverlayPosition(position: OverlayPosition) {
        prefs.setOverlayPosition(position)
    }

    fun setShowQuranInOverlay(show: Boolean) {
        prefs.setShowQuranInOverlay(show)
    }

    fun setShowHadithInOverlay(show: Boolean) {
        prefs.setShowHadithInOverlay(show)
    }

    fun setShowDhikrInOverlay(show: Boolean) {
        prefs.setShowDhikrInOverlay(show)
    }

    fun setShowDuaInOverlay(show: Boolean) {
        prefs.setShowDuaInOverlay(show)
    }

    fun triggerPreviewOverlay(context: Context) {
        val verse = approvedVerses.value.firstOrNull()
        val hadith = approvedHadiths.value.firstOrNull()
        val dhikr = approvedDhikr.value.firstOrNull()

        val item = if (showQuranInOverlay.value && verse != null) {
            OverlayContentItem(
                id = verse.id,
                contentType = ContentType.QURAN,
                badgeText = "${verse.surahName} (آية ${verse.verseNumber})",
                mainText = verse.textUthmani,
                referenceText = "${verse.surahName} • ${verse.source}",
                topic = verse.topic,
                benefitOrTafsir = verse.tafsirSnippet
            )
        } else if (showHadithInOverlay.value && hadith != null) {
            OverlayContentItem(
                id = hadith.id,
                contentType = ContentType.HADITH,
                badgeText = "${hadith.collectionName} - رقم ${hadith.hadithNumber}",
                mainText = hadith.arabicText,
                referenceText = "${hadith.grading} (${hadith.gradingSource})",
                topic = hadith.topic,
                benefitOrTafsir = hadith.benefit
            )
        } else if (dhikr != null) {
            OverlayContentItem(
                id = dhikr.id,
                contentType = ContentType.DHIKR,
                badgeText = dhikr.category,
                mainText = dhikr.arabicText,
                referenceText = "${dhikr.sourceBook} (${dhikr.reference})",
                topic = "أذكار مأثورة",
                benefitOrTafsir = dhikr.virtue
            )
        } else {
            null
        }

        if (item != null) {
            UnlockOverlayManager.showOverlay(
                context = context,
                item = item,
                position = overlayPosition.value
            )
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "أثر") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم النسخ إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
    }

    fun shareContent(context: Context, title: String, content: String, reference: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                """
                |✨ من تطبيق أثر ✨
                |
                |*$title*
                |
                |$content
                |
                |📖 المصدر: $reference
                |
                |«افتح هاتفك على ما ينفع قلبك»
                """.trimMargin()
            )
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة المحتوى الإيماني عبر")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
