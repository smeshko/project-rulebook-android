package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateReceiptResponse(
    @SerialName("status") val status: String,
    @SerialName("credits_granted") val creditsGranted: Int? = null,
    @SerialName("message") val message: String? = null,
)
