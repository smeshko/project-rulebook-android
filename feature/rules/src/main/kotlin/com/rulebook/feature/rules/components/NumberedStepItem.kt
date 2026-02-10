package com.rulebook.feature.rules.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * NumberedStepItem - A composable for displaying a numbered step in the First Round section.
 *
 * Features:
 * - Step number with yellow accent color circle/badge
 * - Step text with consistent typography
 * - Clear sequential ordering for step-by-step guides
 *
 * @param stepNumber The step number to display (1-indexed).
 * @param text The step text content.
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun NumberedStepItem(
    stepNumber: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Step number badge with yellow accent color
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = RulebookTheme.colors.yellow.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = RulebookTheme.colors.yellow
            )
        }

        Spacer(modifier = Modifier.width(RulebookTheme.spacing.xs))

        // Step text
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Step 1 - Light")
@Composable
private fun NumberedStepItemStep1LightPreview() {
    RulebookTheme(darkTheme = false) {
        NumberedStepItem(
            stepNumber = 1,
            text = "Each player rolls both dice. The player with the highest total goes first."
        )
    }
}

@Preview(showBackground = true, name = "Step 2 - Light")
@Composable
private fun NumberedStepItemStep2LightPreview() {
    RulebookTheme(darkTheme = false) {
        NumberedStepItem(
            stepNumber = 2,
            text = "On your turn, roll the dice and collect resources based on the number rolled."
        )
    }
}

@Preview(showBackground = true, name = "Step 1 - Dark")
@Composable
private fun NumberedStepItemStep1DarkPreview() {
    RulebookTheme(darkTheme = true) {
        NumberedStepItem(
            stepNumber = 1,
            text = "Each player rolls both dice. The player with the highest total goes first."
        )
    }
}

@Preview(showBackground = true, name = "Step 2 - Dark")
@Composable
private fun NumberedStepItemStep2DarkPreview() {
    RulebookTheme(darkTheme = true) {
        NumberedStepItem(
            stepNumber = 2,
            text = "On your turn, roll the dice and collect resources based on the number rolled."
        )
    }
}
