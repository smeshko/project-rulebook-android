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

    /**
     * Completes onboarding and awards initial credits in a single atomic operation.
     *
     * This is the preferred method to use when completing onboarding as it ensures
     * both the completion flag and credit balance are set together atomically.
     * This prevents partial state updates if the app crashes mid-operation.
     *
     * This operation is idempotent - if onboarding was already completed,
     * no changes are made and the method returns false.
     *
     * @param creditAmount The number of credits to award to the user.
     * @return true if onboarding was completed and credits were awarded,
     *         false if onboarding was already completed.
     */
    suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean
}
