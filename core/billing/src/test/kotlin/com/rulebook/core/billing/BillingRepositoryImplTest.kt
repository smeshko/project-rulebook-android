package com.rulebook.core.billing

import android.app.Activity
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.billing.repository.PurchaseInfoWithState
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.test.assertIs

class BillingRepositoryImplTest {

    private lateinit var fakeWrapper: FakeBillingClientWrapper
    private lateinit var repository: BillingRepositoryImpl

    @Before
    fun setup() {
        fakeWrapper = FakeBillingClientWrapper()
        repository = BillingRepositoryImpl(fakeWrapper)
    }

    // ==================== products Flow Tests ====================

    @Test
    fun `products flow emits empty list initially`() = runTest {
        val products = repository.products.first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun `products Flow updates after successful queryProducts call`() = runTest {
        val productInfoList = listOf(
            ProductInfo("credits_1", "1 Credit", "$0.99", 1),
            ProductInfo("credits_3", "3 Credits", "$1.99", 3)
        )
        fakeWrapper.productsToReturn = Result.success(productInfoList)

        repository.queryProducts()

        val products = repository.products.first()
        assertEquals(productInfoList, products)
    }

    // ==================== queryProducts Tests ====================

    @Test
    fun `queryProducts returns mapped ProductInfo list from wrapper`() = runTest {
        val productInfoList = listOf(
            ProductInfo("credits_1", "1 Credit", "$0.99", 1),
            ProductInfo("credits_3", "3 Credits", "$1.99", 3),
            ProductInfo("credits_10", "10 Credits", "$4.99", 10)
        )
        fakeWrapper.productsToReturn = Result.success(productInfoList)

        val result = repository.queryProducts()

        assertTrue(result.isSuccess)
        assertEquals(productInfoList, result.getOrNull())
    }

    @Test
    fun `queryProducts returns error Result on wrapper failure`() = runTest {
        fakeWrapper.productsToReturn = Result.failure(Exception("Query failed"))

        val result = repository.queryProducts()

        assertTrue(result.isFailure)
        assertEquals("Query failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `queryProducts returns failure when connection fails`() = runTest {
        fakeWrapper.connectionResult = Result.failure(Exception("Connection failed"))

        val result = repository.queryProducts()

        assertTrue(result.isFailure)
        assertEquals("Connection failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `queryProducts does not update products flow on failure`() = runTest {
        fakeWrapper.productsToReturn = Result.failure(Exception("Error"))

        repository.queryProducts()

        val products = repository.products.first()
        assertTrue(products.isEmpty())
    }

    // ==================== consumePurchase Tests ====================

    @Test
    fun `consumePurchase returns success when wrapper succeeds`() = runTest {
        fakeWrapper.consumeResult = Result.success(Unit)

        val result = repository.consumePurchase("test_token")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `consumePurchase returns failure when wrapper fails`() = runTest {
        fakeWrapper.consumeResult = Result.failure(Exception("Consume failed"))

        val result = repository.consumePurchase("test_token")

        assertTrue(result.isFailure)
        assertEquals("Consume failed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `consumePurchase delegates token to wrapper`() = runTest {
        fakeWrapper.consumeResult = Result.success(Unit)

        repository.consumePurchase("specific_purchase_token")

        assertEquals("specific_purchase_token", fakeWrapper.lastConsumedToken)
    }

    // ==================== queryUnconsumedPurchases Tests ====================

    @Test
    fun `queryUnconsumedPurchases returns purchase list from wrapper`() = runTest {
        val purchases = listOf(
            PurchaseInfo("token_1", "credits_1", "order_1"),
            PurchaseInfo("token_2", "credits_3", "order_2")
        )
        fakeWrapper.purchasesResult = Result.success(purchases)

        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isSuccess)
        assertEquals(purchases, result.getOrNull())
    }

    @Test
    fun `queryUnconsumedPurchases returns empty list when no purchases`() = runTest {
        fakeWrapper.purchasesResult = Result.success(emptyList())

        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PurchaseInfo>(), result.getOrNull())
    }

    @Test
    fun `queryUnconsumedPurchases returns failure when wrapper fails`() = runTest {
        fakeWrapper.purchasesResult = Result.failure(Exception("Query failed"))

        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isFailure)
    }

    @Test
    fun `queryUnconsumedPurchases returns failure when connection fails`() = runTest {
        fakeWrapper.connectionResult = Result.failure(Exception("No connection"))

        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isFailure)
        assertEquals("No connection", result.exceptionOrNull()?.message)
    }

    // ==================== checkPendingPurchases Tests ====================

    @Test
    fun `checkPendingPurchases returns Purchased when token found with isPurchased true`() = runTest {
        fakeWrapper.allPurchasesResult = Result.success(
            listOf(PurchaseInfoWithState("pending-token", "credits_3", "order-1", isPurchased = true))
        )

        val result = repository.checkPendingPurchases("pending-token")

        assertTrue(result.isSuccess)
        val resolution = result.getOrThrow()
        assertIs<PendingPurchaseResolution.Purchased>(resolution)
        assertEquals("pending-token", resolution.token)
        assertEquals("credits_3", resolution.productId)
    }

    @Test
    fun `checkPendingPurchases returns StillPending when token found with isPurchased false`() = runTest {
        fakeWrapper.allPurchasesResult = Result.success(
            listOf(PurchaseInfoWithState("pending-token", "credits_3", "order-1", isPurchased = false))
        )

        val result = repository.checkPendingPurchases("pending-token")

        assertTrue(result.isSuccess)
        assertIs<PendingPurchaseResolution.StillPending>(result.getOrThrow())
    }

    @Test
    fun `checkPendingPurchases returns NotFound when token not in purchase list`() = runTest {
        fakeWrapper.allPurchasesResult = Result.success(emptyList())

        val result = repository.checkPendingPurchases("missing-token")

        assertTrue(result.isSuccess)
        assertIs<PendingPurchaseResolution.NotFound>(result.getOrThrow())
    }

    @Test
    fun `checkPendingPurchases returns failure when connection fails`() = runTest {
        fakeWrapper.connectionResult = Result.failure(Exception("No connection"))

        val result = repository.checkPendingPurchases("pending-token")

        assertTrue(result.isFailure)
        assertEquals("No connection", result.exceptionOrNull()?.message)
    }

    @Test
    fun `checkPendingPurchases returns failure when wrapper queryAllPurchases fails`() = runTest {
        fakeWrapper.allPurchasesResult = Result.failure(Exception("Query failed"))

        val result = repository.checkPendingPurchases("pending-token")

        assertTrue(result.isFailure)
    }

    // ==================== Retry-on-Disconnect Tests ====================

    @Test
    fun `consumePurchase retries when wrapper fails and connection is lost`() = runTest {
        var callCount = 0
        fakeWrapper = object : FakeBillingClientWrapper() {
            override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
                callCount++
                return if (callCount == 1) {
                    // Simulate disconnect mid-operation
                    setConnectionState(false)
                    Result.failure(Exception("Disconnected"))
                } else {
                    Result.success(Unit)
                }
            }
        }
        repository = BillingRepositoryImpl(fakeWrapper)

        val result = repository.consumePurchase("token")

        assertTrue(result.isSuccess)
        assertEquals(2, callCount)
    }

