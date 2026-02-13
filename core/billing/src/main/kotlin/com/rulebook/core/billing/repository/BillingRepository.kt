package com.rulebook.core.billing.repository

import android.app.Activity
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Google Play billing operations.
 *
 * Manages product queries, purchase flows, and purchase consumption
 * for in-app credit packs.
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
     * Consumes a completed purchase by its token.
     *
     * Marks an in-app product as consumed, allowing it to be purchased again.
     * Must be called after awarding credits to the user.
     *
     * @param purchaseToken The purchase token from the completed purchase.
     * @return Result indicating success or error.
     */
    suspend fun consumePurchase(purchaseToken: String): Result<Unit>

    /**
     * Queries for unconsumed purchases that haven't been consumed yet.
     *
     * Used to restore purchases or handle interrupted purchase flows where
     * credits weren't awarded.
     *
     * @return Result containing list of unconsumed purchase info, or error.
     */
    suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>>
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
