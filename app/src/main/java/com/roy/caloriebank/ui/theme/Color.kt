package com.roy.caloriebank.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Backgrounds
val BackgroundColor = Color(0xFF0A0E1A)
val SurfaceColor = Color(0xFF131929)
val SurfaceElevatedColor = Color(0xFF1A2235)
val CardBorderColor = Color(0xFF1E2D45)

// Brand
val PrimaryColor = Color(0xFF00D4AA)
val PrimaryDarkColor = Color(0xFF00A882)
val PrimaryLightColor = Color(0xFF33DDBB)
val AccentColor = Color(0xFF7B61FF)
val AccentLightColor = Color(0xFF9B85FF)

// Semantic
val PositiveColor = Color(0xFF00C97B)
val NegativeColor = Color(0xFFFF4757)
val WarningColor = Color(0xFFFFB347)
val InfoColor = Color(0xFF4ECDC4)

// Text
val TextPrimaryColor = Color(0xFFF0F4FF)
val TextSecondaryColor = Color(0xFF8896B0)
val TextMutedColor = Color(0xFF4A5568)
val TextOnPrimaryColor = Color(0xFF0A0E1A)

// Chart / progress
val ProteinColor = Color(0xFF7B61FF)
val CarbsColor = Color(0xFFFF8C42)
val FatColor = Color(0xFFFF4757)
val FiberColor = Color(0xFF00C97B)
val FiberRingColor = Color(0xFF66BB6A) // hardcoded fiber ring color from macro progress card

// Nutrition-detail extras
val SugarColor = Color(0xFFFFB300)
val SatFatColor = Color(0xFFEF5350)
val TransFatColor = Color(0xFFF44336)
val CholesterolColor = Color(0xFFAB47BC)

// Code block colors (AI settings)
val CodeBlockBackground = Color(0xFF0A0E1A)
val CodeBlockText = Color(0xFF7FDBCA)

// Named gradients
val PrimaryGradient = Brush.linearGradient(listOf(PrimaryColor, PrimaryDarkColor))
val AccentGradient = Brush.linearGradient(listOf(AccentColor, Color(0xFF5A45CC)))
val BankGradient = Brush.verticalGradient(listOf(SurfaceElevatedColor, Color(0xFF0D1526)))
val CardGradient = Brush.linearGradient(listOf(SurfaceElevatedColor, SurfaceColor))
val PositiveGradient = Brush.linearGradient(listOf(PositiveColor, Color(0xFF00A862)))
val NegativeGradient = Brush.linearGradient(listOf(NegativeColor, Color(0xFFCC3344)))

// Additional inline gradients seen in screens
val AiBannerGradient = Brush.linearGradient(listOf(Color(0xFF1A1035), Color(0xFF0F0A2A)))
val BalanceCardGradient = Brush.linearGradient(listOf(Color(0xFF131929), Color(0xFF0E1622)))
val BankBalanceCardGradient = Brush.verticalGradient(listOf(Color(0xFF0F1E3D), Color(0xFF091426)))
val LoginBackgroundGradient = Brush.verticalGradient(
    listOf(Color(0xFF0A0E1A), Color(0xFF0D1526), Color(0xFF0A0E1A)),
)
val OnboardingBackgroundGradient = Brush.verticalGradient(listOf(Color(0xFF0A0E1A), Color(0xFF0D1526)))

fun diagonalGradient(colors: List<Color>): Brush = Brush.linearGradient(
    colors = colors,
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)
