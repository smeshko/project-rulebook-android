package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Settings row displaying static information.
 *
 * Displays a label-value pair for read-only information (e.g., version).
 * Uses brutalist styling with 3dp black border and surface background.
 *
 * @param label The information label.
 * @param value The information value.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsInfoRow(
    label: String,
    value: String,
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

@Preview(showBackground = true, name = "Info Row - Light")
@Composable
private fun SettingsInfoRowLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsInfoRow(
            label = "Version",
            value = "1.0.0"
        )
    }
}

@Preview(showBackground = true, name = "Info Row - Dark")
@Composable
private fun SettingsInfoRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsInfoRow(
            label = "Version",
            value = "1.0.0"
        )
    }
}
