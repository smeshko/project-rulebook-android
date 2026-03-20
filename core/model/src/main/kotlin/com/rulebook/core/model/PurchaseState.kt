package com.rulebook.core.model

/**
 * Represents the current state of a purchase operation.
 *
 * This sealed class tracks the lifecycle of an in-app purchase from
 * initiation through completion or failure.
 */
sealed class PurchaseState {
    /**
     * No purchase operation is in progress.
     */
    data object Idle : PurchaseState()

    /**
     * A purchase is currently being processed.
     *
     * @property sku The product identifier being purchased
     */
    data class Processing(val sku: String) : PurchaseState()

    /**
     * Purchase completed successfully.
     *
     * @property creditsAdded The number of credits added to the user's balance
     */
    data class Success(val creditsAdded: Int) : PurchaseState()

    /**
     * Purchase failed or was cancelled.
     *
     * @property message Human-readable error message
     */
    data class Error(val message: String) : PurchaseState()

    /**
     * Purchase is being validated by the server after Google Play returns success.
     *
     * @property sku The product identifier being validated
     */
    data class Validating(val sku: String) : PurchaseState()

    /**
     * Purchase is pending (e.g., waiting for payment confirmation).
     */
    data object Pending : PurchaseState()
}
