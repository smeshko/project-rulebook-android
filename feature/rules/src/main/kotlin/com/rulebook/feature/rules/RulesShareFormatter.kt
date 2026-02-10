package com.rulebook.feature.rules

import com.rulebook.core.model.Game
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules

/**
 * Utility for formatting game rules into shareable plain text.
 * Produces formatted text with game title, all sections, items, win conditions, and attribution footer.
 */
object RulesShareFormatter {

    private val NUMBERED_ITEM_REGEX = Regex("^\\d+\\..*")

    /**
     * Formats a game and its rules into a plain text string suitable for sharing.
     *
     * @param game The game to format
     * @param rules The rules to format
     * @return Formatted plain text string with game title, sections, items, win conditions, and footer
     */
    fun formatForSharing(game: Game, rules: Rules): String {
        val builder = StringBuilder()

        // Game title as header
        builder.appendLine("# ${game.title}")
        builder.appendLine()

        // Format each section
        formatSection(builder, rules.overview, "Overview")
        formatSection(builder, rules.setup, "Setup")
        formatSection(builder, rules.firstRound, "First Round")
        formatSection(builder, rules.advanced, "Advanced Rules")

        // Footer attribution
        builder.appendLine()
        builder.append("Shared from Rulebook")

        return builder.toString()
    }

    /**
     * Formats a single rule section with title, content, items, and win condition.
     *
     * @param builder The StringBuilder to append to
     * @param section The section to format
     * @param defaultTitle The default title to use if section title is blank
     */
    private fun formatSection(builder: StringBuilder, section: RuleSection, defaultTitle: String) {
        // Use default title if section title is blank
        val title = section.title.ifBlank { defaultTitle }

        builder.appendLine("## $title")
        builder.appendLine()

        // Section content
        if (section.content.isNotBlank()) {
            builder.appendLine(section.content)
            builder.appendLine()
        }

        // Items as a list
        val items = section.items
        if (!items.isNullOrEmpty()) {
            items.forEach { item ->
                // Check if item already starts with a number or marker
                val formattedItem = if (item.trimStart().matches(NUMBERED_ITEM_REGEX)) {
                    // Already numbered, use as-is
                    item
                } else {
                    // Add bullet marker
                    "• $item"
                }
                builder.appendLine(formattedItem)
            }
            builder.appendLine()
        }

        // Win condition if present
        if (!section.winCondition.isNullOrBlank()) {
            builder.appendLine("🏆 Win Condition: ${section.winCondition}")
            builder.appendLine()
        }
    }
}
