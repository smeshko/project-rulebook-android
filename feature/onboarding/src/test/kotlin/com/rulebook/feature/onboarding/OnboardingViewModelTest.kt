package com.rulebook.feature.onboarding

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for OnboardingViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeOnboardingRepository: FakeOnboardingRepository
    private lateinit var fakeAnalyticsManager: FakeAnalyticsManager
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeOnboardingRepository = FakeOnboardingRepository()
        fakeAnalyticsManager = FakeAnalyticsManager()
        viewModel = OnboardingViewModel(fakeOnboardingRepository, fakeAnalyticsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial currentPage should be 0`() = runTest {
        assertEquals(0, viewModel.currentPage.first())
    }

    @Test
    fun `onNextClicked should advance to page 1 from page 0`() = runTest {
        viewModel.onNextClicked()

        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onNextClicked should not advance beyond last page`() = runTest {
        // Advance to last page
        viewModel.onNextClicked()
        assertEquals(1, viewModel.currentPage.first())

        // Try to advance further - should stay on page 1
        viewModel.onNextClicked()
        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should update current page`() = runTest {
        viewModel.onPageChanged(1)

        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should not accept invalid page numbers`() = runTest {
        // Try negative page
        viewModel.onPageChanged(-1)
        assertEquals(0, viewModel.currentPage.first())

        // Try page beyond range
        viewModel.onPageChanged(5)
        assertEquals(0, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should accept page 0`() = runTest {
        viewModel.onPageChanged(1)

        viewModel.onPageChanged(0)

        assertEquals(0, viewModel.currentPage.first())
    }

    @Test
    fun `onGetStartedClicked should mark onboarding as completed and award credits atomically`() = runTest {
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeOnboardingRepository.completedValue)
        assertEquals(3, fakeOnboardingRepository.creditBalance)
        assertTrue(fakeOnboardingRepository.atomicCompletionCalled)
    }

    @Test
    fun `onGetStartedClicked should emit NavigateToLibrary event`() = runTest {
        var receivedEvent: OnboardingNavigationEvent? = null

        // Collect events in a non-blocking way
        val job = launch {
            viewModel.navigationEvent.collect { event ->
                receivedEvent = event
            }
        }

        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(OnboardingNavigationEvent.NavigateToLibrary, receivedEvent)
        job.cancel()
    }

    @Test
    fun `onSkipClicked should mark onboarding as completed and award credits atomically`() = runTest {
        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeOnboardingRepository.completedValue)
        assertEquals(3, fakeOnboardingRepository.creditBalance)
        assertTrue(fakeOnboardingRepository.atomicCompletionCalled)
    }

    @Test
    fun `atomic completion is idempotent - second completion does not double credits`() = runTest {
        // First completion
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, fakeOnboardingRepository.creditBalance)

        // Simulate second completion (should not double credits due to idempotency)
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, fakeOnboardingRepository.creditBalance)
    }

    @Test
    fun `onSkipClicked should emit NavigateToLibrary event`() = runTest {
        var receivedEvent: OnboardingNavigationEvent? = null

        val job = launch {
            viewModel.navigationEvent.collect { event ->
                receivedEvent = event
            }
        }

        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(OnboardingNavigationEvent.NavigateToLibrary, receivedEvent)
        job.cancel()
    }

    // Analytics Tests

    @Test
    fun `onboarding_started event fires on ViewModel init`() = runTest {
        // ViewModel is created in setup, so event should already be tracked
        val startedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_started" }

        assertEquals(1, startedEvents.size)
    }

    @Test
    fun `onboarding_page_viewed event fires for page 1 on init`() = runTest {
        val pageViewedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_page_viewed" }

        assertEquals(1, pageViewedEvents.size)
        assertEquals("1", pageViewedEvents[0].properties["page"])
    }

    @Test
    fun `onboarding_page_viewed event fires when page changes`() = runTest {
        // Clear initial events
        fakeAnalyticsManager.clear()

        viewModel.onPageChanged(1)

        val pageViewedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_page_viewed" }
        assertEquals(1, pageViewedEvents.size)
        assertEquals("2", pageViewedEvents[0].properties["page"])
    }

    @Test
    fun `onboarding_page_viewed event does not fire when page is same`() = runTest {
        // Clear initial events
        fakeAnalyticsManager.clear()

        // Change to same page (0)
        viewModel.onPageChanged(0)

        val pageViewedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_page_viewed" }
        assertEquals(0, pageViewedEvents.size)
    }

    @Test
    fun `onboarding_completed event fires on Get Started click`() = runTest {
        // Clear initial events
        fakeAnalyticsManager.clear()

        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val completedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_completed" }
        assertEquals(1, completedEvents.size)
        assertEquals("get_started", completedEvents[0].properties["method"])
    }

    @Test
    fun `onboarding_skipped event fires on Skip click with page 1`() = runTest {
        // Clear initial events
        fakeAnalyticsManager.clear()

        // On page 0 (page 1 in 1-indexed)
        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val skippedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_skipped" }
        assertEquals(1, skippedEvents.size)
        assertEquals("1", skippedEvents[0].properties["page"])
    }

    @Test
    fun `onboarding_skipped event fires on Skip click with page 2`() = runTest {
        // Clear initial events
        fakeAnalyticsManager.clear()

        // Navigate to page 2
        viewModel.onPageChanged(1)
        fakeAnalyticsManager.clear() // Clear the page view event

        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        val skippedEvents = fakeAnalyticsManager.trackedEvents.filter { it.name == "onboarding_skipped" }
        assertEquals(1, skippedEvents.size)
        assertEquals("2", skippedEvents[0].properties["page"])
    }

    @Test
    fun `analytics events fire in correct order`() = runTest {
        // Verify the order: onboarding_started, then onboarding_page_viewed(1)
        assertEquals(2, fakeAnalyticsManager.trackedEvents.size)
        assertEquals("onboarding_started", fakeAnalyticsManager.trackedEvents[0].name)
        assertEquals("onboarding_page_viewed", fakeAnalyticsManager.trackedEvents[1].name)
        assertEquals("1", fakeAnalyticsManager.trackedEvents[1].properties["page"])
    }
}

/**
 * Fake implementation of OnboardingRepository for testing.
 * Supports the atomic completeOnboardingWithCredits operation.
 */
private class FakeOnboardingRepository : OnboardingRepository {
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding

    var completedValue: Boolean = false
        private set

    var creditBalance: Int = 0
        private set

    var atomicCompletionCalled: Boolean = false
        private set

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        completedValue = completed
        _hasCompletedOnboarding.value = completed
    }

    override suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
        atomicCompletionCalled = true
        return if (!completedValue) {
            completedValue = true
            creditBalance = creditAmount
            _hasCompletedOnboarding.value = true
            true
        } else {
            false
        }
    }
}

/**
 * Fake implementation of AnalyticsManager for testing.
 * Records all tracked events for verification in tests.
 */
private class FakeAnalyticsManager : AnalyticsManager {

    data class TrackedEvent(
        val name: String,
        val properties: Map<String, String>
    )

    private val _trackedEvents = mutableListOf<TrackedEvent>()
    val trackedEvents: List<TrackedEvent> get() = _trackedEvents.toList()

    private val _trackedScreenViews = mutableListOf<String>()
    val trackedScreenViews: List<String> get() = _trackedScreenViews.toList()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        _trackedEvents.add(TrackedEvent(name, properties))
    }

    override fun trackScreenView(screenName: String) {
        _trackedScreenViews.add(screenName)
    }

    fun clear() {
        _trackedEvents.clear()
        _trackedScreenViews.clear()
    }
}
