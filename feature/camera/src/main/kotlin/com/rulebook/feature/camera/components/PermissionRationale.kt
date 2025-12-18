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
import androidx.compose.material.icons.filled.CameraAlt
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
 * Permission rationale screen displayed when camera permission has not been granted.
 *
 * Explains why camera permission is needed (to photograph game boxes for rule extraction)
 * and provides a prominent button to request the permission. Also offers gallery access
 * as an alternative (Story 4.6).
 *
 * This screen follows the brutalist design system with sharp corners, bold typography,
 * and the Rulebook color palette.
 *
 * @param onRequestPermission Callback invoked when user taps to request permission.
 * @param onNavigateBack Callback invoked when user wants to go back without requesting.
 * @param modifier Modifier for the root container.
 * @param onGalleryClick Optional callback for gallery button. If provided, shows gallery option.
 * @param galleryThumbnailUri Optional URI for gallery button thumbnail preview.
 */
@Composable
fun PermissionRationale(
    onRequestPermission: () -> Unit,
    onNavigateBack: () -> Unit,
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
            // Camera icon
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = RulebookTheme.colors.pink
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.lg))

            // Title
            Text(
                text = "Camera Permission Needed",
                style = RulebookTheme.typography.brutalistTitle,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))

            // Explanation
            Text(
                text = "Rulebook needs camera access to photograph game boxes and identify them for you.",
                style = RulebookTheme.typography.body,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.xl))

            // Primary action - request permission
            RulebookButton(
                text = "Allow Camera Access",
                onClick = onRequestPermission
            )

            Spacer(modifier = Modifier.height(RulebookTheme.spacing.md))

            // Secondary action - go back
            RulebookButton(
                text = "Not Now",
                onClick = onNavigateBack,
                variant = ButtonVariant.Secondary
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

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Permission Rationale")
@Composable
private fun PermissionRationalePreview() {
    RulebookTheme(darkTheme = true) {
        PermissionRationale(
            onRequestPermission = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, name = "Permission Rationale with Gallery")
@Composable
private fun PermissionRationaleWithGalleryPreview() {
    RulebookTheme(darkTheme = true) {
        PermissionRationale(
            onRequestPermission = {},
            onNavigateBack = {},
            onGalleryClick = {}
        )
    }
}
