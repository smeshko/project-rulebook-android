package com.rulebook.feature.camera.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Permission denied screen displayed when camera permission has been permanently denied.
 *
 * Shown when the user has selected "Don't ask again" on the permission dialog.
 * Provides a button to open device settings where permission can be manually granted,
 * and offers gallery access as an alternative (Story 4.6).
 *
 * This screen follows the brutalist design system with sharp corners, bold typography,
 * and the Rulebook color palette.
 *
 * @param onOpenSettings Callback invoked when user taps to open device settings.
 * @param modifier Modifier for the root container.
 * @param onGalleryClick Optional callback for gallery button. If provided, shows gallery option.
 * @param galleryThumbnailUri Optional URI for gallery button thumbnail preview.
 */
@Composable
fun PermissionDenied(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onGalleryClick: (() -> Unit)? = null,
    galleryThumbnailUri: Uri? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(RulebookTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Camera blocked icon
            Icon(
                imageVector = Icons.Default.NoPhotography,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = RulebookTheme.colors.red
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))

            // Title
            Text(
                text = "Camera Access Blocked",
                style = RulebookTheme.typography.brutalistTitle,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))

            // Explanation
            Text(
                text = "Camera permission was denied. To scan game boxes, please enable camera access in your device settings.",
                style = RulebookTheme.typography.body,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.xl))

            // Primary action - open settings
            RulebookButton(
                text = "Open Settings",
                onClick = onOpenSettings
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))

            // Alternative explanation
            Text(
                text = "Or use an existing photo:",
                style = RulebookTheme.typography.callout,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Gallery button as alternative - available even without camera permission (Story 4.6)
        if (onGalleryClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(bottom = 48.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                GalleryButton(
                    onClick = onGalleryClick,
                    thumbnailUri = galleryThumbnailUri
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Permission Denied")
@Composable
private fun PermissionDeniedPreview() {
    RulebookTheme(darkTheme = true) {
        PermissionDenied(
            onOpenSettings = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Permission Denied with Gallery")
@Composable
private fun PermissionDeniedWithGalleryPreview() {
    RulebookTheme(darkTheme = true) {
        PermissionDenied(
            onOpenSettings = {},
            onGalleryClick = {}
        )
    }
}
