package com.rulebook.feature.onboarding

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
    private lateinit var fakeRepository: FakeOnboardingRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeOnboardingRepository()
        viewModel = OnboardingViewModel(fakeRepository)
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
    fun `onGetStartedClicked should mark onboarding as completed`() = runTest {
        viewModel.onGetStartedClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepository.completedValue)
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
    fun `onSkipClicked should mark onboarding as completed`() = runTest {
        viewModel.onSkipClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeRepository.completedValue)
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
}

/**
 * Fake implementation of OnboardingRepository for testing.
 */
private class FakeOnboardingRepository : OnboardingRepository {
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding

    var completedValue: Boolean = false
        private set

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        completedValue = completed
        _hasCompletedOnboarding.value = completed
    }
}
