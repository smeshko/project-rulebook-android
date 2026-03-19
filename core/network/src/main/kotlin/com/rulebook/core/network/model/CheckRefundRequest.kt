package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckRefundRequest(
    @SerialName("purchase_tokens") val purchaseTokens: List<String>,
)
