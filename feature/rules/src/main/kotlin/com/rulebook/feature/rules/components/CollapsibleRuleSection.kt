package com.rulebook.feature.rules.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.component.RulebookCard
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * CollapsibleRuleSection - A collapsible card component for displaying rule sections.
 *
 * Features:
 * - Colored accent bar on the left edge
 * - Section title with brutalist styling
 * - Expand/collapse animation with chevron rotation
 * - Content area with text and optional bulleted list
 *
 * @param title The section title (e.g., "Overview", "Setup").
 * @param content The main text content of the section.
 * @param items Optional list of items to display as bullets below the content.
 * @param accentColor The color for the left accent bar (section-specific).
 * @param isExpanded Whether the section is currently expanded.
 * @param onToggle Callback when the section header is clicked.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
fun CollapsibleRuleSection(
    title: String,
    content: String,
    items: List<String>?,
    accentColor: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Chevron rotation"
    )

    RulebookCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Header row with accent bar, title, and chevron
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onToggle,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple()
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .background(accentColor)
                )

                Spacer(modifier = Modifier.width(RulebookTheme.spacing.sm))

                // Section title
                Text(
                    text = title,
                    style = RulebookTheme.typography.brutalistSectionTitle,
                    modifier = Modifier.weight(1f)
                )

                // Chevron icon
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(chevronRotation)
                )
            }

            // Expandable content area
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = RulebookTheme.spacing.md)
                ) {
                    // Main content text
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Optional bulleted list
                    if (items != null && items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = RulebookTheme.spacing.xs)
                            ) {
                                Text(
                                    text = "• ",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Collapsed - Light")
@Composable
private fun CollapsibleRuleSectionCollapsedLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Overview",
            content = "This is the game overview section with important rules.",
            items = null,
            accentColor = RulebookTheme.colors.orange,
            isExpanded = false,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Expanded - Light")
@Composable
private fun CollapsibleRuleSectionExpandedLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Setup",
            content = "Follow these steps to set up the game:",
            items = listOf(
                "Shuffle the deck and deal 7 cards to each player",
                "Place the remaining cards face down as the draw pile",
                "Flip the top card to start the discard pile"
            ),
            accentColor = RulebookTheme.colors.blue,
            isExpanded = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Expanded - Dark")
@Composable
private fun CollapsibleRuleSectionExpandedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "First Round",
            content = "The first round begins with these special rules.",
            items = null,
            accentColor = RulebookTheme.colors.yellow,
            isExpanded = true,
            onToggle = {}
        )
    }
}
