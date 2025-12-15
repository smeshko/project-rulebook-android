package com.rulebook.core.model

/**
 * Domain model representing the complete rules for a board game.
 * Contains four sections: overview, setup, first round, and advanced.
 * Pure Kotlin data class with no Android dependencies.
 */
data class Rules(
    val gameId: String,
    val overview: RuleSection,
    val setup: RuleSection,
    val firstRound: RuleSection,
    val advanced: RuleSection
)
