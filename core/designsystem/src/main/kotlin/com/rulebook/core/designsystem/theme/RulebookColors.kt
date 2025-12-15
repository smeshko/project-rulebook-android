package com.rulebook.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Rulebook Design System - Color Tokens
 *
 * Three-layer color architecture:
 * 1. Surface Palette - Background colors for cards, containers, nested elements
 * 2. Content Palette - Text and icon colors with opacity variants
 * 3. Accent Palette - Semantic colors for sections, states, and actions
 *
 * Each palette has Light and Dark mode variants.
 */

// =============================================================================
// SURFACE PALETTE
// =============================================================================

// Light Mode Surfaces
val SurfacePrimaryLight = Color(0xFFFFFFFF)    // Primary: #FFFFFF - Cards, buttons
val SurfaceSecondaryLight = Color(0xFFFFF9F0)  // Secondary: #FFF9F0 - Cream backgrounds
val SurfaceTertiaryLight = Color(0xFFF5E6D3)   // Tertiary: #F5E6D3 - Nested containers

// Dark Mode Surfaces
val SurfacePrimaryDark = Color(0xFF1C1C1E)     // Primary: #1C1C1E - Cards, buttons
val SurfaceSecondaryDark = Color(0xFF2C2C2E)   // Secondary: #2C2C2E - Backgrounds
val SurfaceTertiaryDark = Color(0xFF3A3A3C)    // Tertiary: #3A3A3C - Nested containers

// =============================================================================
// CONTENT PALETTE
// =============================================================================

// Light Mode Content
val ContentPrimaryLight = Color(0xFF000000)           // Primary: #000000 - Main text/icons
val ContentSecondaryLight = Color(0xB2000000)         // Secondary: #000000B2 (70% opacity)
val ContentTertiaryLight = Color(0x66000000)          // Tertiary: #00000066 (40% opacity)

// Dark Mode Content
val ContentPrimaryDark = Color(0xFFFFFFFF)            // Primary: #FFFFFF - Main text/icons
val ContentSecondaryDark = Color(0xB2FFFFFF)          // Secondary: #FFFFFFB2 (70% opacity)
val ContentTertiaryDark = Color(0x66FFFFFF)           // Tertiary: #FFFFFF66 (40% opacity)

// =============================================================================
// ACCENT PALETTE
// =============================================================================

// Orange - Overview, warnings
val OrangeLight = Color(0xFFFF6B35)
val OrangeDark = Color(0xFFFF8C5F)

// Blue - Setup, info
val BlueLight = Color(0xFF3498DB)
val BlueDark = Color(0xFF5DADE2)

// Yellow - First Round
val YellowLight = Color(0xFFFFD23F)
val YellowDark = Color(0xFFFFE066)

// Purple - Advanced Rules
val PurpleLight = Color(0xFF7209B7)
val PurpleDark = Color(0xFF9D4EDD)

// Pink - Actions, buttons
val PinkLight = Color(0xFFE91E63)
val PinkDark = Color(0xFFF06292)

// Green - Success
val GreenLight = Color(0xFF2ECC71)
val GreenDark = Color(0xFF58D68D)

// Red - Errors, destructive
val RedLight = Color(0xFFE74C3C)
val RedDark = Color(0xFFEC7063)

// Error Container - Tinted backgrounds for error states
val ErrorContainerLight = Color(0xFFFCE4E4)  // Light red-tinted background
val ErrorContainerDark = Color(0xFF4A2020)   // Dark red-tinted background

// =============================================================================
// EXTENDED COLOR SCHEME
// =============================================================================

/**
 * Extended color scheme containing Rulebook-specific semantic colors
 * beyond what Material 3 ColorScheme provides.
 */
@Immutable
data class RulebookExtendedColors(
    // Surface colors
    val surfacePrimary: Color,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,

    // Content colors
    val contentPrimary: Color,
    val contentSecondary: Color,
    val contentTertiary: Color,

    // Accent colors
    val orange: Color,
    val blue: Color,
    val yellow: Color,
    val purple: Color,
    val pink: Color,
    val green: Color,
    val red: Color
)

