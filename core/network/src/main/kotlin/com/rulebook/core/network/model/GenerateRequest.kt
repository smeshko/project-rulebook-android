package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for the generate rules API endpoint.
 */
@Serializable
data class GenerateRequest(
    @SerialName("game_title") val gameTitle: String,
    @SerialName("game_id") val gameId: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)
