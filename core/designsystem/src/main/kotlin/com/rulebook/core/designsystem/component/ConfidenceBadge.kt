package com.rulebook.core.designsystem.component

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
 * Color-coded confidence badge displaying a percentage value.
 *
 * Badge background colors are determined by the confidence level:
 * - Green (>0.80): High confidence
 * - Yellow (0.50–0.80): Medium confidence
 * - Red (<0.50): Low confidence
 *
 * @param confidence A value between 0.0 and 1.0 representing the confidence level.
 * @param modifier Modifier to be applied to the badge.
 */
@Composable
fun ConfidenceBadge(
    confidence: Float,
    modifier: Modifier = Modifier
) {
    val colors = RulebookTheme.colors
    val spacing = RulebookTheme.spacing

    val badgeColor = when {
        confidence > 0.80f -> colors.green
        confidence >= 0.50f -> colors.yellow
        else -> colors.red
    }

    val percentageText = "${(confidence * 100).toInt()}%"

    Text(
        text = percentageText,
        style = RulebookTheme.typography.brutalistButtonText,
        color = Color.Black,
        modifier = modifier
            .brutalistBorder()
            .background(badgeColor)
            .padding(
                horizontal = spacing.sm,
                vertical = spacing.xs
            )
    )
}

/**
 * Returns the badge background color for a given confidence level.
 *
 * Exposed for testing purposes.
 */
internal fun confidenceBadgeColor(confidence: Float, green: Color, yellow: Color, red: Color): Color {
    return when {
        confidence > 0.80f -> green
        confidence >= 0.50f -> yellow
        else -> red
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "High Confidence - Light")
@Composable
private fun ConfidenceBadgeHighLightPreview() {
    RulebookTheme(darkTheme = false) {
        ConfidenceBadge(confidence = 0.92f)
    }
}

@Preview(showBackground = true, name = "High Confidence - Dark")
@Composable
private fun ConfidenceBadgeHighDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ConfidenceBadge(confidence = 0.92f)
    }
}

@Preview(showBackground = true, name = "Medium Confidence - Light")
@Composable
private fun ConfidenceBadgeMediumLightPreview() {
    RulebookTheme(darkTheme = false) {
        ConfidenceBadge(confidence = 0.65f)
    }
}

@Preview(showBackground = true, name = "Medium Confidence - Dark")
@Composable
private fun ConfidenceBadgeMediumDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ConfidenceBadge(confidence = 0.65f)
    }
}

@Preview(showBackground = true, name = "Low Confidence - Light")
@Composable
private fun ConfidenceBadgeLowLightPreview() {
    RulebookTheme(darkTheme = false) {
        ConfidenceBadge(confidence = 0.30f)
    }
}

@Preview(showBackground = true, name = "Low Confidence - Dark")
@Composable
private fun ConfidenceBadgeLowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ConfidenceBadge(confidence = 0.30f)
    }
}
