package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.toArabicDigits
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassBorderBrush
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark

/**
 * Calm, encouraging Liquid Glass card displaying the user's daily spiritual milestones
 * phrased in a warm, comforting tone without technical jargon.
 */
@Composable
fun DailyImpactCard(
    unlockCount: Int,
    tasbeehCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AtharGlassDarkGreen),
        border = BorderStroke(1.2.dp, AtharGlassBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("daily_impact_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0x33D6BC82),
                        border = BorderStroke(1.dp, AtharGold),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.VolunteerActivism,
                                contentDescription = null,
                                tint = AtharGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "أثرك الإيماني اليوم 🌿",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        Text(
                            text = "خطواتك اليومية في ذكر الله وطمأنينة القلب",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharIvoryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Stats Blocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Unlocks & Reminders Stat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x28102D25),
                    border = BorderStroke(1.dp, AtharGlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = AtharGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = toArabicDigits(unlockCount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (unlockCount == 1) "فتحت هاتفك مرة وذكرت الله فيها" else "فتحت هاتفك ${toArabicDigits(unlockCount)} مرات وذكرت الله فيهم",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AtharIvory,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Daily Tasbeeh Stat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x28102D25),
                    border = BorderStroke(1.dp, AtharGlassBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TouchApp,
                            contentDescription = null,
                            tint = AtharGoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = toArabicDigits(tasbeehCount),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "مجموع تسبيحاتك اليوم: ${toArabicDigits(tasbeehCount)} تسبيحة",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AtharIvory,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warm Devotional Quote
            Text(
                text = "«جعل الله كل نظرة في هاتفك شاهداً لك لا عليك 🤍»",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AtharGoldLight.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.5.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
