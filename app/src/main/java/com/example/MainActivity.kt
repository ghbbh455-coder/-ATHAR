package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import android.content.Intent
import com.example.overlay.UnlockOverlayManager
import com.example.overlay.UnlockOverlayService
import com.example.ui.navigation.AtharMainContainer
import com.example.ui.viewmodel.AtharDestination
import com.example.ui.viewmodel.AtharViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AtharViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle navigation destination from Glance Widget or Notifications
        handleIntentDestination(intent)

        // Sync overlay service status if enabled and permission granted
        if (viewModel.prefs.overlayEnabled.value && UnlockOverlayManager.canDrawOverlays(this)) {
            UnlockOverlayService.startService(this)
        }

        setContent {
            AtharMainContainer(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentDestination(intent)
    }

    private fun handleIntentDestination(intent: Intent?) {
        val target = intent?.getStringExtra("TARGET_DESTINATION") ?: return
        when (target.lowercase()) {
            "permissions" -> viewModel.navigateTo(AtharDestination.Permissions)
            "quran" -> viewModel.navigateTo(AtharDestination.Quran)
            "hadith" -> viewModel.navigateTo(AtharDestination.Hadith)
            "adhkar" -> viewModel.navigateTo(AtharDestination.Adhkar)
            "favorites" -> viewModel.navigateTo(AtharDestination.Favorites)
            "settings" -> viewModel.navigateTo(AtharDestination.Settings)
            "all_content" -> viewModel.navigateTo(AtharDestination.AllContent)
            "home" -> viewModel.navigateTo(AtharDestination.Home)
        }
    }

    override fun onResume() {
        super.onResume()
        // If permission was granted while user was in system settings, sync service
        if (viewModel.prefs.overlayEnabled.value && UnlockOverlayManager.canDrawOverlays(this)) {
            UnlockOverlayService.startService(this)
        }
    }
}
