package com.rulebook.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for determining the startup destination.
 *
 * Reads the onboarding completion status from [OnboardingRepository]
 * and exposes the appropriate startup destination. This ViewModel is
 * used to determine where to navigate when the app launches.
 *
 * The [isLoading] state can be used to keep the splash screen visible
 * until the destination is determined, preventing any flash of the
 * wrong screen.
 *
 * @param onboardingRepository Repository for onboarding state access.
 */
class StartupViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {

    private val _startupDestination = MutableStateFlow<StartupDestination?>(null)

    /**
     * The determined startup destination.
     *
     * Initially `null` while loading, then set to either
     * [StartupDestination.Onboarding] or [StartupDestination.Library].
     */
    val startupDestination: StateFlow<StartupDestination?> = _startupDestination.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    /**
     * Whether the startup destination is still being determined.
     *
     * `true` initially, becomes `false` once [startupDestination] is set.
     * Use this to keep the splash screen visible during determination.
     */
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        determineStartupDestination()
    }

    private fun determineStartupDestination() {
        viewModelScope.launch {
            try {
                val hasCompletedOnboarding = onboardingRepository.hasCompletedOnboarding.first()

                _startupDestination.value = if (hasCompletedOnboarding) {
                    StartupDestination.Library
                } else {
                    StartupDestination.Onboarding
                }
            } catch (e: Exception) {
                // If DataStore read fails (IO error, corruption, etc.), default to onboarding
                // to ensure the app remains functional rather than showing an infinite splash
                _startupDestination.value = StartupDestination.Onboarding
            } finally {
                _isLoading.value = false
            }
        }
    }
}
