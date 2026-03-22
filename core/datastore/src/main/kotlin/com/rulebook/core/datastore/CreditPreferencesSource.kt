package com.rulebook.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for credit balance preferences data source.
 *
 * Allows CreditRepository to be tested without DataStore dependency.
 * [RulebookPreferences] implements this interface in production.
 */
interface CreditPreferencesSource {

    /**
     * Flow of current credit balance.
     * Default value is 0.
     */
    val creditBalance: Flow<Int>

    /**
     * Awards initial credits if the current balance is 0.
     * This is an idempotent operation - calling it when credits already
     * exist will not modify the balance.
     *
     * @param amount The number of credits to award.
     * @return true if credits were awarded, false if balance was already positive.
     */
    suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean

    /**
     * Deducts one credit from the balance.
     *
     * @return true if a credit was deducted, false if balance was already 0.
     */
    suspend fun deductCredit(): Boolean

    /**
     * Adds credits to the current balance.
     *
     * @param amount The number of credits to add.
     * @return true if credits were added successfully.
     */
    suspend fun addCredits(amount: Int): Boolean

    /**
     * Removes credits from the current balance, clamping to 0 if insufficient.
     *
     * @param amount The number of credits to remove.
     * @return The actual number of credits removed (may be less than [amount] if balance is low).
     */
    suspend fun removeCredits(amount: Int): Int
}
