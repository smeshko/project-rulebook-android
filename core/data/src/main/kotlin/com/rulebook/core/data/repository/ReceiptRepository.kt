package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult

interface ReceiptRepository {
    suspend fun validatePurchase(purchaseToken: String, productId: String): Result<ValidationResult>
    suspend fun checkRefundStatus(purchaseTokens: List<String>): Result<List<RefundStatus>>
}
