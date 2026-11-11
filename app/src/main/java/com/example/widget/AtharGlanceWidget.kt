package com.example.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.local.AtharPreferences

class AtharGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = AtharPreferences(context)
        val content = prefs.getWidgetContent()

        provideContent {
            AtharWidgetLayout(context = context, content = content)
        }
    }

    companion object {
        private val ColorDeepGreen = Color(0xFF102D25)
        private val ColorSecondaryGreen = Color(0xFF244638)
        private val ColorIvory = Color(0xFFF5F0E6)
        private val ColorGold = Color(0xFFD6BC82)
        private val ColorGoldDark = Color(0xFFB59A5A)

        suspend fun updateWidget(context: Context) {
            try {
                val prefs = AtharPreferences(context)
                val nextItem = AtharContentSelector.getNextRandomItem(context)
                prefs.saveWidgetContent(
                    AtharPreferences.WidgetContent(
                        id = nextItem.id,
                        type = nextItem.targetDestination,
                        badge = nextItem.badge,
                        text = nextItem.text,
                        reference = nextItem.reference,
                        isFavorite = nextItem.isFavorite
                    )
                )
                AtharGlanceWidget().updateAll(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Composable
    private fun AtharWidgetLayout(context: Context, content: AtharPreferences.WidgetContent) {
        val currentSize = LocalSize.current
        val isCompactHeight = currentSize.height < 110.dp
        val isVeryCompactWidth = currentSize.width < 180.dp
        val isExpanded = currentSize.height >= 180.dp

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("TARGET_DESTINATION", content.type)
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(ColorDeepGreen))
                .cornerRadius(18.dp)
                .padding(if (isCompactHeight) 8.dp else 12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row: App Identity + Badge + Action Buttons (Star Favorite & Reload ↻)
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Athar Brand Symbol & Title
                    if (!isVeryCompactWidth) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = GlanceModifier.clickable(actionStartActivity(openAppIntent))
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_athar_gold_symbol),
                                contentDescription = "أثر",
                                modifier = GlanceModifier.size(if (isCompactHeight) 15.dp else 18.dp)
                            )
                            Spacer(modifier = GlanceModifier.width(4.dp))
                            Text(
                                text = "أَثَــر",
                                style = TextStyle(
                                    color = ColorProvider(ColorGold),
                                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = GlanceModifier.width(6.dp))
                    }

                    // Category Badge Pill
                    Box(
                        modifier = GlanceModifier
                            .background(ColorProvider(ColorSecondaryGreen))
                            .cornerRadius(8.dp)
                            .padding(
                                horizontal = if (isCompactHeight) 6.dp else 8.dp,
                                vertical = 2.dp
                            )
                            .clickable(actionStartActivity(openAppIntent))
                    ) {
                        Text(
                            text = content.badge,
                            style = TextStyle(
                                color = ColorProvider(ColorIvory),
                                fontSize = if (isCompactHeight) 9.sp else 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Favorite Toggle Button (Gold Star: filled if favorite, outline if not)
                    Box(
                        modifier = GlanceModifier
                            .size(if (isCompactHeight) 24.dp else 28.dp)
                            .background(ColorProvider(ColorSecondaryGreen))
                            .cornerRadius(14.dp)
                            .clickable(actionRunCallback<ToggleFavoriteActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(
                                if (content.isFavorite) R.drawable.ic_star_gold_filled
                                else R.drawable.ic_star_gold_outline
                            ),
                            contentDescription = if (content.isFavorite) "في المفضلة" else "إضافة للمفضلة",
                            modifier = GlanceModifier.size(if (isCompactHeight) 14.dp else 16.dp)
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(6.dp))

                    // 1-Tap Instant Reload Button (↻)
                    Box(
                        modifier = GlanceModifier
                            .size(if (isCompactHeight) 24.dp else 28.dp)
                            .background(ColorProvider(ColorSecondaryGreen))
                            .cornerRadius(14.dp)
                            .clickable(actionRunCallback<ReloadContentActionCallback>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(R.drawable.ic_refresh_gold),
                            contentDescription = "تبديل المحتوى فورياً",
                            modifier = GlanceModifier.size(if (isCompactHeight) 14.dp else 16.dp)
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(if (isCompactHeight) 4.dp else 8.dp))

                // Scripture / Hadith / Dhikr Content (Clickable -> Opens App)
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                        .clickable(actionStartActivity(openAppIntent)),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSizeVal = when {
                        isCompactHeight -> 11.5.sp
                        isExpanded -> 15.5.sp
                        else -> 13.5.sp
                    }
                    val maxLinesVal = when {
                        isCompactHeight -> 2
                        isExpanded -> 7
                        else -> 4
                    }

                    Text(
                        text = content.text,
                        style = TextStyle(
                            color = ColorProvider(ColorIvory),
                            fontSize = fontSizeVal,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = maxLinesVal
                    )
                }

                Spacer(modifier = GlanceModifier.height(if (isCompactHeight) 2.dp else 4.dp))

                // Footer: Reference in clean Ivory color (#F5F0E6) + optional Tagline
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .clickable(actionStartActivity(openAppIntent)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reference text strictly in ivory #F5F0E6 without any Tanzil or link
                    Text(
                        text = content.reference,
                        style = TextStyle(
                            color = ColorProvider(ColorIvory),
                            fontSize = if (isCompactHeight) 8.5.sp else 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Start
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )

                    if (!isCompactHeight && !isVeryCompactWidth) {
                        Text(
                            text = "«افتح هاتفك على ما ينفع قلبك»",
                            style = TextStyle(
                                color = ColorProvider(ColorGold),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
