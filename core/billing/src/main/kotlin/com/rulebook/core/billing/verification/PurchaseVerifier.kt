package com.rulebook.core.billing.verification

/**
 * The validation status returned by the server during purchase verification.
 */
enum class VerificationStatus {
    /** Purchase is valid and credits should be delivered. */
    VALID,
    /** Purchase was already processed server-side (idempotent retry). */
    ALREADY_PROCESSED
}

/**
 * Result of a successful purchase verification and consumption.
 *
 * @property credits The number of credits to deliver to the user.
 * @property status The server validation status — used to distinguish first-time
 *                  validation from idempotent retries.
 */
data class VerificationResult(
    val credits: Int,
    val status: VerificationStatus = VerificationStatus.VALID
)

/**
 * Interface for verifying and consuming a purchase before delivering credits.
 *
 * The implementation is swappable:
 * - [ClientSidePurchaseVerifier]: Interim client-side consume via BillingClient (Story 8.7)
 * - ServerSidePurchaseVerifier: Server-side receipt validation (Epic 10)
 *
 * Contract: consume FIRST, then return credits only on success.
 * If consume fails, return [Result.failure] — credits must NOT be delivered.
 */
interface PurchaseVerifier {
    /**
     * Verifies the purchase and consumes it, returning the credits to deliver.
     *
     * Implementations must:
     * 1. Consume the purchase token via the billing system
     * 2. Only on successful consumption, resolve the credits for the given [productId]
     * 3. Return [Result.failure] if consumption fails or the product is unknown
     *
     * @param purchaseToken The purchase token from the completed purchase.
     * @param productId The product identifier (SKU) to resolve credit count from.
     * @return [Result.success] with [VerificationResult] on success,
     *         or [Result.failure] if consumption fails or product is unknown.
     */
    suspend fun verifyAndConsume(purchaseToken: String, productId: String): Result<VerificationResult>
}
