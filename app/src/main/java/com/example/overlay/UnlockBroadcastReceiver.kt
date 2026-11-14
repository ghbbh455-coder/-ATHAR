package com.example.overlay

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.example.data.local.AtharPreferences
import com.example.widget.AtharGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnlockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action ?: return
        Log.d(TAG, "UnlockBroadcastReceiver received action: $action")

        val prefs = AtharPreferences(context)
        val isEnabled = prefs.overlayEnabled.value

        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    UnlockOverlayService.startService(context)
                }
                // Refresh widget upon boot/update
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        AtharGlanceWidget.updateWidget(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating widget on boot", e)
                    }
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                val now = SystemClock.elapsedRealtime()
                if (now - lastReceiverTimestamp >= RECEIVER_DEBOUNCE_MS) {
                    lastReceiverTimestamp = now
                    if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                        UnlockOverlayService.startService(context, triggerNow = true)
                    }
                }
                // Refresh widget upon unlock
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        AtharGlanceWidget.updateWidget(context)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating widget on unlock", e)
                    }
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                // If device has no screen lock or keyguard is unlocked, trigger immediately
                if (keyguardManager != null && !keyguardManager.isKeyguardLocked) {
                    val now = SystemClock.elapsedRealtime()
                    if (now - lastReceiverTimestamp >= RECEIVER_DEBOUNCE_MS) {
                        lastReceiverTimestamp = now
                        if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                            UnlockOverlayService.startService(context, triggerNow = true)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "UnlockBroadcastReceiver"
        private const val RECEIVER_DEBOUNCE_MS = 2000L

        @Volatile
        private var lastReceiverTimestamp: Long = 0L
    }
}
