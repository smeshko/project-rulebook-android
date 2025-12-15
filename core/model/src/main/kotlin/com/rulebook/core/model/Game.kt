package com.rulebook.core.model

/**
 * Domain model representing a board game in the user's library.
 * Pure Kotlin data class with no Android dependencies.
 */
data class Game(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val createdAt: Long,
    val lastAccessedAt: Long
)
