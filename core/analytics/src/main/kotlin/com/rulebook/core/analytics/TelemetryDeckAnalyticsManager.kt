package com.rulebook.core.analytics

import android.content.Context
import com.telemetrydeck.sdk.TelemetryDeck

/**
 * TelemetryDeck implementation of AnalyticsManager.
 *
 * Privacy-focused analytics implementation that:
 * - Does not transmit PII (Personally Identifiable Information)
 * - Uses privacy-respecting anonymous identifiers
 * - Is GDPR compliant by design
 *
 * @param context Application context for TelemetryDeck initialization
 * @param appId TelemetryDeck application identifier
 * @param showDebugLogs Enable debug logging (should be false in production)
 */
class TelemetryDeckAnalyticsManager(
    context: Context,
    appId: String,
    showDebugLogs: Boolean = false
) : AnalyticsManager {

    init {
        if (appId.isNotBlank()) {
            val builder = TelemetryDeck.Builder()
                .appID(appId)
                .showDebugLogs(showDebugLogs)

            TelemetryDeck.start(context.applicationContext, builder)
        }
    }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        // Use signal() to queue events for batch delivery (more efficient)
        TelemetryDeck.signal(name, properties)
    }

    override fun trackScreenView(screenName: String) {
        // Track screen views as events with screen_name property
        // Consistent with iOS implementation for cross-platform analytics
        TelemetryDeck.signal("screen_view", mapOf("screen_name" to screenName))
    }
}
