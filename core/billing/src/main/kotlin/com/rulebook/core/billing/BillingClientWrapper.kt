package com.rulebook.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal val SKU_CREDIT_MAP: Map<String, Int> = mapOf(
    "credits_1" to 1,
    "credits_3" to 3,
    "credits_10" to 10
)

/**
 * Public constants for Google Play Billing response codes.
 *
 * Mirrors [com.android.billingclient.api.BillingClient.BillingResponseCode] to allow
 * feature modules to handle purchase results without depending on the billing library directly.
 */
object BillingResponseCode {
    const val OK = 0
    const val USER_CANCELED = 1
    const val SERVICE_UNAVAILABLE = 2
    const val BILLING_UNAVAILABLE = 3
    const val ITEM_UNAVAILABLE = 4
    const val DEVELOPER_ERROR = 5
    const val ERROR = 6
    const val ITEM_ALREADY_OWNED = 7
    const val ITEM_NOT_OWNED = 8
}

internal val SKU_LIST: List<String> = SKU_CREDIT_MAP.keys.toList()

private const val MAX_RETRIES = 3
internal const val INITIAL_RETRY_DELAY_MS = 1_000L

/**
 * Retries the given [block] up to [maxAttempts] times with exponential backoff.
 *
 * Delays between retries: [initialDelayMs], [initialDelayMs]*2, [initialDelayMs]*4, ...
 * Throws the last exception if all attempts fail.
 */
internal suspend fun <T> retryWithExponentialBackoff(
    maxAttempts: Int = MAX_RETRIES,
    initialDelayMs: Long = INITIAL_RETRY_DELAY_MS,
    block: suspend (attempt: Int) -> T
): T {
    var lastException: Exception? = null
    var currentDelay = initialDelayMs
    repeat(maxAttempts) { attempt ->
        try {
            return block(attempt)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxAttempts - 1) {
                delay(currentDelay)
                currentDelay *= 2
            }
        }
    }
    throw lastException ?: Exception("Operation failed after $maxAttempts attempts")
}

/**
 * Represents a purchase result received from the PurchasesUpdatedListener.
 *
 * @property responseCode The BillingClient response code.
 * @property purchaseTokens List of purchase tokens from the update.
 * @property productIds List of product IDs (SKUs) from the update.
 */
data class PurchaseUpdate(
    val responseCode: Int,
    val purchaseTokens: List<String>,
    val productIds: List<String> = emptyList()
)

/**
 * Abstraction over Google Play BillingClient for testability.
 *
 * Uses domain types to avoid Android billing types leaking into callers.
 * The real implementation delegates to BillingClient; fakes can be used in tests.
 */
interface BillingClientWrapper {
    val connectionState: StateFlow<Boolean>
    val purchaseUpdates: SharedFlow<PurchaseUpdate>

    suspend fun ensureConnected(): Result<Unit>
    suspend fun queryProducts(): Result<List<ProductInfo>>
    fun launchBillingFlow(activity: Activity, productId: String): Result<Unit>
    suspend fun consumePurchase(purchaseToken: String): Result<Unit>
    suspend fun queryPurchases(): Result<List<PurchaseInfo>>
    fun disconnect()
}

/**
 * Real Android implementation of [BillingClientWrapper].
 *
 * Wraps Google Play Billing Library 7.x with coroutine-based APIs (via billing-ktx).
 * Connection lifecycle is Application-scoped via Koin singleton injection.
 */
class BillingClientWrapperImpl(context: Context) : BillingClientWrapper {

    private val _connectionState = MutableStateFlow(false)
    override val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(
        replay = 0,
        extraBufferCapacity = 10
    )
    override val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    // In-memory cache for ProductDetails — used by launchBillingFlow.
    // Not persisted; re-queried when empty to avoid stale objects.
    private val cachedProductDetails = mutableMapOf<String, com.android.billingclient.api.ProductDetails>()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        val tokens = purchases?.map { it.purchaseToken } ?: emptyList()
        val productIds = purchases?.flatMap { it.products } ?: emptyList()
        _purchaseUpdates.tryEmit(PurchaseUpdate(billingResult.responseCode, tokens, productIds))
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    override suspend fun ensureConnected(): Result<Unit> {
        if (billingClient.isReady) {
            _connectionState.value = true
            return Result.success(Unit)
        }
        return try {
            retryWithExponentialBackoff(maxAttempts = MAX_RETRIES) {
                connectToBillingService()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun connectToBillingService(): Unit = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            billingClient.endConnection()
            _connectionState.value = false
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val connected = billingResult.responseCode == BillingClient.BillingResponseCode.OK
                _connectionState.value = connected
                if (continuation.isActive) {
                    if (connected) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            Exception(
                                "Billing setup failed: ${billingResult.debugMessage} " +
                                    "(code: ${billingResult.responseCode})"
                            )
                        )
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                _connectionState.value = false
                if (continuation.isActive) {
                    continuation.resumeWithException(
                        Exception("Billing service disconnected during setup")
                    )
                }
            }
        })
    }

    private fun updateConnectionStateOnError(responseCode: Int) {
        if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ||
            responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
        ) {
            _connectionState.value = false
        }
    }

    override suspend fun queryProducts(): Result<List<ProductInfo>> {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                SKU_LIST.map { sku ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(sku)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            )
            .build()

        return try {
            val result = billingClient.queryProductDetails(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val details = result.productDetailsList ?: emptyList()
                cachedProductDetails.clear()
                details.forEach { cachedProductDetails[it.productId] = it }

                val productInfoList = details.mapNotNull { d ->
                    val credits = SKU_CREDIT_MAP[d.productId] ?: return@mapNotNull null
                    ProductInfo(
                        productId = d.productId,
                        title = d.title,
                        price = d.oneTimePurchaseOfferDetails?.formattedPrice ?: "",
                        credits = credits
                    )
                }
                Result.success(productInfoList)
            } else {
                updateConnectionStateOnError(result.billingResult.responseCode)
                Result.failure(
                    Exception("Query products failed: ${result.billingResult.debugMessage}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun launchBillingFlow(activity: Activity, productId: String): Result<Unit> {
        val productDetails = cachedProductDetails[productId]
            ?: return Result.failure(
                Exception("Product $productId not found. Call queryProducts() first.")
            )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            )
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, flowParams)
        return if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Result.success(Unit)
        } else {
            Result.failure(
                Exception("Launch billing flow failed: ${billingResult.debugMessage}")
            )
        }
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()

        return try {
            val result = billingClient.consumePurchase(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Result.success(Unit)
            } else {
                updateConnectionStateOnError(result.billingResult.responseCode)
                Result.failure(
                    Exception("Consume purchase failed: ${result.billingResult.debugMessage}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun queryPurchases(): Result<List<PurchaseInfo>> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        return try {
            val result = billingClient.queryPurchasesAsync(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val purchases = result.purchasesList
                    .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    .filter { it.products.isNotEmpty() }
                    .map { purchase ->
                        PurchaseInfo(
                            purchaseToken = purchase.purchaseToken,
                            productId = purchase.products.first(),
                            orderId = purchase.orderId ?: ""
                        )
                    }
                Result.success(purchases)
            } else {
                updateConnectionStateOnError(result.billingResult.responseCode)
                Result.failure(
                    Exception("Query purchases failed: ${result.billingResult.debugMessage}")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun disconnect() {
        billingClient.endConnection()
        _connectionState.value = false
    }
}
