package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model from the generate rules API endpoint.
 */
@Serializable
data class GenerateResponse(
    @SerialName("game_title") val gameTitle: String,
    @SerialName("rules_summary") val rulesSummary: String,
    @SerialName("rules_sections") val rulesSections: List<RulesSection> = emptyList(),
)

/**
 * A section of game rules.
 */
@Serializable
data class RulesSection(
    @SerialName("title") val title: String,
    @SerialName("content") val content: String,
)
