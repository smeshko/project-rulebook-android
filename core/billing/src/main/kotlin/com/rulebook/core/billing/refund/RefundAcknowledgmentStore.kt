package com.rulebook.core.billing.refund

/**
 * Persists acknowledged refund tokens to prevent duplicate credit revocations.
 *
 * When a refund is detected and credits are revoked, the purchase token is
 * stored here so that subsequent app launches do not revoke credits again.
 *
 * Used by [RefundSyncManager] in Story 10.5.
 */
interface RefundAcknowledgmentStore {

    /**
     * Returns true if the given purchase token has already been acknowledged as refunded.
     *
     * @param purchaseToken The Google Play purchase token to check.
     */
    suspend fun isAcknowledged(purchaseToken: String): Boolean

    /**
     * Marks the given purchase token as acknowledged (refund processed).
     *
     * @param purchaseToken The Google Play purchase token to acknowledge.
     */
    suspend fun acknowledge(purchaseToken: String)

    /**
     * Clears all acknowledged refund tokens. Used for testing only.
     */
    suspend fun clear()
}
