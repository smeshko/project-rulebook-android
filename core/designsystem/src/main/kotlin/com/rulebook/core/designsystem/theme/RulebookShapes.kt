package com.rulebook.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Rulebook Design System - Shape Tokens
 *
 * The brutalist aesthetic requires sharp corners (0dp radius) exclusively.
 * This contrasts with Material Design's default rounded shapes.
 *
 * From the UX design specification:
 * > **Override (Brutalist Identity):**
 * > | Material Default    | Rulebook Style        |
 * > |---------------------|-----------------------|
 * > | Rounded shapes (12dp)| Sharp corners (0dp)  |
 *
 * All shapes use 0dp corner radius for the distinctive brutalist look.
 */

// =============================================================================
// SHAPE DEFINITIONS
// =============================================================================

/**
 * Extra small shape - Sharp corners
 * Used for the smallest UI elements like badges, chips.
 */
val ShapeExtraSmall: Shape = RectangleShape

/**
 * Small shape - Sharp corners
 * Used for smaller elements like text fields, small cards.
 */
val ShapeSmall: Shape = RectangleShape

/**
 * Medium shape - Sharp corners
 * Used for standard UI elements like cards, buttons, dialogs.
 */
val ShapeMedium: Shape = RectangleShape

/**
 * Large shape - Sharp corners
 * Used for larger containers like bottom sheets, expanded cards.
 */
val ShapeLarge: Shape = RectangleShape

/**
 * Extra large shape - Sharp corners
 * Used for full-width elements and major containers.
 */
val ShapeExtraLarge: Shape = RectangleShape

// =============================================================================
// MATERIAL 3 SHAPES
// =============================================================================

/**
 * Material 3 Shapes configured with brutalist sharp corners.
 *
 * All shape roles use 0dp corner radius for the brutalist aesthetic.
 * This replaces Material's default rounded corners:
 * - Material default: 4dp-28dp rounded corners
 * - Rulebook: 0dp (sharp corners) throughout
 */
val RulebookShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

// =============================================================================
// EXTENDED SHAPES
// =============================================================================

/**
 * Extended shape object containing Rulebook-specific shape definitions.
 * Provides semantic shape access beyond Material 3 Shapes.
 */
@Immutable
data class RulebookExtendedShapes(
    val extraSmall: Shape,
    val small: Shape,
    val medium: Shape,
    val large: Shape,
    val extraLarge: Shape
)

val RulebookExtendedShapesInstance = RulebookExtendedShapes(
    extraSmall = ShapeExtraSmall,
    small = ShapeSmall,
    medium = ShapeMedium,
    large = ShapeLarge,
    extraLarge = ShapeExtraLarge
)

val LocalRulebookShapes = staticCompositionLocalOf { RulebookExtendedShapesInstance }
