package com.example.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AtharPreferences
import com.example.widget.AtharGlanceWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnlockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val prefs = AtharPreferences(context)
        val isEnabled = prefs.overlayEnabled.value

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    UnlockOverlayService.startService(context)
                }
                // Refresh widget upon boot/update
                CoroutineScope(Dispatchers.IO).launch {
                    AtharGlanceWidget.updateWidget(context)
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    // Ensures the overlay service is started/active upon unlock
                    UnlockOverlayService.startService(context)
                }
                // Refresh widget upon unlock
                CoroutineScope(Dispatchers.IO).launch {
                    AtharGlanceWidget.updateWidget(context)
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? android.app.KeyguardManager
                    if (keyguardManager != null && !keyguardManager.isKeyguardLocked) {
                        UnlockOverlayService.startService(context)
                    }
                }
            }
        }
    }
}
