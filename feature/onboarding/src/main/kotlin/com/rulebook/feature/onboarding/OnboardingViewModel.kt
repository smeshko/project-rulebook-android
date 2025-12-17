package com.rulebook.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * @param onboardingRepository Repository for persisting onboarding completion state.
 */
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)

    /**
     * The current page index in the onboarding pager.
     * 0 = Value Proposition, 1 = Getting Started
     */
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()

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
            _currentPage.value = _currentPage.value + 1
        }
    }

    /**
     * Updates the current page when the user swipes the pager.
     * Keeps the ViewModel in sync with the pager state.
     *
     * @param page The new page index.
     */
    fun onPageChanged(page: Int) {
        if (page in 0 until OnboardingPage.entries.size) {
            _currentPage.value = page
        }
    }

    /**
     * Called when the "Get Started" button is clicked on the last page.
     * Marks onboarding as completed and triggers navigation to the library.
     */
    fun onGetStartedClicked() {
        viewModelScope.launch {
            // Mark onboarding as completed
            onboardingRepository.setOnboardingCompleted(true)
            // Award initial credits (Story 3.4 - placeholder for now)
            // creditRepository.awardInitialCredits(3)
            // Navigate to Library
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    /**
     * Called when the "Skip" button is clicked.
     * Triggers the same completion flow as "Get Started".
     */
    fun onSkipClicked() {
        onGetStartedClicked()
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