val LightExtendedColors = RulebookExtendedColors(
    surfacePrimary = SurfacePrimaryLight,
    surfaceSecondary = SurfaceSecondaryLight,
    surfaceTertiary = SurfaceTertiaryLight,
    contentPrimary = ContentPrimaryLight,
    contentSecondary = ContentSecondaryLight,
    contentTertiary = ContentTertiaryLight,
    orange = OrangeLight,
    blue = BlueLight,
    yellow = YellowLight,
    purple = PurpleLight,
    pink = PinkLight,
    green = GreenLight,
    red = RedLight
)

val DarkExtendedColors = RulebookExtendedColors(
    surfacePrimary = SurfacePrimaryDark,
    surfaceSecondary = SurfaceSecondaryDark,
    surfaceTertiary = SurfaceTertiaryDark,
    contentPrimary = ContentPrimaryDark,
    contentSecondary = ContentSecondaryDark,
    contentTertiary = ContentTertiaryDark,
    orange = OrangeDark,
    blue = BlueDark,
    yellow = YellowDark,
    purple = PurpleDark,
    pink = PinkDark,
    green = GreenDark,
    red = RedDark
)

val LocalRulebookColors = staticCompositionLocalOf { LightExtendedColors }

// =============================================================================
// MATERIAL COLOR SCHEMES
// =============================================================================

/**
 * Creates the Material 3 light color scheme configured with Rulebook colors.
 *
 * Maps Rulebook semantic colors to Material 3 roles:
 * - primary → Pink (actions, buttons)
 * - secondary → Blue (info, setup)
 * - tertiary → Orange (overview, warnings)
 * - error → Red (errors, destructive)
 * - surface → SurfacePrimary (cards)
 * - background → SurfaceSecondary (screen backgrounds)
 */
fun rulebookLightColorScheme(): ColorScheme = lightColorScheme(
    primary = PinkLight,
    onPrimary = Color.White,
    primaryContainer = SurfaceSecondaryLight,
    onPrimaryContainer = ContentPrimaryLight,
    secondary = BlueLight,
    onSecondary = Color.White,
    secondaryContainer = SurfaceSecondaryLight,
    onSecondaryContainer = ContentPrimaryLight,
    tertiary = OrangeLight,
    onTertiary = Color.White,
    tertiaryContainer = SurfaceSecondaryLight,
    onTertiaryContainer = ContentPrimaryLight,
    error = RedLight,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ContentPrimaryLight,
    background = SurfaceSecondaryLight,
    onBackground = ContentPrimaryLight,
    surface = SurfacePrimaryLight,
    onSurface = ContentPrimaryLight,
    surfaceVariant = SurfaceTertiaryLight,
    onSurfaceVariant = ContentSecondaryLight,
    outline = ContentTertiaryLight,
    outlineVariant = ContentTertiaryLight
)

/**
 * Creates the Material 3 dark color scheme configured with Rulebook colors.
 *
 * Maps Rulebook semantic colors to Material 3 roles:
 * - primary → Pink (actions, buttons)
 * - secondary → Blue (info, setup)
 * - tertiary → Orange (overview, warnings)
 * - error → Red (errors, destructive)
 * - surface → SurfaceSecondary (cards in dark mode)
 * - background → SurfacePrimary (screen backgrounds)
 */
fun rulebookDarkColorScheme(): ColorScheme = darkColorScheme(
    primary = PinkDark,
    onPrimary = Color.Black,
    primaryContainer = SurfacePrimaryDark,
    onPrimaryContainer = ContentPrimaryDark,
    secondary = BlueDark,
    onSecondary = Color.Black,
    secondaryContainer = SurfacePrimaryDark,
    onSecondaryContainer = ContentPrimaryDark,
    tertiary = OrangeDark,
    onTertiary = Color.Black,
    tertiaryContainer = SurfacePrimaryDark,
    onTertiaryContainer = ContentPrimaryDark,
    error = RedDark,
    onError = Color.Black,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ContentPrimaryDark,
    background = SurfacePrimaryDark,
    onBackground = ContentPrimaryDark,
    surface = SurfaceSecondaryDark,
    onSurface = ContentPrimaryDark,
    surfaceVariant = SurfaceTertiaryDark,
    onSurfaceVariant = ContentSecondaryDark,
    outline = ContentTertiaryDark,
    outlineVariant = ContentTertiaryDark
)
