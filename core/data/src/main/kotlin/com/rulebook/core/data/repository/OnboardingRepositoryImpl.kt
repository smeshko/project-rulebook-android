package com.rulebook.core.data.repository

import com.rulebook.core.datastore.OnboardingPreferencesSource
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of [OnboardingRepository].
 *
 * Delegates to [OnboardingPreferencesSource] for data persistence.
 * In production, this wraps [com.rulebook.core.datastore.RulebookPreferences].
 *
 * @param preferencesSource The preferences data source for onboarding state.
 */
class OnboardingRepositoryImpl(
    private val preferencesSource: OnboardingPreferencesSource
) : OnboardingRepository {

    override val hasCompletedOnboarding: Flow<Boolean> =
        preferencesSource.hasCompletedOnboarding

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesSource.setOnboardingCompleted(completed)
    }
}
