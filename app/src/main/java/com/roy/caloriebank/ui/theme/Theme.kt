package com.roy.caloriebank.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/** User-selectable app theme; SYSTEM follows the device's light/dark setting. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

private fun darkScheme(c: AppColorTokens) = darkColorScheme(
    primary = c.primary,
    onPrimary = c.onPrimary,
    primaryContainer = c.primaryDark,
    onPrimaryContainer = c.textOnPrimary,
    secondary = c.secondary,
    onSecondary = c.textOnPrimary,
    tertiary = c.tertiary,
    onTertiary = c.textOnPrimary,
    background = c.background,
    onBackground = c.textPrimary,
    surface = c.surface,
    onSurface = c.textPrimary,
    surfaceVariant = c.surfaceElevated,
    onSurfaceVariant = c.textSecondary,
    error = c.negative,
    onError = c.textOnPrimary,
    outline = c.cardBorder,
    outlineVariant = c.cardBorder,
)

private fun lightScheme(c: AppColorTokens) = lightColorScheme(
    primary = c.primary,
    onPrimary = c.onPrimary,
    primaryContainer = c.primaryLight,
    onPrimaryContainer = c.onPrimary,
    secondary = c.secondary,
    onSecondary = Color.White,
    tertiary = c.tertiary,
    onTertiary = Color.White,
    background = c.background,
    onBackground = c.textPrimary,
    surface = c.surface,
    onSurface = c.textPrimary,
    surfaceVariant = c.surfaceElevated,
    onSurfaceVariant = c.textSecondary,
    error = c.negative,
    onError = Color.White,
    outline = c.cardBorder,
    outlineVariant = c.cardBorder,
)

// Shared corner-radius / border constants — generously rounded per Material 3 Expressive
// guidance (bigger, friendlier shapes rather than sharp/small corners everywhere).
object AppShapes {
    val cardRadius = 20.dp
    val inputRadius = 14.dp
    val buttonRadius = 28.dp
    val chipRadius = 10.dp
    val bottomSheetRadius = 28.dp
    val dialogRadius = 24.dp
    val cardBorderWidth = 1.dp

    val shapes = Shapes(
        extraSmall = RoundedCornerShape(10.dp),
        small = RoundedCornerShape(14.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(36.dp),
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
fun CalorieBankTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val tokens = if (darkTheme) DarkAppColors else LightAppColors
    val context = LocalContext.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        dynamicColor && supportsDynamicColor && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && supportsDynamicColor && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> darkScheme(tokens)
        else -> lightScheme(tokens)
    }

    // enableEdgeToEdge() only sets the status/navigation bar icon color once, based on the
    // *system* dark-mode setting at launch. When the user picks an in-app theme that differs
    // from the system setting (e.g. app forced to Dark while the device is in Light mode), the
    // bar icons never get told to switch — they render dark-on-dark and effectively vanish. Keep
    // them in sync with whatever theme is actually showing.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes.shapes,
            content = content,
        )
    }
}
