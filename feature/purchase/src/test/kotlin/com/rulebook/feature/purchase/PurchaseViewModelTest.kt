package com.rulebook.feature.purchase

import android.app.Activity
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PurchaseViewModel
    private lateinit var fakeCreditRepository: FakeCreditRepository
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCreditRepository = FakeCreditRepository()
        fakeBillingRepository = FakeBillingRepository()
        fakeAnalyticsManager = FakeAnalyticsManager()
        viewModel = PurchaseViewModel(
            creditRepository = fakeCreditRepository,
            billingRepository = fakeBillingRepository,
            analyticsManager = fakeAnalyticsManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty products and zero balance`() = runTest {
        val state = viewModel.uiState.first()

        // After queryProducts() completes immediately (stub returns empty), isLoading = false
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
        assertEquals(0, state.currentBalance)
    }

    @Test
    fun `credit balance updates from repository`() = runTest {
        fakeCreditRepository.setCreditBalance(5)
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(5, state.currentBalance)
    }

    @Test
    fun `products are loaded from billing repository`() = runTest {
        val products = listOf(
            ProductInfo("pack_1", "1 Credit", "$0.99", 1),
            ProductInfo("pack_3", "3 Credits", "$2.49", 3)
        )
        fakeBillingRepository.setProducts(products)
        advanceUntilIdle()
        val state = viewModel.uiState.first()

        assertEquals(2, state.products.size)
        assertEquals("pack_1", state.products[0].productId)
        assertFalse(state.isLoading)
    }

    @Test
    fun `onDismiss emits Dismiss event`() = runTest {
        val events = mutableListOf<PurchaseEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        viewModel.onDismiss()
        advanceUntilIdle()

        assertEquals(1, events.size)
        assertTrue(events[0] is PurchaseEvent.Dismiss)
        job.cancel()
    }

    @Test
    fun `onProductSelected tracks analytics with product id`() = runTest {
        viewModel.onProductSelected("pack_1")

        assertEquals(1, fakeAnalyticsManager.getTrackedEvents().size)
        assertEquals("paywall_product_tapped", fakeAnalyticsManager.getTrackedEvents()[0].first)
        assertEquals(mapOf("product_id" to "pack_1"), fakeAnalyticsManager.getTrackedEvents()[0].second)
    }

    @Test
    fun `onRestorePurchases tracks analytics`() = runTest {
        viewModel.onRestorePurchases()

        assertEquals(1, fakeAnalyticsManager.getTrackedEvents().size)
        assertEquals("paywall_restore_purchases_tapped", fakeAnalyticsManager.getTrackedEvents()[0].first)
    }

    @Test
    fun `onDismiss tracks analytics`() = runTest {
        viewModel.onDismiss()
        advanceUntilIdle()

        assertTrue(fakeAnalyticsManager.getTrackedEvents().any { it.first == "paywall_dismissed" })
    }

    @Test
    fun `error state set when queryProducts fails`() = runTest {
        // Create a new ViewModel with a failing billing repository
        fakeBillingRepository.shouldFailQueryProducts = true
        val failingViewModel = PurchaseViewModel(
            creditRepository = fakeCreditRepository,
            billingRepository = fakeBillingRepository,
            analyticsManager = fakeAnalyticsManager
        )
        advanceUntilIdle()

        val state = failingViewModel.uiState.first()
        assertEquals("Failed to load products", state.error)
        assertFalse(state.isLoading)
    }
}

// ======================================================================
// Fake Repositories
// ======================================================================

class FakeCreditRepository : CreditRepository {
    private val _creditBalance = MutableStateFlow(0)

    override val creditBalance: Flow<Int> = _creditBalance

    fun setCreditBalance(balance: Int) {
        _creditBalance.value = balance
    }

    override suspend fun awardInitialCredits(amount: Int): Boolean {
        if (_creditBalance.value == 0) {
            _creditBalance.value = amount
            return true
        }
        return false
    }

    override suspend fun deductCredit(): Boolean {
        if (_creditBalance.value > 0) {
            _creditBalance.value -= 1
            return true
        }
        return false
    }

    override suspend fun hasCredits(): Boolean {
        return _creditBalance.value > 0
    }
}

class FakeBillingRepository : BillingRepository {
    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    var shouldFailQueryProducts = false

    override val products: Flow<List<ProductInfo>> = _products

    fun setProducts(products: List<ProductInfo>) {
        _products.value = products
    }

    override suspend fun queryProducts(): Result<List<ProductInfo>> {
        if (shouldFailQueryProducts) {
            return Result.failure(RuntimeException("Billing service unavailable"))
        }
        return Result.success(_products.value)
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> {
        return Result.failure(NotImplementedError("Stub"))
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        return Result.failure(NotImplementedError("Stub"))
    }

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> {
        return Result.success(emptyList())
    }
}

class FakeAnalyticsManager : AnalyticsManager {
    private val trackedEvents = mutableListOf<Pair<String, Map<String, String>>>()

    fun getTrackedEvents() = trackedEvents

    override fun trackEvent(name: String, properties: Map<String, String>) {
        trackedEvents.add(name to properties)
    }

    override fun trackScreenView(screenName: String) {
        // No-op for tests
    }
}
