package com.rulebook.core.billing.refund

import android.app.Activity
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.history.PurchaseHistoryEntry
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.billing.repository.PurchaseInfoWithState
import com.rulebook.core.common.Result as AppResult
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.ReceiptRepository
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RefundSyncManagerTest {

    private lateinit var fakePurchaseHistoryStore: FakeRefundSyncPurchaseHistoryStore
    private lateinit var fakeReceiptRepository: FakeRefundSyncReceiptRepository
    private lateinit var fakeCreditRepository: FakeRefundSyncCreditRepository
    private lateinit var fakeRefundAcknowledgmentStore: FakeRefundSyncAcknowledgmentStore
    private lateinit var fakeBillingRepository: FakeRefundSyncBillingRepository
    private lateinit var fakeAnalyticsManager: FakeRefundSyncAnalyticsManager
    private lateinit var manager: RefundSyncManager

    @Before
    fun setup() {
        fakePurchaseHistoryStore = FakeRefundSyncPurchaseHistoryStore()
        fakeReceiptRepository = FakeRefundSyncReceiptRepository()
        fakeCreditRepository = FakeRefundSyncCreditRepository()
        fakeRefundAcknowledgmentStore = FakeRefundSyncAcknowledgmentStore()
        fakeBillingRepository = FakeRefundSyncBillingRepository()
        fakeAnalyticsManager = FakeRefundSyncAnalyticsManager()
        manager = RefundSyncManager(
            purchaseHistoryStore = fakePurchaseHistoryStore,
            receiptRepository = fakeReceiptRepository,
            creditRepository = fakeCreditRepository,
            refundAcknowledgmentStore = fakeRefundAcknowledgmentStore,
            billingRepository = fakeBillingRepository,
            analyticsManager = fakeAnalyticsManager,
        )
    }

    // ==================== No-op Tests ====================

    @Test
    fun `checkRefunds returns zero result when no purchase history`() = runTest {
        val result = manager.checkRefunds()

        assertEquals(0, result.creditsRevoked)
        assertEquals(0, result.tokensRefunded)
    }

    @Test
    fun `checkRefunds returns zero result when all tokens already acknowledged`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeRefundAcknowledgmentStore.acknowledged.add("token-1")

        val result = manager.checkRefunds()

        assertEquals(0, result.creditsRevoked)
        assertEquals(0, result.tokensRefunded)
    }

    @Test
    fun `checkRefunds returns zero result when no refunds detected`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = false))

        val result = manager.checkRefunds()

        assertEquals(0, result.creditsRevoked)
        assertEquals(0, result.tokensRefunded)
    }

    // ==================== Single Refund Tests ====================

    @Test
    fun `checkRefunds revokes credits for single refunded token`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = true))
        fakeCreditRepository.setBalance(10)

        val result = manager.checkRefunds()

        assertEquals(3, result.creditsRevoked)
        assertEquals(1, result.tokensRefunded)
    }

    @Test
    fun `checkRefunds acknowledges refunded token`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = true))
        fakeCreditRepository.setBalance(10)

        manager.checkRefunds()

        assert(fakeRefundAcknowledgmentStore.acknowledged.contains("token-1"))
    }

    @Test
    fun `checkRefunds removes credits from balance`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = true))
        fakeCreditRepository.setBalance(10)

        manager.checkRefunds()

        assertEquals(7, fakeCreditRepository.currentBalance())
    }

    // ==================== Multiple Refund Tests ====================

    @Test
    fun `checkRefunds handles multiple refunded tokens`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-2", "credits_10"))
        fakeReceiptRepository.refundStatuses = listOf(
            RefundStatus("token-1", isRefunded = true),
            RefundStatus("token-2", isRefunded = true),
        )
        fakeCreditRepository.setBalance(20)

        val result = manager.checkRefunds()

        assertEquals(13, result.creditsRevoked)
        assertEquals(2, result.tokensRefunded)
    }

    @Test
    fun `checkRefunds handles mix of refunded and active tokens`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-2", "credits_10"))
        fakeReceiptRepository.refundStatuses = listOf(
            RefundStatus("token-1", isRefunded = true),
            RefundStatus("token-2", isRefunded = false),
        )
        fakeCreditRepository.setBalance(15)

        val result = manager.checkRefunds()

        assertEquals(3, result.creditsRevoked)
        assertEquals(1, result.tokensRefunded)
    }

    // ==================== Already Acknowledged Tests ====================

    @Test
    fun `checkRefunds skips already acknowledged tokens`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-2", "credits_10"))
        fakeRefundAcknowledgmentStore.acknowledged.add("token-1")
        fakeReceiptRepository.refundStatuses = listOf(
            RefundStatus("token-2", isRefunded = true),
        )
        fakeCreditRepository.setBalance(15)

        val result = manager.checkRefunds()

        // Only token-2 was sent and processed
        assertEquals(listOf("token-2"), fakeReceiptRepository.lastCheckedTokens)
        assertEquals(10, result.creditsRevoked)
        assertEquals(1, result.tokensRefunded)
    }

    // ==================== Credits Clamped to 0 Tests ====================

    @Test
    fun `checkRefunds clamps credits to 0 when balance is less than refund amount`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_10"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = true))
        fakeCreditRepository.setBalance(2) // Less than the 10 credits to revoke

        val result = manager.checkRefunds()

        // Only 2 credits were actually removed (clamped from 10)
        assertEquals(2, result.creditsRevoked)
        assertEquals(0, fakeCreditRepository.currentBalance())
    }

    // ==================== Network Error Tests ====================

    @Test
    fun `checkRefunds returns zero result on network error`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.shouldFail = true

        val result = manager.checkRefunds()

        assertEquals(0, result.creditsRevoked)
        assertEquals(0, result.tokensRefunded)
    }

    @Test
    fun `checkRefunds does not acknowledge tokens on network error`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.shouldFail = true

        manager.checkRefunds()

        assert(fakeRefundAcknowledgmentStore.acknowledged.isEmpty())
    }

    // ==================== Analytics Tests ====================

    @Test
    fun `checkRefunds fires analytics when refunds detected`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = true))
        fakeCreditRepository.setBalance(10)

        manager.checkRefunds()

        assertEquals(1, fakeAnalyticsManager.refundDetectedCalls.size)
        assertEquals(1, fakeAnalyticsManager.refundDetectedCalls[0].first)  // tokensRefunded
        assertEquals(3, fakeAnalyticsManager.refundDetectedCalls[0].second) // creditsRevoked
    }

    @Test
    fun `checkRefunds does not fire analytics when no refunds detected`() = runTest {
        fakePurchaseHistoryStore.entries.add(PurchaseHistoryEntry("token-1", "credits_3"))
        fakeReceiptRepository.refundStatuses = listOf(RefundStatus("token-1", isRefunded = false))

        manager.checkRefunds()

        assert(fakeAnalyticsManager.refundDetectedCalls.isEmpty())
    }
}

