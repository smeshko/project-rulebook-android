package com.rulebook.core.data.repository

import com.rulebook.core.common.Result
import com.rulebook.core.common.safeCall
import com.rulebook.core.data.util.NetworkErrorMapper
import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult
import com.rulebook.core.network.api.ReceiptValidationApi
import com.rulebook.core.network.mapper.toDomain
import com.rulebook.core.network.model.CheckRefundRequest
import com.rulebook.core.network.model.ValidateReceiptRequest

class ReceiptRepositoryImpl(
    private val api: ReceiptValidationApi,
    private val packageName: String,
) : ReceiptRepository {

    override suspend fun validatePurchase(
        purchaseToken: String,
        productId: String,
    ): Result<ValidationResult> =
        safeCall {
            val request = ValidateReceiptRequest(
                purchaseToken = purchaseToken,
                productId = productId,
                packageName = packageName,
            )
            val response = api.validateReceipt(request)
            response.toDomain()
        }.let { result ->
            when (result) {
                is Result.Success -> result
                is Result.Error -> Result.Error(
                    message = NetworkErrorMapper.mapToUserMessage(result.cause),
                    cause = result.cause,
                )
            }
        }

    override suspend fun checkRefundStatus(
        purchaseTokens: List<String>,
    ): Result<List<RefundStatus>> =
        safeCall {
            val request = CheckRefundRequest(purchaseTokens = purchaseTokens)
            val response = api.checkRefundStatus(request)
            response.toDomain()
        }.let { result ->
            when (result) {
                is Result.Success -> result
                is Result.Error -> Result.Error(
                    message = NetworkErrorMapper.mapToUserMessage(result.cause),
                    cause = result.cause,
                )
            }
        }
}
