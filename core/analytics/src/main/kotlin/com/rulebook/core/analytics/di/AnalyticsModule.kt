package com.rulebook.core.analytics.di

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.analytics.BuildConfig
import com.rulebook.core.analytics.TelemetryDeckAnalyticsManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsManager> {
        TelemetryDeckAnalyticsManager(
            context = androidContext(),
            appId = BuildConfig.TELEMETRY_APP_ID,
            showDebugLogs = BuildConfig.DEBUG
        )
    }
}
