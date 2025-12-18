package com.rulebook.feature.camera.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Brutalist-styled gallery button for the camera screen.
 *
 * This button allows users to select an existing photo from their gallery
 * instead of capturing a new one. Follows the brutalist design aesthetic with:
 * - Rounded rectangle shape (8dp corners) for differentiation from capture button
 * - Thick border (3dp) characteristic of brutalist design
 * - High contrast white/black color scheme for camera context
 * - Optional thumbnail preview of the last photo
 *
 * The button size (56dp) exceeds the minimum touch target of 48dp,
 * providing a comfortable tap area.
 *
 * @param onClick Callback invoked when the button is tapped to open the gallery picker.
 * @param modifier Modifier for the button container.
 * @param thumbnailUri Optional URI of the last photo to display as a thumbnail preview.
 *                     When null, displays a gallery icon instead.
 */
@Composable
fun GalleryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailUri: Uri? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Use white/black colors for camera context (always visible on camera preview)
    val borderColor = Color.White
    val iconColor = Color.White

    Box(
        modifier = modifier
            .size(56.dp)
            .semantics {
                contentDescription = if (thumbnailUri != null) "Open gallery - shows last photo" else "Open gallery"
                role = Role.Button
            }
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUri != null) {
            AsyncImage(
                model = thumbnailUri,
                contentDescription = null, // Handled by parent Box semantics
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null, // Handled by parent Box semantics
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
