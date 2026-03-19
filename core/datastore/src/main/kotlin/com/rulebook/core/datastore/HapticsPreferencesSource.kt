package com.rulebook.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for haptic feedback preferences data source.
 *
 * Allows SettingsViewModel to be tested without DataStore dependency.
 * [RulebookPreferences] implements this interface in production.
 */
interface HapticsPreferencesSource {

    /**
     * Flow of current haptic feedback enabled status.
     * Default value is `true`.
     */
    val hapticsEnabled: Flow<Boolean>

    /**
     * Sets whether haptic feedback is enabled.
     *
     * @param enabled Whether haptic feedback should be enabled.
     */
    suspend fun setHapticsEnabled(enabled: Boolean)
}
