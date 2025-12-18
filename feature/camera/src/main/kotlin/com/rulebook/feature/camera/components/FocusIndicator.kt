package com.rulebook.feature.camera.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rulebook.feature.camera.FocusPoint

/**
 * Focus indicator composable that displays at the tap location.
 *
 * Shows an animated focus ring that appears when the user taps to focus.
 * The indicator:
 * - Scales down from 1.5x to 1x with a bouncy spring animation
 * - Fades in when appearing
 * - Fades out after the focus completes
 *
 * The indicator is positioned at the center of the tap location, offset
 * to account for the indicator's size.
 *
 * @param focusPoint The tap location in screen coordinates, or null if no focus.
 * @param visible Whether the indicator should be visible.
 * @param modifier Modifier for the indicator container.
 */
@Composable
fun FocusIndicator(
    focusPoint: FocusPoint?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    // Don't render if no focus point
    if (focusPoint == null) return

    // Scale animation: starts larger (1.5x) and springs down to 1x
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "focusScale"
    )

    // Alpha animation: fades in quickly, fades out smoothly
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (visible) 150 else 300
        ),
        label = "focusAlpha"
    )

    // Only render when visible (alpha > 0)
    if (alpha > 0f) {
        val indicatorSizeDp = 64.dp
        val density = LocalDensity.current
        val indicatorSizePx = with(density) { indicatorSizeDp.toPx() }

        Canvas(
            modifier = modifier
                .offset {
                    // Center the indicator on the tap point
                    // focusPoint coordinates are in pixels, so use pixel-converted indicator size
                    IntOffset(
                        x = (focusPoint.x - indicatorSizePx / 2).toInt(),
                        y = (focusPoint.y - indicatorSizePx / 2).toInt()
                    )
                }
                .size(indicatorSizeDp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha
                )
        ) {
            val strokeWidth = 2.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2

            // Draw focus ring
            drawCircle(
                color = Color.White,
                radius = radius,
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = strokeWidth)
            )

            // Draw corner brackets for additional visual interest
            val bracketLength = size.minDimension * 0.2f
            val bracketOffset = size.minDimension * 0.15f
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Top-left bracket
            drawLine(
                color = Color.White,
                start = Offset(bracketOffset, bracketOffset + bracketLength),
                end = Offset(bracketOffset, bracketOffset),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(bracketOffset, bracketOffset),
                end = Offset(bracketOffset + bracketLength, bracketOffset),
                strokeWidth = strokeWidth
            )

            // Top-right bracket
            drawLine(
                color = Color.White,
                start = Offset(size.width - bracketOffset - bracketLength, bracketOffset),
                end = Offset(size.width - bracketOffset, bracketOffset),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width - bracketOffset, bracketOffset),
                end = Offset(size.width - bracketOffset, bracketOffset + bracketLength),
                strokeWidth = strokeWidth
            )

            // Bottom-left bracket
            drawLine(
                color = Color.White,
                start = Offset(bracketOffset, size.height - bracketOffset - bracketLength),
                end = Offset(bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(bracketOffset, size.height - bracketOffset),
                end = Offset(bracketOffset + bracketLength, size.height - bracketOffset),
                strokeWidth = strokeWidth
            )

            // Bottom-right bracket
            drawLine(
                color = Color.White,
                start = Offset(size.width - bracketOffset - bracketLength, size.height - bracketOffset),
                end = Offset(size.width - bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width - bracketOffset, size.height - bracketOffset - bracketLength),
                end = Offset(size.width - bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth
            )
        }
    }
}
