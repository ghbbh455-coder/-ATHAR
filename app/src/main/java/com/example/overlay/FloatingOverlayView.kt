package com.example.overlay

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import com.example.util.ShareImageHelper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.local.AtharPreferences
import com.example.data.local.db.AtharDatabase
import com.example.data.local.db.FavoriteEntity
import com.example.data.model.OverlayPosition
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassBorderHighlight
import com.example.ui.theme.AtharGlassButtonBg
import com.example.ui.theme.AtharGlassButtonBorder
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGlassShadow
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.ui.theme.AtharTheme
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


object FloatingOverlayViewFactory {

    /**
     * Creates a fully initialized ComposeView attached to WindowManager with Liquid Glass UI
     * and hardware-accelerated entry/exit animations.
     */
    fun createOverlayView(
        context: Context,
        item: OverlayContentItem,
        position: OverlayPosition = OverlayPosition.CENTER,
        lifecycleOwner: OverlayLifecycleOwner,
        onRequestDismiss: () -> Unit
    ): ComposeView {
        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AtharTheme(darkTheme = true) {
                        FloatingOverlayCard(
                            item = item,
                            position = position,
                            onCloseRequested = onRequestDismiss
                        )
                    }
                }
            }
        }
        return composeView
    }
}

@Composable
fun FloatingOverlayCard(
    item: OverlayContentItem,
    onCloseRequested: () -> Unit,
    position: OverlayPosition = OverlayPosition.CENTER,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isVisible by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    // Check favorite status on launch
    LaunchedEffect(item.id) {
        withContext(Dispatchers.IO) {
            val db = AtharDatabase.getDatabase(context)
            val fav = db.favoriteDao().isFavoriteSync(item.id)
            withContext(Dispatchers.Main) {
                isFavorite = fav
            }
        }
    }

    // Trigger smooth entrance animation
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Graceful exit with animation
    fun handleClose() {
        coroutineScope.launch {
            isVisible = false
            delay(170L) // Wait for scaleOut + fadeOut
            onCloseRequested()
        }
    }

    fun handleFavoriteToggle() {
        coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val db = AtharDatabase.getDatabase(context)
                val prefs = AtharPreferences(context)
                val exists = db.favoriteDao().isFavoriteSync(item.id)
                if (exists) {
                    db.favoriteDao().deleteFavoriteByContentId(item.id)
                    prefs.toggleFavorite(item.id)
                    withContext(Dispatchers.Main) {
                        isFavorite = false
                        Toast.makeText(context, "تمت الإزالة من المفضلة", Toast.LENGTH_SHORT).show()
                    }
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
                    withContext(Dispatchers.Main) {
                        isFavorite = true
                        Toast.makeText(context, "تم الحفظ في المفضلة", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun handleShare() {
        try {
            val shareText = buildString {
                append("«${item.mainText}»\n\n")
                if (item.badgeText.isNotBlank()) {
                    append("المصدر: ${item.badgeText}\n")
                }
                if (item.benefitOrTafsir.isNotBlank()) {
                    append("الفائدة: ${item.benefitOrTafsir}\n")
                }
                append("\nمن تطبيق أَثَــر | «افتح هاتفك على ما ينفع قلبك»")
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "مشاركة النص الإيماني").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleShareAsImage() {
        coroutineScope.launch {
            ShareImageHelper.shareCardAsImage(
                context = context,
                badgeText = item.badgeText,
                mainText = item.mainText,
                referenceText = item.referenceText,
                benefitOrReflection = item.benefitOrTafsir
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                handleClose()
            },
        contentAlignment = when (position) {
            OverlayPosition.TOP -> Alignment.TopCenter
            OverlayPosition.CENTER -> Alignment.Center
            OverlayPosition.BOTTOM -> Alignment.BottomCenter
        }
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.85f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 250)
            ),
            exit = scaleOut(
                targetScale = 0.9f,
                animationSpec = tween(durationMillis = 150)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 150)
            )
        ) {
            // Liquid Glass Card Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp,
                        vertical = when (position) {
                            OverlayPosition.TOP -> 48.dp
                            OverlayPosition.CENTER -> 16.dp
                            OverlayPosition.BOTTOM -> 48.dp
                        }
                    )
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = AtharGlassShadow,
                        ambientColor = AtharGlassShadow
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Consume click inside card so touching scripture/card does not dismiss the popup
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                AtharGlassDarkGreen,
                                AtharGlassSecondary
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                AtharGlassBorderHighlight,
                                AtharGlassBorder,
                                AtharGlassBorderHighlight
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row: Category Badge (Right in RTL) + Actions (Share, Favorite, Close X)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Badge Pill with Frosted Glass Look
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33F5F0E6))
                            .border(1.dp, AtharGlassBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = AtharIvory
                            )
                        )
                    }

                    // Action Buttons Row (Left in RTL)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Share as Image Button
                        GlassActionButton(
                            onClick = { handleShareAsImage() },
                            contentDescription = "مشاركة كصورة مصممة"
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = AtharGold,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Share Text Button
                        GlassActionButton(
                            onClick = { handleShare() },
                            contentDescription = "مشاركة النص الإيماني"
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = AtharGold,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Favorite Button
                        GlassActionButton(
                            onClick = { handleFavoriteToggle() },
                            contentDescription = if (isFavorite) "إزالة من المفضلة" else "حفظ في المفضلة"
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = null,
                                tint = if (isFavorite) AtharGold else AtharIvory,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Circular Close (X) Button with Glass Glow
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x40D6BC82))
                                .border(1.2.dp, AtharGold, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true, radius = 18.dp),
                                    onClick = { handleClose() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق النافذة الإيمانية",
                                tint = AtharIvory,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Scripture / Hadith / Dhikr Text
                Text(
                    text = item.mainText,
                    style = TextStyle(
                        fontFamily = AmiriQuranFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 21.sp,
                        lineHeight = 36.sp,
                        textAlign = TextAlign.Center,
                        color = AtharIvory
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )

                // Subtext: Benefit or Tafsir Snippet & Reference (Clean, without raw URLs)
                val cleanBenefit = item.benefitOrTafsir
                    .replace("Tanzil Project (tanzil.net)", "")
                    .replace("tanzil.net", "")
                    .trim()
                val cleanRef = item.referenceText
                    .replace("Tanzil Project (tanzil.net)", "")
                    .replace("tanzil.net", "")
                    .trim()

                if (cleanBenefit.isNotBlank() || cleanRef.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val combinedRef = if (cleanBenefit.isNotBlank() && cleanRef.isNotBlank()) {
                        "${cleanBenefit} • ${cleanRef}"
                    } else {
                        cleanBenefit.ifBlank { cleanRef }
                    }
                    Text(
                        text = combinedRef,
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            textAlign = TextAlign.Center,
                            color = AtharIvoryDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtle Tagline at bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(AtharGoldDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "أَثَــر • «افتح هاتفك على ما ينفع قلبك»",
                        style = TextStyle(
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = AtharGold
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(AtharGoldDark)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassActionButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(AtharGlassButtonBg)
            .border(1.dp, AtharGlassButtonBorder, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 18.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
