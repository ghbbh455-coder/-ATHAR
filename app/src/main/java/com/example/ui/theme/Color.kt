package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Athar Visual Identity
// Deep Green: #102D25
val AtharDeepGreen = Color(0xFF102D25)
// Secondary Green: #244638
val AtharSecondaryGreen = Color(0xFF244638)
val AtharGreenMedium = Color(0xFF1A3D32)
val AtharGreenLight = Color(0xFF2D5746)

// Ivory: #F5F0E6
val AtharIvory = Color(0xFFF5F0E6)
val AtharIvoryLight = Color(0xFFFAF7F2)
val AtharIvoryDark = Color(0xFFE8DFC8)

// Calm Gold: #D6BC82
val AtharGold = Color(0xFFD6BC82)
val AtharGoldLight = Color(0xFFEAD9AD)
val AtharGoldDark = Color(0xFFB59A5A)

// Surfaces & Neutral Dark/Light
val AtharDarkBg = Color(0xFF0C1F19)
val AtharDarkSurface = Color(0xFF102D25)
val AtharDarkCard = Color(0xFF17382E)
val AtharTextDarkPrimary = Color(0xFFF5F0E6)
val AtharTextDarkSecondary = Color(0xFFC7D3CD)

val AtharLightBg = Color(0xFFFBF8F2)
val AtharLightSurface = Color(0xFFF5F0E6)
val AtharLightCard = Color(0xFFFFFFFF)
val AtharTextLightPrimary = Color(0xFF102D25)
val AtharTextLightSecondary = Color(0xFF3B564C)

// Liquid Glass & Glassmorphism Colors (Athar Overlay & Cards)
val AtharGlassDarkGreen = Color(0xE0102D25)     // 88% opacity deep green (#102D25)
val AtharGlassSecondary = Color(0xE0244638)     // 88% opacity secondary green (#244638)
val AtharGlassBorder = Color(0x4DD6BC82)        // Thin radiant golden border (0x4DD6BC82)
val AtharGlassBorderHighlight = Color(0x80D6BC82)
val AtharGlassButtonBg = Color(0x26F5F0E6)      // 15% opacity ivory glass button
val AtharGlassButtonBorder = Color(0x4DD6BC82)  // Soft golden button border
val AtharGlassShadow = Color(0x55000000)        // Subtle ambient shadow

val AtharGlassBorderBrush = Brush.linearGradient(
    listOf(Color(0x66D6BC82), Color(0x26D6BC82))
)
val AtharGlassCardGradient = Brush.verticalGradient(
    listOf(Color(0xE0102D25), Color(0xE0244638))
)

