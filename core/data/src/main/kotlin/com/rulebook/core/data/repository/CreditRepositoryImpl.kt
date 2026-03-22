package com.rulebook.core.data.repository

import com.rulebook.core.datastore.CreditPreferencesSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Implementation of [CreditRepository].
 *
 * Delegates to [CreditPreferencesSource] for data persistence.
 * In production, this wraps [com.rulebook.core.datastore.RulebookPreferences].
 *
 * @param preferencesSource The preferences data source for credit balance.
 */
class CreditRepositoryImpl(
    private val preferencesSource: CreditPreferencesSource
) : CreditRepository {

    override val creditBalance: Flow<Int> =
        preferencesSource.creditBalance

    override suspend fun awardInitialCredits(amount: Int): Boolean {
        return preferencesSource.awardInitialCreditsIfNeeded(amount)
    }

    override suspend fun deductCredit(): Boolean {
        return preferencesSource.deductCredit()
    }

    override suspend fun hasCredits(): Boolean {
        return creditBalance.first() > 0
    }

    override suspend fun addCredits(amount: Int): Boolean {
        return preferencesSource.addCredits(amount)
    }

    override suspend fun removeCredits(amount: Int): Int {
        return preferencesSource.removeCredits(amount)
    }

    override suspend fun setCreditBalance(balance: Int) {
        preferencesSource.setCreditBalance(balance)
    }
}
