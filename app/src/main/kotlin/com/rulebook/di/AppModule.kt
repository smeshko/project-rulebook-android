package com.rulebook.di

import com.rulebook.core.analytics.di.analyticsModule
import com.rulebook.core.common.di.commonModule
import com.rulebook.core.data.di.dataModule
import com.rulebook.core.database.di.databaseModule
import com.rulebook.core.datastore.di.dataStoreModule
import com.rulebook.core.network.di.networkModule
import com.rulebook.feature.camera.di.cameraModule
import com.rulebook.feature.library.di.libraryModule
import com.rulebook.feature.onboarding.di.onboardingModule
import com.rulebook.feature.purchase.di.purchaseModule
import com.rulebook.feature.rules.di.rulesModule
import com.rulebook.feature.scan.di.scanFlowModule
import com.rulebook.feature.settings.di.settingsModule
import com.rulebook.startup.StartupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * App-level Koin module for ViewModels that don't belong to a specific feature.
 */
val appModule = module {
    viewModel { StartupViewModel(get()) }
}

/**
 * Aggregates all Koin modules from core and feature modules.
 * This list is loaded in RulebookApplication during app initialization.
 */
val appModules = listOf(
    // App module
    appModule,
    // Core modules
    commonModule,
    databaseModule,
    networkModule,
    dataStoreModule,
    analyticsModule,
    dataModule,
    // Feature modules
    libraryModule,
    cameraModule,
    rulesModule,
    scanFlowModule,
    settingsModule,
    onboardingModule,
    purchaseModule,
)
