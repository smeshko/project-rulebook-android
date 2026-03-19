package com.rulebook.feature.settings

/**
 * One-shot UI events emitted by [SettingsViewModel] for actions
 * that require Android platform context (intents, etc.).
 */
sealed interface SettingsEvent {
    data object ContactSupport : SettingsEvent
    data object ReportBug : SettingsEvent
    data object RateApp : SettingsEvent
}
