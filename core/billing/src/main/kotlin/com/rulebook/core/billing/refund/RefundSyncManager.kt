package com.rulebook.core.billing.refund

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.common.getOrElse
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.ReceiptRepository

/**
 * Contract for running app-launch refund detection and credit revocation.
 *
 * Allows [RefundSyncManager] to be swapped with a test double in unit tests.
 */
interface RefundSync {
    /**
     * Runs the full refund check flow and returns a summary of the outcome.
     */
    suspend fun checkRefunds(): RefundSyncResult
}

/**
 * Summary of a [RefundSyncManager.checkRefunds] run.
 *
 * @property creditsRevoked Total credits revoked from the user during this sync.
 * @property tokensRefunded Number of purchase tokens identified as refunded.
 */
data class RefundSyncResult(
    val creditsRevoked: Int,
    val tokensRefunded: Int,
)

/**
 * Orchestrates app-launch refund detection and credit revocation.
 *
 * On each app launch, checks recent purchase tokens against the backend's
 * refund status endpoint. If any tokens are marked refunded, credits are
 * revoked and the tokens are acknowledged locally to prevent re-processing.
 *
 * This is throttled to once per app launch (runs in [StartupViewModel.init],
 * which fires once per ViewModel lifecycle — not on every resume).
 *
 * Silent on failure — network errors are swallowed and retried on the next launch.
 *
 * @param purchaseHistoryStore Local history of purchase token+productId pairs.
 * @param receiptRepository Repository for calling the backend refund status endpoint.
 * @param creditRepository Repository for revoking credits.
 * @param refundAcknowledgmentStore Store for tracking processed refund tokens.
 * @param billingRepository Repository for looking up credits per product.
 * @param analyticsManager Analytics for tracking refund detection outcomes.
 */
class RefundSyncManager(
    private val purchaseHistoryStore: PurchaseHistoryStore,
    private val receiptRepository: ReceiptRepository,
    private val creditRepository: CreditRepository,
    private val refundAcknowledgmentStore: RefundAcknowledgmentStore,
    private val billingRepository: BillingRepository,
    private val analyticsManager: AnalyticsManager,
) : RefundSync {

    /**
     * Runs the full refund check flow:
     * 1. Reads recent purchase entries (token + productId) from local history.
     * 2. Filters out already-acknowledged tokens.
     * 3. Calls the backend refund status endpoint with unacknowledged tokens.
     * 4. For each refunded token: looks up credits, revokes them, acknowledges the token.
     * 5. Fires analytics with the result.
     *
     * @return [RefundSyncResult] with credits revoked and token counts.
     */
    override suspend fun checkRefunds(): RefundSyncResult {
        // Step 1: Read recent purchase entries
        val entries = purchaseHistoryStore.getRecentEntries()
        if (entries.isEmpty()) return RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0)

        // Step 2: Filter out already-acknowledged tokens
        val unacknowledged = entries.filter { !refundAcknowledgmentStore.isAcknowledged(it.purchaseToken) }
        if (unacknowledged.isEmpty()) return RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0)

        // Step 3: Call backend with unacknowledged tokens
        val tokens = unacknowledged.map { it.purchaseToken }
        val refundStatuses = receiptRepository.checkRefundStatus(tokens)
            .getOrElse { _ -> return RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0) }

        // Build a lookup map from token to productId for credit revocation
        val tokenToProductId = unacknowledged.associate { it.purchaseToken to it.productId }

        // Step 4: Process refunded tokens
        var totalCreditsRevoked = 0
        var tokensRefunded = 0

        for (status in refundStatuses) {
            if (!status.isRefunded) continue

            val productId = tokenToProductId[status.purchaseToken] ?: continue
            val creditsToRevoke = billingRepository.creditsForProduct(productId) ?: continue

            val actualRevoked = creditRepository.removeCredits(creditsToRevoke)
            refundAcknowledgmentStore.acknowledge(status.purchaseToken)

            totalCreditsRevoked += actualRevoked
            tokensRefunded++
        }

        // Step 5: Fire analytics
        if (tokensRefunded > 0) {
            analyticsManager.trackPurchaseRefundDetected(
                tokensRefunded = tokensRefunded,
                creditsRevoked = totalCreditsRevoked,
            )
        }

        return RefundSyncResult(creditsRevoked = totalCreditsRevoked, tokensRefunded = tokensRefunded)
    }
}
