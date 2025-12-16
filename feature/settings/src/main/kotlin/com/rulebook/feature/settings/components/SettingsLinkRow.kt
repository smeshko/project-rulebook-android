package com.rulebook.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
 * Settings row that acts as a navigation link.
 *
 * Displays a label with a chevron icon indicating navigation.
 * Uses brutalist styling with 3dp black border and surface background.
 *
 * @param label The link label to display.
 * @param onClick Callback when the row is clicked.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsLinkRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .brutalistBorder()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = RulebookTheme.typography.body
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = RulebookTheme.colors.contentSecondary
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Link Row - Light")
@Composable
private fun SettingsLinkRowLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsLinkRow(
            label = "Privacy Policy",
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Link Row - Dark")
@Composable
private fun SettingsLinkRowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsLinkRow(
            label = "Privacy Policy",
            onClick = {}
        )
    }
}
