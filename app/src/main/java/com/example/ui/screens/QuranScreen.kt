package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuranSurahCatalog
import com.example.data.local.SurahMetadata
import com.example.data.model.ContentType
import com.example.ui.components.AtharCard
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun QuranScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.quranSearch.collectAsState()
    val selectedTopic by viewModel.selectedQuranTopic.collectAsState()
    val favoriteIds by viewModel.favoriteIdsSet.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val approvedVerses by viewModel.approvedVerses.collectAsState()
    val selectedSurah by viewModel.selectedSurah.collectAsState()

    var activeViewTab by remember { mutableIntStateOf(0) } // 0: Verses, 1: 114 Surahs Index

    val topics = remember(approvedVerses) {
        listOf("الكل") + approvedVerses.map { it.topic }.distinct()
    }

    val filteredVerses = remember(approvedVerses, searchQuery, selectedTopic, selectedSurah) {
        approvedVerses.filter { verse ->
            val matchesSurah = selectedSurah == null || verse.surahNumber == selectedSurah?.number
            val matchesTopic = selectedTopic == "الكل" || verse.topic == selectedTopic
            val matchesSearch = if (searchQuery.isBlank()) true else {
                verse.surahName.contains(searchQuery, ignoreCase = true) ||
                        verse.textUthmani.contains(searchQuery, ignoreCase = true) ||
                        verse.topic.contains(searchQuery, ignoreCase = true)
            }
            matchesSurah && matchesTopic && matchesSearch
        }
    }

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            QuranSurahCatalog.surahs
        } else {
            QuranSurahCatalog.surahs.filter {
                it.arabicName.contains(searchQuery, ignoreCase = true) ||
                        "${it.number}".contains(searchQuery) ||
                        it.revelationType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("quran_screen")
    ) {
        AtharTopBar(
            title = if (selectedSurah != null) "سورة ${selectedSurah?.arabicName}" else "القرآن الكريم (رسم عثماني)",
            canNavigateBack = true,
            onNavigateBack = {
                if (selectedSurah != null) {
                    viewModel.selectSurah(null)
                } else {
                    onNavigateBack()
                }
            }
        )

        // View Mode Switcher: الآيات المباركة vs فهرس السور الـ 114
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (activeViewTab == 0) AtharGold else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (activeViewTab == 0) AtharGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable {
                        activeViewTab = 0
                        viewModel.selectSurah(null)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (activeViewTab == 0) AtharDeepGreen else AtharGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الآيات المعتمدة",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (activeViewTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeViewTab == 0) AtharDeepGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (activeViewTab == 1) AtharGold else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (activeViewTab == 1) AtharGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable { activeViewTab = 1 }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FormatListNumbered,
                        contentDescription = null,
                        tint = if (activeViewTab == 1) AtharDeepGreen else AtharGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "فهرس السور الـ ١١٤",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (activeViewTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (activeViewTab == 1) AtharDeepGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setQuranSearch(it) },
            placeholder = {
                Text(
                    text = if (activeViewTab == 0) "ابحث بالآية، السورة، أو الموضوع..." else "ابحث في أسماء وأرقام سور القرآن الـ 114...",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "بحث",
                    tint = AtharGold
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setQuranSearch("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "مسح",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AtharGold,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("quran_search_field")
        )

        if (activeViewTab == 0) {
            // Selected Surah Banner if filtering by specific Surah
            if (selectedSurah != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "عرض آيات: سورة ${selectedSurah?.arabicName} (${selectedSurah?.revelationType} - ${selectedSurah?.versesCount} آيات)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                        IconButton(
                            onClick = { viewModel.selectSurah(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "إلغاء التحديد",
                                tint = AtharGold
                            )
                        }
                    }
                }
            }

            // Topic Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topics) { topic ->
                    val isSelected = selectedTopic == topic
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectQuranTopic(topic) },
                        label = {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
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

            // Verses List from Room Database
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredVerses, key = { it.id }) { verse ->
                    val isFav = favoriteIds.contains(verse.id)
                    AtharCard(
                        categoryBadge = "${verse.surahName} (الآية ${verse.verseNumber})",
                        title = verse.topic,
                        content = verse.textUthmani,
                        subtext = "وقفة تدبرية: ${verse.tafsirSnippet}",
                        isQuranOrHadith = true,
                        fontSizeMultiplier = fontSize.scaleFactor,
                        isFavorite = isFav,
                        onToggleFavorite = { viewModel.toggleFavorite(verse.id, ContentType.QURAN) },
                        onCopy = {
                            viewModel.copyToClipboard(
                                context,
                                "${verse.textUthmani}\n[${verse.surahName}: ${verse.verseNumber}]"
                            )
                        },
                        onShare = {
                            viewModel.shareContent(
                                context,
                                "${verse.surahName} (الآية ${verse.verseNumber})",
                                verse.textUthmani,
                                "${verse.surahName}: ${verse.verseNumber}"
                            )
                        }
                    )
                }

                if (filteredVerses.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لا توجد آيات مطابقة للبحث في قاعدة البيانات المحلية",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "نصوص القرآن الكريم مضبوطة وفق تدقيق ورسم المصحف العثماني",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AtharGold.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            // 114 Surahs Index View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredSurahs, key = { it.number }) { surah ->
                    SurahIndexItemCard(
                        surah = surah,
                        onClick = {
                            viewModel.selectSurah(surah)
                            activeViewTab = 0
                        }
                    )
                }

                item {
                    Text(
                        text = "نصوص القرآن الكريم مضبوطة وفق رسم المصحف العثماني الشريف",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AtharGold.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahIndexItemCard(
    surah: SurahMetadata,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("surah_item_${surah.number}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Surah Number Badge
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, AtharGold),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${surah.number}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "سورة ${surah.arabicName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = AmiriQuranFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${surah.revelationType} • ${surah.versesCount} آيات",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AtharGold.copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, AtharGold.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "عرض الآيات",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AtharGold,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
