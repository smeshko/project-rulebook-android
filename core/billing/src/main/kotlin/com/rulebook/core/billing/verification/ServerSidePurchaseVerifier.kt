package com.rulebook.core.billing.verification

import com.rulebook.core.common.Result as AppResult
import com.rulebook.core.data.repository.ReceiptRepository
import com.rulebook.core.model.ValidationStatus

/**
 * Server-side implementation of [PurchaseVerifier].
 *
 * Sends the purchase token to the backend for receipt validation instead of
 * consuming locally via BillingClient. This is the Epic 10 implementation that
 * replaces [ClientSidePurchaseVerifier].
 *
 * Validation contract:
 * - [ValidationStatus.VALID]: success — return [VerificationResult] with credits
 * - [ValidationStatus.ALREADY_PROCESSED]: success (idempotent) — deliver credits if not yet delivered
 * - [ValidationStatus.INVALID]: failure — throw [PurchaseValidationException]
 * - Network/server error: failure — return the error message
 *
 * Note: Server handles acknowledgment/consumption. The client does NOT call
 * consumeAsync() or acknowledgePurchase() after this verifier succeeds.
 */
class ServerSidePurchaseVerifier(
    private val receiptRepository: ReceiptRepository
) : PurchaseVerifier {

    override suspend fun verifyAndConsume(
        purchaseToken: String,
        productId: String
    ): Result<VerificationResult> {
        val validationResult = receiptRepository.validatePurchase(purchaseToken, productId)

        return when (validationResult) {
            is AppResult.Success -> {
                when (validationResult.data.status) {
                    ValidationStatus.VALID,
                    ValidationStatus.ALREADY_PROCESSED -> {
                        Result.success(VerificationResult(credits = validationResult.data.creditsGranted))
                    }
                    ValidationStatus.INVALID -> {
                        Result.failure(PurchaseValidationException("Purchase validation failed"))
                    }
                }
            }
            is AppResult.Error -> {
                Result.failure(Exception(validationResult.message))
            }
        }
    }
}
