package com.rulebook.core.billing.verification

import com.rulebook.core.billing.repository.BillingRepository

/**
 * Client-side implementation of [PurchaseVerifier].
 *
 * Interim solution for Story 8.7 — consumes the purchase via [BillingRepository]
 * and resolves credit count from the SKU map. Epic 10 will replace this with
 * a server-side verifier that sends the receipt to a backend.
 *
 * Enforce consume-before-deliver contract:
 * - Consumes purchase FIRST
 * - Only resolves credits if consumption succeeds
 * - Returns [Result.failure] on consume failure or unknown product ID
 */
class ClientSidePurchaseVerifier(
    private val billingRepository: BillingRepository
) : PurchaseVerifier {

    override suspend fun verifyAndConsume(
        purchaseToken: String,
        productId: String
    ): Result<VerificationResult> {
        // Step 1: Consume FIRST — prevents double-deliver on retry
        val consumeResult = billingRepository.consumePurchase(purchaseToken)
        if (consumeResult.isFailure) {
            return Result.failure(
                consumeResult.exceptionOrNull()
                    ?: RuntimeException("Consume failed for token: $purchaseToken")
            )
        }

        // Step 2: Resolve credits for product — only after successful consume
        val credits = billingRepository.creditsForProduct(productId)
            ?: return Result.failure(IllegalArgumentException("Unknown product ID: $productId"))

        return Result.success(VerificationResult(credits = credits))
    }
}
