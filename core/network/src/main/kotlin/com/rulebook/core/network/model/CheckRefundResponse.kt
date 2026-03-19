package com.rulebook.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckRefundResponse(
    @SerialName("refund_statuses") val refundStatuses: List<RefundStatusDto>,
) {
    @Serializable
    data class RefundStatusDto(
        @SerialName("purchase_token") val purchaseToken: String,
        @SerialName("is_refunded") val isRefunded: Boolean,
    )
}
