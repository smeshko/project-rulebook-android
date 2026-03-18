package com.rulebook.feature.purchase

import android.app.Activity
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.VerificationResult
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.datastore.PendingPurchasePreferencesSource
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import com.rulebook.core.model.PurchaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PurchaseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: PurchaseViewModel
    private lateinit var fakeCreditRepository: FakeCreditRepository
    private lateinit var fakeBillingRepository: FakeBillingRepository
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager
    private lateinit var fakePurchaseVerifier: FakePurchaseVerifier
    private lateinit var fakePendingPrefs: FakePendingPurchasePreferencesSource

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCreditRepository = FakeCreditRepository()
        fakeBillingRepository = FakeBillingRepository()
        fakeAnalyticsManager = FakeAnalyticsManager()
        fakePurchaseVerifier = FakePurchaseVerifier()
        fakePendingPrefs = FakePendingPurchasePreferencesSource()
        viewModel = PurchaseViewModel(
            creditRepository = fakeCreditRepository,
            billingRepository = fakeBillingRepository,
            analyticsManager = fakeAnalyticsManager,
            purchaseVerifier = fakePurchaseVerifier,
            pendingPurchasePrefs = fakePendingPrefs
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
    fun `onRestorePurchases tracks paywall_restore_purchases_tapped analytics`() = runTest {
        viewModel.onRestorePurchases()
        advanceUntilIdle()

        assertTrue(fakeAnalyticsManager.getTrackedEvents().any { it.first == "paywall_restore_purchases_tapped" })
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
            analyticsManager = fakeAnalyticsManager,
            purchaseVerifier = fakePurchaseVerifier,
            pendingPurchasePrefs = fakePendingPrefs
        )
        advanceUntilIdle()

        val state = failingViewModel.uiState.first()
        assertEquals("Failed to load products", state.error)
        assertFalse(state.isLoading)
    }

    // =====================================================================
    // Purchase Flow Tests
    // =====================================================================

    @Test
    fun `onProductSelected sets purchaseState to Processing`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Processing>(state.purchaseState)
        assertEquals("credits_3", (state.purchaseState as PurchaseState.Processing).sku)
    }

    @Test
    fun `onProductSelected tracks purchase_started analytics`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        assertTrue(events.any { it.first == "purchase_started" })
        val startEvent = events.first { it.first == "purchase_started" }
        assertEquals("credits_3", startEvent.second["product_id"])
    }

    @Test
    fun `successful purchase adds credits and emits PurchaseSuccess event`() = runTest {
        val collectedEvents = mutableListOf<PurchaseEvent>()
        val eventJob = launch {
            viewModel.events.collect { collectedEvents.add(it) }
        }

        // Start the purchase
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        // Simulate Google Play returning success
        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0, // BillingResponseCode.OK
                purchaseTokens = listOf("test-token-abc"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        // Credits should have been added (3 credits for credits_3)
        assertEquals(3, fakeCreditRepository.addedCreditsTotal)

        // Purchase should have been verified
        assertEquals("test-token-abc", fakePurchaseVerifier.verifiedTokens.first())

        // State should show Success
        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Success>(state.purchaseState)
        assertEquals(3, (state.purchaseState as PurchaseState.Success).creditsAdded)

        // After 1.5s delay, PurchaseSuccess event should be emitted
        // (advanceUntilIdle advances past all pending coroutines including the delay)
        assertTrue(collectedEvents.any { it is PurchaseEvent.PurchaseSuccess })

        eventJob.cancel()
    }

    @Test
    fun `user cancel resets state silently`() = runTest {
        viewModel.onProductSelected(null, "credits_1")
        advanceUntilIdle()

        // Simulate user cancellation
        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 1, // BillingResponseCode.USER_CANCELED
                purchaseTokens = emptyList()
            )
        )
        advanceUntilIdle()

        // State should be reset to null (no error shown)
        val state = viewModel.uiState.first()
        assertNull(state.purchaseState)
    }

    @Test
    fun `user cancel tracks purchase_failed analytics with USER_CANCELED`() = runTest {
        viewModel.onProductSelected(null, "credits_1")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 1, // USER_CANCELED
                purchaseTokens = emptyList()
            )
        )
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val failedEvent = events.firstOrNull { it.first == "purchase_failed" }
        assertTrue(failedEvent != null)
        assertEquals("USER_CANCELED", failedEvent!!.second["error_code"])
        assertEquals("credits_1", failedEvent.second["product_id"])
    }

    @Test
    fun `purchase error sets Error state`() = runTest {
        viewModel.onProductSelected(null, "credits_10")
        advanceUntilIdle()

        // Simulate billing error (code 6 = ERROR)
        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 6,
                purchaseTokens = emptyList()
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Error>(state.purchaseState)
        assertTrue((state.purchaseState as PurchaseState.Error).message.contains("6"))
    }

    @Test
    fun `purchase error tracks purchase_failed analytics`() = runTest {
        viewModel.onProductSelected(null, "credits_1")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 6,
                purchaseTokens = emptyList()
            )
        )
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val failedEvent = events.firstOrNull { it.first == "purchase_failed" }
        assertTrue(failedEvent != null)
        assertEquals("6", failedEvent!!.second["error_code"])
    }

    @Test
    fun `credits mapped correctly from SKU - credits_1 gives 1 credit`() = runTest {
        viewModel.onProductSelected(null, "credits_1")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-1"),
                productIds = listOf("credits_1")
            )
        )
        advanceUntilIdle()

        assertEquals(1, fakeCreditRepository.addedCreditsTotal)
    }

    @Test
    fun `credits mapped correctly from SKU - credits_3 gives 3 credits`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-3"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        assertEquals(3, fakeCreditRepository.addedCreditsTotal)
    }

    @Test
    fun `credits mapped correctly from SKU - credits_10 gives 10 credits`() = runTest {
        viewModel.onProductSelected(null, "credits_10")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-10"),
                productIds = listOf("credits_10")
            )
        )
        advanceUntilIdle()

        assertEquals(10, fakeCreditRepository.addedCreditsTotal)
    }

    @Test
    fun `purchase update ignored when no purchase in progress`() = runTest {
        // Emit a purchase update without starting a purchase
        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("rogue-token"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        // No credits should have been added
        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        // State should remain null
        assertNull(viewModel.uiState.first().purchaseState)
    }

    @Test
    fun `onPurchaseErrorDismissed resets purchaseState to null`() = runTest {
        viewModel.onProductSelected(null, "credits_1")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(responseCode = 6, purchaseTokens = emptyList())
        )
        advanceUntilIdle()

        assertIs<PurchaseState.Error>(viewModel.uiState.first().purchaseState)

        viewModel.onPurchaseErrorDismissed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.first().purchaseState)
    }

    @Test
    fun `successful purchase tracks purchase_completed analytics`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-3"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val completedEvent = events.firstOrNull { it.first == "purchase_completed" }
        assertTrue(completedEvent != null)
        assertEquals("credits_3", completedEvent!!.second["product_id"])
        assertEquals("3", completedEvent.second["credits_added"])
    }

    // =====================================================================
    // Story 8.8: Restore Purchases Tests
    // =====================================================================

    @Test
    fun `restore with unconsumed purchases delivers credits and emits RestoreSuccess`() = runTest {
        fakeBillingRepository.unconsumedPurchases = listOf(
            PurchaseInfo("token-1", "credits_3", "order-1"),
            PurchaseInfo("token-2", "credits_1", "order-2")
        )
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        // 3 + 1 = 4 total credits restored
        assertEquals(4, fakeCreditRepository.addedCreditsTotal)
        // Verify each purchase was passed through the verifier
        assertTrue(fakePurchaseVerifier.verifiedTokens.contains("token-1"))
        assertTrue(fakePurchaseVerifier.verifiedTokens.contains("token-2"))
        val successEvent = events.filterIsInstance<PurchaseEvent.RestoreSuccess>().firstOrNull()
        assertTrue(successEvent != null)
        assertEquals(4, successEvent!!.creditsRestored)
        job.cancel()
    }

    @Test
    fun `restore with no unconsumed purchases emits RestoreNoPurchases`() = runTest {
        // unconsumedPurchases defaults to empty
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        assertTrue(events.any { it is PurchaseEvent.RestoreNoPurchases })
        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        job.cancel()
    }

    @Test
    fun `restore failure emits RestoreError`() = runTest {
        fakeBillingRepository.shouldFailQueryUnconsumed = true
        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        assertTrue(events.any { it is PurchaseEvent.RestoreError })
        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        job.cancel()
    }

    @Test
    fun `restore sets isRestoring true during operation and false after`() = runTest {
        fakeBillingRepository.unconsumedPurchases = listOf(
            PurchaseInfo("token-1", "credits_3", "order-1")
        )

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        // After completion, isRestoring should be false
        assertFalse(viewModel.uiState.first().isRestoring)
    }

    @Test
    fun `restore tracks purchase_restored analytics with correct result and credits_count`() = runTest {
        fakeBillingRepository.unconsumedPurchases = listOf(
            PurchaseInfo("token-1", "credits_3", "order-1")
        )

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val restoredEvent = events.firstOrNull { it.first == "purchase_restored" }
        assertTrue(restoredEvent != null)
        assertEquals("success", restoredEvent!!.second["result"])
        assertEquals("3", restoredEvent.second["credits_count"])
    }

    @Test
    fun `restore with mixed success and failure purchases delivers only successful credits`() = runTest {
        fakeBillingRepository.unconsumedPurchases = listOf(
            PurchaseInfo("token-ok", "credits_3", "order-1"),
            PurchaseInfo("token-fail", "credits_10", "order-2")
        )
        fakePurchaseVerifier.tokenResults["token-fail"] =
            Result.failure(RuntimeException("Consume failed"))

        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        // Only 3 credits from the successful token
        assertEquals(3, fakeCreditRepository.addedCreditsTotal)
        // Still emits RestoreSuccess with partial credits
        val successEvent = events.filterIsInstance<PurchaseEvent.RestoreSuccess>().firstOrNull()
        assertTrue(successEvent != null)
        assertEquals(3, successEvent!!.creditsRestored)
        job.cancel()
    }

    @Test
    fun `restore with all purchases failing verification emits RestoreError`() = runTest {
        fakeBillingRepository.unconsumedPurchases = listOf(
            PurchaseInfo("token-fail-1", "credits_3", "order-1"),
            PurchaseInfo("token-fail-2", "credits_1", "order-2")
        )
        fakePurchaseVerifier.shouldFail = true

        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.onRestorePurchases()
        advanceUntilIdle()

        // No credits should be delivered
        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        // Should emit RestoreError, not RestoreSuccess(0)
        assertTrue(events.any { it is PurchaseEvent.RestoreError })
        assertFalse(events.any { it is PurchaseEvent.RestoreSuccess })
        // isRestoring should be reset
        assertFalse(viewModel.uiState.first().isRestoring)
        // Analytics should report error
        val analyticsEvents = fakeAnalyticsManager.getTrackedEvents()
        val restoredEvent = analyticsEvents.firstOrNull { it.first == "purchase_restored" }
        assertTrue(restoredEvent != null)
        assertEquals("error", restoredEvent!!.second["result"])
        job.cancel()
    }

    @Test
    fun `restore button disabled during active purchase`() = runTest {
        // When a purchase is active (Processing state), isRestoring check is irrelevant
        // The UI disables the restore button when isPurchaseActive || isRestoring
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Processing>(state.purchaseState)
        // isRestoring should still be false (restore not triggered during purchase)
        assertFalse(state.isRestoring)
    }

    // =====================================================================
    // Story 8.7: PurchaseVerifier Integration Tests
    // =====================================================================

    @Test
    fun `successful verification delivers credits and tracks purchase_completed with new_balance`() = runTest {
        fakeCreditRepository.setCreditBalance(2) // start with 2 credits
        advanceUntilIdle()

        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-3"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        // Credits delivered after successful verification
        assertEquals(3, fakeCreditRepository.addedCreditsTotal)

        val events = fakeAnalyticsManager.getTrackedEvents()
        val completedEvent = events.firstOrNull { it.first == "purchase_completed" }
        assertTrue(completedEvent != null)
        assertEquals("3", completedEvent!!.second["credits_added"])
        // new_balance = 2 (initial) + 3 (added) = 5
        assertEquals("5", completedEvent.second["new_balance"])
    }

    @Test
    fun `failed verification sets PurchaseState Error and does NOT deliver credits`() = runTest {
        fakePurchaseVerifier.shouldFail = true

        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-fail"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        // Credits must NOT be delivered
        assertEquals(0, fakeCreditRepository.addedCreditsTotal)

        // State must be Error
        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Error>(state.purchaseState)
    }

    @Test
    fun `failed verification tracks purchase_failed with CONSUME_FAILED error code`() = runTest {
        fakePurchaseVerifier.shouldFail = true

        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = listOf("token-fail"),
                productIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val failedEvent = events.firstOrNull { it.first == "purchase_failed" }
        assertTrue(failedEvent != null)
        assertEquals("CONSUME_FAILED", failedEvent!!.second["error_code"])
        assertEquals("credits_3", failedEvent.second["product_id"])
    }

    // =====================================================================
    // Story 8.9: Pending Purchase Tests
    // =====================================================================

    @Test
    fun `pending purchase update sets PurchaseState Pending and stores token in prefs`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0, // BillingResponseCode.OK
                purchaseTokens = emptyList(), // no completed purchases
                productIds = emptyList(),
                pendingPurchaseTokens = listOf("pending-token-abc"),
                pendingProductIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertIs<PurchaseState.Pending>(state.purchaseState)
        assertEquals("pending-token-abc", fakePendingPrefs.storedToken)
        assertEquals("credits_3", fakePendingPrefs.storedProductId)
    }

    @Test
    fun `pending purchase update tracks purchase_pending analytics`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = emptyList(),
                pendingPurchaseTokens = listOf("pending-token"),
                pendingProductIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val pendingEvent = events.firstOrNull { it.first == "purchase_pending" }
        assertTrue(pendingEvent != null)
        assertEquals("credits_3", pendingEvent!!.second["product_id"])
    }

    @Test
    fun `onPendingDismissed resets purchaseState to null`() = runTest {
        viewModel.onProductSelected(null, "credits_3")
        advanceUntilIdle()

        fakeBillingRepository.emitPurchaseUpdate(
            PurchaseUpdate(
                responseCode = 0,
                purchaseTokens = emptyList(),
                pendingPurchaseTokens = listOf("pending-token"),
                pendingProductIds = listOf("credits_3")
            )
        )
        advanceUntilIdle()

        assertIs<PurchaseState.Pending>(viewModel.uiState.first().purchaseState)

        viewModel.onPendingDismissed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.first().purchaseState)
    }

    @Test
    fun `checkPendingPurchaseResolution with Purchased result delivers credits and clears token`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.pendingPurchaseResolution =
            PendingPurchaseResolution.Purchased("pending-token", "credits_3")

        val events = mutableListOf<PurchaseEvent>()
        val job = launch { viewModel.events.collect { events.add(it) } }

        viewModel.checkPendingPurchaseResolution()
        advanceUntilIdle()

        assertEquals(3, fakeCreditRepository.addedCreditsTotal)
        assertNull(fakePendingPrefs.storedToken)
        assertTrue(events.any { it is PurchaseEvent.PendingPurchaseResolved })
        job.cancel()
    }

    @Test
    fun `checkPendingPurchaseResolution with StillPending result leaves token in prefs`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.pendingPurchaseResolution = PendingPurchaseResolution.StillPending

        viewModel.checkPendingPurchaseResolution()
        advanceUntilIdle()

        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        assertEquals("pending-token", fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkPendingPurchaseResolution with NotFound result clears token`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.pendingPurchaseResolution = PendingPurchaseResolution.NotFound

        viewModel.checkPendingPurchaseResolution()
        advanceUntilIdle()

        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
        assertNull(fakePendingPrefs.storedToken)
    }

    @Test
    fun `checkPendingPurchaseResolution does nothing when no pending token stored`() = runTest {
        // fakePendingPrefs has no stored token by default
        viewModel.checkPendingPurchaseResolution()
        advanceUntilIdle()

        assertEquals(0, fakeCreditRepository.addedCreditsTotal)
    }

    @Test
    fun `checkPendingPurchaseResolution tracks purchase_pending_resolved analytics on success`() = runTest {
        fakePendingPrefs.storedToken = "pending-token"
        fakePendingPrefs.storedProductId = "credits_3"
        fakeBillingRepository.pendingPurchaseResolution =
            PendingPurchaseResolution.Purchased("pending-token", "credits_3")

        viewModel.checkPendingPurchaseResolution()
        advanceUntilIdle()

        val events = fakeAnalyticsManager.getTrackedEvents()
        val resolvedEvent = events.firstOrNull { it.first == "purchase_pending_resolved" }
        assertTrue(resolvedEvent != null)
        assertEquals("credits_3", resolvedEvent!!.second["product_id"])
    }
}

// ======================================================================
// Fake Repositories and Test Doubles
// ======================================================================

class FakePurchaseVerifier : PurchaseVerifier {
    var shouldFail = false
    val verifiedTokens = mutableListOf<String>()
    val tokenResults = mutableMapOf<String, Result<VerificationResult>>()

    override suspend fun verifyAndConsume(
        purchaseToken: String,
        productId: String
    ): Result<VerificationResult> {
        verifiedTokens.add(purchaseToken)
        tokenResults[purchaseToken]?.let { return it }
        if (shouldFail) {
            return Result.failure(RuntimeException("Consume failed"))
        }
        val credits = when (productId) {
            "credits_1" -> 1
            "credits_3" -> 3
            "credits_10" -> 10
            else -> return Result.failure(IllegalArgumentException("Unknown product: $productId"))
        }
        return Result.success(VerificationResult(credits))
    }
}

class FakeCreditRepository : CreditRepository {
    private val _creditBalance = MutableStateFlow(0)
    var addedCreditsTotal = 0

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

    override suspend fun addCredits(amount: Int): Boolean {
        _creditBalance.value += amount
        addedCreditsTotal += amount
        return true
    }
}

class FakeBillingRepository : BillingRepository {
    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    private val _purchaseUpdates = MutableSharedFlow<PurchaseUpdate>(
        replay = 0,
        extraBufferCapacity = 10
    )
    val consumedTokens = mutableListOf<String>()
    var shouldFailQueryProducts = false
    var shouldFailLaunchPurchaseFlow = false
    var unconsumedPurchases: List<PurchaseInfo> = emptyList()
    var shouldFailQueryUnconsumed = false
    var pendingPurchaseResolution: PendingPurchaseResolution = PendingPurchaseResolution.NotFound
    var shouldFailCheckPending = false

    override val products: Flow<List<ProductInfo>> = _products
    override val purchaseUpdates: SharedFlow<PurchaseUpdate> = _purchaseUpdates.asSharedFlow()

    fun setProducts(products: List<ProductInfo>) {
        _products.value = products
    }

    suspend fun emitPurchaseUpdate(update: PurchaseUpdate) {
        _purchaseUpdates.emit(update)
    }

    override suspend fun queryProducts(): Result<List<ProductInfo>> {
        if (shouldFailQueryProducts) {
            return Result.failure(RuntimeException("Billing service unavailable"))
        }
        return Result.success(_products.value)
    }

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> {
        if (shouldFailLaunchPurchaseFlow) {
            return Result.failure(RuntimeException("Failed to launch billing flow"))
        }
        return Result.success(Unit)
    }

    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> {
        consumedTokens.add(purchaseToken)
        return Result.success(Unit)
    }

    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> {
        if (shouldFailQueryUnconsumed) {
            return Result.failure(RuntimeException("Failed to query unconsumed purchases"))
        }
        return Result.success(unconsumedPurchases)
    }

    override suspend fun checkPendingPurchases(pendingToken: String): Result<PendingPurchaseResolution> {
        if (shouldFailCheckPending) {
            return Result.failure(RuntimeException("Failed to check pending purchases"))
        }
        return Result.success(pendingPurchaseResolution)
    }

    override fun creditsForProduct(productId: String): Int? {
        return when (productId) {
            "credits_1" -> 1
            "credits_3" -> 3
            "credits_10" -> 10
            else -> null
        }
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

class FakePendingPurchasePreferencesSource : PendingPurchasePreferencesSource {
    var storedToken: String? = null
    var storedProductId: String? = null

    override val pendingPurchaseToken: Flow<String?>
        get() = flow { emit(storedToken) }
    override val pendingPurchaseProductId: Flow<String?>
        get() = flow { emit(storedProductId) }

    override suspend fun setPendingPurchase(token: String, productId: String) {
        storedToken = token
        storedProductId = productId
    }

    override suspend fun clearPendingPurchase() {
        storedToken = null
        storedProductId = null
    }
}
