package com.rulebook.feature.purchase.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * A small badge label with brutalist styling, used for product card callouts.
 *
 * Applies a bold border, background color, and optional rotation for visual flair.
 *
 * @param text The label text to display.
 * @param backgroundColor The background color of the badge.
 * @param rotation Degrees to rotate the badge. Defaults to 0f (no rotation).
 * @param modifier Modifier to be applied to the badge.
 */
@Composable
fun RulebookBadge(
    text: String,
    backgroundColor: Color,
    rotation: Float = 0f,
    modifier: Modifier = Modifier
) {
    val spacing = RulebookTheme.spacing

    Text(
        text = text,
        style = RulebookTheme.typography.brutalistButtonText,
        color = Color.Black,
        modifier = modifier
            .rotate(rotation)
            .brutalistBorder()
            .background(backgroundColor)
            .padding(
                horizontal = spacing.sm,
                vertical = spacing.xs
            )
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Most Popular Badge - Light")
@Composable
private fun MostPopularBadgeLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookBadge(
            text = "Most Popular",
            backgroundColor = RulebookTheme.colors.pink,
            rotation = -5f
        )
    }
}

@Preview(showBackground = true, name = "Most Popular Badge - Dark")
@Composable
private fun MostPopularBadgeDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookBadge(
            text = "Most Popular",
            backgroundColor = RulebookTheme.colors.pink,
            rotation = -5f
        )
    }
}

@Preview(showBackground = true, name = "Best Value Badge - Light")
@Composable
private fun BestValueBadgeLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookBadge(
            text = "Best Value",
            backgroundColor = RulebookTheme.colors.green
        )
    }
}

@Preview(showBackground = true, name = "Best Value Badge - Dark")
@Composable
private fun BestValueBadgeDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookBadge(
            text = "Best Value",
            backgroundColor = RulebookTheme.colors.green
        )
    }
}
