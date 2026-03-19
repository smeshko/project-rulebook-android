package com.rulebook.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for theme mode preferences data source.
 *
 * Allows SettingsViewModel to be tested without DataStore dependency.
 * [RulebookPreferences] implements this interface in production.
 */
interface ThemePreferencesSource {

    /**
     * Flow of current theme mode setting.
     * Default value is [ThemeMode.SYSTEM].
     */
    val themeMode: Flow<ThemeMode>

    /**
     * Sets the theme mode.
     *
     * @param mode The theme mode to persist.
     */
    suspend fun setThemeMode(mode: ThemeMode)
}
