package com.rulebook.feature.settings

import com.rulebook.core.datastore.ThemeMode

/**
 * UI state for the Settings screen.
 *
 * @param creditBalance The user's current credit balance.
 * @param themeMode The current theme mode selection.
 * @param isHapticsEnabled Whether haptic feedback is enabled (placeholder).
 * @param appVersion The current app version string.
 */
data class SettingsUiState(
    val creditBalance: Int = 0,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isHapticsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
)
