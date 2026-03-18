package com.rulebook.core.model

/**
 * Represents the resolution status of a pending purchase query.
 *
 * Used by [BillingRepository.checkPendingPurchases] to report the current
 * state of a purchase that was previously in PENDING state.
 */
sealed class PendingPurchaseResolution {

    /**
     * The purchase is still waiting for approval (still PENDING).
     */
    data object StillPending : PendingPurchaseResolution()

    /**
     * The purchase was approved and is now PURCHASED.
     *
     * @property token The purchase token to consume.
     * @property productId The product ID (SKU) that was purchased.
     */
    data class Purchased(val token: String, val productId: String) : PendingPurchaseResolution()

    /**
     * No matching purchase was found — likely cancelled by the approver.
     */
    data object NotFound : PendingPurchaseResolution()
}
