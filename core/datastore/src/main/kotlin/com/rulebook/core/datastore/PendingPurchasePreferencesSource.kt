package com.rulebook.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for pending purchase preferences data source.
 *
 * Allows PurchaseViewModel and PendingPurchaseChecker to be tested without
 * a real DataStore dependency. [RulebookPreferences] implements this interface
 * in production.
 */
interface PendingPurchasePreferencesSource {

    /**
     * Flow of the pending purchase token, or null if no purchase is pending.
     */
    val pendingPurchaseToken: Flow<String?>

    /**
     * Flow of the pending purchase product ID, or null if no purchase is pending.
     */
    val pendingPurchaseProductId: Flow<String?>

    /**
     * Stores the pending purchase token and product ID atomically.
     *
     * @param token The purchase token for the pending purchase.
     * @param productId The product ID (SKU) for the pending purchase.
     */
    suspend fun setPendingPurchase(token: String, productId: String)

    /**
     * Clears the pending purchase token and product ID atomically.
     *
     * Called after a pending purchase is resolved (approved or cancelled).
     */
    suspend fun clearPendingPurchase()
}
