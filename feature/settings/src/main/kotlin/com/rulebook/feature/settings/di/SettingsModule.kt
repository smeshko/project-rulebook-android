package com.rulebook.feature.settings.di

import com.rulebook.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val settingsModule = module {
    viewModel { SettingsViewModel() }
}
