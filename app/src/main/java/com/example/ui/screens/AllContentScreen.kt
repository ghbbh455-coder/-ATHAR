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
fun AllContentScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val searchQuery by viewModel.allContentSearch.collectAsState()
    val activeFilter by viewModel.allContentFilter.collectAsState()
    val unifiedItems by viewModel.unifiedContentItems.collectAsState()
    val favoriteIds by viewModel.favoriteIdsSet.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()

    val filterOptions = listOf(
        null to "الكل",
        ContentType.QURAN to "القرآن الكريم",
        ContentType.HADITH to "الحديث النبوي",
        ContentType.DHIKR to "الأذكار المأثورة",
        ContentType.DUA to "الأدعية المباركة"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("all_content_screen")
    ) {
        AtharTopBar(
            title = "كل المحتوى الإيماني",
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        // Comprehensive Universal Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setAllContentSearch(it) },
            placeholder = {
                Text(
                    text = "ابحث في كل الآيات والأحاديث والأذكار والأدعية...",
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
                    IconButton(onClick = { viewModel.setAllContentSearch("") }) {
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
                .testTag("all_content_search_field")
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { (type, label) ->
                val isSelected = activeFilter == type
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAllContentFilter(type) },
                    label = {
                        Text(
                            text = label,
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

        Spacer(modifier = Modifier.height(4.dp))

        // Unified Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(unifiedItems, key = { "${it.type.name}_${it.id}" }) { item ->
                val isFav = favoriteIds.contains(item.id)
                AtharCard(
                    categoryBadge = item.badge,
                    title = item.title,
                    content = item.text,
                    subtext = if (item.benefit.isNotBlank()) "${item.benefit}\nالمصدر: ${item.reference}" else "المصدر: ${item.reference}",
                    isQuranOrHadith = true,
                    fontSizeMultiplier = fontSize.scaleFactor,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleFavorite(item.id, item.type) },
                    onCopy = {
                        viewModel.copyToClipboard(
                            context,
                            "${item.text}\n[${item.reference}]"
                        )
                    },
                    onShare = {
                        viewModel.shareContent(
                            context,
                            item.badge,
                            item.text,
                            item.reference
                        )
                    }
                )
            }

            if (unifiedItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد نتائج مطابقة لبحثك في قاعدة البيانات المحلية",
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
