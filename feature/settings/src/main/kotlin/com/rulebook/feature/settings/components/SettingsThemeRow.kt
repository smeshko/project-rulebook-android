package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Settings row for theme selection with a radio button indicator.
 *
 * Displays a theme option (Light, Dark, or System) with an icon and radio indicator.
 * Uses brutalist styling with 3dp black border and surface background.
 * Selected state shows a filled pink radio circle.
 *
 * @param label The theme option label to display.
 * @param icon The icon representing the theme option.
 * @param isSelected Whether this theme option is currently selected.
 * @param onClick Callback when the row is clicked.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsThemeRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .heightIn(min = 64.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon box
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RulebookTheme.colors.orange,
                modifier = Modifier.size(24.dp)
            )
        }

        // Label
        Text(
            text = label,
            style = RulebookTheme.typography.body,
            modifier = Modifier.weight(1f)
        )

        // Radio indicator
        val radioBackground = if (isSelected) {
            Modifier.background(RulebookTheme.colors.pink)
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .size(20.dp)
                .brutalistBorder()
                .then(radioBackground)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Theme Row - Selected Light")
@Composable
private fun SettingsThemeRowSelectedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsThemeRow(
            label = "Light",
            icon = Icons.Outlined.LightMode,
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Theme Row - Unselected Light")
@Composable
private fun SettingsThemeRowUnselectedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsThemeRow(
            label = "Dark",
            icon = Icons.Outlined.DarkMode,
            isSelected = false,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Theme Row - System Dark")
@Composable
private fun SettingsThemeRowSystemDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsThemeRow(
            label = "System",
            icon = Icons.Outlined.BrightnessAuto,
            isSelected = true,
            onClick = {}
        )
    }
}
