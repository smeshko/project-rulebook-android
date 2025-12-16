package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Settings row with a toggle switch.
 *
 * Displays a label with a switch for boolean settings.
 * Uses brutalist styling with 3dp black border and surface background.
 *
 * @param label The setting label to display.
 * @param checked Whether the switch is currently checked.
 * @param onCheckedChange Callback when the switch state changes.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RulebookTheme.typography.body
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Toggle Row Unchecked - Light")
@Composable
private fun SettingsToggleRowUncheckedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsToggleRow(
            label = "Dark Mode",
            checked = false,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Toggle Row Checked - Light")
@Composable
private fun SettingsToggleRowCheckedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsToggleRow(
            label = "Haptic Feedback",
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true, name = "Toggle Row - Dark")
@Composable
private fun SettingsToggleRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsToggleRow(
            label = "Dark Mode",
            checked = true,
            onCheckedChange = {}
        )
    }
}
