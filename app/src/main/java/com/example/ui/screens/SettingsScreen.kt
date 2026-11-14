package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.VolunteerActivism
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AtharFontSize
import com.example.data.model.AtharThemeMode
import com.example.data.model.OverlayPosition
import com.example.overlay.DevicePermissionHelper
import com.example.overlay.UnlockOverlayManager
import com.example.ui.components.AtharTopBar
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassBorderBrush
import com.example.ui.theme.AtharGlassButtonBg
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGlassShadow
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.ui.theme.AtharSecondaryGreen
import com.example.ui.theme.CairoFontFamily
import com.example.ui.viewmodel.AtharDestination
import com.example.ui.viewmodel.AtharViewModel

@Composable
fun SettingsScreen(
    viewModel: AtharViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPermissions: () -> Unit = {},
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
    val fastingRemindersEnabled by viewModel.fastingRemindersEnabled.collectAsState()

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
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.testTag("overlay_settings_card")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
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
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AtharGold
                                        )
                                    )
                                    Text(
                                        text = "عرض آية أو حديث أو ذكر فور فتح قفل الهاتف",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = CairoFontFamily,
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
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                border = BorderStroke(1.dp, AtharGlassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "⚠️ يلزم منح إذن الظهور فوق التطبيقات",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            color = AtharGold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "ليتمكن أثر من إظهار التذكير الإيماني فور فتح شاشة الهاتف، يرجى تفعيل الإذن من إعدادات النظام.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = CairoFontFamily,
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
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, AtharGold)
                                    ) {
                                        Text(
                                            text = "منح إذن الظهور فوق التطبيقات الآن",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = CairoFontFamily,
                                                color = AtharGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = AtharGlassBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Position Selector (أعلى، منتصف، أسفل)
                        Text(
                            text = "موقع ظهور النافذة على الشاشة:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = CairoFontFamily,
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
                        HorizontalDivider(color = AtharGlassBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Content Inclusion Checkboxes
                        Text(
                            text = "أنواع المحتوى المراد ظهوره في النافذة:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = CairoFontFamily,
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
                                .height(50.dp)
                                .testTag("preview_overlay_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtharGold,
                                contentColor = AtharDeepGreen
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "معاينة النافذة العائمة الآن (تجربة فورية)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 1: Smart OEM Background & Autostart Settings (Infinix / Tecno / XOS / Android)
                        Button(
                            onClick = { DevicePermissionHelper.openOemBackgroundSettings(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_oem_background_settings"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AtharGold,
                                contentColor = AtharDeepGreen
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "حماية الخلفية والتشغيل التلقائي (Infinix / Tecno)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Clear instructions for Infinix & Tecno users
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x28D6BC82),
                            border = BorderStroke(0.8.dp, AtharGold.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 لمستخدمي إنفينيكس وتكنو (XOS / HiOS): فعّل \"التشغيل التلقائي Auto-start\" داخل مدير الهاتف، واقفل التطبيق بقفل 🔒 في قائمة التطبيقات المفتوحة لضمان عدم توقف التذكير.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = CairoFontFamily,
                                    color = AtharGold,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { onNavigateToPermissions() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AtharGold)
                        ) {
                            Text(
                                text = "شاشة فحص وتفعيل الأذونات خطوة بخطوة",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• تظل النافذة ظاهرة حتى تضغط على زر X لإغلاقها يدوياً.\n• لا تظهر النافذة مطلقاً فوق شاشة القفل أو كلمة المرور احتراماً لأمان الجهاز.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = CairoFontFamily,
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
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = AtharGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مظهر التطبيق",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
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
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.FormatSize,
                                contentDescription = null,
                                tint = AtharGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "حجم خط النصوص والآيات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
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
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "إظهار علامات الإعراب والضبط التام للنصوص",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = CairoFontFamily,
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

            // 5. Fasting Reminders Setting Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth().testTag("fasting_reminder_settings_card")
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
                            Surface(
                                shape = CircleShape,
                                color = Color(0x33D6BC82),
                                border = BorderStroke(1.dp, AtharGold),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.NightsStay,
                                        contentDescription = null,
                                        tint = AtharGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تذكير بصيام النوافل (الأيام البيض والاثنين والخميس)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = CairoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                )
                                Text(
                                    text = "تنبيه مساء ليلة الصيام لتبييت النية والسحور، ويشمل عرفة وعاشوراء",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = CairoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = fastingRemindersEnabled,
                            onCheckedChange = { viewModel.setFastingRemindersEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AtharGold,
                                checkedTrackColor = AtharSecondaryGreen
                            ),
                            modifier = Modifier.testTag("fasting_reminders_switch")
                        )
                    }
                }
            }

            // 6. Replay Welcome Screen
            item {
                OutlinedButton(
                    onClick = { viewModel.navigateTo(AtharDestination.Splash) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, AtharGlassBorder)
                ) {
                    Text(
                        text = "عرض شاشة الترحيب والتعريف بالتطبيق",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = CairoFontFamily,
                            color = AtharGold,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // 6. Luxury Liquid Glass: Developer Contact Section (تواصل مع المطور)
            item {
                val devGlassBrush = remember {
                    Brush.verticalGradient(
                        listOf(AtharGlassDarkGreen, AtharGlassSecondary)
                    )
                }
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(brush = devGlassBrush)
                        .testTag("developer_contact_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33F5F0E6))
                                    .border(1.dp, AtharGlassBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolunteerActivism,
                                    contentDescription = null,
                                    tint = AtharGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "تواصل مع المطور",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = AtharGold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "يسعدنا دائماً استقبال استفساراتكم واقتراحاتكم لتطوير التطبيق وخدمة كتاب الله وسنة رسوله ﷺ.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = CairoFontFamily,
                                color = AtharIvoryDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Interactive Row of 5 Social Glass Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SocialContactButton(
                                name = "واتساب",
                                iconRes = R.drawable.ic_social_whatsapp,
                                onClick = {
                                    openSocialUrl(context, "https://wa.me/201019524839")
                                }
                            )
                            SocialContactButton(
                                name = "تليجرام",
                                iconRes = R.drawable.ic_social_telegram,
                                onClick = {
                                    openSocialUrl(context, "https://t.me/body20089")
                                }
                            )
                            SocialContactButton(
                                name = "فيسبوك",
                                iconRes = R.drawable.ic_social_facebook,
                                onClick = {
                                    openSocialUrl(context, "https://www.facebook.com/share/1PDexKZxyG/")
                                }
                            )
                            SocialContactButton(
                                name = "تيك توك",
                                iconRes = R.drawable.ic_social_tiktok,
                                onClick = {
                                    openSocialUrl(context, "https://www.tiktok.com/@3abdal3")
                                }
                            )
                            SocialContactButton(
                                name = "سناب شات",
                                iconRes = R.drawable.ic_social_snapchat,
                                onClick = {
                                    openSocialUrl(context, "https://www.snapchat.com/add/body.oo")
                                }
                            )
                        }
                    }
                }
            }

            // 7. About Athar App Card
            item {
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(1.dp, Color(0x4DD6BC82)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                fontFamily = CairoFontFamily,
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
                                fontFamily = CairoFontFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = AtharGlassBorder.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "نصوص القرآن الكريم مضبوطة وفق تدقيق ورسم المصحف العثماني الشريف، والأحاديث النبوية مخرجة من أصح كتب السنة المعتمدة.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = CairoFontFamily,
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
private fun SocialContactButton(
    name: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 28.dp),
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(AtharGlassButtonBg)
                .border(1.2.dp, AtharGlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = name,
                tint = AtharGold,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            style = TextStyle(
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = AtharIvory
            ),
            textAlign = TextAlign.Center
        )
    }
}

private fun openSocialUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "تعذر فتح الرابط، يرجى المحاولة لاحقاً", Toast.LENGTH_SHORT).show()
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
                fontFamily = CairoFontFamily,
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
                    fontFamily = CairoFontFamily,
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
                    fontFamily = CairoFontFamily,
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
                    fontFamily = CairoFontFamily,
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
