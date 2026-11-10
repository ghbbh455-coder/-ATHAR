package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContentType
import com.example.ui.components.AtharCard
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun FavoritesScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val favoriteFilter by viewModel.favoriteTypeFilter.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val favoritesList by viewModel.favoriteDisplayItems.collectAsState()

    val filterOptions = listOf(
        null to "الكل",
        ContentType.QURAN to "القرآن",
        ContentType.HADITH to "الحديث",
        ContentType.DHIKR to "الأذكار",
        ContentType.DUA to "الأدعية"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen")
    ) {
        AtharTopBar(
            title = "المفضلة والمحفوظات محلياً",
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        // Filter chips: All, Quran, Hadith, Dhikr, Dua
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { (type, label) ->
                val isSelected = favoriteFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFavoriteTypeFilter(type) },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AtharGold,
                        selectedLabelColor = AtharDeepGreen,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) AtharGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        selectedBorderColor = AtharGold
                    )
                )
            }
        }

        if (favoritesList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = AtharGold.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.BookmarkBorder,
                                contentDescription = null,
                                tint = AtharGold,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "قائمة المفضلة فارغة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "جميع الآيات والأحاديث والأذكار التي تضع عليها علامة النجمة تُحفظ محلياً على جهازك لسرعة الرجوع إليها حتى في وضع الطيران دون إنترنت.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(favoritesList, key = { it.id }) { item ->
                    AtharCard(
                        categoryBadge = item.type.titleArabic,
                        title = item.title,
                        content = item.contentText,
                        subtext = "${item.categoryOrTopic} (${item.referenceText})",
                        isQuranOrHadith = true,
                        fontSizeMultiplier = fontSize.scaleFactor,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.toggleFavorite(item.id, item.type) },
                        onCopy = {
                            viewModel.copyToClipboard(
                                context,
                                "${item.contentText}\n[${item.referenceText}]"
                            )
                        },
                        onShare = {
                            viewModel.shareContent(
                                context,
                                item.title,
                                item.contentText,
                                item.referenceText
                            )
                        }
                    )
                }
            }
        }
    }
}
