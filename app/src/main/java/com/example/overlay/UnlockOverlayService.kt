package com.example.overlay

import android.app.AlarmManager
import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AtharPreferences
import com.example.data.local.FastingOccasion
import com.example.data.local.FastingReminderManager
import com.example.data.local.JabrKhatirManager
import com.example.data.local.db.AtharDatabase
import com.example.data.model.ContentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UnlockOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isReceiverRegistered = false

    private enum class PreloadedKind {
        JABR_KHATIR,
        FASTING,
        CONTEXTUAL
    }

    private data class PreloadedOverlay(
        val kind: PreloadedKind,
        val item: OverlayContentItem,
        val fastingOccasion: FastingOccasion? = null,
        val preparedAtElapsed: Long = SystemClock.elapsedRealtime()
    )

    @Volatile
    private var preloadedOverlay: PreloadedOverlay? = null

    @Volatile
    private var cachedCandidatePool: List<OverlayContentItem>? = null

    @Volatile
    private var cachedPoolFlags: String = ""

    private val unlockAndScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ctx = context ?: return
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "Broadcast received: ACTION_USER_PRESENT")
                    triggerUnlockReminder(ctx)
                }
                Intent.ACTION_SCREEN_ON -> {
                    // Briefly wake CPU for 1 second to handle unlock or preload immediately
                    acquireBriefWakeLock(ctx, 1000L)
                    val keyguardManager = ctx.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    if (keyguardManager != null && !keyguardManager.isKeyguardLocked) {
                        // Device has no lock screen / PIN: display immediately without waiting
                        Log.d(TAG, "Broadcast received: ACTION_SCREEN_ON (keyguard unlocked) -> triggering")
                        triggerUnlockReminder(ctx)
                    } else {
                        // Screen is still on lockscreen: preload next item in background for 0ms display upon unlock
                        Log.d(TAG, "Broadcast received: ACTION_SCREEN_ON (keyguard locked) -> preloading item")
                        preloadNextOverlay(ctx)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "UnlockOverlayService onCreate")
        createNotificationChannel()
        startAsForeground()
        registerUnlockReceiver()
        ServiceWatchdogWorker.scheduleWatchdog(this)
        // Warm up candidate pool on background thread
        serviceScope.launch {
            preloadNextOverlay(applicationContext)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerUnlockReceiver()
        if (intent?.getBooleanExtra(EXTRA_TRIGGER_NOW, false) == true) {
            triggerUnlockReminder(this)
        }
        return START_STICKY
    }

    private fun registerUnlockReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_USER_PRESENT)
                addAction(Intent.ACTION_SCREEN_ON)
            }
            ContextCompat.registerReceiver(
                this,
                unlockAndScreenReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isReceiverRegistered = true
            Log.d(TAG, "Dynamic unlockAndScreenReceiver registered with RECEIVER_EXPORTED")
        }
    }

    private fun unregisterUnlockReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(unlockAndScreenReceiver)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering receiver", e)
            }
            isReceiverRegistered = false
        }
    }

    private fun acquireBriefWakeLock(context: Context, timeoutMs: Long = 1000L) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "athar:unlock_wakelock")
            wakeLock?.acquire(timeoutMs)
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring WakeLock", e)
        }
    }

    private fun preloadNextOverlay(context: Context) {
        val prefs = AtharPreferences(context)
        if (!prefs.overlayEnabled.value || !UnlockOverlayManager.canDrawOverlays(context)) {
            return
        }

        serviceScope.launch {
            try {
                val prepared = prepareNextOverlayItem(context, prefs)
                if (prepared != null) {
                    preloadedOverlay = prepared
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error preloading overlay", e)
            }
        }
    }

    /**
     * Handles unlock event with 2000ms in-memory debounce.
     * Uses preloaded item if available for 0ms instant display, and never drops an unlock event
     * even if an old overlay was still attached (UnlockOverlayManager replaces it cleanly).
     */
    fun triggerUnlockReminder(context: Context) {
        val nowElapsed = SystemClock.elapsedRealtime()
        if (nowElapsed - lastTriggerTimestamp < DEBOUNCE_TIME_MS) {
            Log.d(TAG, "Debounce active: ignoring duplicate unlock (${nowElapsed - lastTriggerTimestamp}ms < ${DEBOUNCE_TIME_MS}ms)")
            return
        }
        lastTriggerTimestamp = nowElapsed

        acquireBriefWakeLock(context, 1000L)
        handleUserUnlock(context)
    }

    private fun handleUserUnlock(context: Context) {
        val prefs = AtharPreferences(context)
        if (!prefs.overlayEnabled.value) {
            Log.d(TAG, "Overlay disabled in preferences; skipping")
            return
        }

        if (!UnlockOverlayManager.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot draw overlays: permission missing")
            return
        }

        val currentPosition = prefs.overlayPosition.value
        val cached = preloadedOverlay
        val isFreshCache = cached != null &&
                (SystemClock.elapsedRealtime() - cached.preparedAtElapsed) < PRELOAD_TTL_MS

        if (isFreshCache && cached != null) {
            // Consume immediately for 0ms latency!
            preloadedOverlay = null
            UnlockOverlayManager.showOverlay(
                context = context,
                item = cached.item,
                position = currentPosition
            )
            serviceScope.launch {
                commitOverlayDisplay(context, prefs, cached)
                try {
                    com.example.widget.AtharGlanceWidget.updateWidget(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating widget", e)
                }
            }
            return
        }

        // Fallback: prepare on Dispatchers.IO using in-memory candidate pool if available
        serviceScope.launch {
            try {
                val prepared = prepareNextOverlayItem(context, prefs) ?: return@launch
                withContext(Dispatchers.Main) {
                    UnlockOverlayManager.showOverlay(
                        context = context,
                        item = prepared.item,
                        position = currentPosition
                    )
                }
                commitOverlayDisplay(context, prefs, prepared)
                com.example.widget.AtharGlanceWidget.updateWidget(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleUserUnlock", e)
            }
        }
    }

    private suspend fun prepareNextOverlayItem(
        context: Context,
        prefs: AtharPreferences
    ): PreloadedOverlay? {
        // 1. First Unlock of the Day after Fajr (04:00 AM+): Exclusively show "Jabr Al-Khatir" message
        if (JabrKhatirManager.shouldShowFajrJabrKhatir(context)) {
            val jabr = JabrKhatirManager.getTodayJabrKhatir()
            val jabrItem = OverlayContentItem(
                id = "jabr_${jabr.id}",
                contentType = ContentType.QURAN,
                badgeText = "بشارة فجرية 🌿 | جبر خاطر",
                mainText = jabr.ayahText,
                referenceText = jabr.surahAndVerse,
                topic = "جبر الخواطر والسكينة",
                benefitOrTafsir = jabr.reflection
            )
            return PreloadedOverlay(PreloadedKind.JABR_KHATIR, jabrItem)
        }

        // 2. Fasting Eve Reminder (eve of Monday, Thursday, Ayyam al-Beed, Arafah, Ashura)
        if (FastingReminderManager.shouldShowFastingOverlay(context)) {
            val fasting = FastingReminderManager.getActiveFastingOccasion(context)
            if (fasting != null) {
                val fastingItem = OverlayContentItem(
                    id = fasting.id,
                    contentType = ContentType.HADITH,
                    badgeText = fasting.title,
                    mainText = fasting.hadithText,
                    referenceText = fasting.source,
                    topic = "صيام التطوع (${fasting.occasionName})",
                    benefitOrTafsir = "فضل هذا اليوم: ${fasting.virtue}\n🌙 لا تنسَ تبييت النية والسحور المبارك"
                )
                return PreloadedOverlay(PreloadedKind.FASTING, fastingItem, fasting)
            }
        }

        // 3. Contextual Content Selection from cached Room pool
        val pool = getOrLoadCandidatePool(context, prefs)
        val chosen = ContentSelector.selectContextualContent(context, pool)
        return PreloadedOverlay(PreloadedKind.CONTEXTUAL, chosen)
    }

    private suspend fun getOrLoadCandidatePool(
        context: Context,
        prefs: AtharPreferences
    ): List<OverlayContentItem> {
        val showQuran = prefs.showQuranInOverlay.value
        val showHadith = prefs.showHadithInOverlay.value
        val showDhikr = prefs.showDhikrInOverlay.value
        val showDua = prefs.showDuaInOverlay.value
        val flagsKey = "$showQuran-$showHadith-$showDhikr-$showDua"

        val existing = cachedCandidatePool
        if (existing != null && existing.isNotEmpty() && cachedPoolFlags == flagsKey) {
            return existing
        }

        val db = AtharDatabase.getDatabase(context)
        val approvedVerses = db.quranDao().getApprovedVerses().first()
        val approvedHadiths = db.hadithDao().getApprovedHadiths().first()
        val approvedDhikr = db.dhikrDao().getApprovedDhikr().first()

        val allCandidates = ArrayList<OverlayContentItem>(
            approvedVerses.size + approvedHadiths.size + approvedDhikr.size
        )

        if (showQuran) {
            approvedVerses.forEach { verse ->
                allCandidates.add(
                    OverlayContentItem(
                        id = verse.id,
                        contentType = ContentType.QURAN,
                        badgeText = "سورة ${verse.surahName} (آية ${verse.verseNumber})",
                        mainText = verse.textUthmani,
                        referenceText = "سورة ${verse.surahName}: ${verse.verseNumber}",
                        topic = verse.topic,
                        benefitOrTafsir = verse.tafsirSnippet
                    )
                )
            }
        }

        if (showHadith) {
            approvedHadiths.forEach { hadith ->
                allCandidates.add(
                    OverlayContentItem(
                        id = hadith.id,
                        contentType = ContentType.HADITH,
                        badgeText = hadith.collectionName,
                        mainText = hadith.arabicText,
                        referenceText = hadith.collectionName,
                        topic = hadith.topic,
                        benefitOrTafsir = hadith.benefit
                    )
                )
            }
        }

        approvedDhikr.forEach { dhikr ->
            val isDuaItem = dhikr.category.contains("دعاء") || dhikr.category.contains("أدعية")
            if ((isDuaItem && showDua) || (!isDuaItem && showDhikr)) {
                allCandidates.add(
                    OverlayContentItem(
                        id = dhikr.id,
                        contentType = if (isDuaItem) ContentType.DUA else ContentType.DHIKR,
                        badgeText = dhikr.category,
                        mainText = dhikr.arabicText,
                        referenceText = dhikr.reference,
                        topic = if (isDuaItem) "أدعية مأثورة" else "أذكار مأثورة",
                        benefitOrTafsir = dhikr.virtue
                    )
                )
            }
        }

        if (allCandidates.isNotEmpty()) {
            cachedCandidatePool = allCandidates
            cachedPoolFlags = flagsKey
        }
        return allCandidates
    }

    private fun commitOverlayDisplay(
        context: Context,
        prefs: AtharPreferences,
        displayed: PreloadedOverlay
    ) {
        when (displayed.kind) {
            PreloadedKind.JABR_KHATIR -> {
                JabrKhatirManager.markJabrKhatirShownToday(context)
            }
            PreloadedKind.FASTING -> {
                displayed.fastingOccasion?.let {
                    FastingReminderManager.markFastingOverlayShown(context, it)
                }
            }
            PreloadedKind.CONTEXTUAL -> {
                prefs.recordShownContentId(displayed.item.id)
            }
        }
        prefs.recordOverlayShown()
    }

    private fun startAsForeground() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("أَثَــر | «افتح هاتفك على ما ينفع قلبك»")
            .setContentText("النافذة الإيمانية نشطة للتذكير بآية أو حديث عند فتح قفل الهاتف")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(NOTIFICATION_ID, notification, fgsType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "خدمة التذكير عند فتح القفل",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "إشعار استمرار خدمة نافذة أثر العائمة للتذكير بفتح الهاتف على ما ينفع"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d(TAG, "onTaskRemoved: scheduling service restart")
        scheduleServiceRestart(1000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        unregisterUnlockReceiver()
        serviceScope.cancel()
        val prefs = AtharPreferences(applicationContext)
        if (prefs.overlayEnabled.value && UnlockOverlayManager.canDrawOverlays(applicationContext)) {
            scheduleServiceRestart(1000L)
        }
    }

    private fun scheduleServiceRestart(delayMillis: Long = 1000L) {
        val prefs = AtharPreferences(applicationContext)
        if (!prefs.overlayEnabled.value || !UnlockOverlayManager.canDrawOverlays(applicationContext)) {
            return
        }

        try {
            val restartIntent = Intent(applicationContext, UnlockOverlayService::class.java).also {
                it.setPackage(packageName)
            }
            val restartPendingIntent = PendingIntent.getService(
                this,
                101,
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val triggerAtMillis = SystemClock.elapsedRealtime() + delayMillis

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME,
                    triggerAtMillis,
                    restartPendingIntent
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME,
                    triggerAtMillis,
                    restartPendingIntent
                )
            }
            ServiceWatchdogWorker.scheduleWatchdog(applicationContext)
            Log.d(TAG, "Service restart scheduled in ${delayMillis}ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling service restart", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "UnlockOverlayService"
        const val EXTRA_TRIGGER_NOW = "extra_trigger_now"
        private const val CHANNEL_ID = "athar_unlock_overlay_channel"
        private const val NOTIFICATION_ID = 1001
        private const val DEBOUNCE_TIME_MS = 2000L
        private const val PRELOAD_TTL_MS = 120_000L

        @Volatile
        private var lastTriggerTimestamp: Long = 0L

        fun startService(context: Context, triggerNow: Boolean = false) {
            val intent = Intent(context, UnlockOverlayService::class.java).apply {
                if (triggerNow) {
                    putExtra(EXTRA_TRIGGER_NOW, true)
                }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                ServiceWatchdogWorker.scheduleWatchdog(context)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start UnlockOverlayService", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UnlockOverlayService::class.java)
            context.stopService(intent)
            UnlockOverlayManager.dismissOverlay(context)
            ServiceWatchdogWorker.cancelWatchdog(context)
        }
    }
}
