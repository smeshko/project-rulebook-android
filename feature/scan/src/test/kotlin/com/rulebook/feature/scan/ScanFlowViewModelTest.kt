package com.rulebook.feature.scan

import androidx.lifecycle.SavedStateHandle
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.CreditRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScanFlowViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCreditRepository: FakeCreditRepository
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCreditRepository = FakeCreditRepository()
        fakeAnalyticsManager = FakeAnalyticsManager()
    }

    @Test
    fun `when credits are zero, navigate to paywall`() = runTest {
        // Given
        fakeCreditRepository.setBalance(0)
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "test://image.jpg"))

        // When
        val viewModel = ScanFlowViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            savedStateHandle = savedStateHandle
        )

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is ScanFlowUiState.NavigatingToPaywall)
    }

    @Test
    fun `when credits are positive, proceed to analyze`() = runTest {
        // Given
        fakeCreditRepository.setBalance(3)
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "test://image.jpg"))

        // When
        val viewModel = ScanFlowViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            savedStateHandle = savedStateHandle
        )

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is ScanFlowUiState.ReadyToAnalyze)
        assertEquals(3, (state as ScanFlowUiState.ReadyToAnalyze).creditBalance)
        assertEquals("test://image.jpg", state.imageUri)
    }

    @Test
    fun `credits are not deducted during check`() = runTest {
        // Given
        fakeCreditRepository.setBalance(5)
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "test://image.jpg"))

        // When
        val viewModel = ScanFlowViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            savedStateHandle = savedStateHandle
        )

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0, fakeCreditRepository.deductCallCount)
    }

    @Test
    fun `analytics event tracked when credits positive`() = runTest {
        // Given
        fakeCreditRepository.setBalance(3)
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "test://image.jpg"))

        // When
        val viewModel = ScanFlowViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            savedStateHandle = savedStateHandle
        )

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, fakeAnalyticsManager.trackedEvents.size)
        val event = fakeAnalyticsManager.trackedEvents.first()
        assertEquals("scan_started", event.name)
        assertEquals("camera", event.properties["source"])
        assertEquals("3", event.properties["credit_balance"])
    }

    @Test
    fun `analytics event not tracked when credits zero`() = runTest {
        // Given
        fakeCreditRepository.setBalance(0)
        val savedStateHandle = SavedStateHandle(mapOf("imageUri" to "test://image.jpg"))

        // When
        val viewModel = ScanFlowViewModel(
            creditRepository = fakeCreditRepository,
            analyticsManager = fakeAnalyticsManager,
            savedStateHandle = savedStateHandle
        )

        // Advance past initialization
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0, fakeAnalyticsManager.trackedEvents.size)
    }
}

// Fake implementations for testing
class FakeCreditRepository : CreditRepository {
    private val balanceFlow = MutableStateFlow(0)
    var deductCallCount = 0
        private set

    override val creditBalance = balanceFlow

    fun setBalance(balance: Int) {
        balanceFlow.value = balance
    }

    override suspend fun awardInitialCredits(amount: Int): Boolean = true

    override suspend fun deductCredit(): Boolean {
        deductCallCount++
        return true
    }

    override suspend fun hasCredits(): Boolean = balanceFlow.value > 0
}

class FakeAnalyticsManager : AnalyticsManager {
    data class TrackedEvent(val name: String, val properties: Map<String, String>)

    val trackedEvents = mutableListOf<TrackedEvent>()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        trackedEvents.add(TrackedEvent(name, properties))
    }

    override fun trackScreenView(screenName: String) {
        trackedEvents.add(TrackedEvent("screen_view", mapOf("screen_name" to screenName)))
    }
}
