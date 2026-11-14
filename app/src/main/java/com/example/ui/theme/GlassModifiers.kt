package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance Liquid Glass architecture replacing costly live Modifier.blur()
 * and nested shadow elevations with pre-cached vertical gradients and crisp golden borders
 * for smooth 120fps scrolling across all screens.
 */
object LiquidGlassDefaults {
    val DeepGreenGlass = Color(0xE0102D25)       // #102D25 at 0.88f alpha
    val SecondaryGreenGlass = Color(0xE0244638)  // #244638 at 0.88f alpha
    val ThinGoldBorderColor = Color(0x4DD6BC82)  // #D6BC82 at 0.30f alpha
    val ThinGoldBorder = BorderStroke(1.dp, ThinGoldBorderColor)
    val CardShape = RoundedCornerShape(24.dp)
    val LargeCardShape = RoundedCornerShape(28.dp)

    val StaticGradientBrush: Brush = Brush.verticalGradient(
        colors = listOf(DeepGreenGlass, SecondaryGreenGlass)
    )
}

@Composable
fun rememberLiquidGlassBrush(
    topColor: Color = LiquidGlassDefaults.DeepGreenGlass,
    bottomColor: Color = LiquidGlassDefaults.SecondaryGreenGlass
): Brush = remember(topColor, bottomColor) {
    Brush.verticalGradient(colors = listOf(topColor, bottomColor))
}

fun Modifier.liquidGlass(
    shape: Shape = LiquidGlassDefaults.CardShape,
    borderWidth: Dp = 1.dp,
    borderColor: Color = LiquidGlassDefaults.ThinGoldBorderColor,
    topColor: Color = LiquidGlassDefaults.DeepGreenGlass,
    bottomColor: Color = LiquidGlassDefaults.SecondaryGreenGlass
): Modifier = composed {
    val gradient = remember(topColor, bottomColor) {
        Brush.verticalGradient(colors = listOf(topColor, bottomColor))
    }
    this
        .clip(shape)
        .background(brush = gradient, shape = shape)
        .border(width = borderWidth, color = borderColor, shape = shape)
}

fun Modifier.liquidGlassSurface(
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = LiquidGlassDefaults.ThinGoldBorderColor
): Modifier = liquidGlass(
    shape = RoundedCornerShape(cornerRadius),
    borderWidth = borderWidth,
    borderColor = borderColor
)
