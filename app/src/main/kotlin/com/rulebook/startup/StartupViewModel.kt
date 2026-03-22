package com.rulebook.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.billing.reconciliation.BalanceReconciliation
import com.rulebook.core.billing.recovery.ValidationRecovery
import com.rulebook.core.billing.refund.RefundSync
import com.rulebook.core.data.repository.OnboardingRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for determining the startup destination and running
 * background recovery for purchases that failed validation during previous sessions,
 * as well as detecting refunded purchases and revoking credits.
 *
 * Reads the onboarding completion status from [OnboardingRepository]
 * and exposes the appropriate startup destination. This ViewModel is
 * used to determine where to navigate when the app launches.
 *
 * The [isLoading] state can be used to keep the splash screen visible
 * until the destination is determined, preventing any flash of the
 * wrong screen.
 *
 * Recovery and refund sync run as independent coroutines — app startup is never delayed.
 * If credits are recovered or revoked, the corresponding event is emitted via
 * [recoveryEvents] or [refundEvents] for the UI to display a notification.
 *
 * @param onboardingRepository Repository for onboarding state access.
 * @param validationRecoveryManager Recovery contract for app-launch purchase recovery (Story 10.4).
 * @param refundSync Refund sync contract for app-launch refund detection (Story 10.5).
 * @param balanceReconciliation Balance reconciliation contract for app-launch credit sync (Story 10.7).
 */
class StartupViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val validationRecoveryManager: ValidationRecovery,
    private val refundSync: RefundSync,
    private val balanceReconciliation: BalanceReconciliation,
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

    private val _refundEvents = Channel<RefundEvent>(Channel.BUFFERED)

    /**
     * One-shot events for refund detection outcomes that require UI feedback.
     *
     * Emits [RefundEvent.CreditsRevoked] if credits were revoked for a refunded purchase.
     * Silent if no refunds were detected.
     */
    val refundEvents = _refundEvents.receiveAsFlow()

    init {
        determineStartupDestination()
        runValidationRecovery()
        runRefundSync()
        runBalanceReconciliation()
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
        viewModelScope.launch {
            try {
                val result = validationRecoveryManager.recover()
                if (result.creditsRecovered > 0) {
                    _recoveryEvents.send(RecoveryEvent.CreditsRecovered(result.creditsRecovered))
                }
            } catch (e: Exception) {
                // Recovery failure must never crash the app — silently swallow and continue
            }
        }
    }

    private fun runRefundSync() {
        viewModelScope.launch {
            try {
                val result = refundSync.checkRefunds()
                if (result.creditsRevoked > 0) {
                    _refundEvents.send(RefundEvent.CreditsRevoked(result.creditsRevoked))
                }
            } catch (e: Exception) {
                // Refund sync failure must never crash the app — silently swallow and continue
            }
        }
    }

    private fun runBalanceReconciliation() {
        viewModelScope.launch {
            try {
                balanceReconciliation.reconcile()
            } catch (_: Exception) {
                // Reconciliation failure must never crash the app — silently swallow and continue
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

/**
 * Events emitted by [StartupViewModel] for refund detection outcomes that require UI feedback.
 */
sealed class RefundEvent {
    /** Credits were revoked because a previous purchase was refunded. */
    data class CreditsRevoked(val credits: Int) : RefundEvent()
}
