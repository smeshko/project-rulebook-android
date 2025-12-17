package com.rulebook.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for onboarding state management.
 *
 * Provides access to onboarding completion status and allows
 * marking onboarding as completed. Used to determine startup
 * navigation destination.
 */
interface OnboardingRepository {

    /**
     * Flow of onboarding completion status.
     *
     * Emits `true` if the user has completed onboarding,
     * `false` otherwise. Default value is `false`.
     */
    val hasCompletedOnboarding: Flow<Boolean>

    /**
     * Sets the onboarding completion status.
     *
     * @param completed Whether onboarding has been completed.
     */
    suspend fun setOnboardingCompleted(completed: Boolean)
}
