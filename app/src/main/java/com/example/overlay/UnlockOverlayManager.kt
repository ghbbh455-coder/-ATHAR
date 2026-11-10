package com.example.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.local.AtharPreferences
import com.example.data.local.db.AtharDatabase
import com.example.data.local.db.FavoriteEntity
import com.example.data.model.OverlayPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

object UnlockOverlayManager {

    private var currentOverlayView: View? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // Brand color constants
    private const val COLOR_DEEP_GREEN = 0xFF102D25.toInt()
    private const val COLOR_SECONDARY_GREEN = 0xFF244638.toInt()
    private const val COLOR_IVORY = 0xFFF5F0E6.toInt()
    private const val COLOR_GOLD = 0xFFD6BC82.toInt()
    private const val COLOR_GOLD_DARK = 0xFFB59A5A.toInt()

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    @SuppressLint("RtlHardcoded")
    fun showOverlay(
        context: Context,
        item: OverlayContentItem,
        position: OverlayPosition = OverlayPosition.TOP,
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

                val overlayCard = createOverlayCardView(context, item, onFavoriteToggled)

                val gravityVal = when (position) {
                    OverlayPosition.TOP -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
                    OverlayPosition.CENTER -> Gravity.CENTER
                    OverlayPosition.BOTTOM -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }

                val yOffset = when (position) {
                    OverlayPosition.TOP -> dpToPx(context, 48)
                    OverlayPosition.CENTER -> 0
                    OverlayPosition.BOTTOM -> dpToPx(context, 48)
                }

                val params = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
                ).apply {
                    gravity = gravityVal
                    y = yOffset
                }

                // Initial animation state
                overlayCard.alpha = 0f
                overlayCard.scaleX = 0.94f
                overlayCard.scaleY = 0.94f

                windowManager.addView(overlayCard, params)
                currentOverlayView = overlayCard

                // Smooth entrance animation
                overlayCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(240)
                    .start()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun dismissOverlay(context: Context) {
        mainHandler.post {
            val view = currentOverlayView ?: return@post
            try {
                // Smooth exit animation
                view.animate()
                    .alpha(0f)
                    .scaleX(0.92f)
                    .scaleY(0.92f)
                    .setDuration(180)
                    .withEndAction {
                        dismissOverlayImmediate(context)
                    }
                    .start()
            } catch (e: Exception) {
                e.printStackTrace()
                dismissOverlayImmediate(context)
            }
        }
    }

    private fun dismissOverlayImmediate(context: Context) {
        currentOverlayView?.let { view ->
            try {
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                currentOverlayView = null
            }
        }
    }

    fun isOverlayShowing(): Boolean {
        return currentOverlayView != null && currentOverlayView?.isAttachedToWindow == true
    }

    private fun createOverlayCardView(
        context: Context,
        item: OverlayContentItem,
        onFavoriteToggled: ((Boolean) -> Unit)?
    ): View {
        val rootLayout = FrameLayout(context).apply {
            setPadding(
                dpToPx(context, 16),
                dpToPx(context, 8),
                dpToPx(context, 16),
                dpToPx(context, 8)
            )
        }

        // Inner Card Container
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setPadding(
                dpToPx(context, 18),
                dpToPx(context, 16),
                dpToPx(context, 18),
                dpToPx(context, 16)
            )

            // Background with Athar deep green and subtle gold border
            background = GradientDrawable().apply {
                setColor(COLOR_DEEP_GREEN)
                setStroke(dpToPx(context, 1.5f), COLOR_GOLD)
                cornerRadius = dpToPx(context, 20).toFloat()
            }
            elevation = dpToPx(context, 10).toFloat()

            // Crucial: Tapping the card body does NOT launch the app.
            // It allows the user to read peacefully without being thrown out of their current screen.
        }

        // 1. Top Header Row: Category Badge (Right in RTL) + Actions (Favorite & Prominent Close X on Left)
        val headerRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Badge Pill
        val badge = TextView(context).apply {
            text = item.badgeText
            setTextColor(COLOR_IVORY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTypeface(Typeface.DEFAULT_BOLD)
            setPadding(dpToPx(context, 10), dpToPx(context, 4), dpToPx(context, 10), dpToPx(context, 4))
            background = GradientDrawable().apply {
                setColor(COLOR_SECONDARY_GREEN)
                setStroke(dpToPx(context, 1), COLOR_GOLD)
                cornerRadius = dpToPx(context, 10).toFloat()
            }
        }

        val badgeParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 0f
        }
        headerRow.addView(badge, badgeParams)