    @Test
    fun `queryUnconsumedPurchases retries when wrapper fails and connection is lost`() = runTest {
        val purchases = listOf(PurchaseInfo("t1", "credits_1", "o1"))
        var callCount = 0
        fakeWrapper = object : FakeBillingClientWrapper() {
            override suspend fun queryPurchases(): Result<List<PurchaseInfo>> {
                callCount++
                return if (callCount == 1) {
                    setConnectionState(false)
                    Result.failure(Exception("Disconnected"))
                } else {
                    Result.success(purchases)
                }
            }
        }
        repository = BillingRepositoryImpl(fakeWrapper)

        val result = repository.queryUnconsumedPurchases()

        assertTrue(result.isSuccess)
        assertEquals(purchases, result.getOrNull())
        assertEquals(2, callCount)
    }
}

/**
 * Fake implementation of [BillingClientWrapper] for unit testing [BillingRepositoryImpl].
 *
 * All return values are configurable per test. Follows the Fake pattern from project conventions.
 */
open class FakeBillingClientWrapper : BillingClientWrapper {

    var connectionResult: Result<Unit> = Result.success(Unit)
    var productsToReturn: Result<List<ProductInfo>> = Result.success(emptyList())
    var consumeResult: Result<Unit> = Result.success(Unit)
    var purchasesResult: Result<List<PurchaseInfo>> = Result.success(emptyList())
    var allPurchasesResult: Result<List<PurchaseInfoWithState>> = Result.success(emptyList())

