package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.overlay.UnlockOverlayManager
import com.example.overlay.UnlockOverlayService
import com.example.ui.navigation.AtharMainContainer
import com.example.ui.viewmodel.AtharViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: AtharViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync overlay service status if enabled and permission granted
        if (viewModel.prefs.overlayEnabled.value && UnlockOverlayManager.canDrawOverlays(this)) {
            UnlockOverlayService.startService(this)
        }

        setContent {
            AtharMainContainer(viewModel = viewModel)
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
