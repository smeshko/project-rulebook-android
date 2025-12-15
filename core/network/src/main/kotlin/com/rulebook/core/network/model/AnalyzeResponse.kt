package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model from the analyze image API endpoint.
 */
@Serializable
data class AnalyzeResponse(
    @SerialName("game_title") val gameTitle: String,
    val confidence: Float,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)
