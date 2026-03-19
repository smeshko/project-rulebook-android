package com.rulebook.core.model

data class RefundStatus(
    val purchaseToken: String,
    val isRefunded: Boolean,
)
