package com.rulebook.feature.camera

/**
 * Represents the navigation decision after credit balance check.
 *
 * This sealed class implements the credit-gating pattern, where navigation
 * to premium features (game scanning and rules generation) is controlled
 * by the user's available credit balance.
 *
 * ## Usage Pattern
 * ```kotlin
 * val action = viewModel.checkCreditsAndNavigate()
 * when (action) {
 *     NavigationAction.ProceedToProcessing -> navigateToProcessing(imageUri)
 *     NavigationAction.ShowPaywall -> navigateToPaywall()
 * }
 * ```
 *
 * ## Credit Flow
 * - Credits are **checked** before navigation (Story 5.1)
 * - Credits are **NOT deducted** until successful save (Story 5.7)
 * - This prevents charging users for failed processing attempts
 *
 * @see CameraViewModel.checkCreditsAndNavigate
 */
sealed class NavigationAction {
    /**
     * User has sufficient credits (balance > 0) to proceed to processing.
     * Navigate to the game recognition and rules generation flow.
     */
    data object ProceedToProcessing : NavigationAction()

    /**
     * User has insufficient credits (balance = 0) to proceed.
     * Navigate to the paywall/purchase screen to acquire more credits.
     */
    data object ShowPaywall : NavigationAction()
}
