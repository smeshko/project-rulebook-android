package com.rulebook.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the onboarding screen.
 *
 * Manages the current page state for the HorizontalPager and provides
 * navigation actions for the onboarding flow.
 *
 * @param onboardingRepository Repository for persisting onboarding completion state
 *                             and awarding initial credits atomically.
 * @param analyticsManager Manager for tracking analytics events.
 */
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    /**
     * Guard to prevent multiple completion attempts from double-taps.
     * Set to true when completion is in progress to prevent duplicate navigation events.
     */
    private var isCompleting = false

    private val _currentPage = MutableStateFlow(0)

    /**
     * The current page index in the onboarding pager.
     * 0 = Value Proposition, 1 = Getting Started
     */
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()

    init {
        // Track onboarding started when ViewModel is created (screen appears)
        analyticsManager.trackOnboardingStarted()
        // Track initial page view (page 1, 1-indexed for analytics)
        analyticsManager.trackOnboardingPageViewed(pageNumber = 1)
    }

    /**
     * Flow of navigation events for the onboarding screen.
     * Emitted when onboarding is completed and the user should navigate to the library.
     */
    val navigationEvent = _navigationEvent.receiveAsFlow()

    /**
     * Advances to the next page if not on the last page.
     * Called when the "Next" button is clicked.
     */
    fun onNextClicked() {
        val maxPage = OnboardingPage.entries.size - 1
        if (_currentPage.value < maxPage) {
            val newPage = _currentPage.value + 1
            _currentPage.value = newPage
            // Track page view (1-indexed for analytics)
            analyticsManager.trackOnboardingPageViewed(pageNumber = newPage + 1)
        }
    }

    /**
     * Updates the current page when the user swipes the pager.
     * Keeps the ViewModel in sync with the pager state and tracks page views.
     *
     * @param page The new page index.
     */
    fun onPageChanged(page: Int) {
        if (page in 0 until OnboardingPage.entries.size && page != _currentPage.value) {
            _currentPage.value = page
            // Track page view (1-indexed for analytics)
            analyticsManager.trackOnboardingPageViewed(pageNumber = page + 1)
        }
    }

    /**
     * Called when the "Get Started" button is clicked on the last page.
     * Marks onboarding as completed, awards initial credits, and triggers
     * navigation to the library.
     *
     * Uses atomic transaction to ensure both onboarding completion and credit
     * award happen together, preventing partial state updates.
     *
     * Guards against double-taps by checking [isCompleting] flag before proceeding.
     * Only the first tap will trigger completion and navigation.
     */
    fun onGetStartedClicked() {
        if (isCompleting) return
        isCompleting = true

        // Track completion event before navigation
        analyticsManager.trackOnboardingCompleted()
        viewModelScope.launch {
            // Complete onboarding and award credits atomically
            // This is idempotent - safe to call multiple times
            onboardingRepository.completeOnboardingWithCredits(INITIAL_CREDITS)
            // Navigate to Library
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    companion object {
        /**
         * Number of credits awarded to new users upon completing onboarding.
         */
        const val INITIAL_CREDITS = 3
    }

    /**
     * Called when the "Skip" button is clicked.
     * Tracks the skip event and completes onboarding.
     */
    fun onSkipClicked() {
        // Track skip event with current page (1-indexed for analytics)
        analyticsManager.trackOnboardingSkipped(pageNumber = _currentPage.value + 1)
        viewModelScope.launch {
            // Complete onboarding and award credits atomically
            // This is idempotent - safe to call multiple times
            onboardingRepository.completeOnboardingWithCredits(INITIAL_CREDITS)
            // Navigate to Library
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }
}

/**
 * Navigation events emitted by the onboarding ViewModel.
 */
sealed class OnboardingNavigationEvent {
    /**
     * Navigate to the library screen after completing onboarding.
     */
    data object NavigateToLibrary : OnboardingNavigationEvent()
}
