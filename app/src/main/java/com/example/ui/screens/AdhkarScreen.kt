package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ContentType
import com.example.ui.components.AtharCard
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun AdhkarScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedCategory by viewModel.selectedDhikrCategory.collectAsState()
    val counters by viewModel.dhikrCounters.collectAsState()
    val favoriteIds by viewModel.favoriteIdsSet.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val approvedDhikr by viewModel.approvedDhikr.collectAsState()
    var dhikrSearchQuery by remember { mutableStateOf("") }

    val default18Categories = listOf(
        "أذكار الصباح",
        "أذكار المساء",
        "أذكار النوم",
        "أذكار الاستيقاظ",
        "أذكار بعد الصلاة",
        "أذكار دخول وخروج المنزل",
        "أذكار المسجد",
        "أذكار الطعام والشراب",
        "أذكار السفر",
        "أذكار الكرب والهم",
        "أذكار الاستغفار والتوبة",
        "أذكار التسبيح والحمد",
        "أدعية من القرآن الكريم",
        "أدعية الأنبياء عليهم السلام",
        "أدعية من السنة النبوية",
        "أذكار الوضوء",
        "أدعية الرزق والبركة",
        "أذكار وأدعية متنوعة"
    )

    val categories = remember(approvedDhikr) {
        val list = approvedDhikr.map { it.category }.distinct()
        if (list.isEmpty()) default18Categories else list
    }

    val filteredAdhkar = remember(approvedDhikr, selectedCategory, dhikrSearchQuery) {
        approvedDhikr.filter { item ->
            val matchesCategory = if (dhikrSearchQuery.isNotBlank()) true else item.category == selectedCategory
            val matchesQuery = if (dhikrSearchQuery.isBlank()) true else {
                item.arabicText.contains(dhikrSearchQuery, ignoreCase = true) ||
                        item.category.contains(dhikrSearchQuery, ignoreCase = true) ||
                        item.virtue.contains(dhikrSearchQuery, ignoreCase = true) ||
                        item.sourceBook.contains(dhikrSearchQuery, ignoreCase = true)
            }
            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("adhkar_screen")
    ) {
        AtharTopBar(
            title = "الأذكار والأدعية النبوية (١٨ قسماً)",
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        // Search in Adhkar
        OutlinedTextField(
            value = dhikrSearchQuery,
            onValueChange = { dhikrSearchQuery = it },
            placeholder = {
                Text(
                    text = "ابحث في الأذكار والأدعية أو الفضائل...",
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
                if (dhikrSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { dhikrSearchQuery = "" }) {
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
                .testTag("adhkar_search_field")
        )

        // Categories Scrollable Row (18 categories)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category && dhikrSearchQuery.isBlank()
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        dhikrSearchQuery = ""
                        viewModel.selectDhikrCategory(category)
                    },
                    label = {
                        Text(
                            text = category,
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

        // Adhkar List from Room Database
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredAdhkar, key = { it.id }) { dhikr ->
                val currentCount = counters[dhikr.id] ?: 0
                val isCompleted = currentCount >= dhikr.repetitionCount
                val isFav = favoriteIds.contains(dhikr.id)
                val isDua = dhikr.category.contains("دعاء") || dhikr.category.contains("أدعية")

                AtharCard(
                    categoryBadge = if (dhikr.repetitionCount > 1) "${dhikr.category} • التكرار: ${dhikr.repetitionCount} مرات" else "${dhikr.category} • مرة واحدة",
                    title = dhikr.category,
                    content = dhikr.arabicText,
                    subtext = "الفضل: ${dhikr.virtue}\nالمصدر: ${dhikr.sourceBook} (${dhikr.reference})",
                    isQuranOrHadith = true,
                    fontSizeMultiplier = fontSize.scaleFactor,
                    isFavorite = isFav,
                    onToggleFavorite = { viewModel.toggleFavorite(dhikr.id, if (isDua) ContentType.DUA else ContentType.DHIKR) },
                    onCopy = {
                        viewModel.copyToClipboard(
                            context,
                            "${dhikr.arabicText}\n[${dhikr.sourceBook}: ${dhikr.reference}]"
                        )
                    },
                    onShare = {
                        viewModel.shareContent(
                            context,
                            dhikr.category,
                            dhikr.arabicText,
                            "${dhikr.sourceBook} (${dhikr.reference})"
                        )
                    },
                    extraContent = {
                        // Interactive Tasbih Counter Control
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isCompleted) "تم إتمام الذكر بحمد الله" else "الإنجاز: $currentCount / ${dhikr.repetitionCount}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isCompleted) AtharGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                if (isCompleted) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = AtharGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentCount > 0) {
                                    IconButton(
                                        onClick = { viewModel.resetDhikr(dhikr.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "إعادة البدء",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            viewModel.incrementDhikr(dhikr.id, dhikr.repetitionCount)
                                        }
                                        .testTag("dhikr_counter_button_${dhikr.id}"),
                                    color = if (isCompleted) AtharGold else MaterialTheme.colorScheme.primaryContainer,
                                    border = BorderStroke(1.5.dp, AtharGold),
                                    shape = CircleShape
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$currentCount",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCompleted) AtharDeepGreen else AtharIvory
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            if (filteredAdhkar.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أذكار أو أدعية مطابقة للبحث في هذا الباب حالياً",
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
