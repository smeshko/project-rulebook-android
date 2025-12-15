package com.rulebook

import android.app.Application
import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.di.appModules
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class RulebookApplication : Application() {

    // Eagerly initialize analytics to ensure TelemetryDeck is configured on app start
    private val analyticsManager: AnalyticsManager by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@RulebookApplication)
            modules(appModules)
        }

        // Trigger eager initialization of analytics
        // This ensures TelemetryDeck is ready before any other components need it
        analyticsManager.trackEvent("app_launched")
    }
}
