package com.rulebook.core.datastore

/**
 * Abstraction for resetting all preferences while preserving purchased credits.
 *
 * Implemented by [RulebookPreferences] to allow the Settings feature to reset
 * preferences without depending on the concrete DataStore implementation.
 */
interface ResettablePreferences {
    /**
     * Resets all preferences to defaults while preserving the credit balance.
     *
     * Clears all DataStore keys and re-writes the preserved credit balance
     * in a single atomic operation.
     */
    suspend fun reset()
}
