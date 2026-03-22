package com.rulebook.core.billing.recovery

import android.app.Activity
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.billing.PurchaseUpdate
import com.rulebook.core.billing.history.PurchaseHistoryStore
import com.rulebook.core.billing.pending.PendingValidation
import com.rulebook.core.billing.pending.PendingValidationStore
import com.rulebook.core.billing.repository.BillingRepository
import com.rulebook.core.billing.repository.PurchaseInfo
import com.rulebook.core.billing.verification.PurchaseValidationException
import com.rulebook.core.billing.verification.PurchaseVerifier
import com.rulebook.core.billing.verification.VerificationResult
import com.rulebook.core.billing.verification.VerificationStatus
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.model.PendingPurchaseResolution
import com.rulebook.core.model.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [ValidationRecoveryManager].
 *
 * Tests all recovery paths: success, transient failure, permanent failure,
 * expired entries, orphaned purchases, and deduplication guards.
 */
class ValidationRecoveryManagerTest {

    private lateinit var fakePendingStore: FakeRecoveryPendingValidationStore
    private lateinit var fakeBillingRepository: FakeRecoveryBillingRepository
    private lateinit var fakePurchaseVerifier: FakeRecoveryPurchaseVerifier
    private lateinit var fakeCreditRepository: FakeRecoveryCreditRepository
    private lateinit var fakePurchaseHistoryStore: FakeRecoveryPurchaseHistoryStore
    private lateinit var fakeAnalyticsManager: FakeRecoveryAnalyticsManager
    private lateinit var manager: ValidationRecoveryManager

    @Before
    fun setup() {
        fakePendingStore = FakeRecoveryPendingValidationStore()
        fakeBillingRepository = FakeRecoveryBillingRepository()
        fakePurchaseVerifier = FakeRecoveryPurchaseVerifier()
        fakeCreditRepository = FakeRecoveryCreditRepository()
        fakePurchaseHistoryStore = FakeRecoveryPurchaseHistoryStore()
        fakeAnalyticsManager = FakeRecoveryAnalyticsManager()
        manager = ValidationRecoveryManager(
            pendingValidationStore = fakePendingStore,
            billingRepository = fakeBillingRepository,
            purchaseVerifier = fakePurchaseVerifier,
            creditRepository = fakeCreditRepository,
            purchaseHistoryStore = fakePurchaseHistoryStore,
            analyticsManager = fakeAnalyticsManager
        )
    }

    // ==================== No-op tests ====================

    @Test
    fun `recover returns zero counts when no pending entries and no unconsumed purchases`() = runTest {
        val result = manager.recover()

        assertEquals(0, result.creditsRecovered)
        assertEquals(0, result.pendingCount)
        assertEquals(0, result.recoveredCount)
        assertEquals(0, result.failedCount)
    }

    // ==================== Pending store — success path ====================

