package com.rulebook.feature.rules.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * SetupChecklistItem - A composable for displaying a setup step with a checkbox.
 *
 * Features:
 * - Material 3 Checkbox with custom colors
 * - Step number prefix (e.g., "1. ", "2. ")
 * - Step text with strikethrough when checked
 * - Reduced opacity when checked for visual feedback
 *
 * @param stepNumber The step number to display (1-indexed).
 * @param text The step text content.
 * @param isChecked Whether the checkbox is currently checked.
 * @param onToggle Callback when the checkbox is toggled.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun SetupChecklistItem(
    stepNumber: Int,
    text: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox with custom colors
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = RulebookTheme.colors.blue,
                checkmarkColor = RulebookTheme.colors.green
            )
        )

        Spacer(modifier = Modifier.width(RulebookTheme.spacing.xs))

        // Step number and text
        Text(
            text = "$stepNumber. $text",
            style = if (isChecked) {
                MaterialTheme.typography.bodyMedium.copy(
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            modifier = Modifier
                .weight(1f)
                .alpha(if (isChecked) 0.6f else 1f)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Unchecked - Light")
@Composable
private fun SetupChecklistItemUncheckedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SetupChecklistItem(
            stepNumber = 1,
            text = "Shuffle the terrain hexes and arrange them randomly",
            isChecked = false,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Checked - Light")
@Composable
private fun SetupChecklistItemCheckedLightPreview() {
    RulebookTheme(darkTheme = false) {
        SetupChecklistItem(
            stepNumber = 2,
            text = "Place number tokens on each hex",
            isChecked = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Unchecked - Dark")
@Composable
private fun SetupChecklistItemUncheckedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SetupChecklistItem(
            stepNumber = 3,
            text = "Deal two settlements and two roads to each player",
            isChecked = false,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Checked - Dark")
@Composable
private fun SetupChecklistItemCheckedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SetupChecklistItem(
            stepNumber = 4,
            text = "Each player places their first settlement and road",
            isChecked = true,
            onToggle = {}
        )
    }
}
