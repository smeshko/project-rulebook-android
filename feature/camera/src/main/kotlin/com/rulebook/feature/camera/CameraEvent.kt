package com.rulebook.feature.camera

/**
 * Sealed class representing one-time navigation events from the Camera screen.
 *
 * These events are emitted via a Channel to ensure they are handled exactly once,
 * unlike StateFlow which would re-emit on recomposition. This follows the MVI
 * pattern for side effects.
 *
 * @see CameraViewModel.events
 */
sealed class CameraEvent {

    /**
     * Event indicating the user should proceed to image analysis.
     *
     * Emitted when the user has sufficient credits to perform a scan.
     *
     * @param imageUri The URI of the captured or selected image to analyze.
     */
    data class ProceedToAnalysis(val imageUri: String) : CameraEvent()

    /**
     * Event indicating the user should be navigated to the paywall.
     *
     * Emitted when the user attempts to scan but has no credits available.
     * The actual paywall UI is handled by Epic 8.
     */
    data object NavigateToPaywall : CameraEvent()
}
