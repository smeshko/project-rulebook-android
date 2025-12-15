package com.rulebook.core.analytics

/**
 * Interface for analytics tracking operations.
 * Provides a testable abstraction over the analytics implementation.
 */
interface AnalyticsManager {
    /**
     * Track a custom event with optional properties.
     *
     * @param name The event name (e.g., "scan_completed", "purchase_started")
     * @param properties Optional key-value pairs for additional event context
     */
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())

    /**
     * Track a screen view event.
     *
     * @param screenName The name of the screen being viewed
     */
    fun trackScreenView(screenName: String)
}
