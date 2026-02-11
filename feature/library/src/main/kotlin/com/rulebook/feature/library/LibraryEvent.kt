package com.rulebook.feature.library

/**
 * Sealed class representing one-time events from the Library screen.
 *
 * These events are emitted via a Channel to ensure they are handled exactly once,
 * following the same MVI pattern as GenerationEvent.
 *
 * @see LibraryViewModel.events
 */
sealed class LibraryEvent {

    /**
     * Event indicating a snackbar message should be shown to the user.
     *
     * @param message The message to display in the snackbar.
     */
    data class ShowSnackbar(val message: String) : LibraryEvent()
}
