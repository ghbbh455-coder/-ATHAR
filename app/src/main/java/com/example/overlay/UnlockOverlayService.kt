package com.example.overlay

import android.app.Notification
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
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AtharPreferences
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

import android.app.KeyguardManager
import android.os.PowerManager

class UnlockOverlayService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isReceiverRegistered = false

    private val unlockAndScreenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ctx = context ?: return
            when (intent?.action) {
                Intent.ACTION_USER_PRESENT -> {
                    triggerUnlockReminder(ctx)
                }
                Intent.ACTION_SCREEN_ON -> {
                    val keyguardManager = ctx.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                    // Fallback for phones without lockscreen / PIN: if screen turned on and is NOT locked, trigger
                    if (keyguardManager != null && !keyguardManager.isKeyguardLocked) {
                        triggerUnlockReminder(ctx)
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startAsForeground()
        registerUnlockReceiver()
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
            registerReceiver(unlockAndScreenReceiver, filter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterUnlockReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(unlockAndScreenReceiver)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isReceiverRegistered = false
        }
    }

    private fun triggerUnlockReminder(context: Context) {
        // Prevent stacking two overlay windows on top of each other
        if (UnlockOverlayManager.isOverlayShowing()) {
            return
        }

        try {
            // Briefly acquire partial wakelock (3s) to ensure CPU does not sleep during overlay launch
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "athar:unlock_wakelock")
            wakeLock?.acquire(3000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        handleUserUnlock(context)
        serviceScope.launch {
            com.example.widget.AtharGlanceWidget.updateWidget(context)
        }
    }

    private fun handleUserUnlock(context: Context) {
        val prefs = AtharPreferences(context)
        if (!prefs.canShowOverlayNow()) {
            return
        }

        if (!UnlockOverlayManager.canDrawOverlays(context)) {
            return
        }

        serviceScope.launch {
            try {
                val currentPosition = prefs.overlayPosition.value

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

                    withContext(Dispatchers.Main) {
                        UnlockOverlayManager.showOverlay(
                            context = context,
                            item = jabrItem,
                            position = currentPosition
                        )
                    }

                    JabrKhatirManager.markJabrKhatirShownToday(context)
                    prefs.recordOverlayShown()
                    return@launch
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

                        withContext(Dispatchers.Main) {
                            UnlockOverlayManager.showOverlay(
                                context = context,
                                item = fastingItem,
                                position = currentPosition
                            )
                        }

                        FastingReminderManager.markFastingOverlayShown(context, fasting)
                        prefs.recordOverlayShown()
                        return@launch
                    }
                }

                val db = AtharDatabase.getDatabase(context)
                val approvedVerses = db.quranDao().getApprovedVerses().first()
                val approvedHadiths = db.hadithDao().getApprovedHadiths().first()
                val approvedDhikr = db.dhikrDao().getApprovedDhikr().first()

                val showQuran = prefs.showQuranInOverlay.value
                val showHadith = prefs.showHadithInOverlay.value
                val showDhikr = prefs.showDhikrInOverlay.value
                val showDua = prefs.showDuaInOverlay.value

                // Create a pool of approved items based on user preferences
                val allCandidates = mutableListOf<OverlayContentItem>()

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

                // 2. Contextual Content Selection (Morning, Evening, Night, Friday, etc.)
                val chosen = ContentSelector.selectContextualContent(context, allCandidates)

                withContext(Dispatchers.Main) {
                    UnlockOverlayManager.showOverlay(
                        context = context,
                        item = chosen,
                        position = currentPosition
                    )
                }

                prefs.recordShownContentId(chosen.id)
                prefs.recordOverlayShown()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
            alarmManager?.set(
                android.app.AlarmManager.ELAPSED_REALTIME,
                android.os.SystemClock.elapsedRealtime() + 1500L,
                restartPendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterUnlockReceiver()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_TRIGGER_NOW = "extra_trigger_now"
        private const val CHANNEL_ID = "athar_unlock_overlay_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context, triggerNow: Boolean = false) {
            val intent = Intent(context, UnlockOverlayService::class.java).apply {
                if (triggerNow) {
                    putExtra(EXTRA_TRIGGER_NOW, true)
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, UnlockOverlayService::class.java)
            context.stopService(intent)
            UnlockOverlayManager.dismissOverlay(context)
        }
    }
}