// ==========================================================================
// Fake dependencies for RefundSyncManagerTest
// ==========================================================================

class FakeRefundSyncPurchaseHistoryStore : PurchaseHistoryStore {
    val entries: MutableList<PurchaseHistoryEntry> = mutableListOf()

    override suspend fun savePurchase(purchaseToken: String, productId: String) {
        entries.add(PurchaseHistoryEntry(purchaseToken, productId))
    }

    override suspend fun getRecentTokens(): List<String> = entries.map { it.purchaseToken }

    override suspend fun getRecentEntries(): List<PurchaseHistoryEntry> = entries.toList()

    override suspend fun clear() = entries.clear()
}

class FakeRefundSyncReceiptRepository : ReceiptRepository {
    var refundStatuses: List<RefundStatus> = emptyList()
    var shouldFail = false
    var lastCheckedTokens: List<String> = emptyList()

    override suspend fun validatePurchase(purchaseToken: String, productId: String): AppResult<ValidationResult> {
        return AppResult.Error("Not implemented in this fake")
    }

    override suspend fun checkRefundStatus(purchaseTokens: List<String>): AppResult<List<RefundStatus>> {
        lastCheckedTokens = purchaseTokens
        if (shouldFail) return AppResult.Error("Network error", RuntimeException("Simulated failure"))
        return AppResult.Success(refundStatuses)
    }

    override suspend fun getServerBalance(): AppResult<Int> {
        return AppResult.Success(0)
    }
}

class FakeRefundSyncCreditRepository : CreditRepository {
    private val _balance = MutableStateFlow(0)
    override val creditBalance: Flow<Int> = _balance

    override suspend fun awardInitialCredits(amount: Int): Boolean = true
    override suspend fun deductCredit(): Boolean = true
    override suspend fun hasCredits(): Boolean = true

    override suspend fun addCredits(amount: Int): Boolean {
        _balance.value += amount
        return true
    }

    override suspend fun removeCredits(amount: Int): Int {
        val current = _balance.value
        val actualRemoved = minOf(current, amount)
        _balance.value = (current - amount).coerceAtLeast(0)
        return actualRemoved
    }

    fun setBalance(balance: Int) {
        _balance.value = balance
    }

    fun currentBalance(): Int = _balance.value
}

class FakeRefundSyncAcknowledgmentStore : RefundAcknowledgmentStore {
    val acknowledged: MutableSet<String> = mutableSetOf()

    override suspend fun isAcknowledged(purchaseToken: String): Boolean = purchaseToken in acknowledged
    override suspend fun acknowledge(purchaseToken: String) { acknowledged.add(purchaseToken) }
    override suspend fun clear() = acknowledged.clear()
}

class FakeRefundSyncBillingRepository : BillingRepository {
    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    override val products: Flow<List<ProductInfo>> = _products
    override val purchaseUpdates: kotlinx.coroutines.flow.SharedFlow<PurchaseUpdate> = MutableSharedFlow()

    override suspend fun queryProducts() = Result.success(emptyList<ProductInfo>())
    override suspend fun launchPurchaseFlow(activity: Activity, productId: String) = Result.success(Unit)
    override suspend fun queryUnacknowledgedPurchases() = Result.success(emptyList<PurchaseInfo>())
    override suspend fun checkPendingPurchases(pendingToken: String) =
        Result.success(PendingPurchaseResolution.NotFound)

    override fun creditsForProduct(productId: String): Int? = when (productId) {
        "credits_1" -> 1
        "credits_3" -> 3
        "credits_10" -> 10
        else -> null
    }
}

class FakeRefundSyncAnalyticsManager : AnalyticsManager {
    val trackedEvents: MutableList<Pair<String, Map<String, String>>> = mutableListOf()
    val refundDetectedCalls: MutableList<Pair<Int, Int>> = mutableListOf() // (tokensRefunded, creditsRevoked)

    override fun trackEvent(name: String, properties: Map<String, String>) {
        trackedEvents.add(name to properties)
        if (name == "purchase_refund_detected") {
            val tokensRefunded = properties["tokens_refunded"]?.toIntOrNull() ?: 0
            val creditsRevoked = properties["credits_revoked"]?.toIntOrNull() ?: 0
            refundDetectedCalls.add(tokensRefunded to creditsRevoked)
        }
    }

    override fun trackScreenView(screenName: String) {}
}
