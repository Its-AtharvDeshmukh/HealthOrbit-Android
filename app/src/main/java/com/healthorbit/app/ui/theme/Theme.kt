package com.healthorbit.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// HealthOrbit Primary Clean Light Scheme (Aligned with Web UI)
private val LightColorScheme = lightColorScheme(
    primary = HealthOrbitTeal,
    onPrimary = SurfaceCardLight,
    primaryContainer = HealthOrbitTealLight,
    onPrimaryContainer = HealthOrbitTealDark,
    secondary = HealthOrbitEmerald,
    onSecondary = SurfaceCardLight,
    secondaryContainer = StatusSuccessBg,
    onSecondaryContainer = HealthOrbitEmeraldDark,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceCardLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorderLight,
    error = VitalHeartRed,
    onError = SurfaceCardLight,
    errorContainer = VitalHeartRedBg,
    onErrorContainer = VitalHeartRed
)

// Dark Scheme fallback maintaining brand tones
private val DarkColorScheme = darkColorScheme(
    primary = HealthOrbitTeal,
    onPrimary = TextPrimary,
    primaryContainer = HealthOrbitTealDark,
    onPrimaryContainer = BackgroundLight,
    secondary = HealthOrbitEmerald,
    onSecondary = TextPrimary,
    secondaryContainer = HealthOrbitEmeraldDark,
    onSecondaryContainer = BackgroundLight,
    background = TextPrimary,
    onBackground = BackgroundLight,
    surface = TextSecondary,
    onSurface = BackgroundLight,
    surfaceVariant = TextSecondary,
    onSurfaceVariant = TextMuted,
    outline = TextSecondary,
    error = VitalHeartRed,
    onError = BackgroundLight
)

@Composable
fun HealthOrbitTheme(
    // Default to false so the app presents the clean light medical aesthetic of the web platform
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}