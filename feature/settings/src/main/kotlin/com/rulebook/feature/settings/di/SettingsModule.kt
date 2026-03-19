package com.rulebook.feature.settings.di

import com.rulebook.core.database.ClearableDatabase
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ResettablePreferences
import com.rulebook.core.datastore.ThemePreferencesSource
import com.rulebook.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        SettingsViewModel(
            get<CreditPreferencesSource>(),
            get<ThemePreferencesSource>(),
            get<HapticsPreferencesSource>(),
            get<ClearableDatabase>(),
            get<ResettablePreferences>()
        )
    }
}
