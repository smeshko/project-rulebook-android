package com.rulebook.feature.rules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * MetadataBadge - A brutalist-styled badge for displaying game metadata.
 *
 * Used for displaying key game information like player count, play time,
 * and complexity in a consistent brutalist style.
 *
 * Features:
 * - Brutalist border
 * - Neutral background color
 * - Compact padding
 * - Label and value display
 *
 * @param label The metadata category (e.g., "Players", "Time", "Complexity").
 * @param value The metadata value (e.g., "2-4", "30 min", "Medium").
 * @param modifier Modifier to be applied to the badge.
 */
@Composable
fun MetadataBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "$label: $value",
        style = RulebookTheme.typography.brutalistButtonText,
        color = Color.Black,
        modifier = modifier
            .brutalistBorder()
            .background(RulebookTheme.colors.surfaceTertiary)
            .padding(
                horizontal = RulebookTheme.spacing.sm,
                vertical = RulebookTheme.spacing.xs
            )
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Players Badge - Light")
@Composable
private fun MetadataBadgePlayersLightPreview() {
    RulebookTheme(darkTheme = false) {
        MetadataBadge(label = "Players", value = "2-4")
    }
}

@Preview(showBackground = true, name = "Players Badge - Dark")
@Composable
private fun MetadataBadgePlayersDarkPreview() {
    RulebookTheme(darkTheme = true) {
        MetadataBadge(label = "Players", value = "2-4")
    }
}

@Preview(showBackground = true, name = "Time Badge - Light")
@Composable
private fun MetadataBadgeTimeLightPreview() {
    RulebookTheme(darkTheme = false) {
        MetadataBadge(label = "Time", value = "30 min")
    }
}

@Preview(showBackground = true, name = "Complexity Badge - Light")
@Composable
private fun MetadataBadgeComplexityLightPreview() {
    RulebookTheme(darkTheme = false) {
        MetadataBadge(label = "Complexity", value = "Medium")
    }
}
