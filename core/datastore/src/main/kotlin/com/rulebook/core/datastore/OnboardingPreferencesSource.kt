package com.rulebook.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for onboarding preferences data source.
 *
 * Allows OnboardingRepository to be tested without DataStore dependency.
 * [RulebookPreferences] implements this interface in production.
 */
interface OnboardingPreferencesSource {

    /**
     * Flow of onboarding completion status.
     */
    val hasCompletedOnboarding: Flow<Boolean>

    /**
     * Sets the onboarding completion status.
     */
    suspend fun setOnboardingCompleted(completed: Boolean)
}
