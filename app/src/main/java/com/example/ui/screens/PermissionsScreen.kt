package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.overlay.DevicePermissionHelper
import com.example.overlay.UnlockOverlayService
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharSecondaryGreen

@Composable
fun PermissionsScreen(
    onContinueClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasOverlayPermission by remember {
        mutableStateOf(DevicePermissionHelper.canDrawOverlays(context))
    }
    var isIgnoringBattery by remember {
        mutableStateOf(DevicePermissionHelper.isIgnoringBatteryOptimizations(context))
    }
    var hasNotificationPermission by remember {
        mutableStateOf(DevicePermissionHelper.isNotificationPermissionGranted(context))
    }

    // Refresh state whenever user returns from Settings screens
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = DevicePermissionHelper.canDrawOverlays(context)
                isIgnoringBattery = DevicePermissionHelper.isIgnoringBatteryOptimizations(context)
                hasNotificationPermission = DevicePermissionHelper.isNotificationPermissionGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    val isXiaomiDevice = remember { DevicePermissionHelper.isXiaomi() }
    val brandName = remember { DevicePermissionHelper.getDeviceBrandDisplayName() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AtharDeepGreen)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Symbol & Athar Title
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(AtharSecondaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.ic_athar_gold_symbol),
                            contentDescription = "شعار أثر",
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "أَثَــر | إعداد أذونات التشغيل",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = AmiriQuranFont,
                            fontWeight = FontWeight.Bold,
                            color = AtharGold,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "«افتح هاتفك على ما ينفع قلبك»",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = AtharIvory.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "لضمان ظهور التذكير الإيماني فور فتح قفل الهاتف دون توقف أو إغلاق من النظام في الخلفية، يرجى تفعيل الصلاحيات التالية:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AtharIvory.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            fontSize = 12.5.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }

            // 1. Overlay Permission Card (Mandatory)
            item {
                PermissionStepCard(
                    stepNumber = "1",
                    title = "الظهور فوق التطبيقات الأخرى",
                    description = "يسمح بعرض النافذة الإيمانية الهادئة فور فتح قفل الهاتف مباشرة.",
                    icon = Icons.Filled.Layers,
                    isGranted = hasOverlayPermission,
                    buttonText = if (hasOverlayPermission) "تم التفعيل بنجاح ✅" else "تفعيل إذن الظهور فوق التطبيقات ➔",
                    onActionClick = {
                        DevicePermissionHelper.requestOverlayPermission(context)
                    }
                )
            }

            // 2. Battery Optimization Card (Crucial for background survival)
            item {
                PermissionStepCard(
                    stepNumber = "2",
                    title = "استثناء التطبيق من توفير البطارية",
                    description = "يمنع نظام الهاتف من قتل أو إيقاف خدمة التذكير عند قفل الشاشة أو في الخلفية.",
                    icon = Icons.Filled.BatteryAlert,
                    isGranted = isIgnoringBattery,
                    buttonText = if (isIgnoringBattery) "تم الاستثناء بنجاح ✅" else "استثناء من توفير البطارية ➔",
                    onActionClick = {
                        DevicePermissionHelper.requestIgnoreBatteryOptimizations(context)
                    }
                )
            }

            // 3. Notification Permission Card (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    PermissionStepCard(
                        stepNumber = "3",
                        title = "صلاحية الإشعارات الهادئة",
                        description = "ضرورية لتشغيل الخدمة الأمامية في الخلفية بأمان وثبات وفق نظام أندرويد الحديث.",
                        icon = Icons.Filled.Notifications,
                        isGranted = hasNotificationPermission,
                        buttonText = if (hasNotificationPermission) "الصلاحية ممنوحة ✅" else "منح صلاحية الإشعارات ➔",
                        onActionClick = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            // 4. Custom OEM Autostart Card (Xiaomi, Samsung, Oppo, Vivo, etc.)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AtharSecondaryGreen),
                    border = BorderStroke(1.dp, AtharGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(AtharDeepGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = null,
                                    tint = AtharGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "إعدادات أجهزة $brandName",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = AtharGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "التشغيل التلقائي والنوافذ المنبثقة",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = AtharIvory.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isXiaomiDevice) {
                                "في أجهزة شاومي (MIUI/HyperOS): يرجى تفعيل (التشغيل التلقائي - Autostart) ومنح إذن (عرض النوافذ المنبثقة أثناء العمل في الخلفية) لضمان ظهور النافذة العائمة."
                            } else {
                                "لضمان استمرار الخدمة على أجهزة $brandName: يرجى تفعيل التشغيل التلقائي وإلغاء قيود إيقاف التطبيق في الخلفية."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = AtharIvory.copy(alpha = 0.85f),
                                lineHeight = 18.sp,
                                fontSize = 12.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { DevicePermissionHelper.openAutostartSettings(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, AtharGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AtharGold)
                            ) {
                                Text(
                                    text = "التشغيل التلقائي",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isXiaomiDevice) {
                                OutlinedButton(
                                    onClick = { DevicePermissionHelper.openXiaomiPopUpPermission(context) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AtharGold),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AtharGold)
                                ) {
                                    Text(
                                        text = "نوافذ شاومي المنبثقة",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Continue Action Button
            item {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        // Start service if overlay permission is granted
                        if (hasOverlayPermission) {
                            UnlockOverlayService.startService(context)
                        }
                        onContinueClick()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AtharGold,
                        contentColor = AtharDeepGreen
                    )
                ) {
                    Text(
                        text = if (hasOverlayPermission && isIgnoringBattery) {
                            "اكتملت الأذونات • متابعة إلى التطبيق ➔"
                        } else {
                            "المتابعة إلى التطبيق ➔"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PermissionStepCard(
    stepNumber: String,
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    buttonText: String,
    onActionClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AtharSecondaryGreen),
        border = BorderStroke(
            1.dp,
            if (isGranted) AtharGold.copy(alpha = 0.7f) else AtharGold.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(AtharDeepGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) AtharGold else AtharIvory,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = AtharGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AtharIvory.copy(alpha = 0.75f),
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) AtharGold.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f),
                    border = BorderStroke(
                        0.5.dp,
                        if (isGranted) AtharGold.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isGranted) AtharGold else Color(0xFFFF8A80),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGranted) "مفعل" else "مطلوب",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isGranted) AtharGold else Color(0xFFFF8A80),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onActionClick,
                enabled = !isGranted,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) AtharDeepGreen.copy(alpha = 0.6f) else AtharGold,
                    contentColor = if (isGranted) AtharIvory.copy(alpha = 0.6f) else AtharDeepGreen,
                    disabledContainerColor = AtharDeepGreen.copy(alpha = 0.6f),
                    disabledContentColor = AtharGold
                )
            ) {
                Text(
                    text = buttonText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