    @Test
    fun `recover grants credits for valid pending entries`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 1))

        val result = manager.recover()

        assertEquals(3, result.creditsRecovered)
        assertEquals(1, result.pendingCount)
        assertEquals(1, result.recoveredCount)
        assertEquals(3, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `recover removes pending entry on successful validation`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))

        manager.recover()

        assertEquals(0, fakePendingStore.entries.size)
    }

    @Test
    fun `recover saves purchase to history store on successful validation`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))

        manager.recover()

        assertEquals(listOf("token-1"), fakePurchaseHistoryStore.savedTokens)
    }

    @Test
    fun `recover processes multiple pending entries independently`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_1", timestamp = now(), retryCount = 0))
        fakePendingStore.entries.add(PendingValidation("token-2", "credits_3", timestamp = now(), retryCount = 0))

        val result = manager.recover()

        assertEquals(4, result.creditsRecovered) // 1 + 3
        assertEquals(2, result.pendingCount)
        assertEquals(2, result.recoveredCount)
    }

    // ==================== Pending store — transient failure ====================

    @Test
    fun `recover increments retryCount and keeps entry on transient failure`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 2))
        fakePurchaseVerifier.transientFailureTokens.add("token-1")

        manager.recover()

        assertEquals(1, fakePendingStore.entries.size)
        assertEquals(3, fakePendingStore.entries.first().retryCount) // incremented from 2
    }

    @Test
    fun `recover counts transient failures in failedCount`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))
        fakePurchaseVerifier.transientFailureTokens.add("token-1")

        val result = manager.recover()

        assertEquals(0, result.creditsRecovered)
        assertEquals(1, result.failedCount)
    }

    // ==================== Pending store — permanent failure (INVALID) ====================

    @Test
    fun `recover removes entry on PurchaseValidationException (server INVALID)`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-bad", "credits_3", timestamp = now(), retryCount = 0))
        fakePurchaseVerifier.invalidTokens.add("token-bad")

        manager.recover()

        assertEquals(0, fakePendingStore.entries.size)
        assertEquals(0, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `recover does not count INVALID entries as recovered or failed`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-bad", "credits_3", timestamp = now(), retryCount = 0))
        fakePurchaseVerifier.invalidTokens.add("token-bad")

        val result = manager.recover()

        assertEquals(0, result.recoveredCount)
        assertEquals(0, result.failedCount)
        assertEquals(0, result.creditsRecovered)
    }

    // ==================== Duplicate delivery guard (ALREADY_PROCESSED) ====================

    @Test
    fun `recover skips credit delivery when ALREADY_PROCESSED and token in history`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))
        fakePurchaseHistoryStore.recentTokens.add("token-1")
        fakePurchaseVerifier.alreadyProcessedTokens.add("token-1")

        val result = manager.recover()

        assertEquals(0, result.creditsRecovered)
        assertEquals(0, fakeCreditRepository.addedCredits)
        // Entry is still cleaned up from pending store
        assertEquals(0, fakePendingStore.entries.size)
    }

    @Test
    fun `recover delivers credits when ALREADY_PROCESSED but token not in history`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))
        fakePurchaseVerifier.alreadyProcessedTokens.add("token-1")
        // token-1 NOT in history store

        val result = manager.recover()

        assertEquals(3, result.creditsRecovered)
        assertEquals(3, fakeCreditRepository.addedCredits)
    }

    // ==================== Orphaned purchase recovery ====================

    @Test
    fun `recover validates orphaned unconsumed purchases not in pending store or history`() = runTest {
        fakeBillingRepository.unconsumedPurchases.add(PurchaseInfo("orphan-token", "credits_1", "order-1"))

        val result = manager.recover()

        assertEquals(1, result.creditsRecovered)
        assertEquals(1, result.recoveredCount)
        assertEquals(1, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `recover skips unconsumed purchase when token is in pending store`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))
        fakeBillingRepository.unconsumedPurchases.add(PurchaseInfo("token-1", "credits_3", "order-1"))

        val result = manager.recover()

        // token-1 is processed via pending store path; not double-processed via unconsumed path
        assertEquals(1, result.recoveredCount)
        assertEquals(3, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `recover skips unconsumed purchase when token is in purchase history`() = runTest {
        fakePurchaseHistoryStore.recentTokens.add("already-done")
        fakeBillingRepository.unconsumedPurchases.add(PurchaseInfo("already-done", "credits_3", "order-1"))

        val result = manager.recover()

        assertEquals(0, result.creditsRecovered)
        assertEquals(0, fakeCreditRepository.addedCredits)
    }

    @Test
    fun `recover gracefully handles queryUnacknowledgedPurchases failure`() = runTest {
        fakeBillingRepository.queryUnconsumedShouldFail = true
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))

        val result = manager.recover()

        // Pending store processing still succeeds
        assertEquals(3, result.creditsRecovered)
        assertEquals(1, result.recoveredCount)
    }

    @Test
    fun `recover gracefully handles orphaned purchase validation failure`() = runTest {
        fakeBillingRepository.unconsumedPurchases.add(PurchaseInfo("orphan-bad", "credits_3", "order-1"))
        fakePurchaseVerifier.transientFailureTokens.add("orphan-bad")

        val result = manager.recover()

        // No crash; orphan is silently skipped (not added to pending store)
        assertEquals(0, result.creditsRecovered)
    }

    // ==================== Analytics ====================

    @Test
    fun `recover fires purchase_recovery_attempted analytics with correct counts`() = runTest {
        fakePendingStore.entries.add(PendingValidation("token-1", "credits_3", timestamp = now(), retryCount = 0))
        fakePendingStore.entries.add(PendingValidation("token-2", "credits_1", timestamp = now(), retryCount = 1))
        fakePurchaseVerifier.transientFailureTokens.add("token-2")

        manager.recover()

        assertEquals(1, fakeAnalyticsManager.recoveryAttemptedEvents.size)
        val event = fakeAnalyticsManager.recoveryAttemptedEvents.first()
        assertEquals(2, event.pendingCount)
        assertEquals(1, event.recoveredCount)
        assertEquals(0, event.expiredCount)
    }

    // ==================== Helpers ====================

    private fun now() = System.currentTimeMillis()
}

// ==========================================================================
// Fake dependencies for ValidationRecoveryManagerTest
// ==========================================================================

class FakeRecoveryPendingValidationStore : PendingValidationStore {
    val entries: MutableList<PendingValidation> = mutableListOf()

    override suspend fun save(pendingValidation: PendingValidation) {
        entries.removeAll { it.purchaseToken == pendingValidation.purchaseToken }
        entries.add(pendingValidation)
    }

    override suspend fun getAll(): List<PendingValidation> =
        entries.filter { !it.isExpired() }

    override suspend fun remove(purchaseToken: String) {
        entries.removeAll { it.purchaseToken == purchaseToken }
    }

