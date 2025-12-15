package com.rulebook.core.database.mapper

import com.rulebook.core.database.entity.RulesEntity
import com.rulebook.core.model.RuleSection
import com.rulebook.core.model.Rules
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Internal JSON serializer for RuleSection.
 */
private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

/**
 * Serializable data class for RuleSection storage.
 * Used for JSON serialization to/from database.
 */
@kotlinx.serialization.Serializable
private data class RuleSectionDto(
    val title: String,
    val content: String,
    val items: List<String>? = null
)

private fun RuleSection.toDto(): RuleSectionDto = RuleSectionDto(
    title = title,
    content = content,
    items = items
)

private fun RuleSectionDto.toDomain(): RuleSection = RuleSection(
    title = title,
    content = content,
    items = items
)

private fun String.toRuleSection(): RuleSection {
    return try {
        json.decodeFromString<RuleSectionDto>(this).toDomain()
    } catch (e: Exception) {
        // Fallback for plain text content (backwards compatibility)
        RuleSection(title = "", content = this, items = null)
    }
}

private fun RuleSection.toJsonString(): String {
    return json.encodeToString(this.toDto())
}

/**
 * Extension function to convert a RulesEntity to a Rules domain model.
 * Parses JSON-stored sections into RuleSection objects.
 */
fun RulesEntity.toDomain(): Rules = Rules(
    gameId = gameId,
    overview = overview.toRuleSection(),
    setup = setup.toRuleSection(),
    firstRound = firstRound.toRuleSection(),
    advanced = advanced.toRuleSection()
)

/**
 * Extension function to convert a Rules domain model to a RulesEntity.
 * Serializes RuleSection objects to JSON strings for storage.
 *
 * @param id The unique identifier for this rules entity
 * @param rawJson The original raw JSON response from the API (for reference/debugging)
 */
fun Rules.toEntity(id: String, rawJson: String = ""): RulesEntity = RulesEntity(
    id = id,
    gameId = gameId,
    overview = overview.toJsonString(),
    setup = setup.toJsonString(),
    firstRound = firstRound.toJsonString(),
    advanced = advanced.toJsonString(),
    rawJson = rawJson
)
