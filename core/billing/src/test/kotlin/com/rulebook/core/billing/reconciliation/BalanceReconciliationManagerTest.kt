package com.rulebook.core.billing.reconciliation

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result as AppResult
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.ReceiptRepository
import com.rulebook.core.model.RefundStatus
import com.rulebook.core.model.ValidationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BalanceReconciliationManagerTest {

    private lateinit var fakeReceiptRepository: FakeReconciliationReceiptRepository
    private lateinit var fakeCreditRepository: FakeReconciliationCreditRepository
    private lateinit var fakeAnalyticsManager: FakeReconciliationAnalyticsManager
    private lateinit var manager: BalanceReconciliationManager

    @Before
    fun setup() {
        fakeReceiptRepository = FakeReconciliationReceiptRepository()
        fakeCreditRepository = FakeReconciliationCreditRepository()
        fakeAnalyticsManager = FakeReconciliationAnalyticsManager()
        manager = BalanceReconciliationManager(
            receiptRepository = fakeReceiptRepository,
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
        )
    }

    // ==================== No-op / Skip Tests ====================

    @Test
    fun `reconcile returns not reconciled when server unreachable`() = runTest {
        fakeReceiptRepository.shouldFail = true

        val result = manager.reconcile()

        assertFalse(result.reconciled)
    }

    @Test
    fun `reconcile returns not reconciled when balances match`() = runTest {
        fakeReceiptRepository.serverBalance = 5
        fakeCreditRepository.setBalance(5)

        val result = manager.reconcile()

        assertFalse(result.reconciled)
        assertEquals(5, result.localBalance)
        assertEquals(5, result.serverBalance)
        assertEquals(0, result.delta)
    }

    // ==================== Happy Path Tests ====================

    @Test
    fun `reconcile updates local balance when server balance is higher`() = runTest {
        fakeReceiptRepository.serverBalance = 10
        fakeCreditRepository.setBalance(3)

        val result = manager.reconcile()

        assertTrue(result.reconciled)
        assertEquals(3, result.localBalance)
        assertEquals(10, result.serverBalance)
        assertEquals(7, result.delta)
        assertEquals(10, fakeCreditRepository.currentBalance())
    }

    @Test
    fun `reconcile updates local balance when server balance is lower`() = runTest {
        fakeReceiptRepository.serverBalance = 2
        fakeCreditRepository.setBalance(8)

        val result = manager.reconcile()

        assertTrue(result.reconciled)
        assertEquals(8, result.localBalance)
        assertEquals(2, result.serverBalance)
        assertEquals(-6, result.delta)
        assertEquals(2, fakeCreditRepository.currentBalance())
    }

    @Test
    fun `reconcile handles new install where local is 0 and server has history`() = runTest {
        fakeReceiptRepository.serverBalance = 5
        fakeCreditRepository.setBalance(0)

        val result = manager.reconcile()

        assertTrue(result.reconciled)
        assertEquals(0, result.localBalance)
        assertEquals(5, result.serverBalance)
        assertEquals(5, result.delta)
        assertEquals(5, fakeCreditRepository.currentBalance())
    }

    @Test
    fun `reconcile handles server balance of 0`() = runTest {
        fakeReceiptRepository.serverBalance = 0
        fakeCreditRepository.setBalance(5)

        val result = manager.reconcile()

        assertTrue(result.reconciled)
        assertEquals(5, result.localBalance)
        assertEquals(0, result.serverBalance)
        assertEquals(-5, result.delta)
        assertEquals(0, fakeCreditRepository.currentBalance())
    }

    // ==================== Analytics Tests ====================

    @Test
    fun `reconcile fires analytics with correct values when reconciled`() = runTest {
        fakeReceiptRepository.serverBalance = 10
        fakeCreditRepository.setBalance(3)

        manager.reconcile()

        assertEquals(1, fakeAnalyticsManager.reconciledCalls.size)
        val (local, server, delta) = fakeAnalyticsManager.reconciledCalls[0]
        assertEquals(3, local)
        assertEquals(10, server)
        assertEquals(7, delta)
    }

    @Test
    fun `reconcile does not fire analytics when balances match`() = runTest {
        fakeReceiptRepository.serverBalance = 5
        fakeCreditRepository.setBalance(5)

        manager.reconcile()

        assertTrue(fakeAnalyticsManager.reconciledCalls.isEmpty())
    }

    @Test
    fun `reconcile does not fire analytics when server unreachable`() = runTest {
        fakeReceiptRepository.shouldFail = true

        manager.reconcile()

        assertTrue(fakeAnalyticsManager.reconciledCalls.isEmpty())
    }
}

// ==========================================================================
// Fake dependencies for BalanceReconciliationManagerTest
// ==========================================================================

class FakeReconciliationReceiptRepository : ReceiptRepository {
    var serverBalance: Int = 0
    var shouldFail = false

    override suspend fun validatePurchase(purchaseToken: String, productId: String): AppResult<ValidationResult> {
        return AppResult.Error("Not implemented in this fake")
    }

    override suspend fun checkRefundStatus(purchaseTokens: List<String>): AppResult<List<RefundStatus>> {
        return AppResult.Success(emptyList())
    }

    override suspend fun getServerBalance(): AppResult<Int> {
        if (shouldFail) return AppResult.Error("Network error", RuntimeException("Simulated failure"))
        return AppResult.Success(serverBalance)
    }
}

class FakeReconciliationCreditRepository : CreditRepository {
    private val _balance = MutableStateFlow(0)
    override val creditBalance: Flow<Int> = _balance

    override suspend fun awardInitialCredits(amount: Int): Boolean = true
    override suspend fun deductCredit(): Boolean = true
    override suspend fun hasCredits(): Boolean = _balance.value > 0

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

    override suspend fun setCreditBalance(balance: Int) {
        _balance.value = balance.coerceAtLeast(0)
    }

    fun setBalance(balance: Int) {
        _balance.value = balance
    }

    fun currentBalance(): Int = _balance.value
}

class FakeReconciliationAnalyticsManager : AnalyticsManager {
    val trackedEvents: MutableList<Pair<String, Map<String, String>>> = mutableListOf()
    // Triple: (localBalance, serverBalance, delta)
    val reconciledCalls: MutableList<Triple<Int, Int, Int>> = mutableListOf()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        trackedEvents.add(name to properties)
        if (name == "credit_balance_reconciled") {
            val local = properties["local_balance"]?.toIntOrNull() ?: 0
            val server = properties["server_balance"]?.toIntOrNull() ?: 0
            val delta = properties["delta"]?.toIntOrNull() ?: 0
            reconciledCalls.add(Triple(local, server, delta))
        }
    }

    override fun trackScreenView(screenName: String) {}
}
