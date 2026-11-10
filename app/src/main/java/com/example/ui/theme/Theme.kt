package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtharDarkColorScheme = darkColorScheme(
    primary = AtharGold,
    onPrimary = AtharDeepGreen,
    primaryContainer = AtharSecondaryGreen,
    onPrimaryContainer = AtharIvory,
    secondary = AtharGoldLight,
    onSecondary = AtharDeepGreen,
    secondaryContainer = AtharGreenMedium,
    onSecondaryContainer = AtharIvoryLight,
    tertiary = AtharGold,
    onTertiary = AtharDeepGreen,
    background = AtharDarkBg,
    onBackground = AtharTextDarkPrimary,
    surface = AtharDarkSurface,
    onSurface = AtharTextDarkPrimary,
    surfaceVariant = AtharDarkCard,
    onSurfaceVariant = AtharGoldLight,
    outline = AtharSecondaryGreen,
    outlineVariant = AtharGreenMedium
)

private val AtharLightColorScheme = lightColorScheme(
    primary = AtharDeepGreen,
    onPrimary = AtharIvory,
    primaryContainer = AtharSecondaryGreen,
    onPrimaryContainer = AtharIvory,
    secondary = AtharGoldDark,
    onSecondary = AtharIvoryLight,
    secondaryContainer = AtharIvoryDark,
    onSecondaryContainer = AtharDeepGreen,
    tertiary = AtharGold,
    onTertiary = AtharDeepGreen,
    background = AtharLightBg,
    onBackground = AtharTextLightPrimary,
    surface = AtharLightSurface,
    onSurface = AtharTextLightPrimary,
    surfaceVariant = AtharLightCard,
    onSurfaceVariant = AtharTextLightSecondary,
    outline = AtharIvoryDark,
    outlineVariant = Color(0xFFDED5C2)
)

@Composable
fun AtharTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AtharDarkColorScheme else AtharLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AtharTypography,
        content = content
    )
}
