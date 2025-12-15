package com.rulebook.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Rulebook Design System - Theme
 *
 * Three-layer design system architecture:
 * 1. Material 3 Foundation - Provides accessibility, Android conventions, Compose integration
 * 2. Rulebook Theme - Custom colors, typography, shapes from iOS design tokens
 * 3. Rulebook Components - Custom composables implementing brutalist aesthetic
 *
 * This theme wraps MaterialTheme with Rulebook-specific customizations while
 * preserving Material behaviors users expect (ripples, touch targets, etc.).
 *
 * Usage:
 * ```kotlin
 * RulebookTheme {
 *     // Access Material colors
 *     val primary = MaterialTheme.colorScheme.primary
 *
 *     // Access extended Rulebook colors
 *     val orange = RulebookTheme.colors.orange
 *
 *     // Access extended typography
 *     val brutalistTitle = RulebookTheme.typography.brutalistTitle
 *
 *     // Access spacing
 *     val spacing = RulebookTheme.spacing.md
 * }
 * ```
 */

/**
 * Rulebook theme wrapper that applies brutalist design tokens to MaterialTheme.
 *
 * @param darkTheme Whether to use dark theme colors. Defaults to system setting.
 * @param content The content to display with the theme applied.
 */
@Composable
fun RulebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Select color schemes based on theme mode
    val colorScheme = if (darkTheme) rulebookDarkColorScheme() else rulebookLightColorScheme()
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // Provide extended design tokens via CompositionLocal
    CompositionLocalProvider(
        LocalRulebookColors provides extendedColors,
        LocalRulebookTypography provides RulebookExtendedTypographyInstance,
        LocalRulebookSpacing provides RulebookSpacing,
        LocalRulebookShapes provides RulebookExtendedShapesInstance
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RulebookTypography,
            shapes = RulebookShapes,
            content = content
        )
    }
}

/**
 * Object providing convenient access to Rulebook design tokens.
 *
 * Use this object within a RulebookTheme scope to access extended tokens
 * that aren't available in MaterialTheme.
 */
object RulebookTheme {
    /**
     * Extended Rulebook colors including accent palette and semantic colors.
     * These complement MaterialTheme.colorScheme with Rulebook-specific tokens.
     */
    val colors: RulebookExtendedColors
        @Composable
        @ReadOnlyComposable
        get() = LocalRulebookColors.current

    /**
     * Extended Rulebook typography including brutalist styles.
     * These complement MaterialTheme.typography with Rulebook-specific text styles.
     */
    val typography: RulebookExtendedTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalRulebookTypography.current

    /**
     * Rulebook spacing values including spacing scale and brutalist specifications.
     */
    val spacing: RulebookSpacingValues
        @Composable
        @ReadOnlyComposable
        get() = LocalRulebookSpacing.current

    /**
     * Extended Rulebook shapes (all 0dp corners for brutalist aesthetic).
     * These complement MaterialTheme.shapes with semantic shape access.
     */
    val shapes: RulebookExtendedShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalRulebookShapes.current
}
