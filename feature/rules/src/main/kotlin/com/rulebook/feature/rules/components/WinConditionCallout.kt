package com.rulebook.feature.rules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * WinConditionCallout - A visually distinct component for displaying win conditions.
 *
 * Features brutalist design with:
 * - Orange-tinted background (~10% opacity)
 * - Left accent bar (4dp, orange)
 * - Trophy icon
 * - "Win Condition" label in brutalistSectionTitle style
 * - Condition text in body style
 *
 * @param winCondition The win condition text to display.
 * @param modifier Modifier to be applied to the callout.
 */
@Composable
fun WinConditionCallout(
    winCondition: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RulebookTheme.colors.orange.copy(alpha = 0.1f))
            .padding(RulebookTheme.spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .size(height = 60.dp, width = 4.dp)
                .background(RulebookTheme.colors.orange)
        )

        Spacer(modifier = Modifier.width(RulebookTheme.spacing.md))

        // Trophy icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Win condition",
            tint = RulebookTheme.colors.orange,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(RulebookTheme.spacing.sm))

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Win Condition",
                style = RulebookTheme.typography.brutalistSectionTitle,
                color = RulebookTheme.colors.orange
            )

            Spacer(modifier = Modifier.size(RulebookTheme.spacing.xs))

            Text(
                text = winCondition,
                style = RulebookTheme.typography.body
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Win Condition - Light - Short Text")
@Composable
private fun WinConditionCalloutLightShortPreview() {
    RulebookTheme(darkTheme = false) {
        WinConditionCallout(
            winCondition = "Be the first player to collect 10 victory points."
        )
    }
}

@Preview(showBackground = true, name = "Win Condition - Dark - Short Text")
@Composable
private fun WinConditionCalloutDarkShortPreview() {
    RulebookTheme(darkTheme = true) {
        WinConditionCallout(
            winCondition = "Be the first player to collect 10 victory points."
        )
    }
}

@Preview(showBackground = true, name = "Win Condition - Light - Long Text")
@Composable
private fun WinConditionCalloutLightLongPreview() {
    RulebookTheme(darkTheme = false) {
        WinConditionCallout(
            winCondition = "Win by being the first player to complete all three of your secret " +
                "objectives while simultaneously controlling at least two strategic territories " +
                "on the game board and maintaining a minimum of 5 resource cards in your hand."
        )
    }
}

@Preview(showBackground = true, name = "Win Condition - Dark - Long Text")
@Composable
private fun WinConditionCalloutDarkLongPreview() {
    RulebookTheme(darkTheme = true) {
        WinConditionCallout(
            winCondition = "Win by being the first player to complete all three of your secret " +
                "objectives while simultaneously controlling at least two strategic territories " +
                "on the game board and maintaining a minimum of 5 resource cards in your hand."
        )
    }
}
