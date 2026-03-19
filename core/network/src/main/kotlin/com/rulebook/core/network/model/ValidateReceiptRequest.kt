package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateReceiptRequest(
    @SerialName("purchase_token") val purchaseToken: String,
    @SerialName("product_id") val productId: String,
    @SerialName("package_name") val packageName: String,
)