    var lastConsumedToken: String? = null

    private val _connectionState = MutableStateFlow(true)
    override val connectionState: StateFlow<Boolean> = _connectionState

    fun setConnectionState(connected: Boolean) {
        _connectionState.value = connected
    }

    override val purchaseUpdates: SharedFlow<PurchaseUpdate> = MutableSharedFlow()

    override suspend fun ensureConnected(): Result<Unit> = connectionResult

    override suspend fun queryProducts(): Result<List<ProductInfo>> = productsToReturn

    override fun launchBillingFlow(activity: Activity, productId: String): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        lastConsumedToken = purchaseToken
        return consumeResult
    }

    override suspend fun queryPurchases(): Result<List<PurchaseInfo>> = purchasesResult

    override suspend fun queryAllPurchases(): Result<List<PurchaseInfoWithState>> = allPurchasesResult

    override fun disconnect() {
        _connectionState.value = false
    }
}

// ==================== retryWithExponentialBackoff Tests ====================

class RetryWithExponentialBackoffTest {

    @Test
    fun `retryWithExponentialBackoff returns result on first success`() = runTest {
        var attempts = 0
        val result = retryWithExponentialBackoff(maxAttempts = 3, initialDelayMs = 0) { _ ->
            attempts++
            "success"
        }
        assertEquals("success", result)
        assertEquals(1, attempts)
    }

    @Test
    fun `retryWithExponentialBackoff retries on failure and succeeds on third attempt`() = runTest {
        var attempts = 0
        val result = retryWithExponentialBackoff(maxAttempts = 3, initialDelayMs = 0) { _ ->
            attempts++
            if (attempts < 3) throw Exception("fail")
            "success"
        }
        assertEquals("success", result)
        assertEquals(3, attempts)
    }

    @Test
    fun `retryWithExponentialBackoff throws last exception after max attempts`() = runTest {
        var attempts = 0
        var thrownException: Exception? = null
        try {
            retryWithExponentialBackoff(maxAttempts = 3, initialDelayMs = 0) { _ ->
                attempts++
                throw Exception("attempt $attempts failed")
            }
        } catch (e: Exception) {
            thrownException = e
        }
        assertEquals(3, attempts)
        assertNotNull(thrownException)
        assertEquals("attempt 3 failed", thrownException?.message)
    }

    @Test
    fun `retryWithExponentialBackoff makes exactly maxAttempts attempts on total failure`() = runTest {
        var attempts = 0
        try {
            retryWithExponentialBackoff(maxAttempts = 5, initialDelayMs = 0) { _ ->
                attempts++
                throw Exception("always fails")
            }
        } catch (_: Exception) {}
        assertEquals(5, attempts)
    }

    @Test
    fun `retryWithExponentialBackoff succeeds on second attempt`() = runTest {
        var attempts = 0
        val result = retryWithExponentialBackoff(maxAttempts = 3, initialDelayMs = 0) { _ ->
            attempts++
            if (attempts == 1) throw Exception("first fail")
            "second try success"
        }
        assertEquals("second try success", result)
        assertEquals(2, attempts)
    }
}
