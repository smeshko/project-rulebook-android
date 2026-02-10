package com.rulebook.core.network.mapper

import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import com.rulebook.core.network.model.GenerateResponse
import java.util.Locale

/**
 * Maps a [GenerateResponse] from the network layer to a [Rules] domain model.
 *
 * The API returns a flat list of [RulesSection]s, which are mapped to the four named fields
 * in the [Rules] domain model (overview, setup, firstRound, advanced).
 *
 * Mapping strategy:
 * 1. Match by title (case-insensitive) with fallbacks
 * 2. Fall back to positional mapping if title matching fails
 * 3. Provide empty defaults for missing sections
 */
fun GenerateResponse.toDomain(): Rules {
    val sectionMap = rulesSections.associateBy { it.title.lowercase(Locale.ROOT) }

    // Map sections by title with fallbacks, or use positional index
    val overview = sectionMap.findSection(
        keys = listOf("overview", "summary", "introduction"),
        fallbackIndex = 0,
        allSections = rulesSections,
        fallbackContent = rulesSummary.takeIf { it.isNotBlank() }
    )

    val setup = sectionMap.findSection(
        keys = listOf("setup", "game setup", "preparation"),
        fallbackIndex = 1,
        allSections = rulesSections
    )

    val firstRound = sectionMap.findSection(
        keys = listOf("first round", "first turn", "starting play", "how to play"),
        fallbackIndex = 2,
        allSections = rulesSections
    )

    val advanced = sectionMap.findSection(
        keys = listOf("advanced", "advanced rules", "detailed rules", "edge cases"),
        fallbackIndex = 3,
        allSections = rulesSections
    )

    return Rules(
        gameId = gameTitle,
        overview = overview,
        setup = setup,
        firstRound = firstRound,
        advanced = advanced
    )
}

/**
 * Helper function to find a section by multiple possible keys or fall back to positional index.
 * For overview sections (fallbackIndex == 0), attempts to extract win condition from content.
 */
private fun Map<String, com.rulebook.core.network.model.RulesSection>.findSection(
    keys: List<String>,
    fallbackIndex: Int,
    allSections: List<com.rulebook.core.network.model.RulesSection>,
    fallbackContent: String? = null
): RuleSection {
    val isOverview = fallbackIndex == 0

    // Try to find by any of the provided keys
    for (key in keys) {
        val section = this[key]
        if (section != null) {
            val (content, winCondition) = if (isOverview) {
                extractWinCondition(section.content)
            } else {
                section.content to null
            }
            return RuleSection(
                title = section.title,
                content = content,
                items = null,
                winCondition = winCondition
            )
        }
    }

    // Fall back to positional index
    return if (fallbackIndex < allSections.size) {
        val section = allSections[fallbackIndex]
        val (content, winCondition) = if (isOverview) {
            extractWinCondition(section.content)
        } else {
            section.content to null
        }
        RuleSection(
            title = section.title,
            content = content,
            items = null,
            winCondition = winCondition
        )
    } else {
        // Provide empty default if section is missing, using fallbackContent if available
        val (content, winCondition) = if (isOverview && fallbackContent != null) {
            extractWinCondition(fallbackContent)
        } else {
            (fallbackContent ?: "") to null
        }
        RuleSection(
            title = keys.first().replaceFirstChar { it.uppercase() },
            content = content,
            items = null,
            winCondition = winCondition
        )
    }
}

/**
 * Extracts win condition from content by scanning for common win condition patterns.
 * Returns a pair of (remaining content, extracted win condition or null).
 *
 * Patterns searched (case-insensitive):
 * - "win by"
 * - "wins" (with preceding context)
 * - "win condition"
 * - "goal is to"
 * - "objective is"
 */
private fun extractWinCondition(content: String): Pair<String, String?> {
    val patterns = listOf(
        Regex("(?i)\\b(win by .+?)([.!])", RegexOption.DOT_MATCHES_ALL),
        Regex("(?i)((?:the |a )?player (?:who |that ).+?wins? .+?)([.!])", RegexOption.DOT_MATCHES_ALL),
        Regex("(?i)(win condition:? .+?)([.!])", RegexOption.DOT_MATCHES_ALL),
        Regex("(?i)\\b((?:your |the )?goal is to .+?)([.!])", RegexOption.DOT_MATCHES_ALL),
        Regex("(?i)\\b((?:your |the )?objective is .+?)([.!])", RegexOption.DOT_MATCHES_ALL)
    )

    for (pattern in patterns) {
        val match = pattern.find(content)
        if (match != null) {
            val winConditionText = match.groupValues[1].trim() + match.groupValues[2]
            val prefix = content.substring(0, match.range.first).trim()
            val suffix = content.substring(match.range.last + 1).trim()
            val remainingContent = if (prefix.isNotEmpty() && suffix.isNotEmpty()) {
                "$prefix $suffix"
            } else {
                prefix + suffix
            }
            return remainingContent.trim() to winConditionText
        }
    }

    // No pattern found, return original content with null win condition
    return content to null
}
