package com.rulebook.feature.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.data.repository.CreditRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val ARG_IMAGE_URI = "imageUri"

/**
 * ViewModel for the scan flow screen.
 *
 * Manages the scan flow state machine:
 * 1. Check credit balance on initialization
 * 2. Navigate to paywall if credits == 0
 * 3. Proceed to image analysis if credits > 0
 * 4. Track analytics events
 *
 * Note: Credits are NOT deducted here - deduction happens in Story 5.7
 * after successful rules generation.
 *
 * @param creditRepository Repository for checking credit balance.
 * @param analyticsManager Manager for tracking analytics events.
 * @param savedStateHandle Handle for accessing navigation arguments.
 */
class ScanFlowViewModel(
    private val creditRepository: CreditRepository,
    private val analyticsManager: AnalyticsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUri: String = checkNotNull(savedStateHandle[ARG_IMAGE_URI]) {
        "ScanFlowViewModel requires imageUri argument"
    }

    private val _uiState = MutableStateFlow<ScanFlowUiState>(ScanFlowUiState.CheckingCredits)
    val uiState: StateFlow<ScanFlowUiState> = _uiState.asStateFlow()

    init {
        checkCreditsAndProceed()
    }

    /**
     * Checks the user's credit balance and transitions to the appropriate state.
     *
     * - If credits > 0: Transition to ReadyToAnalyze and track analytics event
     * - If credits == 0: Transition to NavigatingToPaywall
     * - On error: Transition to Error state
     */
    private fun checkCreditsAndProceed() {
        viewModelScope.launch {
            try {
                val balance = creditRepository.creditBalance.first()

                if (balance > 0) {
                    _uiState.value = ScanFlowUiState.ReadyToAnalyze(
                        imageUri = imageUri,
                        creditBalance = balance
                    )
                    trackScanStarted(balance)
                } else {
                    _uiState.value = ScanFlowUiState.NavigatingToPaywall
                }
            } catch (e: Exception) {
                _uiState.value = ScanFlowUiState.Error("Failed to check credit balance")
            }
        }
    }

    /**
     * Tracks the scan_started analytics event with relevant properties.
     *
     * @param creditBalance The current credit balance when scan started.
     */
    private fun trackScanStarted(creditBalance: Int) {
        analyticsManager.trackEvent(
            name = "scan_started",
            properties = mapOf(
                "source" to "camera", // Fixed to camera for now; gallery support in future stories
                "credit_balance" to creditBalance.toString()
            )
        )
    }
}
