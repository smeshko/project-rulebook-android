package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request model for the analyze image API endpoint.
 */
@Serializable
data class AnalyzeRequest(
    @SerialName("image_data") val imageData: String,
    @SerialName("image_format") val imageFormat: String = "jpeg",
)
