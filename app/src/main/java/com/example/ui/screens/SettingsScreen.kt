package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AtharFontSize
import com.example.data.model.AtharThemeMode
import com.example.data.model.OverlayPosition
import com.example.data.sources.TanzilImporter
import com.example.overlay.UnlockOverlayManager
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharSecondaryGreen
import com.example.ui.viewmodel.AtharDestination
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun SettingsScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeMode by viewModel.themeMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val showTashkeel by viewModel.showTashkeel.collectAsState()
    val overlayEnabled by viewModel.overlayEnabled.collectAsState()
    val overlayPosition by viewModel.overlayPosition.collectAsState()
    val showQuranInOverlay by viewModel.showQuranInOverlay.collectAsState()
    val showHadithInOverlay by viewModel.showHadithInOverlay.collectAsState()
    val showDhikrInOverlay by viewModel.showDhikrInOverlay.collectAsState()
    val showDuaInOverlay by viewModel.showDuaInOverlay.collectAsState()

    val hasOverlayPermission = UnlockOverlayManager.canDrawOverlays(context)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        AtharTopBar(
            title = "الإعدادات والتفضيلات",
            canNavigateBack = true,
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Core Feature: Floating Faith Popup Settings Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.2.dp, AtharGold.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("overlay_settings_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = AtharGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "النافذة الإيمانية عند فتح القفل",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AtharGold
                                        )
                                    )
                                    Text(
                                        text = "عرض آية أو حديث أو ذكر فور فتح قفل الهاتف",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Switch(
                                checked = overlayEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked && !hasOverlayPermission) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        viewModel.setOverlayEnabled(context, isChecked)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AtharGold,
                                    checkedTrackColor = AtharSecondaryGreen
                                ),
                                modifier = Modifier.testTag("overlay_toggle_switch")
                            )
                        }

                        // Overlay Permission Notice if missing
                        if (!hasOverlayPermission) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "⚠️ يلزم منح إذن الظهور فوق التطبيقات",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AtharGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ليتمكن أثر من إظهار التذكير الإيماني فور فتح شاشة الهاتف، يرجى تفعيل الإذن من إعدادات النظام.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            lineHeight = 18.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                Uri.parse("package:${context.packageName}")
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, AtharGold)
                                    ) {
                                        Text(
                                            text = "منح إذن الظهور فوق التطبيقات الآن",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = AtharGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = AtharGold.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Position Selector (أعلى، منتصف، أسفل)
                        Text(
                            text = "موقع ظهور النافذة على الشاشة:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PositionChip(
                                selected = overlayPosition == OverlayPosition.TOP,
                                label = "أعلى الشاشة",
                                onClick = { viewModel.setOverlayPosition(OverlayPosition.TOP) },
                                modifier = Modifier.weight(1f)
                            )
                            PositionChip(
                                selected = overlayPosition == OverlayPosition.CENTER,
                                label = "منتصف الشاشة",
                                onClick = { viewModel.setOverlayPosition(OverlayPosition.CENTER) },
                                modifier = Modifier.weight(1f)
                            )
                            PositionChip(
                                selected = overlayPosition == OverlayPosition.BOTTOM,
                                label = "أسفل الشاشة",
                                onClick = { viewModel.setOverlayPosition(OverlayPosition.BOTTOM) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = AtharGold.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Content Inclusion Checkboxes
                        Text(
                            text = "أنواع المحتوى المراد ظهوره في النافذة:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        ContentCheckboxRow(
                            title = "القرآن الكريم (رسم عثماني موثق)",
                            checked = showQuranInOverlay,
                            onCheckedChange = { viewModel.setShowQuranInOverlay(it) }
                        )
                        ContentCheckboxRow(
                            title = "الحديث النبوي الصحيح (من كتب السنة الستة)",
                            checked = showHadithInOverlay,
                            onCheckedChange = { viewModel.setShowHadithInOverlay(it) }
                        )
                        ContentCheckboxRow(
                            title = "الأذكار المأثورة (من حصن المسلم)",
                            checked = showDhikrInOverlay,
                            onCheckedChange = { viewModel.setShowDhikrInOverlay(it) }
                        )
                        ContentCheckboxRow(
                            title = "الأدعية القرآنية والنبوية المباركة",
                            checked = showDuaInOverlay,
                            onCheckedChange = { viewModel.setShowDuaInOverlay(it) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Preview Test Button
                        Button(
                            onClick = {
                                if (hasOverlayPermission) {
                                    viewModel.triggerPreviewOverlay(context)
                                } else {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("preview_overlay_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtharGold,
                                contentColor = AtharDeepGreen
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "معاينة النافذة العائمة الآن (تجربة فورية)",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• تظل النافذة ظاهرة حتى تضغط على زر X لإغلاقها يدوياً.\n• لا تظهر النافذة مطلقاً فوق شاشة القفل أو كلمة المرور احتراماً لأمان الجهاز.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.5.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }

            // 2. Theme Mode Preference
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = AtharGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مظهر التطبيق",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeChip(
                                selected = themeMode == AtharThemeMode.SYSTEM,
                                label = "تلقائي",
                                onClick = { viewModel.setThemeMode(AtharThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeChip(
                                selected = themeMode == AtharThemeMode.LIGHT,
                                label = "نهاري",
                                onClick = { viewModel.setThemeMode(AtharThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeChip(
                                selected = themeMode == AtharThemeMode.DARK,
                                label = "ليلي",
                                onClick = { viewModel.setThemeMode(AtharThemeMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Font Size Preference
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FormatSize,
                                contentDescription = null,
                                tint = AtharGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "حجم خط النصوص والآيات",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FontSizeChip(
                                selected = fontSize == AtharFontSize.SMALL,
                                label = "صغير",
                                onClick = { viewModel.setFontSize(AtharFontSize.SMALL) },
                                modifier = Modifier.weight(1f)
                            )
                            FontSizeChip(
                                selected = fontSize == AtharFontSize.MEDIUM,
                                label = "متوسط",
                                onClick = { viewModel.setFontSize(AtharFontSize.MEDIUM) },
                                modifier = Modifier.weight(1f)
                            )
                            FontSizeChip(
                                selected = fontSize == AtharFontSize.LARGE,
                                label = "كبير",
                                onClick = { viewModel.setFontSize(AtharFontSize.LARGE) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Tashkeel Toggle
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spellcheck,
                                contentDescription = null,
                                tint = AtharGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "عرض التشكيل والحركات",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "إظهار علامات الإعراب والضبط التام للنصوص",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = showTashkeel,
                            onCheckedChange = { viewModel.setShowTashkeel(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AtharGold,
                                checkedTrackColor = AtharSecondaryGreen
                            ),
                            modifier = Modifier.testTag("tashkeel_switch")
                        )
                    }
                }
            }

            // 5. Replay Welcome Screen
            item {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AtharDestination.Splash) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "عرض شاشة الترحيب والتعريف بالتطبيق",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = AtharGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // 6. About Athar App Card & Tanzil Attribution
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, AtharGold),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_athar_icon_1794331926985),
                                contentDescription = "أثر",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "أَثَــر | ATHAR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = AtharGold
                            )
                        )

                        Text(
                            text = "«افتح هاتفك على ما ينفع قلبك»",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = AmiriQuranFont,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "تطبيق إسلامي يعرض آيات قرآنية بالرسم العثماني وأحاديث نبوية صحيحة وأذكارًا وأدعية موثقة دون حاجة إلى اتصال بالإنترنت.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = AtharGold.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = TanzilImporter.TANZIL_COPYRIGHT_NOTICE,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharGold,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AtharGold,
                checkmarkColor = AtharDeepGreen
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun PositionChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.5.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AtharGold,
            selectedLabelColor = AtharDeepGreen,
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) AtharGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            selectedBorderColor = AtharGold
        ),
        modifier = modifier
    )
}

@Composable
private fun ThemeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AtharGold,
            selectedLabelColor = AtharDeepGreen
        ),
        modifier = modifier
    )
}

@Composable
private fun FontSizeChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AtharGold,
            selectedLabelColor = AtharDeepGreen
        ),
        modifier = modifier
    )
}
