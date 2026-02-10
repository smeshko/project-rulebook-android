package com.rulebook.feature.rules.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import com.rulebook.core.designsystem.theme.BrutalistShadowOffset
import com.rulebook.core.designsystem.theme.BrutalistShadowOffsetMedium
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * CollapsibleRuleSection - A collapsible card component for displaying rule sections.
 *
 * Features:
 * - Colored accent bar on the left edge
 * - Section title with brutalist styling
 * - Expand/collapse animation with chevron rotation
 * - Content area with text and optional bulleted list OR checklist OR numbered steps
 * - Optional win condition callout (displayed between content and items)
 *
 * @param title The section title (e.g., "Overview", "Setup").
 * @param content The main text content of the section.
 * @param items Optional list of items to display as bullets or checklist below the content.
 * @param accentColor The color for the left accent bar (section-specific).
 * @param isExpanded Whether the section is currently expanded.
 * @param onToggle Callback when the section header is clicked.
 * @param winCondition Optional win condition text to display in a callout.
 * @param checkedItems Optional set of checked item indices (enables checklist mode when not null).
 * @param onItemToggle Optional callback when a checklist item is toggled (required if checkedItems is not null).
 * @param useNumberedSteps Whether to display items as numbered steps (default false, displays bullets).
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
    winCondition: String? = null,
    checkedItems: Set<Int>? = null,
    onItemToggle: ((Int) -> Unit)? = null,
    useNumberedSteps: Boolean = false,
    modifier: Modifier = Modifier
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "Chevron rotation"
    )

    val shadowOffset by animateDpAsState(
        targetValue = if (isExpanded) BrutalistShadowOffsetMedium else BrutalistShadowOffset,
        label = "Shadow offset"
    )

    RulebookCard(
        modifier = modifier.fillMaxWidth(),
        shadowOffset = shadowOffset
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

                    // Optional win condition callout
                    if (winCondition != null) {
                        Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))
                        WinConditionCallout(winCondition = winCondition)
                    }

                    // Optional bulleted list OR checklist OR numbered steps
                    if (items != null && items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(RulebookTheme.spacing.sm))

                        // Checklist mode when checkedItems is not null
                        if (checkedItems != null && onItemToggle != null) {
                            items.forEachIndexed { index, item ->
                                SetupChecklistItem(
                                    stepNumber = index + 1,
                                    text = item,
                                    isChecked = index in checkedItems,
                                    onToggle = { onItemToggle(index) },
                                    modifier = Modifier.padding(vertical = RulebookTheme.spacing.xs)
                                )
                            }
                        } else if (useNumberedSteps) {
                            // Numbered steps mode
                            items.forEachIndexed { index, item ->
                                NumberedStepItem(
                                    stepNumber = index + 1,
                                    text = item,
                                    modifier = Modifier.padding(vertical = RulebookTheme.spacing.xs)
                                )
                            }
                        } else {
                            // Bullet point mode (backward compatible)
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
            onToggle = {},
            winCondition = "Be the first player to collect 10 victory points."
        )
    }
}

@Preview(showBackground = true, name = "Collapsed - Dark")
@Composable
private fun CollapsibleRuleSectionCollapsedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "Overview",
            content = "This is the game overview section with important rules.",
            items = null,
            accentColor = RulebookTheme.colors.orange,
            isExpanded = false,
            onToggle = {},
            winCondition = "Be the first player to collect 10 victory points."
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

@Preview(showBackground = true, name = "Expanded with Win Condition - Light")
@Composable
private fun CollapsibleRuleSectionWithWinConditionLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Overview",
            content = "Catan is a strategy game where players build settlements and cities on a hexagonal board.",
            items = listOf(
                "Trade resources with other players",
                "Build roads, settlements, and cities",
                "Collect resource cards on each turn"
            ),
            accentColor = RulebookTheme.colors.orange,
            isExpanded = true,
            onToggle = {},
            winCondition = "Be the first player to reach 10 victory points."
        )
    }
}

@Preview(showBackground = true, name = "Expanded with Win Condition - Dark")
@Composable
private fun CollapsibleRuleSectionWithWinConditionDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "Overview",
            content = "Catan is a strategy game where players build settlements and cities on a hexagonal board.",
            items = listOf(
                "Trade resources with other players",
                "Build roads, settlements, and cities",
                "Collect resource cards on each turn"
            ),
            accentColor = RulebookTheme.colors.orange,
            isExpanded = true,
            onToggle = {},
            winCondition = "Be the first player to reach 10 victory points."
        )
    }
}

