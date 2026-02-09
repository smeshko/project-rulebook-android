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
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Error screen content shown when recognition fails.
 * Story 5.9: Retry Failed Recognition
 *
 * Displays a friendly error message with options to retry (return to camera)
 * or enter the game name manually.
 *
 * @param errorMessage The user-friendly error message to display.
 * @param onRetry Callback when user taps "Try Again" to return to camera.
 * @param onManualEntry Callback when user taps "Enter Manually".
 * @param modifier Modifier to be applied to the content.
 */
@Composable
internal fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onManualEntry: () -> Unit,
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
        RulebookHeaderBar(title = "Oops!")

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
                        text = "Something went wrong",
                        style = typography.brutalistTitle,
                        color = colors.contentPrimary
                    )

                    Spacer(modifier = Modifier.height(spacing.md))

                    Text(
                        text = errorMessage,
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
                text = "Try Again",
                onClick = onRetry,
                variant = ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )

            RulebookButton(
                text = "Enter Manually",
                onClick = onManualEntry,
                variant = ButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Error Content - Light")
@Composable
private fun ErrorContentLightPreview() {
    RulebookTheme(darkTheme = false) {
        ErrorContent(
            errorMessage = "No internet connection. Please check your network.",
            onRetry = {},
            onManualEntry = {}
        )
    }
}

@Preview(showBackground = true, name = "Error Content - Dark")
@Composable
private fun ErrorContentDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ErrorContent(
            errorMessage = "No internet connection. Please check your network.",
            onRetry = {},
            onManualEntry = {}
        )
    }
}

@Preview(showBackground = true, name = "Error Content Generic - Light")
@Composable
private fun ErrorContentGenericLightPreview() {
    RulebookTheme(darkTheme = false) {
        ErrorContent(
            errorMessage = "Something went wrong. Please try again.",
            onRetry = {},
            onManualEntry = {}
        )
    }
}
