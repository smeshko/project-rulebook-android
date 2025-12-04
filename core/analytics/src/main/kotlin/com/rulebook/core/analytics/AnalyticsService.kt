package com.rulebook.core.analytics

import android.app.Application

/**
 * Analytics service wrapper for TelemetryDeck.
 * TODO: Implement TelemetryDeck integration in Story 1.6
 */
class AnalyticsService(private val application: Application) {

    fun trackEvent(name: String, parameters: Map<String, String> = emptyMap()) {
        // TODO: TelemetryDeck.signal(name, parameters)
    }

    fun trackScreenView(screenName: String) {
        trackEvent("screen_view", mapOf("screen_name" to screenName))
    }
}
