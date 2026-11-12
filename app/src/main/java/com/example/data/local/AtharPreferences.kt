package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AtharFontSize
import com.example.data.model.AtharThemeMode
import com.example.data.model.ContentType
import com.example.data.model.FavoriteItem
import com.example.data.model.OverlayPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AtharPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("athar_app_prefs", Context.MODE_PRIVATE)

    private val _favoriteIds = MutableStateFlow<Set<String>>(loadFavoriteIds())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AtharThemeMode> = _themeMode.asStateFlow()

    private val _fontSize = MutableStateFlow(loadFontSize())
    val fontSize: StateFlow<AtharFontSize> = _fontSize.asStateFlow()

    private val _showTashkeel = MutableStateFlow(loadTashkeel())
    val showTashkeel: StateFlow<Boolean> = _showTashkeel.asStateFlow()

    private val _hasSeenWelcome = MutableStateFlow(loadHasSeenWelcome())
    val hasSeenWelcome: StateFlow<Boolean> = _hasSeenWelcome.asStateFlow()

    private val _hasCompletedPermissionsOnboarding = MutableStateFlow(loadHasCompletedPermissionsOnboarding())
    val hasCompletedPermissionsOnboarding: StateFlow<Boolean> = _hasCompletedPermissionsOnboarding.asStateFlow()

    private val _dhikrCounters = MutableStateFlow<Map<String, Int>>(loadDhikrCounters())
    val dhikrCounters: StateFlow<Map<String, Int>> = _dhikrCounters.asStateFlow()

    // Unlock Popup Overlay Settings
    private val _overlayEnabled = MutableStateFlow(loadOverlayEnabled())
    val overlayEnabled: StateFlow<Boolean> = _overlayEnabled.asStateFlow()

    private val _overlayPosition = MutableStateFlow(loadOverlayPosition())
    val overlayPosition: StateFlow<OverlayPosition> = _overlayPosition.asStateFlow()

    private val _showQuranInOverlay = MutableStateFlow(loadBoolean(KEY_OVERLAY_SHOW_QURAN, true))
    val showQuranInOverlay: StateFlow<Boolean> = _showQuranInOverlay.asStateFlow()

    private val _showHadithInOverlay = MutableStateFlow(loadBoolean(KEY_OVERLAY_SHOW_HADITH, true))
    val showHadithInOverlay: StateFlow<Boolean> = _showHadithInOverlay.asStateFlow()

    private val _showDhikrInOverlay = MutableStateFlow(loadBoolean(KEY_OVERLAY_SHOW_DHIKR, true))
    val showDhikrInOverlay: StateFlow<Boolean> = _showDhikrInOverlay.asStateFlow()

    private val _showDuaInOverlay = MutableStateFlow(loadBoolean(KEY_OVERLAY_SHOW_DUA, true))
    val showDuaInOverlay: StateFlow<Boolean> = _showDuaInOverlay.asStateFlow()

    private val _lastOverlayTimestamp = MutableStateFlow(loadLastOverlayTimestamp())
    val lastOverlayTimestamp: StateFlow<Long> = _lastOverlayTimestamp.asStateFlow()

    // Daily Spiritual Impact Tracking
    private val _dailyUnlockCount = MutableStateFlow(loadDailyUnlockCount())
    val dailyUnlockCount: StateFlow<Int> = _dailyUnlockCount.asStateFlow()

    private val _dailyTasbeehCount = MutableStateFlow(loadDailyTasbeehCount())
    val dailyTasbeehCount: StateFlow<Int> = _dailyTasbeehCount.asStateFlow()

    // Fasting Reminders Preference
    private val _fastingRemindersEnabled = MutableStateFlow(loadBoolean(KEY_FASTING_REMINDERS_ENABLED, true))
    val fastingRemindersEnabled: StateFlow<Boolean> = _fastingRemindersEnabled.asStateFlow()

    private fun loadFavoriteIds(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun toggleFavorite(id: String) {
        val current = _favoriteIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        _favoriteIds.value = current
    }

    fun isFavorite(id: String): Boolean {
        return _favoriteIds.value.contains(id)
    }

    fun getAllFavorites(): List<FavoriteItem> {
        val favs = _favoriteIds.value
        val list = mutableListOf<FavoriteItem>()

        // Check Quran verses
        for (verse in AuthenticData.quranVerses) {
            if (favs.contains(verse.id)) {
                list.add(
                    FavoriteItem(
                        id = verse.id,
                        type = ContentType.QURAN,
                        title = "${verse.surahName} (الآية ${verse.ayahNumber})",
                        contentText = verse.textWithTashkeel,
                        referenceText = "${verse.surahName}: ${verse.ayahNumber}",
                        categoryOrTopic = verse.topic
                    )
                )
            }
        }

        // Check Hadiths
        for (hadith in AuthenticData.hadiths) {
            if (favs.contains(hadith.id)) {
                list.add(
                    FavoriteItem(
                        id = hadith.id,
                        type = ContentType.HADITH,
                        title = hadith.title,
                        contentText = hadith.matnWithTashkeel,
                        referenceText = hadith.source,
                        categoryOrTopic = hadith.topic
                    )
                )
            }
        }

        // Check Adhkar
        for (dhikr in AuthenticData.adhkar) {
            if (favs.contains(dhikr.id)) {
                list.add(
                    FavoriteItem(
                        id = dhikr.id,
                        type = ContentType.DHIKR,
                        title = dhikr.category.title,
                        contentText = dhikr.textWithTashkeel,
                        referenceText = dhikr.reference,
                        categoryOrTopic = dhikr.virtue
                    )
                )
            }
        }

        return list
    }

    private fun loadThemeMode(): AtharThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AtharThemeMode.SYSTEM.name)
        return try {
            AtharThemeMode.valueOf(name ?: AtharThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            AtharThemeMode.SYSTEM
        }
    }

    fun setThemeMode(mode: AtharThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadFontSize(): AtharFontSize {
        val name = prefs.getString(KEY_FONT_SIZE, AtharFontSize.MEDIUM.name)
        return try {
            AtharFontSize.valueOf(name ?: AtharFontSize.MEDIUM.name)
        } catch (_: Exception) {
            AtharFontSize.MEDIUM
        }
    }

    fun setFontSize(size: AtharFontSize) {
        prefs.edit().putString(KEY_FONT_SIZE, size.name).apply()
        _fontSize.value = size
    }

    private fun loadTashkeel(): Boolean {
        return prefs.getBoolean(KEY_SHOW_TASHKEEL, true)
    }

    fun setShowTashkeel(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_TASHKEEL, show).apply()
        _showTashkeel.value = show
    }

    private fun loadHasSeenWelcome(): Boolean {
        return prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false)
    }

    fun setHasSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_SEEN_WELCOME, seen).apply()
        _hasSeenWelcome.value = seen
    }

    private fun loadHasCompletedPermissionsOnboarding(): Boolean {
        return prefs.getBoolean(KEY_PERMISSIONS_ONBOARDING_COMPLETED, false)
    }

    fun setHasCompletedPermissionsOnboarding(completed: Boolean) {
        prefs.edit().putBoolean(KEY_PERMISSIONS_ONBOARDING_COMPLETED, completed).apply()
        _hasCompletedPermissionsOnboarding.value = completed
    }

    private fun loadDhikrCounters(): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        AuthenticData.adhkar.forEach { item ->
            val count = prefs.getInt("dhikr_count_${item.id}", 0)
            map[item.id] = count
        }
        return map
    }

    fun incrementDhikrCount(id: String, maxCount: Int) {
        val current = _dhikrCounters.value[id] ?: 0
        val next = if (current >= maxCount) 0 else current + 1
        prefs.edit().putInt("dhikr_count_$id", next).apply()
        val mutable = _dhikrCounters.value.toMutableMap()
        mutable[id] = next
        _dhikrCounters.value = mutable
    }

    fun resetDhikrCount(id: String) {
        prefs.edit().putInt("dhikr_count_$id", 0).apply()
        val mutable = _dhikrCounters.value.toMutableMap()
        mutable[id] = 0
        _dhikrCounters.value = mutable
    }

    private fun loadOverlayEnabled(): Boolean {
        return prefs.getBoolean(KEY_OVERLAY_ENABLED, true)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_ENABLED, enabled).apply()
        _overlayEnabled.value = enabled
    }

    private fun loadOverlayPosition(): OverlayPosition {
        val name = prefs.getString(KEY_OVERLAY_POSITION, OverlayPosition.CENTER.name)
        return try {
            OverlayPosition.valueOf(name ?: OverlayPosition.CENTER.name)
        } catch (_: Exception) {
            OverlayPosition.CENTER
        }
    }

    fun setOverlayPosition(position: OverlayPosition) {
        prefs.edit().putString(KEY_OVERLAY_POSITION, position.name).apply()
        _overlayPosition.value = position
    }

    fun getQuranBookmarkPage(): Int = prefs.getInt("quran_bookmark_page", 1)

    fun setQuranBookmarkPage(page: Int) {
        prefs.edit().putInt("quran_bookmark_page", page).apply()
    }

    fun getLastReadPage(): Int = prefs.getInt("quran_last_read_page", 1)

    fun setLastReadPage(page: Int) {
        prefs.edit().putInt("quran_last_read_page", page).apply()
    }

    // Daily Fajr "Jabr Al-Khatir" Preference
    fun getLastJabrKhatirDate(): String = prefs.getString("key_last_jabr_khatir_date", "") ?: ""

    fun setLastJabrKhatirDate(date: String) {
        prefs.edit().putString("key_last_jabr_khatir_date", date).apply()
    }

    // Smart Floating Tasbeeh Preferences
    fun getFloatingTasbeehCount(dhikrKey: String): Int = prefs.getInt("tasbeeh_count_$dhikrKey", 0)

    fun setFloatingTasbeehCount(dhikrKey: String, count: Int) {
        prefs.edit().putInt("tasbeeh_count_$dhikrKey", count).apply()
    }

    fun getSelectedTasbeehIndex(): Int = prefs.getInt("tasbeeh_selected_index", 0)

    fun setSelectedTasbeehIndex(index: Int) {
        prefs.edit().putInt("tasbeeh_selected_index", index).apply()
    }

    fun isFloatingTasbeehEnabled(): Boolean = prefs.getBoolean("key_floating_tasbeeh_enabled", true)

    fun setFloatingTasbeehEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("key_floating_tasbeeh_enabled", enabled).apply()
    }

    // Fasting Reminder Preferences
    fun isFastingRemindersEnabled(): Boolean = prefs.getBoolean(KEY_FASTING_REMINDERS_ENABLED, true)

    fun setFastingRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FASTING_REMINDERS_ENABLED, enabled).apply()
        _fastingRemindersEnabled.value = enabled
    }

    fun getLastFastingReminderShownDate(): String = prefs.getString(KEY_LAST_FASTING_REMINDER_DATE, "") ?: ""

    fun setLastFastingReminderShownDate(dateStr: String) {
        prefs.edit().putString(KEY_LAST_FASTING_REMINDER_DATE, dateStr).apply()
    }

    private fun loadBoolean(key: String, default: Boolean): Boolean {
        return prefs.getBoolean(key, default)
    }

    fun setShowQuranInOverlay(show: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_SHOW_QURAN, show).apply()
        _showQuranInOverlay.value = show
    }

    fun setShowHadithInOverlay(show: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_SHOW_HADITH, show).apply()
        _showHadithInOverlay.value = show
    }

    fun setShowDhikrInOverlay(show: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_SHOW_DHIKR, show).apply()
        _showDhikrInOverlay.value = show
    }

    fun setShowDuaInOverlay(show: Boolean) {
        prefs.edit().putBoolean(KEY_OVERLAY_SHOW_DUA, show).apply()
        _showDuaInOverlay.value = show
    }

    private fun loadLastOverlayTimestamp(): Long {
        return prefs.getLong(KEY_LAST_OVERLAY_TIMESTAMP, 0L)
    }

    // Debounce to prevent double-firing on the exact same unlock
    fun canShowOverlayNow(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        if (!_overlayEnabled.value) return false
        val last = _lastOverlayTimestamp.value
        if (last <= 0L) return true

        val elapsed = currentTimeMillis - last
        // Debounce of 3 seconds only to prevent multiple callbacks during a single unlock
        return elapsed >= 3_000L
    }

    private fun getTodayDateString(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    private fun checkAndResetDailyImpact() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_DAILY_IMPACT_DATE, "") ?: ""
        if (lastDate != today) {
            prefs.edit()
                .putString(KEY_DAILY_IMPACT_DATE, today)
                .putInt(KEY_DAILY_UNLOCK_COUNT, 0)
                .putInt(KEY_DAILY_TASBEEH_COUNT, 0)
                .apply()
            _dailyUnlockCount.value = 0
            _dailyTasbeehCount.value = 0
        }
    }

    private fun loadDailyUnlockCount(): Int {
        checkAndResetDailyImpact()
        return prefs.getInt(KEY_DAILY_UNLOCK_COUNT, 0)
    }

    private fun loadDailyTasbeehCount(): Int {
        checkAndResetDailyImpact()
        return prefs.getInt(KEY_DAILY_TASBEEH_COUNT, 0)
    }

    fun incrementDailyUnlockCount() {
        checkAndResetDailyImpact()
        val newCount = _dailyUnlockCount.value + 1
        prefs.edit().putInt(KEY_DAILY_UNLOCK_COUNT, newCount).apply()
        _dailyUnlockCount.value = newCount
    }

    fun incrementDailyTasbeehCount(amount: Int = 1) {
        checkAndResetDailyImpact()
        val newCount = _dailyTasbeehCount.value + amount
        prefs.edit().putInt(KEY_DAILY_TASBEEH_COUNT, newCount).apply()
        _dailyTasbeehCount.value = newCount
    }

    fun recordOverlayShown(timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(KEY_LAST_OVERLAY_TIMESTAMP, timestamp).apply()
        _lastOverlayTimestamp.value = timestamp
        incrementDailyUnlockCount()
    }

    // Smart Local Rotation: Track shown IDs to prevent immediate repeats until list is exhausted
    fun getShownOverlayIds(): Set<String> {
        return prefs.getStringSet(KEY_SHOWN_OVERLAY_IDS, emptySet()) ?: emptySet()
    }

    fun recordShownContentId(id: String) {
        val current = getShownOverlayIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_SHOWN_OVERLAY_IDS, current).apply()
    }

    fun resetShownHistory() {
        prefs.edit().remove(KEY_SHOWN_OVERLAY_IDS).apply()
    }

    data class WidgetContent(
        val id: String,
        val type: String,
        val badge: String,
        val text: String,
        val reference: String,
        val isFavorite: Boolean = false
    )

    fun getWidgetContent(): WidgetContent {
        val id = prefs.getString(KEY_WIDGET_ID, "quran_1") ?: "quran_1"
        val type = prefs.getString(KEY_WIDGET_TYPE, "quran") ?: "quran"
        val badge = prefs.getString(KEY_WIDGET_BADGE, "سورة الرَّعْد (آية 28)") ?: "سورة الرَّعْد (آية 28)"
        val text = prefs.getString(
            KEY_WIDGET_TEXT,
            "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"
        ) ?: "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"
        val reference = prefs.getString(KEY_WIDGET_REF, "سورة الرَّعْد: 28") ?: "سورة الرَّعْد: 28"
        val isFav = prefs.getBoolean(KEY_WIDGET_IS_FAV, false)
        return WidgetContent(id, type, badge, text, reference, isFav)
    }

    fun saveWidgetContent(content: WidgetContent) {
        prefs.edit()
            .putString(KEY_WIDGET_ID, content.id)
            .putString(KEY_WIDGET_TYPE, content.type)
            .putString(KEY_WIDGET_BADGE, content.badge)
            .putString(KEY_WIDGET_TEXT, content.text)
            .putString(KEY_WIDGET_REF, content.reference)
            .putBoolean(KEY_WIDGET_IS_FAV, content.isFavorite)
            .commit()
    }

    companion object {
        private const val KEY_FAVORITES = "pref_favorites"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_FONT_SIZE = "pref_font_size"
        private const val KEY_SHOW_TASHKEEL = "pref_show_tashkeel"
        private const val KEY_HAS_SEEN_WELCOME = "pref_has_seen_welcome"
        private const val KEY_OVERLAY_ENABLED = "pref_overlay_enabled"
        private const val KEY_OVERLAY_POSITION = "pref_overlay_position"
        private const val KEY_OVERLAY_SHOW_QURAN = "pref_overlay_show_quran"
        private const val KEY_OVERLAY_SHOW_HADITH = "pref_overlay_show_hadith"
        private const val KEY_OVERLAY_SHOW_DHIKR = "pref_overlay_show_dhikr"
        private const val KEY_OVERLAY_SHOW_DUA = "pref_overlay_show_dua"
        private const val KEY_LAST_OVERLAY_TIMESTAMP = "pref_last_overlay_timestamp"
        private const val KEY_SHOWN_OVERLAY_IDS = "pref_shown_overlay_ids"
        private const val KEY_WIDGET_ID = "pref_widget_id"
        private const val KEY_WIDGET_TYPE = "pref_widget_type"
        private const val KEY_WIDGET_BADGE = "pref_widget_badge"
        private const val KEY_WIDGET_TEXT = "pref_widget_text"
        private const val KEY_WIDGET_REF = "pref_widget_ref"
        private const val KEY_WIDGET_IS_FAV = "pref_widget_is_fav"
        private const val KEY_PERMISSIONS_ONBOARDING_COMPLETED = "pref_permissions_onboarding_completed"
        private const val KEY_DAILY_IMPACT_DATE = "pref_daily_impact_date"
        private const val KEY_DAILY_UNLOCK_COUNT = "pref_daily_unlock_count"
        private const val KEY_DAILY_TASBEEH_COUNT = "pref_daily_tasbeeh_count"
        private const val KEY_FASTING_REMINDERS_ENABLED = "pref_fasting_reminders_enabled"
        private const val KEY_LAST_FASTING_REMINDER_DATE = "pref_last_fasting_reminder_date"
    }
}
