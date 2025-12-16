package com.rulebook.feature.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Section header component for grouping settings items.
 *
 * Displays an uppercase title with brutalist styling - 60% alpha text
 * for visual hierarchy without competing with row items.
 *
 * @param title The section title to display (will be uppercased).
 * @param modifier Modifier to be applied to the header.
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title.uppercase(),
        style = RulebookTheme.typography.brutalistSectionTitle,
        color = RulebookTheme.colors.contentPrimary.copy(alpha = 0.6f),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Section Header - Light")
@Composable
private fun SettingsSectionHeaderLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsSectionHeader(title = "Appearance")
    }
}

@Preview(showBackground = true, name = "Section Header - Dark")
@Composable
private fun SettingsSectionHeaderDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsSectionHeader(title = "Appearance")
    }
}
