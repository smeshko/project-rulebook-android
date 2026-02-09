package com.rulebook.core.network.mapper

import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import com.rulebook.core.network.model.GenerateResponse

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
    val sectionMap = rulesSections.associateBy { it.title.lowercase() }

    // Map sections by title with fallbacks, or use positional index
    val overview = sectionMap.findSection(
        keys = listOf("overview", "summary", "introduction"),
        fallbackIndex = 0,
        allSections = rulesSections
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
 */
private fun Map<String, com.rulebook.core.network.model.RulesSection>.findSection(
    keys: List<String>,
    fallbackIndex: Int,
    allSections: List<com.rulebook.core.network.model.RulesSection>
): RuleSection {
    // Try to find by any of the provided keys
    for (key in keys) {
        val section = this[key]
        if (section != null) {
            return RuleSection(
                title = section.title,
                content = section.content,
                items = null
            )
        }
    }

    // Fall back to positional index
    return if (fallbackIndex < allSections.size) {
        val section = allSections[fallbackIndex]
        RuleSection(
            title = section.title,
            content = section.content,
            items = null
        )
    } else {
        // Provide empty default if section is missing
        RuleSection(
            title = keys.first().replaceFirstChar { it.uppercase() },
            content = "",
            items = null
        )
    }
}
