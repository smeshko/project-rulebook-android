package com.rulebook.startup

/**
 * Represents the possible startup destinations for the app.
 *
 * Used by [StartupViewModel] to determine where to navigate
 * when the app launches based on onboarding completion status.
 */
sealed class StartupDestination {

    /**
     * Navigate to the onboarding flow.
     * Used when the user has not completed onboarding.
     */
    data object Onboarding : StartupDestination()

    /**
     * Navigate to the main library screen.
     * Used when the user has already completed onboarding.
     */
    data object Library : StartupDestination()
}
