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
}
