package com.rulebook.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * RulebookFAB - Brutalist-styled Floating Action Button component.
 *
 * A FAB component that follows the Rulebook brutalist design system,
 * featuring pink fill, sharp corners, offset shadow, and bold border.
 * Primarily used for the camera capture action throughout the app.
 *
 * Styling specifications:
 * - Fill color: Pink (#E91E63 light / #F06292 dark)
 * - Border: 3dp black
 * - Shadow: 4dp offset (brutalist)
 * - Shape: RectangleShape (0dp corners)
 * - Icon color: Black
 * - Icon size: 24dp
 * - Min size: 56dp (standard FAB)
 *
 * The FAB includes Material 3 ripple feedback by default through FloatingActionButton.
 *
 * @param onClick Callback invoked when the FAB is clicked.
 * @param modifier Modifier to be applied to the FAB.
 * @param icon The icon to display. Defaults to camera icon.
 * @param contentDescription Accessibility description. Defaults to "Camera".
 */
@Composable
fun RulebookFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.CameraAlt,
    contentDescription: String = "Camera"
) {
    val colors = RulebookTheme.colors

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .brutalistShadow()
            .brutalistBorder(),
        containerColor = colors.pink,
        contentColor = Color.Black,
        shape = RectangleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp, // We use brutalist shadow instead
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "FAB - Light")
@Composable
private fun RulebookFABLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookFAB(onClick = {})
    }
}

@Preview(showBackground = true, name = "FAB - Dark")
@Composable
private fun RulebookFABDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookFAB(onClick = {})
    }
}
