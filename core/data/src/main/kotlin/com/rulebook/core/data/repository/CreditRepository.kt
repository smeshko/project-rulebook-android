package com.rulebook.core.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user credit balance management.
 *
 * Provides access to the credit balance and operations for awarding
 * and deducting credits. Credits are used to enable scanning functionality.
 */
interface CreditRepository {

    /**
     * Flow of the current credit balance.
     *
     * Emits the current number of credits available to the user.
     * Default value is 0 for new users.
     */
    val creditBalance: Flow<Int>

    /**
     * Awards initial credits to new users if they haven't received any yet.
     *
     * This operation is idempotent - calling it when credits already exist
     * will not modify the balance. This ensures first-time users receive
     * credits exactly once, even if called multiple times.
     *
     * @param amount The number of credits to award (typically 3 for new users).
     * @return true if credits were awarded, false if user already had credits.
     */
    suspend fun awardInitialCredits(amount: Int): Boolean

    /**
     * Deducts one credit from the user's balance.
     *
     * Used when the user performs a scan operation.
     *
     * @return true if a credit was successfully deducted, false if balance was 0.
     */
    suspend fun deductCredit(): Boolean

    /**
     * Checks if the user has any credits available.
     *
     * @return true if the user has at least one credit, false otherwise.
     */
    suspend fun hasCredits(): Boolean
}
