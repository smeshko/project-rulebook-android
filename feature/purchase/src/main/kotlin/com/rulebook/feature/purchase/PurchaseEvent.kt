package com.rulebook.feature.purchase

/**
 * One-time events emitted by PurchaseViewModel for the UI to handle.
 */
sealed class PurchaseEvent {
    /**
     * User dismissed the paywall.
     */
    data object Dismiss : PurchaseEvent()

    /**
     * Purchase completed successfully.
     *
     * @property creditsAdded The number of credits added to the user's balance.
     */
    data class PurchaseSuccess(val creditsAdded: Int) : PurchaseEvent()

    /**
     * Restore purchases completed with credits delivered.
     *
     * @property creditsRestored The total number of credits restored across all purchases.
     */
    data class RestoreSuccess(val creditsRestored: Int) : PurchaseEvent()

    /**
     * Restore purchases completed but no unconsumed purchases were found.
     */
    data object RestoreNoPurchases : PurchaseEvent()

    /**
     * Restore purchases failed with an error.
     *
     * @property message The error message to display to the user.
     */
    data class RestoreError(val message: String) : PurchaseEvent()

    /**
     * A pending purchase (Ask-to-Buy) was approved and credits were delivered.
     *
     * @property creditsAdded The number of credits added to the user's balance.
     * @property productId The product ID that was approved.
     */
    data class PendingPurchaseResolved(val creditsAdded: Int, val productId: String) : PurchaseEvent()

    /**
     * Server validation failed after all retry attempts due to a transient network error.
     *
     * The purchase token has been saved to [PendingValidationStore] for recovery on the next
     * app launch (Story 10.4). Credits are NOT granted until validation succeeds.
     */
    data object ValidationPending : PurchaseEvent()
}
