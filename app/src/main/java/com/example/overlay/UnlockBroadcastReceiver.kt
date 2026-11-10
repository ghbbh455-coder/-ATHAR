package com.example.overlay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.AtharPreferences

class UnlockBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val prefs = AtharPreferences(context)
        val isEnabled = prefs.overlayEnabled.value

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    UnlockOverlayService.startService(context)
                }
            }
            Intent.ACTION_USER_PRESENT -> {
                if (isEnabled && UnlockOverlayManager.canDrawOverlays(context)) {
                    // Ensures the overlay service is started/active upon unlock
                    UnlockOverlayService.startService(context)
                }
            }
        }
    }
}
