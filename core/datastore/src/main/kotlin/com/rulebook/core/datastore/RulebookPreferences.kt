package com.rulebook.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rulebook_preferences")

class RulebookPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CREDIT_BALANCE = intPreferencesKey("credit_balance")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    val creditBalance: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[Keys.CREDIT_BALANCE] ?: 0 }

    suspend fun setCreditBalance(balance: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CREDIT_BALANCE] = balance
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            ThemeMode.fromString(preferences[Keys.THEME_MODE])
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.HAPTICS_ENABLED] ?: true }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAPTICS_ENABLED] = enabled
        }
    }
}
