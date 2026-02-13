package com.rulebook.feature.settings

/**
 * UI state for the Settings screen.
 *
 * Contains placeholder states for settings options that will be
 * fully functional in Epic 9.
 *
 * @param creditBalance The user's current credit balance.
 * @param isDarkTheme Whether dark theme is enabled (placeholder).
 * @param isHapticsEnabled Whether haptic feedback is enabled (placeholder).
 * @param appVersion The current app version string.
 */
data class SettingsUiState(
    val creditBalance: Int = 0,
    val isDarkTheme: Boolean = false,
    val isHapticsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)
