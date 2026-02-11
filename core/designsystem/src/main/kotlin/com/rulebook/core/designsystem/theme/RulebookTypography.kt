package com.rulebook.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Rulebook Design System - Typography Tokens
 *
 * Typography hierarchy following the UX design specification:
 *
 * | Category   | Style           | Size  | Weight    |
 * |------------|-----------------|-------|-----------|
 * | Display    | Large Title     | 34sp  | Bold      |
 * |            | Title           | 28sp  | Bold      |
 * |            | Title 2         | 22sp  | SemiBold  |
 * | Brutalist  | Title           | 24sp  | Black (900)|
 * |            | Section Title   | 16sp  | Black     |
 * |            | Button Text     | 14sp  | Black     |
 * | Body       | Body            | 17sp  | Regular   |
 * |            | Callout         | 16sp  | Regular   |
 * | Heading    | Card Title      | 17sp  | SemiBold  |
 * | Detail     | Caption         | 12sp  | Regular   |
 *
 * Key Principle: Black weight (900) for brutalist emphasis,
 * regular weight for readable content.
 */

// =============================================================================
// DISPLAY STYLES
// =============================================================================

/**
 * Large Title - 34sp Bold
 * Used for primary screen titles and hero text.
 */
val DisplayLargeTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 41.sp,
    letterSpacing = 0.sp
)

/**
 * Title - 28sp Bold
 * Used for section headers and secondary titles.
 */
val DisplayTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.sp
)

/**
 * Title 2 - 22sp SemiBold
 * Used for tertiary titles and emphasized content.
 */
val DisplayTitle2 = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

// =============================================================================
// BRUTALIST STYLES
// =============================================================================

/**
 * Brutalist Title - 24sp Black (900)
 * Used for bold, attention-grabbing titles in the brutalist aesthetic.
 */
val BrutalistTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Black,
    fontSize = 24.sp,
    lineHeight = 29.sp,
    letterSpacing = 0.sp
)

/**
 * Brutalist Section Title - 16sp Black
 * Used for collapsible section headers (Overview, Setup, etc.).
 */
val BrutalistSectionTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Black,
    fontSize = 16.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.5.sp
)

/**
 * Brutalist Button Text - 14sp Black
 * Used for button labels with the brutalist aesthetic.
 */
val BrutalistButtonText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Black,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.5.sp
)

// =============================================================================
// BODY STYLES
// =============================================================================

/**
 * Body - 17sp Regular
 * Primary text style for readable content.
 */
val BodyText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 17.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp
)

/**
 * Callout - 16sp Regular
 * Secondary body text for supplementary content.
 */
val BodyCallout = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

// =============================================================================
// DETAIL STYLES
// =============================================================================

/**
 * Caption - 12sp Regular
 * Used for metadata, timestamps, and auxiliary information.
 */
val CaptionText = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.sp
)

// =============================================================================
// HEADING STYLES
// =============================================================================

/**
 * Card Title - 17sp SemiBold
 * Used for game card titles in grid displays.
 */
val HeadingCardTitle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp
)

// =============================================================================
// EXTENDED TYPOGRAPHY
// =============================================================================

/**
 * Extended typography containing Rulebook-specific text styles
 * beyond what Material 3 Typography provides.
 */
@Immutable
data class RulebookExtendedTypography(
    // Display styles
    val displayLargeTitle: TextStyle,
    val displayTitle: TextStyle,
    val displayTitle2: TextStyle,

    // Brutalist styles
    val brutalistTitle: TextStyle,
    val brutalistSectionTitle: TextStyle,
    val brutalistButtonText: TextStyle,

    // Body styles
    val body: TextStyle,
    val callout: TextStyle,

    // Detail styles
    val caption: TextStyle,

    // Heading styles
    val cardTitle: TextStyle
)

val RulebookExtendedTypographyInstance = RulebookExtendedTypography(
    displayLargeTitle = DisplayLargeTitle,
    displayTitle = DisplayTitle,
    displayTitle2 = DisplayTitle2,
    brutalistTitle = BrutalistTitle,
    brutalistSectionTitle = BrutalistSectionTitle,
    brutalistButtonText = BrutalistButtonText,
    body = BodyText,
    callout = BodyCallout,
    caption = CaptionText,
    cardTitle = HeadingCardTitle
)

val LocalRulebookTypography = staticCompositionLocalOf { RulebookExtendedTypographyInstance }

// =============================================================================
// MATERIAL 3 TYPOGRAPHY
// =============================================================================

/**
 * Material 3 Typography configured with Rulebook styles.
 *
 * Maps Rulebook typography to Material 3 roles:
 * - displayLarge → Display Large Title (34sp Bold)
 * - displayMedium → Display Title (28sp Bold)
 * - displaySmall → Display Title 2 (22sp SemiBold)
 * - headlineLarge → Brutalist Title (24sp Black)
 * - headlineMedium → Brutalist Section Title (16sp Black)
 * - headlineSmall → Brutalist Button Text (14sp Black)
 * - bodyLarge → Body (17sp Regular)
 * - bodyMedium → Callout (16sp Regular)
 * - bodySmall → Caption (12sp Regular)
 * - titleLarge/Medium/Small → mapped appropriately
 * - labelLarge/Medium/Small → mapped appropriately
 */
val RulebookTypography = Typography(
    // Display styles
    displayLarge = DisplayLargeTitle,
    displayMedium = DisplayTitle,
    displaySmall = DisplayTitle2,

    // Headline styles - mapped to brutalist
    headlineLarge = BrutalistTitle,
    headlineMedium = BrutalistSectionTitle,
    headlineSmall = BrutalistButtonText,

    // Title styles - mixed display and brutalist
    titleLarge = DisplayTitle,
    titleMedium = DisplayTitle2,
    titleSmall = BrutalistSectionTitle,

    // Body styles
    bodyLarge = BodyText,
    bodyMedium = BodyCallout,
    bodySmall = CaptionText,

    // Label styles - for buttons and smaller UI elements
    labelLarge = BrutalistButtonText,
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
