package com.rulebook.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.datastore.ThemePreferencesSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 *
 * Manages UI state for settings options and emits one-shot [SettingsEvent]s
 * for actions that require Android platform context (e.g., launching intents).
 *
 * @param creditPreferencesSource Preferences source for reading credit balance.
 * @param themePreferencesSource Preferences source for reading and writing theme mode.
 * @param hapticsPreferencesSource Preferences source for reading and writing haptics enabled state.
 */
class SettingsViewModel(
    private val creditPreferencesSource: CreditPreferencesSource,
    private val themePreferencesSource: ThemePreferencesSource,
    private val hapticsPreferencesSource: HapticsPreferencesSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // Collect credit balance from preferences
        // catch emits 0 on DataStore IOException to keep UI functional
        viewModelScope.launch {
            creditPreferencesSource.creditBalance
                .catch { emit(0) }
                .collect { balance ->
                    _uiState.update { it.copy(creditBalance = balance) }
                }
        }

        // Collect theme mode from preferences
        // catch emits SYSTEM on DataStore IOException to keep UI functional
        viewModelScope.launch {
            themePreferencesSource.themeMode
                .catch { emit(ThemeMode.SYSTEM) }
                .collect { mode ->
                    _uiState.update { it.copy(themeMode = mode) }
                }
        }

        // Collect haptics enabled state from preferences
        // catch emits true on DataStore IOException to keep UI functional
        viewModelScope.launch {
            hapticsPreferencesSource.hapticsEnabled
                .catch { emit(true) }
                .collect { enabled ->
                    _uiState.update { it.copy(isHapticsEnabled = enabled) }
                }
        }
    }

    /**
     * Selects the theme mode and persists the selection.
     * Updates local UI state immediately and persists to DataStore.
     */
    fun onThemeSelected(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
        viewModelScope.launch {
            themePreferencesSource.setThemeMode(mode)
        }
    }

    /**
     * Toggles the haptic feedback setting.
     * Updates local UI state immediately and persists to DataStore.
     */
    fun onHapticsToggle(enabled: Boolean) {
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
        viewModelScope.launch {
            hapticsPreferencesSource.setHapticsEnabled(enabled)
        }
    }

    /**
     * Clears all user data.
     * Placeholder - will be implemented in Epic 9.
     */
    fun onClearData() {
        // TODO: Implement in Epic 9
    }

    /**
     * Emits a [SettingsEvent.ContactSupport] event to open the email composer.
     */
    fun onContactUs() {
        viewModelScope.launch {
            _events.send(SettingsEvent.ContactSupport)
        }
    }

    /**
     * Emits a [SettingsEvent.ReportBug] event to open the bug report email composer.
     */
    fun onReportBug() {
        viewModelScope.launch {
            _events.send(SettingsEvent.ReportBug)
        }
    }

    /**
     * Emits a [SettingsEvent.RateApp] event to open the Play Store listing.
     */
    fun onRateApp() {
        viewModelScope.launch {
            _events.send(SettingsEvent.RateApp)
        }
    }

    /**
     * Opens the privacy policy page.
     * Placeholder - will be implemented in Epic 9.
     */
    fun onPrivacyPolicy() {
        // TODO: Implement in Epic 9
    }

    /**
     * Opens the terms of service page.
     * Placeholder - will be implemented in Epic 9.
     */
    fun onTermsOfService() {
        // TODO: Implement in Epic 9
    }
}
