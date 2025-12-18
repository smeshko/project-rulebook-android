package com.rulebook.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Brutalist-styled capture button for the camera screen.
 *
 * This button follows the brutalist design aesthetic with:
 * - Large circular shape (72dp) for easy tapping and prominence
 * - Thick border (4dp) characteristic of brutalist design
 * - High contrast white/black color scheme for camera context
 * - Inner circle providing visual depth
 * - Loading indicator when capture is in progress
 *
 * The button size exceeds the minimum touch target of 48dp,
 * providing a comfortable tap area for photo capture.
 *
 * @param onClick Callback invoked when the button is tapped.
 * @param modifier Modifier for the button container.
 * @param enabled Whether the button is enabled. When false, the button
 *                appears dimmed and doesn't respond to clicks.
 * @param isCapturing Whether a photo capture is in progress. When true,
 *                    shows a loading indicator and disables the button.
 */
@Composable
fun CaptureButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isCapturing: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Button is disabled when explicitly disabled OR when capturing
    val isEnabled = enabled && !isCapturing

    // Use white/black colors for camera context (always visible on camera preview)
    val borderColor = Color.White
    val innerCircleColor = Color.White

    Box(
        modifier = modifier
            .size(72.dp)
            .alpha(if (isEnabled) 1f else 0.5f)
            .semantics {
                contentDescription = if (isCapturing) "Capturing photo" else "Capture photo"
                role = Role.Button
            }
            .border(
                width = 4.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(Color.Transparent)
            .clickable(
                enabled = isEnabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCapturing) {
            // Show loading indicator during capture
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        } else {
            // Inner circle for visual depth - characteristic of camera capture buttons
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = innerCircleColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
