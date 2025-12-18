package com.rulebook.feature.camera.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rulebook.feature.camera.FlashMode

/**
 * A toggle button for cycling through flash modes.
 *
 * Displays the current flash mode as an icon and cycles to the next mode when tapped.
 * The toggle uses a semi-transparent circular background for visibility against
 * the camera preview.
 *
 * ## Flash Mode Cycle
 * OFF → ON → AUTO → OFF
 *
 * ## Icons
 * - OFF: Flash off icon (bolt with line through)
 * - ON: Flash on icon (solid bolt) - also indicates torch is active
 * - AUTO: Flash auto icon (bolt with 'A')
 *
 * ## Styling
 * Uses brutalist design principles with:
 * - Clear, high-contrast icons
 * - Semi-transparent dark background for visibility
 * - Generous tap target (48dp minimum)
 * - Subtle color animation on mode change
 *
 * @param flashMode Current flash mode to display.
 * @param onToggle Callback invoked when the toggle is tapped.
 * @param modifier Modifier for positioning and styling.
 */
@Composable
fun FlashToggle(
    flashMode: FlashMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animate icon tint to provide visual feedback on mode change
    val iconTint by animateColorAsState(
        targetValue = when (flashMode) {
            FlashMode.OFF -> Color.White.copy(alpha = 0.7f)
            FlashMode.ON -> Color.Yellow
            FlashMode.AUTO -> Color.White
        },
        animationSpec = tween(durationMillis = 150),
        label = "flash_icon_tint"
    )

    val icon = when (flashMode) {
        FlashMode.OFF -> Icons.Default.FlashOff
        FlashMode.ON -> Icons.Default.FlashOn
        FlashMode.AUTO -> Icons.Default.FlashAuto
    }

    val contentDescriptionText = when (flashMode) {
        FlashMode.OFF -> "Flash off. Tap to turn flash on."
        FlashMode.ON -> "Flash on. Tap to set flash to auto."
        FlashMode.AUTO -> "Flash auto. Tap to turn flash off."
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onToggle)
            .semantics {
                role = Role.Button
                contentDescription = contentDescriptionText
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Content description handled by parent
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}
