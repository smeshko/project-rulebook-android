package com.rulebook.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
