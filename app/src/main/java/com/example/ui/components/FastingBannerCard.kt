package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FastingOccasion
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassBorderBrush
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.util.ShareImageHelper
import kotlinx.coroutines.launch

/**
 * Seasonal Liquid Glass Banner displayed on the Home Screen during the eve of fasting
 * (from 19:00 PM until 04:00 AM Fajr) reminding the user to set their intention and prepare for Suhoor.
 */
@Composable
fun FastingBannerCard(
    occasion: FastingOccasion,
    onCopy: (String) -> Unit,
    onShare: (String, String) -> Unit,
    fontSizeMultiplier: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = AtharGlassDarkGreen),
        border = BorderStroke(1.2.dp, AtharGlassBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("fasting_banner_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Badge & Image Share Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AtharGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NightsStay,
                            contentDescription = null,
                            tint = AtharGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = occasion.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                    }
                }

                // Share as Image Action Button
                Surface(
                    shape = CircleShape,
                    color = AtharGold.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AtharGold),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            coroutineScope.launch {
                                ShareImageHelper.shareCardAsImage(
                                    context = context,
                                    badgeText = occasion.title,
                                    mainText = occasion.hadithText,
                                    referenceText = occasion.source,
                                    benefitOrReflection = "فضل الصيام: ${occasion.virtue}\nلا تنسَ تبييت النية والسحور 🌙"
                                )
                            }
                        }
                        .testTag("fasting_share_image_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "مشاركة كصورة",
                            tint = AtharGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hadith Text
            Text(
                text = occasion.hadithText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = AmiriQuranFont,
                    fontSize = (20 * fontSizeMultiplier).sp,
                    lineHeight = (30 * fontSizeMultiplier).sp,
                    color = AtharGoldLight,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Source & Hijri Date
            Text(
                text = "${occasion.source} • ${occasion.dateDisplay}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AtharIvoryDark,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Reflection & Niyyah reminder box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0x30102D25),
                border = BorderStroke(0.8.dp, AtharGold.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💡 ${occasion.virtue}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AtharIvory,
                            lineHeight = 18.sp,
                            fontSize = 12.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🌙 «تبييت النية قبل الفجر والسحور بركة وسنة متبعة.. تقبل الله طاعتكم»",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AtharGold,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Copy & Share Text Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onCopy("${occasion.title}\n${occasion.hadithText}\n[${occasion.source}]\n${occasion.virtue}")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "نسخ",
                        tint = AtharIvory.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = {
                        onShare(occasion.hadithText, "${occasion.title} - ${occasion.source}")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة",
                        tint = AtharIvory.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
