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

    /**
     * Track when onboarding screen is first displayed.
     * This is a convenience method that fires the "onboarding_started" event.
     */
    fun trackOnboardingStarted() {
        trackEvent("onboarding_started")
    }

    /**
     * Track when a specific onboarding page is viewed.
     * This is a convenience method that fires the "onboarding_page_viewed" event.
     *
     * @param pageNumber The 1-indexed page number (1 = Value Proposition, 2 = Getting Started)
     */
    fun trackOnboardingPageViewed(pageNumber: Int) {
        trackEvent("onboarding_page_viewed", mapOf("page" to pageNumber.toString()))
    }

    /**
     * Track when onboarding is completed by tapping "Get Started".
     * This is a convenience method that fires the "onboarding_completed" event.
     */
    fun trackOnboardingCompleted() {
        trackEvent("onboarding_completed", mapOf("method" to "get_started"))
    }

    /**
     * Track when onboarding is skipped.
     * This is a convenience method that fires the "onboarding_skipped" event.
     *
     * @param pageNumber The 1-indexed page number where skip was tapped
     */
    fun trackOnboardingSkipped(pageNumber: Int) {
        trackEvent("onboarding_skipped", mapOf("page" to pageNumber.toString()))
    }

    /**
     * Track when a scan credit check is performed (Story 5.1).
     * This is a convenience method that fires the "scan_credit_check" event.
     *
     * @param hasCredits Whether the user has credits available
     * @param creditBalance The user's current credit balance
     */
    fun trackScanCreditCheck(hasCredits: Boolean, creditBalance: Int) {
        trackEvent(
            "scan_credit_check",
            mapOf(
                "has_credits" to hasCredits.toString(),
                "credit_balance" to creditBalance.toString()
            )
        )
    }
}
