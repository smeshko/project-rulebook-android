package com.rulebook.feature.scan

/**
 * UI state for the scan flow screen.
 *
 * This sealed interface represents the different states of the scan flow
 * as the user progresses from credit check through image analysis to rules generation.
 */
sealed interface ScanFlowUiState {
    /**
     * Initial state while checking if user has sufficient credits.
     */
    data object CheckingCredits : ScanFlowUiState

    /**
     * User has zero credits - navigating to paywall screen.
     */
    data object NavigatingToPaywall : ScanFlowUiState

    /**
     * User has credits and is ready to proceed with image analysis.
     *
     * @property imageUri The URI of the captured/selected image to analyze.
     * @property creditBalance The current number of credits available.
     */
    data class ReadyToAnalyze(
        val imageUri: String,
        val creditBalance: Int
    ) : ScanFlowUiState

    /**
     * An error occurred during credit check.
     *
     * @property message The error message to display to the user.
     */
    data class Error(val message: String) : ScanFlowUiState
}
