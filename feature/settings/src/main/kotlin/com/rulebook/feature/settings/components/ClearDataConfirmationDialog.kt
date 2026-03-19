package com.rulebook.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * ClearDataConfirmationDialog - Brutalist-styled confirmation dialog for clearing all app data.
 *
 * Displays a confirmation dialog when the user taps "Clear All Data" in Settings,
 * warning them that all saved games and settings will be deleted.
 *
 * @param onConfirm Callback invoked when the user confirms data clearing.
 * @param onDismiss Callback invoked when the user cancels or dismisses the dialog.
 */
@Composable
fun ClearDataConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.sm)
            ) {
                // Cancel button (left)
                RulebookButton(
                    text = "Cancel",
                    onClick = onDismiss,
                    variant = ButtonVariant.Secondary,
                    modifier = Modifier.weight(1f)
                )

                // Clear Data button (right, destructive)
                RulebookButton(
                    text = "Clear Data",
                    onClick = onConfirm,
                    variant = ButtonVariant.Destructive,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        title = {
            Text(
                text = "CLEAR ALL DATA?",
                style = RulebookTheme.typography.brutalistTitle
            )
        },
        text = {
            Text(
                text = "This will delete all saved games and reset settings.",
                style = RulebookTheme.typography.body
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RectangleShape,
        modifier = Modifier
            .brutalistShadow(offset = RulebookTheme.spacing.shadowOffsetMedium)
            .brutalistBorder()
    )
}

@Preview(name = "Clear Data Confirmation Dialog - Light")
@Composable
private fun ClearDataConfirmationDialogPreview() {
    RulebookTheme(darkTheme = false) {
        ClearDataConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Clear Data Confirmation Dialog - Dark")
@Composable
private fun ClearDataConfirmationDialogDarkPreview() {
    RulebookTheme(darkTheme = true) {
        ClearDataConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
