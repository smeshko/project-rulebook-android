package com.rulebook.feature.generation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.ConfidenceBadge
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Confirmation screen content shown when confidence is below the auto-proceed threshold.
 *
 * Displays the identified game name with a confidence badge and asks the user
 * to confirm or reject the identification.
 *
 * @param gameTitle The identified game name to display.
 * @param confidence The confidence level (0.0-1.0).
 * @param onConfirm Callback when user taps "Yes, continue".
 * @param onReject Callback when user taps "No, enter manually".
 * @param modifier Modifier to be applied to the content.
 */
@Composable
fun ConfirmationContent(
    gameTitle: String,
    confidence: Float,
    onConfirm: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = RulebookTheme.spacing
    val typography = RulebookTheme.typography
    val colors = RulebookTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RulebookHeaderBar(title = "Game Identified")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RulebookCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = gameTitle,
                        style = typography.brutalistTitle,
                        color = colors.contentPrimary
                    )

                    Spacer(modifier = Modifier.height(spacing.md))

                    ConfidenceBadge(confidence = confidence)

                    Spacer(modifier = Modifier.height(spacing.lg))

                    Text(
                        text = "Is this your game?",
                        style = typography.body,
                        color = colors.contentSecondary
                    )
                }
            }
        }

        // Action buttons at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            RulebookButton(
                text = "Yes, continue",
                onClick = onConfirm,
                variant = ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            RulebookButton(
                text = "No, enter manually",
                onClick = onReject,
                variant = ButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Confirmation - Medium Confidence - Light")
@Composable
private fun ConfirmationContentMediumLightPreview() {
    RulebookTheme(darkTheme = false) {
        ConfirmationContent(
            gameTitle = "Settlers of Catan",
            confidence = 0.65f,
            onConfirm = {},
            onReject = {}
        )
    }
}

@Preview(showBackground = true, name = "Confirmation - Medium Confidence - Dark")
@Composable
private fun ConfirmationContentMediumDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ConfirmationContent(
            gameTitle = "Settlers of Catan",
            confidence = 0.65f,
            onConfirm = {},
            onReject = {}
        )
    }
}

@Preview(showBackground = true, name = "Confirmation - Low Confidence - Light")
@Composable
private fun ConfirmationContentLowLightPreview() {
    RulebookTheme(darkTheme = false) {
        ConfirmationContent(
            gameTitle = "Unknown Board Game",
            confidence = 0.30f,
            onConfirm = {},
            onReject = {}
        )
    }
}

@Preview(showBackground = true, name = "Confirmation - Low Confidence - Dark")
@Composable
private fun ConfirmationContentLowDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ConfirmationContent(
            gameTitle = "Unknown Board Game",
            confidence = 0.30f,
            onConfirm = {},
            onReject = {}
        )
    }
}
