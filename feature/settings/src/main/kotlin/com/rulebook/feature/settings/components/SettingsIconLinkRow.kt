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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Star
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
 * Settings row that acts as a navigation link with a colored icon box.
 *
 * Displays a colored icon, label, and chevron indicating navigation.
 * Uses brutalist styling with 3dp black border and surface background.
 * Minimum row height is 64dp to meet touch target guidelines.
 *
 * @param label The link label to display.
 * @param icon The icon to display in the colored icon box.
 * @param iconTint The tint color applied to the icon.
 * @param onClick Callback when the row is clicked.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SettingsIconLinkRow(
    label: String,
    icon: ImageVector,
    iconTint: Color,
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

        // Chevron
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

@Preview(showBackground = true, name = "Icon Link Row - Contact Light")
@Composable
private fun SettingsIconLinkRowContactLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsIconLinkRow(
            label = "Contact Us",
            icon = Icons.Outlined.Email,
            iconTint = RulebookTheme.colors.blue,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Icon Link Row - Bug Dark")
@Composable
private fun SettingsIconLinkRowBugDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsIconLinkRow(
            label = "Report a Bug",
            icon = Icons.Outlined.BugReport,
            iconTint = RulebookTheme.colors.orange,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Icon Link Row - Rate Light")
@Composable
private fun SettingsIconLinkRowRateLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsIconLinkRow(
            label = "Rate the App",
            icon = Icons.Outlined.Star,
            iconTint = RulebookTheme.colors.yellow,
            onClick = {}
        )
    }
}