        // Spacer to push action buttons to the end
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, 1).apply {
                weight = 1f
            }
        }
        headerRow.addView(spacer)

        // Favorite Button
        var isFav = false
        val favButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.star_off)
            setColorFilter(COLOR_IVORY)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8))
            minimumWidth = dpToPx(context, 48)
            minimumHeight = dpToPx(context, 48)
            contentDescription = "حفظ في المفضلة"
        }

        // Check favorite status asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val db = AtharDatabase.getDatabase(context)
            isFav = db.favoriteDao().isFavoriteSync(item.id)
            withContext(Dispatchers.Main) {
                if (isFav) {
                    favButton.setImageResource(android.R.drawable.star_on)
                    favButton.setColorFilter(COLOR_GOLD)
                }
            }
        }

        favButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AtharDatabase.getDatabase(context)
                val prefs = AtharPreferences(context)
                val exists = db.favoriteDao().isFavoriteSync(item.id)
                if (exists) {
                    db.favoriteDao().deleteFavoriteByContentId(item.id)
                    prefs.toggleFavorite(item.id)
                    isFav = false
                } else {
                    db.favoriteDao().insertFavorite(
                        FavoriteEntity(
                            id = UUID.randomUUID().toString(),
                            contentId = item.id,
                            contentType = item.contentType.name,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    prefs.toggleFavorite(item.id)
                    isFav = true
                }
                withContext(Dispatchers.Main) {
                    if (isFav) {
                        favButton.setImageResource(android.R.drawable.star_on)
                        favButton.setColorFilter(COLOR_GOLD)
                        Toast.makeText(context, "تم الحفظ في المفضلة", Toast.LENGTH_SHORT).show()
                    } else {
                        favButton.setImageResource(android.R.drawable.star_off)
                        favButton.setColorFilter(COLOR_IVORY)
                        Toast.makeText(context, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show()
                    }
                    onFavoriteToggled?.invoke(isFav)
                }
            }
        }
        headerRow.addView(favButton)

        // Prominent Close "X" Button
        val closeButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(COLOR_GOLD)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8), dpToPx(context, 8))
            minimumWidth = dpToPx(context, 48)
            minimumHeight = dpToPx(context, 48)
            contentDescription = "إغلاق النافذة الإيمانية"
            setOnClickListener {
                dismissOverlay(context)
            }
        }
        headerRow.addView(closeButton)

        card.addView(headerRow)

        // 2. Main Scripture Text
        val amiriTypeface = try {
            ResourcesCompat.getFont(context, R.font.amiri)
        } catch (_: Exception) {
            Typeface.SERIF
        }

        val mainTextView = TextView(context).apply {
            text = item.mainText
            setTextColor(COLOR_IVORY)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 19.5f)
            typeface = amiriTypeface
            gravity = Gravity.CENTER
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            setLineSpacing(0f, 1.35f)
            setPadding(0, dpToPx(context, 14), 0, dpToPx(context, 10))
        }
        card.addView(mainTextView)

        // 3. Subtext (Tafsir / Virtue / Reference)
        if (item.benefitOrTafsir.isNotBlank() || item.referenceText.isNotBlank()) {
            val subTextView = TextView(context).apply {
                val combined = if (item.benefitOrTafsir.isNotBlank()) {
                    "${item.benefitOrTafsir} • ${item.referenceText}"
                } else {
                    item.referenceText
                }
                text = combined
                setTextColor(COLOR_GOLD_DARK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                gravity = Gravity.CENTER
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                setPadding(0, 0, 0, dpToPx(context, 8))
            }
            card.addView(subTextView)
        }

        // 4. Subtle Tagline Indicator at Bottom
        val taglineView = TextView(context).apply {
            text = "أَثَــر • «افتح هاتفك على ما ينفع قلبك»"
            setTextColor(COLOR_GOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(context, 4), 0, 0)
        }
        card.addView(taglineView)

        rootLayout.addView(card)
        return rootLayout
    }

    private fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    private fun dpToPx(context: Context, dp: Int): Int = dpToPx(context, dp.toFloat())
}
