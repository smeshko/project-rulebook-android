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

    /**
     * Completes onboarding and awards initial credits in a single atomic transaction.
     *
     * This is idempotent - if onboarding was already completed, no changes are made.
     * Both `hasCompletedOnboarding` and `creditBalance` are set together to prevent
     * partial state updates.
     *
     * @param creditAmount The number of credits to award.
     * @return true if onboarding was completed and credits were awarded,
     *         false if onboarding was already completed.
     */
    suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean
}
