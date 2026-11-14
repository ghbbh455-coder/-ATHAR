package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.mutableStateOf
import com.example.ui.components.DailyImpactCard
import com.example.ui.components.MoodGuidanceSheet
import com.example.ui.components.MoodSelectorCard
import com.example.ui.components.SpiritualMood
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.FastingReminderManager
import com.example.data.local.JabrKhatirManager
import com.example.data.model.ContentType
import com.example.overlay.UnlockOverlayManager
import com.example.ui.components.AtharCard
import com.example.ui.components.FastingBannerCard
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorderBrush
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharGoldLight
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharSecondaryGreen
import com.example.ui.viewmodel.AtharDestination
import com.example.ui.viewmodel.AtharViewModel
import com.example.util.ShareImageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AtharViewModel,
    onNavigate: (AtharDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val favoriteIds by viewModel.favoriteIdsSet.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val hasOverlayPermission = UnlockOverlayManager.canDrawOverlays(context)

    val dailyUnlockCount by viewModel.prefs.dailyUnlockCount.collectAsState()
    val dailyTasbeehCount by viewModel.prefs.dailyTasbeehCount.collectAsState()

    var activeMood by remember { mutableStateOf<SpiritualMood?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val approvedVerses by viewModel.approvedVerses.collectAsState()
    val approvedHadiths by viewModel.approvedHadiths.collectAsState()
    val approvedDhikr by viewModel.approvedDhikr.collectAsState()

    var selectedFaithTab by remember { mutableIntStateOf(0) } // 0: Quran, 1: Hadith, 2: Dhikr
    var quickCounter by remember { mutableIntStateOf(0) }

    val dailyAyah = approvedVerses.firstOrNull()
    val dailyHadith = approvedHadiths.firstOrNull()
    val dailyDhikr = approvedDhikr.firstOrNull()

    // Daily Jabr Al-Khatir reflection pinned for today
    val todayJabr = remember { JabrKhatirManager.getTodayJabrKhatir() }

    // Seasonal Fasting Eve Occasion (active 19:00 PM to Fajr 04:00 AM)
    val fastingOccasion = remember { FastingReminderManager.getActiveFastingOccasion(context) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Hero Header Section with Liquid Glass & Header Shortcuts (Favorites & Settings)
        item(key = "hero_header", contentType = "hero") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(245.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_islamic_pattern_1794331942075),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    AtharDeepGreen.copy(alpha = 0.55f),
                                    AtharDeepGreen.copy(alpha = 0.90f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )

                // Top Floating Glass Icons (Favorites & Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings Glass Button
                    Surface(
                        shape = CircleShape,
                        color = AtharGlassSecondary,
                        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onNavigate(AtharDestination.Settings) }
                            .testTag("home_top_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "الإعدادات",
                                tint = AtharGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Favorites Glass Button
                    Surface(
                        shape = CircleShape,
                        color = AtharGlassSecondary,
                        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { onNavigate(AtharDestination.Favorites) }
                            .testTag("home_top_favorites_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Bookmark,
                                contentDescription = "المفضلة",
                                tint = AtharGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Center Branding
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, AtharGold),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_athar_icon_1794331926985),
                                contentDescription = "شعار أثر",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "أَثَــر",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AtharGold
                                )
                            )
                            Text(
                                text = "ATHAR",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AtharIvory.copy(alpha = 0.8f),
                                    letterSpacing = 2.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // App Mottos
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = AtharSecondaryGreen.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = "«افتح هاتفك على ما ينفع قلبك»",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = AmiriQuranFont,
                                color = AtharGoldLight,
                                fontSize = 18.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Offline-First Status Indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = AtharGold.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "نظام إيماني موثوق يعمل بالكامل دون إنترنت",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AtharIvory.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        // Section: Daily Fajr "Jabr Al-Khatir" Pinned Glass Card (بشارة فجرية 🌿 | جبر خاطر)
        item(key = "jabr_khatir_card", contentType = "jabr_card") {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AtharGlassDarkGreen
                ),
                border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_jabr_khatir_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Row with Badge & Image Share Action
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WbSunny,
                                    contentDescription = null,
                                    tint = AtharGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "بشارة فجرية 🌿 | جبر خاطر",
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
                                            badgeText = "بشارة فجرية 🌿 | جبر خاطر",
                                            mainText = todayJabr.ayahText,
                                            referenceText = todayJabr.surahAndVerse,
                                            benefitOrReflection = todayJabr.reflection
                                        )
                                    }
                                }
                                .testTag("jabr_share_image_button")
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

                    // Ayah Scripture
                    Text(
                        text = "«${todayJabr.ayahText}»",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AmiriQuranFont,
                            fontSize = (21 * fontSize.scaleFactor).sp,
                            color = AtharGoldLight,
                            textAlign = TextAlign.Center,
                            lineHeight = (32 * fontSize.scaleFactor).sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = todayJabr.surahAndVerse,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AtharIvory.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reflection Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0x30102D25),
                        border = BorderStroke(0.8.dp, AtharGold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 ${todayJabr.reflection}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharIvory,
                                lineHeight = 19.sp,
                                fontSize = 12.5.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Section: Seasonal Fasting Eve Reminder (if active)
        if (fastingOccasion != null) {
            item(key = "fasting_banner", contentType = "fasting_banner") {
                FastingBannerCard(
                    occasion = fastingOccasion,
                    onCopy = { text -> viewModel.copyToClipboard(context, text) },
                    onShare = { text, ref -> viewModel.shareContent(context, fastingOccasion.title, text, ref) },
                    fontSizeMultiplier = fontSize.scaleFactor
                )
            }
        }

        // Section: "بماذا تشعر الآن؟" (Interactive Mood & Spiritual Guidance)
        item(key = "mood_selector", contentType = "mood_card") {
            MoodSelectorCard(
                onSelectMood = { activeMood = it }
            )
        }

        // Section: Daily Spiritual Impact Card
        item(key = "daily_impact", contentType = "impact_card") {
            DailyImpactCard(
                unlockCount = dailyUnlockCount,
                tasbeehCount = dailyTasbeehCount
            )
        }

        // Section: Floating Popup Status & Quick Toggle Card
        item(key = "overlay_status_card", contentType = "status_card") {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_overlay_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = AtharGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "نافذة التذكير عند فتح القفل",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AtharGold
                                )
                            )
                            Text(
                                text = if (overlayEnabled && hasOverlayPermission) {
                                    "مفعلة وتعمل عند كل فتح للقفل"
                                } else if (!hasOverlayPermission) {
                                    "تحتاج إلى منح إذن الظهور"
                                } else {
                                    "متوقفة مؤقتاً"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (overlayEnabled && hasOverlayPermission) AtharGoldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = overlayEnabled && hasOverlayPermission,
                        onCheckedChange = { isChecked ->
                            if (!hasOverlayPermission) {
                                onNavigate(AtharDestination.Permissions)
                            } else {
                                viewModel.setOverlayEnabled(context, isChecked)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AtharGold,
                            checkedTrackColor = AtharSecondaryGreen
                        )
                    )
                }
            }
        }

        // Section: Dedicated Faith Content Area
        item(key = "daily_faith_content", contentType = "faith_content") {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "المحتوى الإيماني لليوم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AtharGold.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, AtharGold.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable { onNavigate(AtharDestination.AllContent) }
                    ) {
                        Text(
                            text = "تصفح كل المحتوى ←",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AtharGold,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: الآية / الحديث / الذكر
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FaithTabChip(
                        selected = selectedFaithTab == 0,
                        title = "آية كريمة",
                        onClick = { selectedFaithTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    FaithTabChip(
                        selected = selectedFaithTab == 1,
                        title = "حديث شريف",
                        onClick = { selectedFaithTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    FaithTabChip(
                        selected = selectedFaithTab == 2,
                        title = "ذكر مأثور",
                        onClick = { selectedFaithTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Crossfade(targetState = selectedFaithTab, label = "faith_content_anim") { tab ->
                    when (tab) {
                        0 -> {
                            if (dailyAyah != null) {
                                val isFav = favoriteIds.contains(dailyAyah.id)
                                AtharCard(
                                    categoryBadge = "${dailyAyah.surahName} (الآية ${dailyAyah.verseNumber})",
                                    title = "الآية ${dailyAyah.verseNumber}",
                                    content = dailyAyah.textUthmani,
                                    subtext = "وقفة تدبرية: ${dailyAyah.tafsirSnippet}",
                                    isQuranOrHadith = true,
                                    fontSizeMultiplier = fontSize.scaleFactor,
                                    isFavorite = isFav,
                                    onToggleFavorite = { viewModel.toggleFavorite(dailyAyah.id, ContentType.QURAN) },
                                    onCopy = {
                                        viewModel.copyToClipboard(
                                            context,
                                            "${dailyAyah.textUthmani}\n[${dailyAyah.surahName}: ${dailyAyah.verseNumber}]"
                                        )
                                    },
                                    onShare = {
                                        viewModel.shareContent(
                                            context,
                                            "آية اليوم",
                                            dailyAyah.textUthmani,
                                            "${dailyAyah.surahName}: ${dailyAyah.verseNumber}"
                                        )
                                    },
                                    onShareImage = {
                                        coroutineScope.launch {
                                            ShareImageHelper.shareCardAsImage(
                                                context = context,
                                                badgeText = "${dailyAyah.surahName} (الآية ${dailyAyah.verseNumber})",
                                                mainText = dailyAyah.textUthmani,
                                                referenceText = "${dailyAyah.surahName}: ${dailyAyah.verseNumber}",
                                                benefitOrReflection = dailyAyah.tafsirSnippet
                                            )
                                        }
                                    }
                                )
                            } else {
                                EmptyOfflineNotice(message = "جاري تهيئة قاعدة بيانات الآيات القرآنية محلياً...")
                            }
                        }

                        1 -> {
                            if (dailyHadith != null) {
                                val isFav = favoriteIds.contains(dailyHadith.id)
                                AtharCard(
                                    categoryBadge = "${dailyHadith.collectionName} - رقم ${dailyHadith.hadithNumber}",
                                    title = dailyHadith.chapterName,
                                    content = dailyHadith.arabicText,
                                    subtext = "الفائدة الإيمانية: ${dailyHadith.benefit}\nالمصدر: ${dailyHadith.collectionName}",
                                    isQuranOrHadith = true,
                                    fontSizeMultiplier = fontSize.scaleFactor,
                                    isFavorite = isFav,
                                    onToggleFavorite = { viewModel.toggleFavorite(dailyHadith.id, ContentType.HADITH) },
                                    onCopy = {
                                        viewModel.copyToClipboard(
                                            context,
                                            "${dailyHadith.arabicText}\n[${dailyHadith.collectionName}: ${dailyHadith.hadithNumber}]"
                                        )
                                    },
                                    onShare = {
                                        viewModel.shareContent(
                                            context,
                                            dailyHadith.chapterName,
                                            dailyHadith.arabicText,
                                            "${dailyHadith.collectionName} - رقم ${dailyHadith.hadithNumber}"
                                        )
                                    },
                                    onShareImage = {
                                        coroutineScope.launch {
                                            ShareImageHelper.shareCardAsImage(
                                                context = context,
                                                badgeText = "${dailyHadith.collectionName} - رقم ${dailyHadith.hadithNumber}",
                                                mainText = dailyHadith.arabicText,
                                                referenceText = dailyHadith.chapterName,
                                                benefitOrReflection = dailyHadith.benefit
                                            )
                                        }
                                    }
                                )
                            } else {
                                EmptyOfflineNotice(message = "جاري تهيئة قاعدة بيانات الأحاديث الصحيحة محلياً...")
                            }
                        }

                        2 -> {
                            if (dailyDhikr != null) {
                                val isFav = favoriteIds.contains(dailyDhikr.id)
                                AtharCard(
                                    categoryBadge = dailyDhikr.category,
                                    title = "التكرار: ${dailyDhikr.repetitionCount} مرة",
                                    content = dailyDhikr.arabicText,
                                    subtext = "الفضل: ${dailyDhikr.virtue}\nالمصدر: ${dailyDhikr.sourceBook} (${dailyDhikr.reference})",
                                    isQuranOrHadith = true,
                                    fontSizeMultiplier = fontSize.scaleFactor,
                                    isFavorite = isFav,
                                    onToggleFavorite = { viewModel.toggleFavorite(dailyDhikr.id, ContentType.DHIKR) },
                                    onCopy = {
                                        viewModel.copyToClipboard(
                                            context,
                                            "${dailyDhikr.arabicText}\n[${dailyDhikr.sourceBook} - ${dailyDhikr.reference}]"
                                        )
                                    },
                                    onShare = {
                                        viewModel.shareContent(
                                            context,
                                            "ذكر اليوم",
                                            dailyDhikr.arabicText,
                                            "${dailyDhikr.sourceBook} (${dailyDhikr.reference})"
                                        )
                                    },
                                    onShareImage = {
                                        coroutineScope.launch {
                                            ShareImageHelper.shareCardAsImage(
                                                context = context,
                                                badgeText = dailyDhikr.category,
                                                mainText = dailyDhikr.arabicText,
                                                referenceText = "${dailyDhikr.sourceBook} (${dailyDhikr.reference})",
                                                benefitOrReflection = dailyDhikr.virtue
                                            )
                                        }
                                    }
                                )
                            } else {
                                EmptyOfflineNotice(message = "جاري تهيئة قاعدة بيانات الأذكار الموثقة محلياً...")
                            }
                        }
                    }
                }
            }
        }

        // Section: Instant Dhikr & Tasbih Tap Box
        item(key = "instant_tasbih", contentType = "tasbih_box") {
            Spacer(modifier = Modifier.height(18.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_instant_tasbih"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مسبحة أثر السريعة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        IconButton(
                            onClick = { quickCounter = 0 },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "إعادة ضبط",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "«سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ»",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = AmiriQuranFont,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .clickable {
                                quickCounter++
                                viewModel.prefs.incrementDailyTasbeehCount(1)
                            },
                        color = AtharGold,
                        shape = CircleShape,
                        border = BorderStroke(2.dp, AtharIvory)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$quickCounter",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AtharDeepGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "اضغط للتسبيح واحتساب الأثر",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }

        // Section: Main Navigation Grid (with wrapContentHeight to prevent text clipping)
        item(key = "main_nav_grid", contentType = "nav_grid") {
            Spacer(modifier = Modifier.height(22.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "أقسام أثر",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavGridCard(
                        title = "القرآن الكريم",
                        subtitle = "فهرس ١١٤ سورة ورسم عثماني",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        onClick = { onNavigate(AtharDestination.Quran) },
                        modifier = Modifier.weight(1f)
                    )
                    NavGridCard(
                        title = "الحديث النبوي",
                        subtitle = "الكتب الستة وأبواب الإيمان",
                        icon = Icons.Filled.FormatQuote,
                        onClick = { onNavigate(AtharDestination.Hadith) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavGridCard(
                        title = "الأذكار والأدعية",
                        subtitle = "حصن المسلم وأدعية القرآن والسنة",
                        icon = Icons.Filled.VolunteerActivism,
                        onClick = { onNavigate(AtharDestination.Adhkar) },
                        modifier = Modifier.weight(1f)
                    )
                    NavGridCard(
                        title = "كل المحتوى",
                        subtitle = "بحث وتصفح جامع شامل",
                        icon = Icons.Filled.AutoAwesome,
                        onClick = { onNavigate(AtharDestination.AllContent) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NavGridCard(
                        title = "المفضلة",
                        subtitle = "آثارك المحفوظة للرجوع",
                        icon = Icons.Filled.Bookmark,
                        onClick = { onNavigate(AtharDestination.Favorites) },
                        modifier = Modifier.weight(1f)
                    )
                    NavGridCard(
                        title = "الإعدادات",
                        subtitle = "تخصيص النافذة والخط والمظهر",
                        icon = Icons.Filled.Settings,
                        onClick = { onNavigate(AtharDestination.Settings) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Spiritual Mood Guidance
    if (activeMood != null) {
        MoodGuidanceSheet(
            mood = activeMood!!,
            sheetState = sheetState,
            onDismiss = { activeMood = null },
            onCopy = { text -> viewModel.copyToClipboard(context, text) },
            onShare = { text, ref -> viewModel.shareContent(context, "تذكير إيماني", text, ref) },
            fontSizeMultiplier = fontSize.scaleFactor
        )
    }
}

@Composable
fun EmptyOfflineNotice(message: String) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.CloudOff,
                contentDescription = null,
                tint = AtharGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun FaithTabChip(
    selected: Boolean,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) AtharGold else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (selected) AtharGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) AtharDeepGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun NavGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .testTag("nav_card_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = AtharGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AtharGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
