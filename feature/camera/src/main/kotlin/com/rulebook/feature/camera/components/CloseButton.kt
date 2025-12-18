package com.rulebook.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Close button composable for the camera screen.
 *
 * Displays an "X" icon button with a semi-transparent background for visibility
 * against the camera preview. Positioned in the top-left corner of the camera
 * screen to allow users to navigate back to the previous screen (Library).
 *
 * Design specifications:
 * - 48dp touch target for accessibility
 * - Semi-transparent black background (30% opacity) for visibility on any preview
 * - White icon for contrast
 * - Circular shape for brutalist design consistency
 *
 * @param onClick Callback invoked when the close button is tapped.
 * @param modifier Optional modifier for the button container.
 */
@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close camera",
            tint = Color.White
        )
    }
}
