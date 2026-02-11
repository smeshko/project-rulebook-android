package com.rulebook.feature.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
 * DeleteConfirmationDialog - Brutalist-styled confirmation dialog for deleting games.
 *
 * Displays a confirmation dialog when the user attempts to delete a game,
 * following the Rulebook brutalist design system.
 *
 * @param gameName The name of the game being deleted.
 * @param onConfirm Callback invoked when the user confirms deletion.
 * @param onDismiss Callback invoked when the user cancels or dismisses the dialog.
 */
@Composable
fun DeleteConfirmationDialog(
    gameName: String,
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

                // Delete button (right)
                RulebookButton(
                    text = "Delete",
                    onClick = onConfirm,
                    variant = ButtonVariant.Destructive,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        title = {
            Text(
                text = "Delete $gameName?",
                style = RulebookTheme.typography.brutalistTitle
            )
        },
        text = {
            Text(
                text = "This will permanently remove this game and its rules.",
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

@Preview(name = "Delete Confirmation Dialog - Light")
@Composable
private fun DeleteConfirmationDialogPreview() {
    RulebookTheme(darkTheme = false) {
        DeleteConfirmationDialog(
            gameName = "Catan",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Delete Confirmation Dialog - Dark")
@Composable
private fun DeleteConfirmationDialogPreviewDark() {
    RulebookTheme(darkTheme = true) {
        DeleteConfirmationDialog(
            gameName = "Pandemic",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