@Preview(showBackground = true, name = "Expanded with Checklist - Light")
@Composable
private fun CollapsibleRuleSectionWithChecklistLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Setup",
            content = "Follow these steps to set up the game:",
            items = listOf(
                "Shuffle the terrain hexes and arrange them randomly",
                "Place number tokens on each hex",
                "Deal two settlements and two roads to each player"
            ),
            accentColor = RulebookTheme.colors.blue,
            isExpanded = true,
            onToggle = {},
            checkedItems = setOf(0, 2),
            onItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Expanded with Checklist - Dark")
@Composable
private fun CollapsibleRuleSectionWithChecklistDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "Setup",
            content = "Follow these steps to set up the game:",
            items = listOf(
                "Shuffle the terrain hexes and arrange them randomly",
                "Place number tokens on each hex",
                "Deal two settlements and two roads to each player"
            ),
            accentColor = RulebookTheme.colors.blue,
            isExpanded = true,
            onToggle = {},
            checkedItems = setOf(1),
            onItemToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Expanded with Numbered Steps - Light")
@Composable
private fun CollapsibleRuleSectionWithNumberedStepsLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "First Round",
            content = "Follow these steps for your first round:",
            items = listOf(
                "Each player rolls both dice. The player with the highest total goes first.",
                "On your turn, roll the dice and collect resources based on the number rolled.",
                "You may trade resources with other players or the bank.",
                "Build roads, settlements, or buy development cards with your resources."
            ),
            accentColor = RulebookTheme.colors.yellow,
            isExpanded = true,
            onToggle = {},
            useNumberedSteps = true
        )
    }
}

@Preview(showBackground = true, name = "Expanded with Numbered Steps - Dark")
@Composable
private fun CollapsibleRuleSectionWithNumberedStepsDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "First Round",
            content = "Follow these steps for your first round:",
            items = listOf(
                "Each player rolls both dice. The player with the highest total goes first.",
                "On your turn, roll the dice and collect resources based on the number rolled.",
                "You may trade resources with other players or the bank.",
                "Build roads, settlements, or buy development cards with your resources."
            ),
            accentColor = RulebookTheme.colors.yellow,
            isExpanded = true,
            onToggle = {},
            useNumberedSteps = true
        )
    }
}

@Preview(showBackground = true, name = "Advanced Section - Expanded with Items - Light")
@Composable
private fun CollapsibleRuleSectionAdvancedExpandedLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Advanced Rules",
            content = "These advanced rules cover edge cases and optional variants for experienced players. Reference these situations as they come up during gameplay.",
            items = listOf(
                "If two players tie, the player who went first loses the tiebreaker",
                "Trading is not allowed during the first two rounds",
                "A player may skip their turn voluntarily",
                "Resources stolen by the robber cannot be traded in the same turn",
                "Development cards purchased on a turn cannot be played until the next turn"
            ),
            accentColor = RulebookTheme.colors.purple,
            isExpanded = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Advanced Section - Expanded with Items - Dark")
@Composable
private fun CollapsibleRuleSectionAdvancedExpandedDarkPreview() {
    RulebookTheme(darkTheme = true) {
        CollapsibleRuleSection(
            title = "Advanced Rules",
            content = "These advanced rules cover edge cases and optional variants for experienced players. Reference these situations as they come up during gameplay.",
            items = listOf(
                "If two players tie, the player who went first loses the tiebreaker",
                "Trading is not allowed during the first two rounds",
                "A player may skip their turn voluntarily",
                "Resources stolen by the robber cannot be traded in the same turn",
                "Development cards purchased on a turn cannot be played until the next turn"
            ),
            accentColor = RulebookTheme.colors.purple,
            isExpanded = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true, name = "Advanced Section - Content Only - Light")
@Composable
private fun CollapsibleRuleSectionAdvancedContentOnlyLightPreview() {
    RulebookTheme(darkTheme = false) {
        CollapsibleRuleSection(
            title = "Advanced Rules",
            content = "For this game, all standard rules apply. There are no special edge cases or advanced rules to be aware of. Play proceeds normally according to the overview and setup instructions.",
            items = null,
            accentColor = RulebookTheme.colors.purple,
            isExpanded = true,
            onToggle = {}
        )
    }
}
