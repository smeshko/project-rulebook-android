package com.rulebook.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Rulebook Design System - Spacing Tokens
 *
 * Spacing scale following the UX design specification:
 *
 * | Token | Value | Usage           |
 * |-------|-------|-----------------|
 * | xs    | 4dp   | Micro gaps      |
 * | sm    | 8dp   | List spacing, tight padding |
 * | md    | 16dp  | Standard padding |
 * | lg    | 24dp  | Section spacing  |
 * | xl    | 32dp  | Large sections   |
 *
 * Brutalist specifications:
 *
 * | Element      | Value      |
 * |--------------|------------|
 * | Border width | 3dp standard, 4dp thick |
 * | Shadow offset| 4dp (normal) to 12dp (elevated) |
 * | Corner radius| 0dp (sharp corners) |
 * | Button padding| 20dp horizontal |
 */

// =============================================================================
// SPACING SCALE
// =============================================================================

/**
 * Extra small spacing - 4dp
 * Used for micro gaps between tightly coupled elements.
 */
val SpacingXs: Dp = 4.dp

/**
 * Small spacing - 8dp
 * Used for list item spacing and tight internal padding.
 */
val SpacingSm: Dp = 8.dp

/**
 * Medium spacing - 16dp
 * Used for standard padding in cards, containers, and screen margins.
 */
val SpacingMd: Dp = 16.dp

/**
 * Large spacing - 24dp
 * Used for section spacing and generous padding.
 */
val SpacingLg: Dp = 24.dp

/**
 * Extra large spacing - 32dp
 * Used for large section breaks and major content separation.
 */
val SpacingXl: Dp = 32.dp

// =============================================================================
// BRUTALIST SPECIFICATIONS
// =============================================================================

/**
 * Standard border width - 3dp
 * Used for card borders, section outlines, and standard UI elements.
 */
val BrutalistBorderWidth: Dp = 3.dp

/**
 * Thick border width - 4dp
 * Used for emphasized elements and heavy visual weight.
 */
val BrutalistBorderWidthThick: Dp = 4.dp

/**
 * Normal shadow offset - 4dp
 * Used for standard brutalist shadow effect on cards and buttons.
 */
val BrutalistShadowOffset: Dp = 4.dp

/**
 * Medium shadow offset - 8dp
 * Used for intermediate elevation effects.
 */
val BrutalistShadowOffsetMedium: Dp = 8.dp

/**
 * Large shadow offset - 12dp
 * Used for elevated elements with strong visual depth.
 */
val BrutalistShadowOffsetLarge: Dp = 12.dp

/**
 * Corner radius - 0dp
 * Brutalist aesthetic uses sharp corners exclusively.
 */
val BrutalistCornerRadius: Dp = 0.dp

/**
 * Button horizontal padding - 20dp
 * Standard internal padding for brutalist buttons.
 */
val ButtonPaddingHorizontal: Dp = 20.dp

// =============================================================================
// LAYOUT SPECIFICATIONS
// =============================================================================

/**
 * Screen margin - 16dp
 * Standard horizontal margin for screen content.
 */
val ScreenMargin: Dp = 16.dp

/**
 * Card padding - 16dp
 * Standard internal padding for cards and containers.
 */
val CardPadding: Dp = 16.dp

/**
 * Grid spacing - 16dp
 * Standard spacing between grid items (e.g., 2-column library).
 */
val GridSpacing: Dp = 16.dp

/**
 * Tab bar height - 60dp (plus safe area)
 * Standard height for bottom navigation.
 */
val TabBarHeight: Dp = 60.dp

// =============================================================================
// SPACING OBJECT
// =============================================================================

/**
 * Centralized spacing object providing access to all spacing tokens.
 * Provides both the standard scale and brutalist specifications.
 */
@Immutable
data class RulebookSpacingValues(
    // Spacing scale
    val xs: Dp,
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val xl: Dp,

    // Brutalist specifications
    val borderWidth: Dp,
    val borderWidthThick: Dp,
    val shadowOffset: Dp,
    val shadowOffsetMedium: Dp,
    val shadowOffsetLarge: Dp,
    val cornerRadius: Dp,
    val buttonPaddingHorizontal: Dp,

    // Layout specifications
    val screenMargin: Dp,
    val cardPadding: Dp,
    val gridSpacing: Dp,
    val tabBarHeight: Dp
)

/**
 * Default Rulebook spacing values instance.
 */
val RulebookSpacing = RulebookSpacingValues(
    // Spacing scale
    xs = SpacingXs,
    sm = SpacingSm,
    md = SpacingMd,
    lg = SpacingLg,
    xl = SpacingXl,

    // Brutalist specifications
    borderWidth = BrutalistBorderWidth,
    borderWidthThick = BrutalistBorderWidthThick,
    shadowOffset = BrutalistShadowOffset,
    shadowOffsetMedium = BrutalistShadowOffsetMedium,
    shadowOffsetLarge = BrutalistShadowOffsetLarge,
    cornerRadius = BrutalistCornerRadius,
    buttonPaddingHorizontal = ButtonPaddingHorizontal,

    // Layout specifications
    screenMargin = ScreenMargin,
    cardPadding = CardPadding,
    gridSpacing = GridSpacing,
    tabBarHeight = TabBarHeight
)

val LocalRulebookSpacing = staticCompositionLocalOf { RulebookSpacing }
