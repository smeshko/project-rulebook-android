package com.rulebook.feature.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the onboarding screen.
 *
 * Manages the current page state for the HorizontalPager and provides
 * navigation actions for the onboarding flow.
 */
class OnboardingViewModel : ViewModel() {

    private val _currentPage = MutableStateFlow(0)

    /**
     * The current page index in the onboarding pager.
     * 0 = Value Proposition, 1 = Getting Started
     */
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

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
}
