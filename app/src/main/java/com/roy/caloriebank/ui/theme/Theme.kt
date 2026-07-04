package com.roy.caloriebank.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// This app is dark-only (no light theme) per the original design spec.
private val CalorieBankColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = TextOnPrimaryColor,
    primaryContainer = PrimaryDarkColor,
    onPrimaryContainer = TextOnPrimaryColor,
    secondary = AccentColor,
    onSecondary = TextPrimaryColor,
    tertiary = InfoColor,
    background = BackgroundColor,
    onBackground = TextPrimaryColor,
    surface = SurfaceColor,
    onSurface = TextPrimaryColor,
    surfaceVariant = SurfaceElevatedColor,
    onSurfaceVariant = TextSecondaryColor,
    error = NegativeColor,
    onError = TextPrimaryColor,
    outline = CardBorderColor,
    outlineVariant = CardBorderColor,
)

// Shared corner-radius / border constants matching the component theme section of the spec.
object AppShapes {
    val cardRadius = 16.dp
    val inputRadius = 12.dp
    val buttonRadius = 12.dp
    val chipRadius = 8.dp
    val bottomSheetRadius = 24.dp
    val dialogRadius = 20.dp
    val cardBorderWidth = 1.dp

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )
}

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}

@Composable
fun CalorieBankTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalorieBankColorScheme,
        typography = Typography,
        shapes = AppShapes.shapes,
        content = content,
    )
}
