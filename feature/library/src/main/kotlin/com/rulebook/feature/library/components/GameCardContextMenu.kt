package com.rulebook.feature.library.components

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * GameCardContextMenu - Brutalist-styled context menu for game card actions.
 *
 * Displays a dropdown menu with quick actions for a game card:
 * - View Rules: Navigate to the game's rules
 * - Delete: Trigger delete confirmation dialog
 *
 * Follows the Rulebook brutalist design system with solid borders, offset shadows,
 * and sharp corners.
 *
 * @param expanded Whether the menu is currently visible.
 * @param onDismiss Callback invoked when the menu should be dismissed.
 * @param onViewRules Callback invoked when the user selects "View Rules".
 * @param onDelete Callback invoked when the user selects "Delete".
 * @param modifier Optional modifier for the menu container.
 */
@Composable
fun GameCardContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onViewRules: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(RulebookTheme.spacing.xs, RulebookTheme.spacing.xs),
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RectangleShape
            )
            .brutalistShadow(offset = RulebookTheme.spacing.shadowOffset)
            .brutalistBorder(width = RulebookTheme.spacing.borderWidth)
    ) {
        // View Rules menu item
        DropdownMenuItem(
            text = {
                Text(
                    text = "View Rules",
                    style = RulebookTheme.typography.body,
                    color = RulebookTheme.colors.contentPrimary
                )
            },
            onClick = {
                onViewRules()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "View Rules",
                    tint = RulebookTheme.colors.contentPrimary
                )
            }
        )

        // Delete menu item
        DropdownMenuItem(
            text = {
                Text(
                    text = "Delete",
                    style = RulebookTheme.typography.body,
                    color = RulebookTheme.colors.red
                )
            },
            onClick = {
                onDelete()
                onDismiss()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = RulebookTheme.colors.red
                )
            }
        )
    }
}

@Preview(name = "Game Card Context Menu - Light")
@Composable
private fun GameCardContextMenuPreview() {
    RulebookTheme(darkTheme = false) {
        GameCardContextMenu(
            expanded = true,
            onDismiss = {},
            onViewRules = {},
            onDelete = {}
        )
    }
}

@Preview(name = "Game Card Context Menu - Dark")
@Composable
private fun GameCardContextMenuPreviewDark() {
    RulebookTheme(darkTheme = true) {
        GameCardContextMenu(
            expanded = true,
            onDismiss = {},
            onViewRules = {},
            onDelete = {}
        )
    }
}
