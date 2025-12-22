package com.rulebook.feature.camera

/**
 * Represents navigation actions from the camera screen based on credit check.
 *
 * Used to determine navigation flow after photo capture.
 */
sealed class NavigationAction {
    /**
     * User has credits - proceed to image processing screen.
     */
    data object ProceedToProcessing : NavigationAction()

    /**
     * User has no credits - show paywall screen.
     */
    data object ShowPaywall : NavigationAction()
}
