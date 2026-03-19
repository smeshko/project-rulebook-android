package com.rulebook.core.network.mapper

import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult
import com.rulebook.core.model.ValidationStatus
import com.rulebook.core.network.model.CheckRefundResponse
import com.rulebook.core.network.model.ValidateReceiptResponse

fun ValidateReceiptResponse.toDomain(): ValidationResult = ValidationResult(
    status = when (status) {
        "valid" -> ValidationStatus.VALID
        "invalid" -> ValidationStatus.INVALID
        "already_processed" -> ValidationStatus.ALREADY_PROCESSED
        else -> ValidationStatus.INVALID
    },
    creditsGranted = creditsGranted ?: 0,
)

fun CheckRefundResponse.RefundStatusDto.toDomain(): RefundStatus = RefundStatus(
    purchaseToken = purchaseToken,
    isRefunded = isRefunded,
)

fun CheckRefundResponse.toDomain(): List<RefundStatus> = refundStatuses.map { it.toDomain() }
