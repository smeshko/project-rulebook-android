package com.rulebook.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.GameRepository
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ResettablePreferences
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
 * @param clearDatabase Suspend function that wipes all Room database tables (called from IO thread internally).
 * @param resettablePreferences Preferences interface for resetting to defaults (preserving credits).
 * @param gameRepository Repository for accessing game data (used for games_count in analytics).
 * @param analyticsManager Analytics manager for tracking settings events.
 */
class SettingsViewModel(
    private val creditPreferencesSource: CreditPreferencesSource,
    private val themePreferencesSource: ThemePreferencesSource,
    private val hapticsPreferencesSource: HapticsPreferencesSource,
    private val clearDatabase: suspend () -> Unit,
    private val resettablePreferences: ResettablePreferences,
    private val gameRepository: GameRepository,
    private val analyticsManager: AnalyticsManager
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
        val previousTheme = _uiState.value.themeMode
        analyticsManager.trackSettingsThemeChanged(mode.name.lowercase(), previousTheme.name.lowercase())
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
        analyticsManager.trackSettingsHapticsChanged(enabled)
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
        viewModelScope.launch {
            hapticsPreferencesSource.setHapticsEnabled(enabled)
        }
    }

    /**
     * Updates the app version information displayed in the UI and used in email subjects.
     */
    fun updateVersionInfo(versionName: String, versionCode: String) {
        _uiState.update { it.copy(appVersion = versionName, appVersionCode = versionCode) }
    }

    /**
     * Shows the clear data confirmation dialog.
     */
    fun onShowClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = true) }
    }

    /**
     * Dismisses the clear data confirmation dialog without clearing data.
     */
    fun onDismissClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    /**
     * Clears all user data after confirmation.
     *
     * 1. Dismisses the confirmation dialog
     * 2. Clears all Room database tables via [clearDatabase]
     * 3. Resets DataStore preferences (preserving credit balance)
     * 4. Emits a success snackbar event
     * 5. Emits a navigate-to-onboarding event
     *
     * On failure, emits an error snackbar event.
     */
    fun onClearData() {
        _uiState.update { it.copy(showClearConfirmation = false) }
        viewModelScope.launch {
            val gamesCount = when (val result = gameRepository.getGames()) {
                is Result.Success -> result.data.size
                is Result.Error -> 0
            }
            try {
                clearDatabase()
            } catch (_: Exception) {
                _events.send(SettingsEvent.ShowSnackbar("Failed to clear data"))
                return@launch
            }
            try {
                resettablePreferences.reset()
            } catch (_: Exception) {
                // Database already cleared — proceed to onboarding despite preferences error
            }
            analyticsManager.trackSettingsDataCleared(gamesCount)
            _events.send(SettingsEvent.ShowSnackbar("All data cleared"))
            _events.send(SettingsEvent.NavigateToOnboarding)
        }
    }

    /**
     * Emits a [SettingsEvent.ContactSupport] event to open the email composer.
     */
    fun onContactUs() {
        analyticsManager.trackSettingsSupportTapped("contact")
        viewModelScope.launch {
            _events.send(SettingsEvent.ContactSupport)
        }
    }

    /**
     * Emits a [SettingsEvent.ReportBug] event to open the bug report email composer.
     */
    fun onReportBug() {
        analyticsManager.trackSettingsSupportTapped("bug")
        viewModelScope.launch {
            _events.send(SettingsEvent.ReportBug)
        }
    }

    /**
     * Emits a [SettingsEvent.RateApp] event to open the Play Store listing.
     */
    fun onRateApp() {
        analyticsManager.trackSettingsSupportTapped("rate")
        viewModelScope.launch {
            _events.send(SettingsEvent.RateApp)
        }
    }

    /**
     * Emits a [SettingsEvent.OpenPrivacyPolicy] event to open the privacy policy in the browser.
     */
    fun onPrivacyPolicy() {
        analyticsManager.trackSettingsSupportTapped("privacy")
        viewModelScope.launch {
            _events.send(SettingsEvent.OpenPrivacyPolicy)
        }
    }

    /**
     * Emits a [SettingsEvent.OpenTermsOfService] event to open the terms of service in the browser.
     */
    fun onTermsOfService() {
        analyticsManager.trackSettingsSupportTapped("terms")
        viewModelScope.launch {
            _events.send(SettingsEvent.OpenTermsOfService)
        }
    }
}
