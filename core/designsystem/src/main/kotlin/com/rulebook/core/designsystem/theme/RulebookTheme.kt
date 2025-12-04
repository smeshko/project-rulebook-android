package com.rulebook.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Rulebook Brutalist Color Palette
object RulebookColors {
    // Surface colors - Light
    val SurfacePrimaryLight = Color(0xFFFFFFFF)
    val SurfaceSecondaryLight = Color(0xFFFFF9F0)  // Cream
    val SurfaceTertiaryLight = Color(0xFFF5E6D3)

    // Surface colors - Dark
    val SurfacePrimaryDark = Color(0xFF1C1C1E)
    val SurfaceSecondaryDark = Color(0xFF2C2C2E)
    val SurfaceTertiaryDark = Color(0xFF3A3A3C)

    // Accent colors - Light
    val OrangeLight = Color(0xFFFF6B35)
    val BlueLight = Color(0xFF3498DB)
    val YellowLight = Color(0xFFFFD23F)
    val PurpleLight = Color(0xFF7209B7)
    val PinkLight = Color(0xFFE91E63)
    val GreenLight = Color(0xFF2ECC71)
    val RedLight = Color(0xFFE74C3C)

    // Accent colors - Dark
    val OrangeDark = Color(0xFFFF8C5F)
    val BlueDark = Color(0xFF5DADE2)
    val YellowDark = Color(0xFFFFE066)
    val PurpleDark = Color(0xFF9D4EDD)
    val PinkDark = Color(0xFFF06292)
    val GreenDark = Color(0xFF58D68D)
    val RedDark = Color(0xFFEC7063)
}

private val LightColorScheme = lightColorScheme(
    primary = RulebookColors.PinkLight,
    onPrimary = Color.White,
    primaryContainer = RulebookColors.SurfaceSecondaryLight,
    secondary = RulebookColors.BlueLight,
    tertiary = RulebookColors.OrangeLight,
    background = RulebookColors.SurfaceSecondaryLight,
    surface = RulebookColors.SurfacePrimaryLight,
    error = RulebookColors.RedLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = RulebookColors.PinkDark,
    onPrimary = Color.Black,
    primaryContainer = RulebookColors.SurfacePrimaryDark,
    secondary = RulebookColors.BlueDark,
    tertiary = RulebookColors.OrangeDark,
    background = RulebookColors.SurfacePrimaryDark,
    surface = RulebookColors.SurfaceSecondaryDark,
    error = RulebookColors.RedDark,
)

@Composable
fun RulebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
