package com.example.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.example.data.model.OverlayPosition

object UnlockOverlayManager {

    private const val TAG = "UnlockOverlayManager"

    @Volatile
    private var currentOverlayView: View? = null

    @Volatile
    private var currentLifecycleOwner: OverlayLifecycleOwner? = null

    @Volatile
    private var isShowing: Boolean = false

    private val mainHandler = Handler(Looper.getMainLooper())

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isOverlayShowing(): Boolean {
        return isShowing && currentOverlayView != null
    }

    @SuppressLint("RtlHardcoded")
    fun showOverlay(
        context: Context,
        item: OverlayContentItem,
        position: OverlayPosition = OverlayPosition.CENTER,
        onFavoriteToggled: ((Boolean) -> Unit)? = null
    ) {
        if (!canDrawOverlays(context)) {
            Log.w(TAG, "Cannot draw overlays: SYSTEM_ALERT_WINDOW permission missing")
            return
        }

        mainHandler.post {
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                if (windowManager == null) {
                    Log.e(TAG, "WindowManager service unavailable")
                    return@post
                }

                // If previous overlay is still lingering, remove it immediately to prevent frozen/stacked views
                dismissOverlayImmediate(context)

                // Create a fresh OverlayLifecycleOwner initialized in RESUMED state
                val lifecycleOwner = OverlayLifecycleOwner()
                currentLifecycleOwner = lifecycleOwner

                // Create a fresh ComposeView bound to the new lifecycle owner
                val overlayView = FloatingOverlayViewFactory.createOverlayView(
                    context = context,
                    item = item,
                    position = position,
                    lifecycleOwner = lifecycleOwner,
                    onRequestDismiss = {
                        dismissOverlay(context)
                    }
                )

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    x = 0
                    y = 0
                }

                try {
                    windowManager.addView(overlayView, params)
                    currentOverlayView = overlayView
                    isShowing = true
                    Log.d(TAG, "Overlay successfully added to WindowManager (Item: ${item.id})")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add overlay view to WindowManager", e)
                    dismissOverlayImmediate(context)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in showOverlay", e)
                dismissOverlayImmediate(context)
            }
        }
    }

    fun dismissOverlay(context: Context) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            dismissOverlayImmediate(context)
        } else {
            mainHandler.post {
                dismissOverlayImmediate(context)
            }
        }
    }

    private fun dismissOverlayImmediate(context: Context) {
        val viewToRemove = currentOverlayView
        currentOverlayView = null
        isShowing = false

        if (viewToRemove != null) {
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
                windowManager?.removeViewImmediate(viewToRemove)
                Log.d(TAG, "Overlay view removed immediately via removeViewImmediate")
            } catch (e: IllegalArgumentException) {
                // View was not attached to window manager
                Log.w(TAG, "View was not attached when removing: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception during removeViewImmediate", e)
            }
        }

        try {
            currentLifecycleOwner?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying OverlayLifecycleOwner", e)
        } finally {
            currentLifecycleOwner = null
        }
    }
}