    override suspend fun clear() {
        entries.clear()
    }
}

class FakeRecoveryBillingRepository : BillingRepository {
    var queryUnconsumedShouldFail = false
    val unconsumedPurchases: MutableList<PurchaseInfo> = mutableListOf()

    private val _products = MutableStateFlow<List<ProductInfo>>(emptyList())
    override val products: Flow<List<ProductInfo>> = _products
    override val purchaseUpdates: kotlinx.coroutines.flow.SharedFlow<PurchaseUpdate> = MutableSharedFlow()

    override suspend fun queryProducts(): Result<List<ProductInfo>> = Result.success(emptyList())

    override suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun queryUnacknowledgedPurchases(): Result<List<PurchaseInfo>> {
        if (queryUnconsumedShouldFail) return Result.failure(RuntimeException("Query failed"))
        return Result.success(unconsumedPurchases.toList())
    }

    override suspend fun checkPendingPurchases(pendingToken: String) =
        Result.success(com.rulebook.core.model.PendingPurchaseResolution.NotFound)

    override fun creditsForProduct(productId: String): Int? = when (productId) {
        "credits_1" -> 1
        "credits_3" -> 3
        "credits_10" -> 10
        else -> null
    }
}

class FakeRecoveryPurchaseVerifier : PurchaseVerifier {
    val transientFailureTokens: MutableSet<String> = mutableSetOf()
    val invalidTokens: MutableSet<String> = mutableSetOf()
    val alreadyProcessedTokens: MutableSet<String> = mutableSetOf()

    override suspend fun verifyAndConsume(
        purchaseToken: String,
        productId: String
    ): Result<VerificationResult> {
        if (purchaseToken in invalidTokens) {
            return Result.failure(PurchaseValidationException("Purchase is INVALID"))
        }
        if (purchaseToken in transientFailureTokens) {
            return Result.failure(RuntimeException("Network error"))
        }
        val credits = when (productId) {
            "credits_1" -> 1
            "credits_3" -> 3
            "credits_10" -> 10
            else -> return Result.failure(IllegalArgumentException("Unknown product: $productId"))
        }
        val status = if (purchaseToken in alreadyProcessedTokens) {
            VerificationStatus.ALREADY_PROCESSED
        } else {
            VerificationStatus.VALID
        }
        return Result.success(VerificationResult(credits, status))
    }
}

class FakeRecoveryCreditRepository : CreditRepository {
    var addedCredits = 0
    private val _balance = MutableStateFlow(0)
    override val creditBalance: Flow<Int> = _balance

    override suspend fun awardInitialCredits(amount: Int): Boolean = true
    override suspend fun deductCredit(): Boolean = true
    override suspend fun hasCredits(): Boolean = true

    override suspend fun addCredits(amount: Int): Boolean {
        addedCredits += amount
        _balance.value += amount
        return true
    }

    override suspend fun removeCredits(amount: Int): Int {
        val current = _balance.value
        val actualRemoved = minOf(current, amount)
        _balance.value = (current - amount).coerceAtLeast(0)
        return actualRemoved
    }
}

class FakeRecoveryPurchaseHistoryStore : PurchaseHistoryStore {
    val savedTokens: MutableList<String> = mutableListOf()
    val recentTokens: MutableSet<String> = mutableSetOf()
    val savedEntries: MutableList<com.rulebook.core.billing.history.PurchaseHistoryEntry> = mutableListOf()

    override suspend fun savePurchase(purchaseToken: String, productId: String) {
        savedTokens.add(purchaseToken)
        recentTokens.add(purchaseToken)
        savedEntries.add(com.rulebook.core.billing.history.PurchaseHistoryEntry(purchaseToken, productId))
    }

    override suspend fun getRecentTokens(): List<String> = recentTokens.toList()

    override suspend fun getRecentEntries(): List<com.rulebook.core.billing.history.PurchaseHistoryEntry> = savedEntries.toList()

    override suspend fun clear() {
        savedTokens.clear()
        recentTokens.clear()
        savedEntries.clear()
    }
}

data class RecoveryAttemptedEvent(
    val pendingCount: Int,
    val recoveredCount: Int,
    val expiredCount: Int
)

class FakeRecoveryAnalyticsManager : AnalyticsManager {
    val recoveryAttemptedEvents: MutableList<RecoveryAttemptedEvent> = mutableListOf()
    val trackedEvents: MutableList<Pair<String, Map<String, String>>> = mutableListOf()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        trackedEvents.add(name to properties)
    }

    override fun trackScreenView(screenName: String) {}

    override fun trackPurchaseRecoveryAttempted(pendingCount: Int, recoveredCount: Int, expiredCount: Int) {
        recoveryAttemptedEvents.add(RecoveryAttemptedEvent(pendingCount, recoveredCount, expiredCount))
    }
}
