package com.rulebook.startup

import com.rulebook.core.billing.reconciliation.BalanceReconciliation
import com.rulebook.core.billing.reconciliation.ReconciliationResult
import com.rulebook.core.billing.recovery.RecoveryResult
import com.rulebook.core.billing.recovery.ValidationRecovery
import com.rulebook.core.billing.refund.RefundSync
import com.rulebook.core.billing.refund.RefundSyncResult
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for StartupViewModel.
 *
 * Tests startup destination determination based on onboarding status,
 * and recovery event emission when credits are recovered on launch.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StartupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeOnboardingRepository
    private lateinit var fakeRecoveryManager: FakeValidationRecovery
    private lateinit var viewModel: StartupViewModel

    private lateinit var fakeRefundSync: FakeRefundSync
    private lateinit var fakeBalanceReconciliation: FakeBalanceReconciliation

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeOnboardingRepository()
        fakeRecoveryManager = FakeValidationRecovery()
        fakeRefundSync = FakeRefundSync()
        fakeBalanceReconciliation = FakeBalanceReconciliation()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initial State Tests ====================

    @Test
    fun `initial state has null destination and isLoading true`() = runTest {
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        assertNull(viewModel.startupDestination.value)
        assertTrue(viewModel.isLoading.value)
    }

    // ==================== Destination Determination Tests ====================

    @Test
    fun `when onboarding not completed, destination is Onboarding`() = runTest {
        fakeRepository.setOnboardingCompletedSync(false)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(StartupDestination.Onboarding, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `when onboarding completed, destination is Library`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Loading State Tests ====================

    @Test
    fun `isLoading becomes false after destination is determined`() = runTest {
        fakeRepository.setOnboardingCompletedSync(false)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        assertTrue(viewModel.isLoading.value)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Error Handling Tests ====================

    @Test
    fun `when repository throws exception, destination defaults to Onboarding`() = runTest {
        val errorRepository = ErrorThrowingOnboardingRepository()
        viewModel = StartupViewModel(errorRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(StartupDestination.Onboarding, viewModel.startupDestination.value)
    }

    @Test
    fun `when repository throws exception, isLoading becomes false`() = runTest {
        val errorRepository = ErrorThrowingOnboardingRepository()
        viewModel = StartupViewModel(errorRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Recovery Tests ====================

    @Test
    fun `recovery does not block startup destination determination`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        fakeRecoveryManager.result = RecoveryResult(
            creditsRecovered = 3,
            pendingCount = 1,
            recoveredCount = 1,
            expiredCount = 0,
            failedCount = 0
        )
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        // Startup destination determined correctly despite recovery running
        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `recovery emits CreditsRecovered event when credits are recovered`() = runTest {
        fakeRecoveryManager.result = RecoveryResult(
            creditsRecovered = 5,
            pendingCount = 1,
            recoveredCount = 1,
            expiredCount = 0,
            failedCount = 0
        )
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        val event = viewModel.recoveryEvents.first()
        assertTrue(event is RecoveryEvent.CreditsRecovered)
        assertEquals(5, (event as RecoveryEvent.CreditsRecovered).credits)
    }

    @Test
    fun `recovery emits no event when no credits are recovered`() = runTest {
        fakeRecoveryManager.result = RecoveryResult(
            creditsRecovered = 0,
            pendingCount = 0,
            recoveredCount = 0,
            expiredCount = 0,
            failedCount = 0
        )
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        // Verify recovery ran exactly once
        assertEquals(1, fakeRecoveryManager.recoverCallCount)
    }

    @Test
    fun `recovery failure does not affect startup flow`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        fakeRecoveryManager.shouldThrow = true
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        // Startup destination is still determined correctly
        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Refund Sync Tests ====================

    @Test
    fun `refund sync emits CreditsRevoked event when credits are revoked`() = runTest {
        fakeRefundSync.result = RefundSyncResult(creditsRevoked = 3, tokensRefunded = 1)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        val event = viewModel.refundEvents.first()
        assertTrue(event is RefundEvent.CreditsRevoked)
        assertEquals(3, (event as RefundEvent.CreditsRevoked).credits)
    }

    @Test
    fun `refund sync emits no event when no credits are revoked`() = runTest {
        fakeRefundSync.result = RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(1, fakeRefundSync.checkRefundsCallCount)
    }

    @Test
    fun `refund sync failure does not affect startup flow`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        fakeRefundSync.shouldThrow = true
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        // Startup destination is still determined correctly despite refund sync failure
        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Balance Reconciliation Tests ====================

    @Test
    fun `reconciliation failure does not affect startup flow`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        fakeBalanceReconciliation.shouldThrow = true
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `reconciliation does not block startup destination determination`() = runTest {
        fakeRepository.setOnboardingCompletedSync(true)
        viewModel = StartupViewModel(fakeRepository, fakeRecoveryManager, fakeRefundSync, fakeBalanceReconciliation)

        advanceUntilIdle()

        assertEquals(StartupDestination.Library, viewModel.startupDestination.value)
        assertFalse(viewModel.isLoading.value)
        assertEquals(1, fakeBalanceReconciliation.reconcileCallCount)
    }
}

/**
 * Fake implementation of [RefundSync] for testing StartupViewModel.
 */
class FakeRefundSync : RefundSync {
    var result: RefundSyncResult = RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0)
    var shouldThrow = false
    var checkRefundsCallCount = 0

    override suspend fun checkRefunds(): RefundSyncResult {
        checkRefundsCallCount++
        if (shouldThrow) throw RuntimeException("Refund sync failed")
        return result
    }
}

/**
 * Fake implementation of [ValidationRecovery] for testing StartupViewModel.
 */
class FakeValidationRecovery : ValidationRecovery {
    var result: RecoveryResult = RecoveryResult(
        creditsRecovered = 0,
        pendingCount = 0,
        recoveredCount = 0,
        expiredCount = 0,
        failedCount = 0
    )
    var shouldThrow = false
    var recoverCallCount = 0

    override suspend fun recover(): RecoveryResult {
        recoverCallCount++
        if (shouldThrow) throw RuntimeException("Recovery failed")
        return result
    }
}

/**
 * Fake implementation of [OnboardingRepository] for testing.
 */
class FakeOnboardingRepository : OnboardingRepository {
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        _hasCompletedOnboarding.value = completed
    }

    override suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
        _hasCompletedOnboarding.value = true
        return true
    }

    fun setOnboardingCompletedSync(value: Boolean) {
        _hasCompletedOnboarding.value = value
    }
}

/**
 * Fake implementation that throws an exception when reading onboarding status.
 * Used to test error handling in StartupViewModel.
 */
class ErrorThrowingOnboardingRepository : OnboardingRepository {
    override val hasCompletedOnboarding: Flow<Boolean> = kotlinx.coroutines.flow.flow {
        throw java.io.IOException("Simulated DataStore IO error")
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        throw java.io.IOException("Simulated DataStore IO error")
    }

    override suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
        throw java.io.IOException("Simulated DataStore IO error")
    }
}

/**
 * Fake implementation of [BalanceReconciliation] for testing StartupViewModel.
 */
class FakeBalanceReconciliation : BalanceReconciliation {
    var shouldThrow = false
    var reconcileCallCount = 0

    override suspend fun reconcile(): ReconciliationResult {
        reconcileCallCount++
        if (shouldThrow) throw RuntimeException("Reconciliation failed")
        return ReconciliationResult(reconciled = false, localBalance = 0, serverBalance = 0, delta = 0)
    }
}

