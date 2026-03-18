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
}
