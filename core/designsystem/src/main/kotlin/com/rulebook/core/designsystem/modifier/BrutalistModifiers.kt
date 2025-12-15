package com.rulebook.core.designsystem.modifier

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import com.rulebook.core.designsystem.theme.BrutalistBorderWidth
import com.rulebook.core.designsystem.theme.BrutalistShadowOffset

/**
 * Rulebook Design System - Brutalist Modifier Extensions
 *
 * Provides modifier extensions for applying the brutalist visual style
 * to any composable. These modifiers implement the signature visual
 * characteristics of the Rulebook design system:
 *
 * - Solid offset shadows (not blurred)
 * - Sharp corners (0dp radius)
 * - Bold borders
 *
 * Usage:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(100.dp)
 *         .brutalistShadow()
 *         .background(Color.White)
 * )
 * ```
 */

/**
 * Applies a brutalist-style offset shadow behind the composable.
 *
 * Unlike traditional drop shadows that use blur and alpha, the brutalist shadow
 * is a solid color rectangle offset from the composable. This creates a bold,
 * graphic visual effect characteristic of brutalist design.
 *
 * The shadow is drawn behind the content using [drawBehind], ensuring it doesn't
 * interfere with the composable's content or touch handling.
 *
 * @param offset The distance to offset the shadow from the composable.
 *               Defaults to [BrutalistShadowOffset] (4dp).
 * @param color The color of the shadow. Defaults to [Color.Black].
 * @return A [Modifier] that draws an offset shadow behind the content.
 *
 * @sample
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(100.dp)
 *         .brutalistShadow(offset = 8.dp, color = Color.DarkGray)
 *         .background(Color.White)
 * )
 * ```
 */
fun Modifier.brutalistShadow(
    offset: Dp = BrutalistShadowOffset,
    color: Color = Color.Black
): Modifier = this.drawBehind {
    drawRect(
        color = color,
        topLeft = Offset(offset.toPx(), offset.toPx()),
        size = size
    )
}

/**
 * Applies a brutalist-style border to the composable.
 *
 * The brutalist border uses thick lines and sharp corners (RectangleShape)
 * to create a bold, graphic visual effect characteristic of brutalist design.
 *
 * @param width The width of the border. Defaults to [BrutalistBorderWidth] (3dp).
 * @param color The color of the border. Defaults to [Color.Black].
 * @return A [Modifier] that applies a border with sharp corners.
 *
 * @sample
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(100.dp)
 *         .brutalistBorder(width = 4.dp, color = Color.DarkGray)
 *         .background(Color.White)
 * )
 * ```
 */
fun Modifier.brutalistBorder(
    width: Dp = BrutalistBorderWidth,
    color: Color = Color.Black
): Modifier = this.border(
    width = width,
    color = color,
    shape = RectangleShape
)

/**
 * Applies both brutalist shadow and border to create a complete card styling.
 *
 * This is a convenience modifier that combines [brutalistShadow] and [brutalistBorder]
 * into a single call, providing the complete brutalist card aesthetic with sensible
 * defaults for typical card usage.
 *
 * @param shadowOffset The distance to offset the shadow. Defaults to [BrutalistShadowOffset] (4dp).
 * @param borderWidth The width of the border. Defaults to [BrutalistBorderWidth] (3dp).
 * @param shadowColor The color of the shadow. Defaults to [Color.Black].
 * @param borderColor The color of the border. Defaults to [Color.Black].
 * @return A [Modifier] that applies both shadow and border with brutalist styling.
 *
 * @sample
 * ```kotlin
 * Card(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .brutalistCard()
 *         .background(Color.White)
 * ) {
 *     // Card content
 * }
 * ```
 */
fun Modifier.brutalistCard(
    shadowOffset: Dp = BrutalistShadowOffset,
    borderWidth: Dp = BrutalistBorderWidth,
    shadowColor: Color = Color.Black,
    borderColor: Color = Color.Black
): Modifier = this
    .brutalistShadow(offset = shadowOffset, color = shadowColor)
    .brutalistBorder(width = borderWidth, color = borderColor)
