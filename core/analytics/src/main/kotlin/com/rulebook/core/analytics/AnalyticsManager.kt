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

    /**
     * Track when image analysis completes with confidence result (Story 5.4).
     *
     * @param confidence The confidence level from the scan result (0.0-1.0)
     * @param autoProceeded Whether the system auto-proceeded due to high confidence
     */
    fun trackScanAnalysisComplete(confidence: Float, autoProceeded: Boolean) {
        trackEvent(
            "scan_analysis_complete",
            mapOf(
                "confidence" to confidence.toString(),
                "auto_proceed" to autoProceeded.toString()
            )
        )
    }

    /**
     * Track when user confirms the identified game (Story 5.4).
     *
     * @param confidence The confidence level at the time of confirmation
     */
    fun trackScanConfirmed(confidence: Float) {
        trackEvent(
            "scan_confirmed",
            mapOf("confidence" to confidence.toString())
        )
    }

    /**
     * Track when user rejects the identified game and opts for manual entry (Story 5.4).
     *
     * @param confidence The confidence level at the time of rejection
     */
    fun trackScanManualEntry(confidence: Float) {
        trackEvent(
            "scan_manual_entry",
            mapOf("confidence" to confidence.toString())
        )
    }

    /**
     * Track when user submits a manually entered game name (Story 5.5).
     *
     * @param gameName The game name entered by the user
     */
    fun trackScanManualNameSubmitted(gameName: String) {
        trackEvent(
            "scan_manual_name_submitted",
            mapOf("game_name" to gameName)
        )
    }
}
