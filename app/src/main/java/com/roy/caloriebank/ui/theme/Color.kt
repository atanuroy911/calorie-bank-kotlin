package com.roy.caloriebank.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * One brand hue family (teal/emerald primary, cyan-blue secondary, amber tertiary) used
 * consistently everywhere — cards, buttons, gradients — instead of the old mix of unrelated
 * navy-blue, purple, and green tones per-card. Light and dark are tonal variants of the same
 * palette, not separate designs.
 */
data class AppColorTokens(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val cardBorder: Color,
    val positive: Color,
    val negative: Color,
    val warning: Color,
    val info: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textOnPrimary: Color,
    val protein: Color,
    val carbs: Color,
    val fat: Color,
    val fiber: Color,
    val sugar: Color,
    val satFat: Color,
    val transFat: Color,
    val cholesterol: Color,
    val codeBlockBackground: Color,
    val codeBlockText: Color,
)

val DarkAppColors = AppColorTokens(
    primary = Color(0xFF1FD9A8),
    primaryDark = Color(0xFF00A882),
    primaryLight = Color(0xFF6BEBC8),
    onPrimary = Color(0xFF00341F),
    secondary = Color(0xFF6FCBEF),
    secondaryLight = Color(0xFFA6E0F7),
    tertiary = Color(0xFFF7B955),
    background = Color(0xFF060A0C),
    surface = Color(0xFF10171A),
    surfaceElevated = Color(0xFF1C2A2E),
    cardBorder = Color(0xFF2E4046),
    positive = Color(0xFF34D399),
    negative = Color(0xFFFF6B6B),
    warning = Color(0xFFF7B955),
    info = Color(0xFF6FCBEF),
    textPrimary = Color(0xFFF1F6F5),
    textSecondary = Color(0xFF9AABAC),
    textMuted = Color(0xFF56676A),
    textOnPrimary = Color(0xFF00341F),
    protein = Color(0xFF9C8CFF),
    carbs = Color(0xFFFFA85C),
    fat = Color(0xFFFF6B6B),
    fiber = Color(0xFF34D399),
    sugar = Color(0xFFF7C948),
    satFat = Color(0xFFEF6F6C),
    transFat = Color(0xFFE5484D),
    cholesterol = Color(0xFFBE8CF0),
    codeBlockBackground = Color(0xFF0A0F12),
    codeBlockText = Color(0xFF7FE0C6),
)

val LightAppColors = AppColorTokens(
    primary = Color(0xFF00997A),
    primaryDark = Color(0xFF00785F),
    primaryLight = Color(0xFF4CBBA0),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF1C86B4),
    secondaryLight = Color(0xFF5FB2D6),
    tertiary = Color(0xFFB4790C),
    background = Color(0xFFF6FAF8),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEEF4F1),
    cardBorder = Color(0xFFDCE7E2),
    positive = Color(0xFF12946F),
    negative = Color(0xFFD8383E),
    warning = Color(0xFFB4790C),
    info = Color(0xFF1C86B4),
    textPrimary = Color(0xFF10201D),
    textSecondary = Color(0xFF4B5F5A),
    textMuted = Color(0xFF8398A1),
    textOnPrimary = Color(0xFFFFFFFF),
    protein = Color(0xFF6C55D6),
    carbs = Color(0xFFC96A1F),
    fat = Color(0xFFD8383E),
    fiber = Color(0xFF12946F),
    sugar = Color(0xFFB4870C),
    satFat = Color(0xFFC94A47),
    transFat = Color(0xFFB3261E),
    cholesterol = Color(0xFF8A4FC9),
    codeBlockBackground = Color(0xFFEEF4F1),
    codeBlockText = Color(0xFF00785F),
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// Backwards-compatible theme-aware accessors — existing screens reference these by name as if
// they were plain constants; they now resolve against whichever palette CalorieBankTheme provides.
val PrimaryColor: Color @Composable get() = LocalAppColors.current.primary
val PrimaryDarkColor: Color @Composable get() = LocalAppColors.current.primaryDark
val PrimaryLightColor: Color @Composable get() = LocalAppColors.current.primaryLight
val AccentColor: Color @Composable get() = LocalAppColors.current.secondary
val AccentLightColor: Color @Composable get() = LocalAppColors.current.secondaryLight
val TertiaryColor: Color @Composable get() = LocalAppColors.current.tertiary

