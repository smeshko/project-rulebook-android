package com.rulebook.core.billing.history

/**
 * Represents a single purchase history record with both the purchase token and product ID.
 *
 * Used by Story 10.5 (Refund Sync) to look up the credit count for each token.
 *
 * @property purchaseToken The Google Play purchase token.
 * @property productId The product identifier (SKU) of the purchased item.
 */
data class PurchaseHistoryEntry(
    val purchaseToken: String,
    val productId: String,
)

/**
 * Stores a local history of completed purchase tokens for refund tracking.
 *
 * This store is used by Story 10.2 to persist validated purchases and will be
 * consumed by Story 10.5 (Refund Sync) to detect refunded purchases.
 *
 * Keeps at most [MAX_ENTRIES] recent purchase records.
 */
interface PurchaseHistoryStore {

    /**
     * Saves a validated purchase to the history store.
     *
     * @param purchaseToken The Google Play purchase token.
     * @param productId The product identifier (SKU) of the purchased item.
     */
    suspend fun savePurchase(purchaseToken: String, productId: String)

    /**
     * Returns the list of recent purchase tokens, newest first.
     * Returns at most [MAX_ENTRIES] tokens.
     */
    suspend fun getRecentTokens(): List<String>

    /**
     * Returns the list of recent purchase entries (token + productId), newest first.
     * Returns at most [MAX_ENTRIES] entries.
     *
     * Used by Story 10.5 (Refund Sync) to look up credits per product during revocation.
     */
    suspend fun getRecentEntries(): List<PurchaseHistoryEntry>

    /**
     * Clears all purchase history records.
     */
    suspend fun clear()

    companion object {
        const val MAX_ENTRIES = 20
    }
}
