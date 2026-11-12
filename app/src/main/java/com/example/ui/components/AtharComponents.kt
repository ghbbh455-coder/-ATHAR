package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.util.ShareImageHelper
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharGoldDark
import com.example.ui.theme.AtharSecondaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtharTopBar(
    title: String,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("topbar_title")
            )
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("topbar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = {
            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}

@Composable
fun AtharCard(
    modifier: Modifier = Modifier,
    categoryBadge: String? = null,
    title: String,
    content: String,
    subtext: String? = null,
    isQuranOrHadith: Boolean = true,
    fontSizeMultiplier: Float = 1.0f,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onShareImage: (() -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cardBg = MaterialTheme.colorScheme.surfaceVariant
    val favTint by animateColorAsState(
        targetValue = if (isFavorite) AtharGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        label = "fav_color"
    )

    val cleanSubtext = subtext
        ?.replace("Tanzil Project (tanzil.net)", "")
        ?.replace("tanzil.net", "")
        ?.trim()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .testTag("athar_card_$title"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, com.example.ui.theme.AtharGlassBorderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(20.dp)
        ) {
            // Header Row: Category Badge + Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (categoryBadge != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x33F5F0E6),
                        border = BorderStroke(0.8.dp, com.example.ui.theme.AtharGlassBorder)
                    ) {
                        Text(
                            text = categoryBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AtharGold,
                        lineHeight = 24.sp
                    ),
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Scripture or Faith Content
            Text(
                text = content,
                style = if (isQuranOrHadith) {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = AmiriQuranFont,
                        fontSize = (21 * fontSizeMultiplier).sp,
                        lineHeight = (38 * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (16 * fontSizeMultiplier).sp,
                        lineHeight = (28 * fontSizeMultiplier).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 4.dp)
                    .testTag("athar_card_content")
            )

            // Subtext (Tafsir / Benefit / Reference)
            if (!cleanSubtext.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                    border = BorderStroke(0.8.dp, com.example.ui.theme.AtharGlassBorder.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = cleanSubtext,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (13 * fontSizeMultiplier).sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            if (extraContent != null) {
                Spacer(modifier = Modifier.height(12.dp))
                extraContent()
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                color = AtharGold.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons Row (Favorite, Copy, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.testTag("action_favorite")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isFavorite) "إزالة من المحفوظات" else "حفظ في المفضلة",
                        tint = favTint
                    )
                }

                IconButton(
                    onClick = onCopy,
                    modifier = Modifier.testTag("action_copy")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "نسخ النص",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        if (onShareImage != null) {
                            onShareImage()
                        } else {
                            coroutineScope.launch {
                                ShareImageHelper.shareCardAsImage(
                                    context = context,
                                    badgeText = categoryBadge ?: "",
                                    mainText = content,
                                    referenceText = title,
                                    benefitOrReflection = cleanSubtext ?: ""
                                )
                            }
                        }
                    },
                    modifier = Modifier.testTag("action_share_image")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "مشاركة كصورة مصممة",
                        tint = AtharGold
                    )
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("action_share")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
