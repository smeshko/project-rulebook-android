package com.rulebook.feature.generation

/**
 * Sealed class representing one-time navigation events from the Generation screen.
 *
 * These events are emitted via a Channel to ensure they are handled exactly once,
 * following the same MVI pattern as [CameraEvent] from Story 5.1.
 *
 * @see GenerationViewModel.events
 */
sealed class GenerationEvent {

    /**
     * Event indicating the scan was cancelled by the user.
     * The UI should navigate back to the camera screen.
     */
    data object Cancelled : GenerationEvent()

    /**
     * Event indicating the user chose to retry from the error screen.
     * The UI should navigate back to the camera screen to take a new photo.
     * Story 5.9: Retry failed recognition.
     */
    data object RetryFromCamera : GenerationEvent()

    /**
     * Event indicating an error occurred during the scan.
     * The UI should show an error state or navigate to retry.
     *
     * @param message A user-friendly error message.
     */
    data class Error(val message: String) : GenerationEvent()

    /**
     * Event indicating the system is auto-proceeding due to high confidence.
     * The UI should briefly flash the identified game name.
     *
     * @param gameName The name of the identified game.
     */
    data class AutoProceeding(val gameName: String) : GenerationEvent()

    /**
     * Event indicating the game and rules were saved successfully.
     * The UI should navigate to the Rules display screen with the given game ID.
     *
     * @param gameId The ID of the saved game to display.
     */
    data class NavigateToRules(val gameId: String) : GenerationEvent()
}
