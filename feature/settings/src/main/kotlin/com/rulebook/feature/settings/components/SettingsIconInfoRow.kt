package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Settings row displaying static information with a colored icon box.
 *
 * Displays a colored icon, label, and a secondary-colored value — non-clickable.
 * Uses brutalist styling with 3dp black border and surface background.
 * Minimum row height is 64dp to meet touch target guidelines.
 *
 * @param label The information label.
 * @param value The information value displayed as a secondary accessory.
 * @param icon The icon to display in the colored icon box.
 * @param iconTint The tint color applied to the icon.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsIconInfoRow(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
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
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
        }

        // Label
        Text(
            text = label,
            style = RulebookTheme.typography.body,
            modifier = Modifier.weight(1f)
        )

        // Value accessory
        Text(
            text = value,
            style = RulebookTheme.typography.body,
            color = RulebookTheme.colors.contentSecondary
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Icon Info Row - Light")
@Composable
private fun SettingsIconInfoRowLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsIconInfoRow(
            label = "Version",
            value = "1.0.0 (build 1)",
            icon = Icons.Outlined.Info,
            iconTint = RulebookTheme.colors.contentSecondary
        )
    }
}

@Preview(showBackground = true, name = "Icon Info Row - Dark")
@Composable
private fun SettingsIconInfoRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsIconInfoRow(
            label = "Version",
            value = "1.0.0 (build 1)",
            icon = Icons.Outlined.Info,
            iconTint = RulebookTheme.colors.contentSecondary
        )
    }
}
