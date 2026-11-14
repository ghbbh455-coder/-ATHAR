package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AtharPreferences
import com.example.data.local.TasbeehDataStore
import com.example.ui.screens.toArabicDigits
import com.example.ui.theme.AmiriQuranFont
import com.example.ui.theme.AtharDeepGreen
import com.example.ui.theme.AtharGlassBorder
import com.example.ui.theme.AtharGlassDarkGreen
import com.example.ui.theme.AtharGlassSecondary
import com.example.ui.theme.AtharGold
import com.example.ui.theme.AtharIvory
import com.example.ui.theme.AtharIvoryDark
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.LiquidGlassDefaults
import com.example.ui.theme.liquidGlass
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Immutable
data class TasbeehDhikr(
    val key: String,
    val arabicText: String,
    val target: Int
)

val defaultTasbeehList = listOf(
    TasbeehDhikr("subhanallah", "سُبْحَانَ اللَّهِ", 33),
    TasbeehDhikr("alhamdulillah", "الْحَمْدُ لِلَّهِ", 33),
    TasbeehDhikr("la_ilaha_illallah", "لَا إِلَهَ إِلَّا اللَّهُ", 33),
    TasbeehDhikr("allahu_akbar", "اللَّهُ أَكْبَرُ", 33),
    TasbeehDhikr("astaghfirullah", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", 100),
    TasbeehDhikr("hawqalah", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", 100),
    TasbeehDhikr("salawat", "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", 100),
    TasbeehDhikr("tasbeeh_aziem", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ", 100)
)

/**
 * Floating Draggable Tasbeeh Bubble that stays at the screen border and expands
 * into an interactive Liquid Glass mini Tasbeeh dialog.
 */
@Composable
fun FloatingTasbeehBubble(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { AtharPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    var isDialogVisible by remember { mutableStateOf(false) }

    // Draggable position coordinates
    var offsetX by remember { mutableFloatStateOf(16f) }
    var offsetY by remember { mutableFloatStateOf(420f) }

    // Selected Dhikr index (backed by DataStore)
    var selectedIndex by remember { mutableIntStateOf(prefs.getSelectedTasbeehIndex()) }
    val currentDhikr = defaultTasbeehList[selectedIndex.coerceIn(0, defaultTasbeehList.lastIndex)]

    // Counter for current dhikr (backed by DataStore)
    var count by remember { mutableIntStateOf(prefs.getFloatingTasbeehCount(currentDhikr.key)) }

    LaunchedEffect(Unit) {
        val dsIndex = TasbeehDataStore.selectedIndexFlow(context).first()
        selectedIndex = dsIndex.coerceIn(0, defaultTasbeehList.lastIndex)
        val key = defaultTasbeehList[selectedIndex].key
        val dsCount = TasbeehDataStore.countFlow(context, key).first()
        if (dsCount > 0 || count == 0) {
            count = dsCount
        }
    }

    // Tap pulse animation
    var isPressed by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(),
        label = "pulse_scale"
    )

    fun performHaptic(isGoalReached: Boolean = false) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (isGoalReached) {
                    // Double pulse upon reaching 33, 99 or 100
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 70, 70, 110), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(longArrayOf(0, 70, 70, 110), -1)
                    }
                } else {
                    // Light tap pulse
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(28L, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(28L)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Increment count with haptic & auto-save to DataStore
    fun incrementCount() {
        isPressed = true
        val newCount = count + 1
        count = newCount
        prefs.setFloatingTasbeehCount(currentDhikr.key, newCount)
        prefs.incrementDailyTasbeehCount(1)
        coroutineScope.launch {
            TasbeehDataStore.saveCount(context, currentDhikr.key, newCount)
        }

        val isGoal = (newCount % currentDhikr.target == 0) || (newCount == 33) || (newCount == 100)
        performHaptic(isGoal)
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100L)
            isPressed = false
        }
    }

    // 1. Floating Bubble on screen
    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }
    ) {
        Surface(
            shape = CircleShape,
            color = AtharGlassDarkGreen,
            border = BorderStroke(1.2.dp, AtharGold),
            shadowElevation = 0.dp,
            modifier = Modifier
                .size(54.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 27.dp),
                    onClick = { isDialogVisible = true }
                )
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = "المسبحة الإلكترونية العائمة",
                    tint = AtharGold,
                    modifier = Modifier.size(24.dp)
                )

                // Small badge with count
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AtharGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (count > 99) "+99" else "$count",
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = AtharDeepGreen
                            )
                        )
                    }
                }
            }
        }
    }

    // 2. Interactive Liquid Glass Tasbeeh Dialog
    if (isDialogVisible) {
        Dialog(onDismissRequest = { isDialogVisible = false }) {
            Card(
                shape = LiquidGlassDefaults.LargeCardShape,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .liquidGlass(shape = LiquidGlassDefaults.LargeCardShape)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0x33D6BC82),
                                border = BorderStroke(1.dp, AtharGold),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.TouchApp,
                                        contentDescription = null,
                                        tint = AtharGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "المسبحة الإلكترونية الذكية 📿",
                                style = TextStyle(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AtharGold
                                )
                            )
                        }

                        IconButton(
                            onClick = { isDialogVisible = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "إغلاق",
                                tint = AtharIvory
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Horizontal Dhikr Tabs Selector
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(
                            items = defaultTasbeehList,
                            key = { _, item -> item.key },
                            contentType = { _, _ -> "tasbeeh_chip" }
                        ) { index, item ->
                            val isSelected = index == selectedIndex
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AtharGold else AtharGlassSecondary,
                                border = BorderStroke(1.dp, if (isSelected) AtharGold else AtharGlassBorder),
                                modifier = Modifier.clickable {
                                    selectedIndex = index
                                    prefs.setSelectedTasbeehIndex(index)
                                    coroutineScope.launch {
                                        TasbeehDataStore.saveSelectedIndex(context, index)
                                        count = TasbeehDataStore.countFlow(context, item.key).first()
                                    }
                                }
                            ) {
                                Text(
                                    text = item.arabicText,
                                    style = TextStyle(
                                        fontFamily = AmiriQuranFont,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) AtharDeepGreen else AtharIvory
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Current Dhikr Display
                    Text(
                        text = currentDhikr.arabicText,
                        style = TextStyle(
                            fontFamily = AmiriQuranFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = AtharIvory,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Large Circular Interactive Tap Button
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .scale(scaleAnim)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(
                                        Color(0xFF244638),
                                        AtharDeepGreen
                                    )
                                )
                            )
                            .border(2.5.dp, AtharGold, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 90.dp),
                                onClick = { incrementCount() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Circular progress toward target (e.g. 33 or 100)
                        val progress = (count % currentDhikr.target).toFloat() / currentDhikr.target.toFloat()
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = AtharGold,
                            trackColor = Color(0x33D6BC82),
                            strokeWidth = 4.dp
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = toArabicDigits(count),
                                style = TextStyle(
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp,
                                    color = AtharGold
                                )
                            )
                            Text(
                                text = "الهدف: ${toArabicDigits(currentDhikr.target)}",
                                style = TextStyle(
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = AtharIvoryDark
                                )
                            )
                            Text(
                                text = "اضغط للتسبيح",
                                style = TextStyle(
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.sp,
                                    color = AtharIvory.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Control Row: Reset & Total
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المجموع: ${toArabicDigits(count)} تسبيحة",
                            style = TextStyle(
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                color = AtharIvoryDark
                            )
                        )

                        // Reset Counter Button (Long click to zero out)
                        @OptIn(ExperimentalFoundationApi::class)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33F5F0E6),
                            border = BorderStroke(0.8.dp, AtharGlassBorder),
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        count = 0
                                        prefs.setFloatingTasbeehCount(currentDhikr.key, 0)
                                        coroutineScope.launch {
                                            TasbeehDataStore.saveCount(context, currentDhikr.key, 0)
                                        }
                                        performHaptic(false)
                                    },
                                    onLongClick = {
                                        count = 0
                                        prefs.setFloatingTasbeehCount(currentDhikr.key, 0)
                                        coroutineScope.launch {
                                            TasbeehDataStore.saveCount(context, currentDhikr.key, 0)
                                        }
                                        performHaptic(true)
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "تصفير",
                                    tint = AtharGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تصفير العداد",
                                    style = TextStyle(
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AtharGold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
