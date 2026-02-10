package com.rulebook.core.model

/**
 * Domain model representing a section of game rules.
 * Used for overview, setup, first round, and advanced sections.
 * Pure Kotlin data class with no Android dependencies.
 */
data class RuleSection(
    val title: String,
    val content: String,
    val items: List<String>? = null,
    val winCondition: String? = null
)
