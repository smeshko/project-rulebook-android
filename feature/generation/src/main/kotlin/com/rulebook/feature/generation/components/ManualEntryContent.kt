package com.rulebook.feature.generation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.component.RulebookTextField
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Manual entry content shown when the user rejects the AI suggestion
 * or confidence is very low.
 *
 * Allows the user to type a game name manually and submit it
 * for rules generation.
 *
 * @param gameName The current text value of the game name input.
 * @param onGameNameChanged Callback invoked when the game name text changes.
 * @param onSubmit Callback invoked when the user submits the manual game name.
 * @param onCancel Callback invoked when the user taps the back/cancel button.
 * @param modifier Modifier to be applied to the content.
 */
@Composable
internal fun ManualEntryContent(
    gameName: String,
    onGameNameChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = RulebookTheme.spacing
    val colors = RulebookTheme.colors
    val focusRequester = remember { FocusRequester() }
    val isSubmitEnabled = gameName.isNotBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RulebookHeaderBar(title = "Enter Game Name", onBackClick = onCancel)

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
                RulebookTextField(
                    value = gameName,
                    onValueChange = onGameNameChanged,
                    placeholder = {
                        Text(
                            text = "Type game name...",
                            style = RulebookTheme.typography.body,
                            color = colors.contentTertiary
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Words
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isSubmitEnabled) onSubmit() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
            }
        }

        // Submit button at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.md)
                .padding(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            RulebookButton(
                text = "Generate Rules",
                onClick = onSubmit,
                variant = ButtonVariant.Primary,
                enabled = isSubmitEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Manual Entry Empty - Light")
@Composable
private fun ManualEntryContentEmptyLightPreview() {
    RulebookTheme(darkTheme = false) {
        ManualEntryContent(
            gameName = "",
            onGameNameChanged = {},
            onSubmit = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Manual Entry Empty - Dark")
@Composable
private fun ManualEntryContentEmptyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ManualEntryContent(
            gameName = "",
            onGameNameChanged = {},
            onSubmit = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Manual Entry With Text - Light")
@Composable
private fun ManualEntryContentWithTextLightPreview() {
    RulebookTheme(darkTheme = false) {
        ManualEntryContent(
            gameName = "Settlers of Catan",
            onGameNameChanged = {},
            onSubmit = {},
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Manual Entry With Text - Dark")
@Composable
private fun ManualEntryContentWithTextDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ManualEntryContent(
            gameName = "Settlers of Catan",
            onGameNameChanged = {},
            onSubmit = {},
            onCancel = {}
        )
    }
}
