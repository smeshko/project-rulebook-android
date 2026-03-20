package com.rulebook.core.billing.history

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
     * Clears all purchase history records.
     */
    suspend fun clear()

    companion object {
        const val MAX_ENTRIES = 20
    }
}
