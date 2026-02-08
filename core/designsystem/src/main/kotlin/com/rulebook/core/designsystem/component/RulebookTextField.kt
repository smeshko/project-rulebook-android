package com.rulebook.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * RulebookTextField - Brutalist-styled text field component.
 *
 * A text input component that follows the Rulebook brutalist design system,
 * featuring 3dp black borders, sharp corners, and offset shadows.
 *
 * @param value The current text value.
 * @param onValueChange Callback invoked when the text changes.
 * @param modifier Modifier to be applied to the text field.
 * @param placeholder Optional placeholder composable shown when text is empty.
 * @param keyboardOptions Software keyboard options for the text field.
 * @param keyboardActions Actions to perform when IME action is triggered.
 * @param singleLine Whether the text field is single line. Defaults to true.
 */
@Composable
fun RulebookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    val colors = RulebookTheme.colors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .heightIn(min = 48.dp)
            .brutalistShadow()
            .brutalistBorder(),
        placeholder = placeholder,
        textStyle = RulebookTheme.typography.body.copy(
            color = colors.contentPrimary
        ),
        singleLine = singleLine,
        shape = RectangleShape,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surfacePrimary,
            unfocusedContainerColor = colors.surfacePrimary,
            focusedTextColor = colors.contentPrimary,
            unfocusedTextColor = colors.contentPrimary,
            cursorColor = colors.contentPrimary,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedPlaceholderColor = colors.contentTertiary,
            unfocusedPlaceholderColor = colors.contentTertiary
        )
    )
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "TextField Empty - Light")
@Composable
private fun RulebookTextFieldEmptyLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Type game name...") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "TextField Empty - Dark")
@Composable
private fun RulebookTextFieldEmptyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Type game name...") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "TextField With Text - Light")
@Composable
private fun RulebookTextFieldWithTextLightPreview() {
    RulebookTheme(darkTheme = false) {
        RulebookTextField(
            value = "Settlers of Catan",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "TextField With Text - Dark")
@Composable
private fun RulebookTextFieldWithTextDarkPreview() {
    RulebookTheme(darkTheme = true) {
        RulebookTextField(
            value = "Settlers of Catan",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}
