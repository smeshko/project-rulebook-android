package com.rulebook.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Rulebook Brutalist Color Palette - Light
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),      // Pink - Actions, buttons
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFF9F0),  // Cream background
    onPrimaryContainer = Color.Black,
    secondary = Color(0xFF3498DB),    // Blue - Setup, info
    onSecondary = Color.White,
    tertiary = Color(0xFFFF6B35),     // Orange - Game Overview
    onTertiary = Color.White,
    background = Color(0xFFFFF9F0),   // Cream
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFF5E6D3),  // Tertiary cream
    onSurfaceVariant = Color.Black,
    error = Color(0xFFE74C3C),        // Red
    onError = Color.White,
)

// Rulebook Brutalist Color Palette - Dark
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF06292),      // Pink - lighter for dark mode
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1C1C1E),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF5DADE2),    // Blue - lighter
    onSecondary = Color.Black,
    tertiary = Color(0xFFFF8C5F),     // Orange - lighter
    onTertiary = Color.Black,
    background = Color(0xFF1C1C1E),
    onBackground = Color.White,
    surface = Color(0xFF2C2C2E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3A3A3C),
    onSurfaceVariant = Color.White,
    error = Color(0xFFEC7063),
    onError = Color.Black,
)

@Composable
fun RulebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
