package com.rulebook.core.billing.repository

import android.app.Activity
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Repository interface for Google Play billing operations.
 *
 * Manages product queries and purchase flows for in-app credit packs.
 * Server-side validation handles purchase acknowledgment.
 */
interface BillingRepository {

    /**
     * Flow of available in-app products (credit packs).
     *
     * Emits the list of products whenever they are loaded or updated.
     * Empty list indicates products haven't been loaded yet or are unavailable.
     */
    val products: Flow<List<ProductInfo>>

    /**
     * SharedFlow of purchase update events from PurchasesUpdatedListener.
     *
     * Emits whenever a purchase result is received from Google Play.
     * Used by the ViewModel to handle purchase completion, cancellation, or errors.
     */
    val purchaseUpdates: SharedFlow<PurchaseUpdate>

    /**
     * Queries Google Play for available product details.
     *
     * Fetches current pricing and product information from the Play Store.
     * Updates the [products] Flow with results.
     *
     * @return Result containing the list of products on success, or error on failure.
     */
    suspend fun queryProducts(): Result<List<ProductInfo>>

    /**
     * Launches the Google Play purchase flow for a specific product.
     *
     * Opens the Play Store billing sheet for the user to complete the purchase.
     * The purchase result is handled asynchronously via BillingClient callbacks.
     *
     * @param activity The activity context required to launch the billing flow.
     * @param productId The product identifier (SKU) to purchase.
     * @return Result indicating if the flow was launched successfully, or error.
     */
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit>

    /**
     * Queries for unacknowledged purchases that haven't been validated by the server yet.
     *
     * Used to restore purchases or handle interrupted purchase flows where
     * credits weren't awarded.
     *
     * @return Result containing list of unacknowledged purchase info, or error.
     */
    suspend fun queryUnacknowledgedPurchases(): Result<List<PurchaseInfo>>

    /**
     * Checks whether a pending purchase (Ask-to-Buy) has been resolved.
     *
     * Queries all in-app purchases to find a pending purchase and determine
     * if it has been approved (PURCHASED) or is still waiting (PENDING).
     *
     * @param pendingToken The purchase token that was previously stored as pending.
     * @return Result containing [PendingPurchaseResolution] indicating the current state.
     */
    suspend fun checkPendingPurchases(pendingToken: String): Result<PendingPurchaseResolution>

    /**
     * Returns the number of credits for a given product ID.
     *
     * @param productId The product SKU to look up.
     * @return The credit count for the product, or null if unknown.
     */
    fun creditsForProduct(productId: String): Int?
}

/**
 * Represents a completed but unconsumed purchase.
 *
 * @property purchaseToken The unique token identifying this purchase.
 * @property productId The product identifier (SKU) that was purchased.
 * @property orderId The Google Play order ID.
 */
data class PurchaseInfo(
    val purchaseToken: String,
    val productId: String,
    val orderId: String
)

/**
 * Represents a purchase with its current purchase state.
 *
 * Used by [BillingClientWrapper.queryAllPurchases] to return purchases
 * regardless of state (including PENDING state for Ask-to-Buy).
 *
 * @property purchaseToken The unique token identifying this purchase.
 * @property productId The product identifier (SKU) that was purchased.
 * @property orderId The Google Play order ID.
 * @property isPurchased True if the purchase state is PURCHASED (approved), false if PENDING.
 */
data class PurchaseInfoWithState(
    val purchaseToken: String,
    val productId: String,
    val orderId: String,
    val isPurchased: Boolean
)
