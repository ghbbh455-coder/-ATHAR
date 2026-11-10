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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ContentType
import com.example.ui.components.AtharCard
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun HadithScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.hadithSearch.collectAsState()
    val selectedTopic by viewModel.selectedHadithTopic.collectAsState()
    val favoriteIds by viewModel.favoriteIdsSet.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val approvedHadiths by viewModel.approvedHadiths.collectAsState()

    val topics = remember(approvedHadiths) {
        listOf("الكل") + approvedHadiths.map { it.topic }.distinct()
    }

    val filteredHadiths = remember(approvedHadiths, searchQuery, selectedTopic) {
        approvedHadiths.filter { hadith ->
            val matchesTopic = selectedTopic == "الكل" || hadith.topic == selectedTopic
            val matchesSearch = if (searchQuery.isBlank()) true else {
                hadith.arabicText.contains(searchQuery, ignoreCase = true) ||
                        hadith.collectionName.contains(searchQuery, ignoreCase = true) ||
                        hadith.chapterName.contains(searchQuery, ignoreCase = true) ||
                        hadith.hadithNumber.contains(searchQuery, ignoreCase = true) ||
                        hadith.topic.contains(searchQuery, ignoreCase = true)
            }
            matchesTopic && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("hadith_screen")
    ) {
        AtharTopBar(
            title = "الأحاديث النبوية الصحيحة",
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setHadithSearch(it) },
            placeholder = {
                Text(
                    text = "ابحث بالمتن، الكتاب، أو رقم الحديث...",
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
                    IconButton(onClick = { viewModel.setHadithSearch("") }) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("hadith_search_field")
        )

        // Topic Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topics) { topic ->
                val isSelected = selectedTopic == topic
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectHadithTopic(topic) },
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

        Spacer(modifier = Modifier.height(6.dp))

        // Hadiths List from Room Database
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredHadiths, key = { it.id }) { hadith ->
                val isFav = favoriteIds.contains(hadith.id)
                AtharCard(
                    categoryBadge = "${hadith.collectionName} - رقم ${hadith.hadithNumber}",
                    title = hadith.chapterName,
                    content = hadith.arabicText,
                    subtext = "الحكم المعتمد: ${hadith.grading} (${hadith.gradingSource})\nالفائدة الإيمانية: ${hadith.benefit}",
                    isQuranOrHadith = true,
                    fontSizeMultiplier = fontSize.scaleFactor,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleFavorite(hadith.id, ContentType.HADITH) },
                    onCopy = {
                        viewModel.copyToClipboard(
                            context,
                            "${hadith.arabicText}\n[${hadith.collectionName}: رقم ${hadith.hadithNumber}]"
                        )
                    },
                    onShare = {
                        viewModel.shareContent(
                            context,
                            "${hadith.collectionName} - رقم ${hadith.hadithNumber}",
                            hadith.arabicText,
                            "${hadith.collectionName}: ${hadith.hadithNumber} (${hadith.grading} - ${hadith.gradingSource})"
                        )
                    }
                )
            }

            if (filteredHadiths.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (approvedHadiths.isEmpty()) "جاري تحميل الأحاديث الصحيحة المعتمدة محلياً..." else "لا توجد أحاديث مطابقة للبحث في قاعدة البيانات المحلية",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
