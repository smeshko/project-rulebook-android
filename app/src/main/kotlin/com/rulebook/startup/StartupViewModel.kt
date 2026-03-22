package com.rulebook.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.billing.recovery.ValidationRecoveryManager
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for determining the startup destination and running
 * background recovery for purchases that failed validation during previous sessions.
 *
 * Reads the onboarding completion status from [OnboardingRepository]
 * and exposes the appropriate startup destination. This ViewModel is
 * used to determine where to navigate when the app launches.
 *
 * The [isLoading] state can be used to keep the splash screen visible
 * until the destination is determined, preventing any flash of the
 * wrong screen.
 *
 * Recovery runs non-blocking on [Dispatchers.IO] — app startup is never delayed.
 * If credits are recovered, a [RecoveryEvent.CreditsRecovered] event is emitted
 * via [recoveryEvents] for the UI to display a notification.
 *
 * @param onboardingRepository Repository for onboarding state access.
 * @param validationRecoveryManager Manager for app-launch purchase recovery (Story 10.4).
 */
class StartupViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val validationRecoveryManager: ValidationRecoveryManager
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

    private val _recoveryEvents = Channel<RecoveryEvent>(Channel.BUFFERED)

    /**
     * One-shot events for recovery outcomes that require UI feedback.
     *
     * Emits [RecoveryEvent.CreditsRecovered] if credits were recovered from a
     * previous purchase that failed validation. Silent if no credits were recovered.
     */
    val recoveryEvents = _recoveryEvents.receiveAsFlow()

    init {
        determineStartupDestination()
        runValidationRecovery()
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

    private fun runValidationRecovery() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = validationRecoveryManager.recover()
                if (result.creditsRecovered > 0) {
                    _recoveryEvents.send(RecoveryEvent.CreditsRecovered(result.creditsRecovered))
                }
            } catch (e: Exception) {
                // Recovery failure must never crash the app — log and continue
                android.util.Log.e("StartupViewModel", "Validation recovery failed unexpectedly", e)
            }
        }
    }
}

/**
 * Events emitted by [StartupViewModel] for recovery outcomes that require UI feedback.
 */
sealed class RecoveryEvent {
    /** Credits were recovered from a previous purchase that failed validation. */
    data class CreditsRecovered(val credits: Int) : RecoveryEvent()
}