val BackgroundColor: Color @Composable get() = LocalAppColors.current.background
val SurfaceColor: Color @Composable get() = LocalAppColors.current.surface
val SurfaceElevatedColor: Color @Composable get() = LocalAppColors.current.surfaceElevated
val CardBorderColor: Color @Composable get() = LocalAppColors.current.cardBorder

val PositiveColor: Color @Composable get() = LocalAppColors.current.positive
val NegativeColor: Color @Composable get() = LocalAppColors.current.negative
val WarningColor: Color @Composable get() = LocalAppColors.current.warning
val InfoColor: Color @Composable get() = LocalAppColors.current.info

val TextPrimaryColor: Color @Composable get() = LocalAppColors.current.textPrimary
val TextSecondaryColor: Color @Composable get() = LocalAppColors.current.textSecondary
val TextMutedColor: Color @Composable get() = LocalAppColors.current.textMuted
val TextOnPrimaryColor: Color @Composable get() = LocalAppColors.current.textOnPrimary

val ProteinColor: Color @Composable get() = LocalAppColors.current.protein
val CarbsColor: Color @Composable get() = LocalAppColors.current.carbs
val FatColor: Color @Composable get() = LocalAppColors.current.fat
val FiberColor: Color @Composable get() = LocalAppColors.current.fiber
val FiberRingColor: Color @Composable get() = LocalAppColors.current.fiber

val SugarColor: Color @Composable get() = LocalAppColors.current.sugar
val SatFatColor: Color @Composable get() = LocalAppColors.current.satFat
val TransFatColor: Color @Composable get() = LocalAppColors.current.transFat
val CholesterolColor: Color @Composable get() = LocalAppColors.current.cholesterol

val CodeBlockBackground: Color @Composable get() = LocalAppColors.current.codeBlockBackground
val CodeBlockText: Color @Composable get() = LocalAppColors.current.codeBlockText

// Named gradients — all tonal variants of the same brand hue now, so cards read as one system.
val PrimaryGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.primary, c.primaryDark))
}
val AccentGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.secondary, c.info))
}
val BankGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.verticalGradient(listOf(c.surfaceElevated, c.surface))
}
val CardGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.surfaceElevated, c.surface))
}
val PositiveGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.positive, c.primaryDark))
}
val NegativeGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.negative, c.negative.copy(alpha = 0.75f)))
}

// Same neutral tonal gradient reused everywhere a "featured" card/banner is needed (AI banner,
// balance card, bank balance card) — previously these were purple / navy-blue / navy-blue
// respectively, three unrelated hues on one screen. Kept neutral (rather than a saturated brand
// gradient) so existing default-colored text/icons inside them stay legible in both themes; the
// brand hue shows through via icon tints and the thin border instead.
val AiBannerGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.surfaceElevated, c.surface))
}
val BalanceCardGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.surfaceElevated, c.surface))
}
val BankBalanceCardGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.linearGradient(listOf(c.surfaceElevated, c.surface))
}
val LoginBackgroundGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.verticalGradient(listOf(c.background, c.surfaceElevated, c.background))
}
val OnboardingBackgroundGradient: Brush @Composable get() {
    val c = LocalAppColors.current
    return Brush.verticalGradient(listOf(c.background, c.surfaceElevated))
}

fun diagonalGradient(colors: List<Color>): Brush = Brush.linearGradient(
    colors = colors,
    start = Offset(0f, 0f),
    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
)
