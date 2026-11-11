package com.example.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.example.data.model.OverlayPosition

object UnlockOverlayManager {

    private var currentOverlayView: View? = null
    private var currentLifecycleOwner: OverlayLifecycleOwner? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun isOverlayShowing(): Boolean {
        return currentOverlayView != null && currentOverlayView?.isAttachedToWindow == true
    }

    @SuppressLint("RtlHardcoded")
    fun showOverlay(
        context: Context,
        item: OverlayContentItem,
        position: OverlayPosition = OverlayPosition.CENTER,
        onFavoriteToggled: ((Boolean) -> Unit)? = null
    ) {
        if (!canDrawOverlays(context)) {
            return
        }

        mainHandler.post {
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

                // Remove existing overlay cleanly if attached
                dismissOverlayImmediate(context)

                val lifecycleOwner = OverlayLifecycleOwner()
                currentLifecycleOwner = lifecycleOwner

                val overlayView = FloatingOverlayViewFactory.createOverlayView(
                    context = context,
                    item = item,
                    position = position,
                    lifecycleOwner = lifecycleOwner,
                    onRequestDismiss = {
                        dismissOverlayImmediate(context)
                    }
                )

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = Gravity.CENTER
                    x = 0
                    y = 0
                }

                try {
                    windowManager.addView(overlayView, params)
                    currentOverlayView = overlayView
                } catch (e: WindowManager.BadTokenException) {
                    e.printStackTrace()
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissOverlay(context: Context) {
        mainHandler.post {
            dismissOverlayImmediate(context)
        }
    }

    private fun dismissOverlayImmediate(context: Context) {
        currentOverlayView?.let { view ->
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            } catch (e: IllegalArgumentException) {
                // View was not attached or already detached
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                currentOverlayView = null
            }
        }
        currentLifecycleOwner?.destroy()
        currentLifecycleOwner = null
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
}
