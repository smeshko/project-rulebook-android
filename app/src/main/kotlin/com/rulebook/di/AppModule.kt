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
import com.rulebook.feature.settings.di.settingsModule

/**
 * Aggregates all Koin modules from core and feature modules.
 * This list is loaded in RulebookApplication during app initialization.
 */
val appModules = listOf(
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
    settingsModule,
    onboardingModule,
    purchaseModule,
)
