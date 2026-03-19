package com.rulebook.feature.settings.di

import com.rulebook.core.database.RulebookDatabase
import com.rulebook.core.datastore.CreditPreferencesSource
import com.rulebook.core.datastore.HapticsPreferencesSource
import com.rulebook.core.datastore.ResettablePreferences
import com.rulebook.core.datastore.ThemePreferencesSource
import com.rulebook.feature.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel {
        val database = get<RulebookDatabase>()
        SettingsViewModel(
            creditPreferencesSource = get<CreditPreferencesSource>(),
            themePreferencesSource = get<ThemePreferencesSource>(),
            hapticsPreferencesSource = get<HapticsPreferencesSource>(),
            clearDatabase = { withContext(Dispatchers.IO) { database.clearAllTables() } },
            resettablePreferences = get<ResettablePreferences>()
        )
    }
}
