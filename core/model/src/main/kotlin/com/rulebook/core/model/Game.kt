package com.rulebook.core.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing a board game in the user's library.
 */
@Serializable
data class Game(
    val id: String,
    val title: String,
    val imageUrl: String? = null,
    val confidence: Float = 0f,
    val createdAt: Long = System.currentTimeMillis(),
)
