package com.rulebook.core.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyticsManagerTest {

    private lateinit var analyticsManager: FakeAnalyticsManager

    @Before
    fun setUp() {
        analyticsManager = FakeAnalyticsManager()
    }

    @Test
    fun `trackEvent records event name`() {
        analyticsManager.trackEvent("test_event")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("test_event", analyticsManager.trackedEvents[0].name)
    }

    @Test
    fun `trackEvent records event with properties`() {
        val properties = mapOf("key1" to "value1", "key2" to "value2")

        analyticsManager.trackEvent("test_event", properties)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("test_event", analyticsManager.trackedEvents[0].name)
        assertEquals(properties, analyticsManager.trackedEvents[0].properties)
    }

    @Test
    fun `trackEvent with no properties has empty map`() {
        analyticsManager.trackEvent("test_event")

        assertTrue(analyticsManager.trackedEvents[0].properties.isEmpty())
    }

    @Test
    fun `trackScreenView records screen name`() {
        analyticsManager.trackScreenView("HomeScreen")

        assertEquals(1, analyticsManager.trackedScreenViews.size)
        assertEquals("HomeScreen", analyticsManager.trackedScreenViews[0])
    }

    @Test
    fun `multiple events are recorded in order`() {
        analyticsManager.trackEvent("event1")
        analyticsManager.trackEvent("event2")
        analyticsManager.trackScreenView("Screen1")
        analyticsManager.trackEvent("event3")

        assertEquals(3, analyticsManager.trackedEvents.size)
        assertEquals("event1", analyticsManager.trackedEvents[0].name)
        assertEquals("event2", analyticsManager.trackedEvents[1].name)
        assertEquals("event3", analyticsManager.trackedEvents[2].name)
        assertEquals(1, analyticsManager.trackedScreenViews.size)
    }

    @Test
    fun `clear removes all tracked data`() {
        analyticsManager.trackEvent("event1")
        analyticsManager.trackScreenView("Screen1")

        analyticsManager.clear()

        assertTrue(analyticsManager.trackedEvents.isEmpty())
        assertTrue(analyticsManager.trackedScreenViews.isEmpty())
    }

    @Test
    fun `trackOnboardingStarted fires onboarding_started event`() {
        analyticsManager.trackOnboardingStarted()

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_started", analyticsManager.trackedEvents[0].name)
        assertTrue(analyticsManager.trackedEvents[0].properties.isEmpty())
    }

    @Test
    fun `trackOnboardingPageViewed fires event with page number`() {
        analyticsManager.trackOnboardingPageViewed(pageNumber = 1)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_page_viewed", analyticsManager.trackedEvents[0].name)
        assertEquals("1", analyticsManager.trackedEvents[0].properties["page"])
    }

    @Test
    fun `trackOnboardingPageViewed fires event with page 2`() {
        analyticsManager.trackOnboardingPageViewed(pageNumber = 2)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_page_viewed", analyticsManager.trackedEvents[0].name)
        assertEquals("2", analyticsManager.trackedEvents[0].properties["page"])
    }

    @Test
    fun `trackOnboardingCompleted fires event with get_started method`() {
        analyticsManager.trackOnboardingCompleted()

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_completed", analyticsManager.trackedEvents[0].name)
        assertEquals("get_started", analyticsManager.trackedEvents[0].properties["method"])
    }

    @Test
    fun `trackOnboardingSkipped fires event with page number`() {
        analyticsManager.trackOnboardingSkipped(pageNumber = 1)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_skipped", analyticsManager.trackedEvents[0].name)
        assertEquals("1", analyticsManager.trackedEvents[0].properties["page"])
    }

    @Test
    fun `trackOnboardingSkipped fires event with page 2`() {
        analyticsManager.trackOnboardingSkipped(pageNumber = 2)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("onboarding_skipped", analyticsManager.trackedEvents[0].name)
        assertEquals("2", analyticsManager.trackedEvents[0].properties["page"])
    }

    @Test
    fun `trackScanStarted fires scan_started event`() {
        analyticsManager.trackScanStarted()

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_started", analyticsManager.trackedEvents[0].name)
        assertTrue(analyticsManager.trackedEvents[0].properties.isEmpty())
    }

    @Test
    fun `trackScanCancelled fires scan_cancelled event with phase and progress`() {
        analyticsManager.trackScanCancelled(phase = "ANALYZING", progress = 0.5f)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_cancelled", analyticsManager.trackedEvents[0].name)
        assertEquals("ANALYZING", analyticsManager.trackedEvents[0].properties["phase"])
        assertEquals("0.5", analyticsManager.trackedEvents[0].properties["progress"])
    }

    @Test
    fun `trackScanAnalysisComplete fires scan_analysis_complete with confidence and auto_proceed`() {
        analyticsManager.trackScanAnalysisComplete(confidence = 0.95f, autoProceeded = true)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_analysis_complete", analyticsManager.trackedEvents[0].name)
        assertEquals("0.95", analyticsManager.trackedEvents[0].properties["confidence"])
        assertEquals("true", analyticsManager.trackedEvents[0].properties["auto_proceed"])
    }

    @Test
    fun `trackScanConfirmed fires scan_confirmed with confidence`() {
        analyticsManager.trackScanConfirmed(confidence = 0.85f)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_confirmed", analyticsManager.trackedEvents[0].name)
        assertEquals("0.85", analyticsManager.trackedEvents[0].properties["confidence"])
    }

    @Test
    fun `trackScanManualEntry fires scan_manual_entry with confidence`() {
        analyticsManager.trackScanManualEntry(confidence = 0.45f)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_manual_entry", analyticsManager.trackedEvents[0].name)
        assertEquals("0.45", analyticsManager.trackedEvents[0].properties["confidence"])
    }

    @Test
    fun `trackScanManualNameSubmitted fires scan_manual_name_submitted with game_name`() {
        analyticsManager.trackScanManualNameSubmitted(gameName = "Catan")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_manual_name_submitted", analyticsManager.trackedEvents[0].name)
        assertEquals("Catan", analyticsManager.trackedEvents[0].properties["game_name"])
    }

    @Test
    fun `trackScanGenerationComplete fires scan_generation_complete with game_name and duration_ms`() {
        analyticsManager.trackScanGenerationComplete(gameName = "Wingspan", durationMs = 2500L)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_generation_complete", analyticsManager.trackedEvents[0].name)
        assertEquals("Wingspan", analyticsManager.trackedEvents[0].properties["game_name"])
        assertEquals("2500", analyticsManager.trackedEvents[0].properties["duration_ms"])
    }

    @Test
    fun `trackScanFailed fires scan_failed with error_type`() {
        analyticsManager.trackScanFailed(errorType = "network_error")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_failed", analyticsManager.trackedEvents[0].name)
        assertEquals("network_error", analyticsManager.trackedEvents[0].properties["error_type"])
    }

    @Test
    fun `trackScanCreditCheck fires scan_credit_check with has_credits and credit_balance`() {
        analyticsManager.trackScanCreditCheck(hasCredits = true, creditBalance = 5)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_credit_check", analyticsManager.trackedEvents[0].name)
        assertEquals("true", analyticsManager.trackedEvents[0].properties["has_credits"])
        assertEquals("5", analyticsManager.trackedEvents[0].properties["credit_balance"])
    }

    @Test
    fun `trackScanCreditCheck fires event when user has no credits`() {
        analyticsManager.trackScanCreditCheck(hasCredits = false, creditBalance = 0)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_credit_check", analyticsManager.trackedEvents[0].name)
        assertEquals("false", analyticsManager.trackedEvents[0].properties["has_credits"])
        assertEquals("0", analyticsManager.trackedEvents[0].properties["credit_balance"])
    }

    @Test
    fun `trackScanFallbackUsed fires scan_fallback_used with correct properties when fallback succeeds`() {
        analyticsManager.trackScanFallbackUsed(
            primaryErrorType = "low_confidence",
            primaryConfidence = 0.45f,
            fallbackResult = "success"
        )

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_fallback_used", analyticsManager.trackedEvents[0].name)
        assertEquals("low_confidence", analyticsManager.trackedEvents[0].properties["primary_error_type"])
        assertEquals("0.45", analyticsManager.trackedEvents[0].properties["primary_confidence"])
        assertEquals("success", analyticsManager.trackedEvents[0].properties["fallback_result"])
    }

    @Test
    fun `trackScanFallbackUsed fires event without confidence when primary model errored`() {
        analyticsManager.trackScanFallbackUsed(
            primaryErrorType = "timeout",
            primaryConfidence = null,
            fallbackResult = "failure"
        )

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_fallback_used", analyticsManager.trackedEvents[0].name)
        assertEquals("timeout", analyticsManager.trackedEvents[0].properties["primary_error_type"])
        assertEquals(false, analyticsManager.trackedEvents[0].properties.containsKey("primary_confidence"))
        assertEquals("failure", analyticsManager.trackedEvents[0].properties["fallback_result"])
    }

    @Test
    fun `trackScanFallbackFailed fires scan_fallback_failed with both error types`() {
        analyticsManager.trackScanFallbackFailed(
            primaryErrorType = "server_error",
            fallbackErrorType = "timeout"
        )

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_fallback_failed", analyticsManager.trackedEvents[0].name)
        assertEquals("server_error", analyticsManager.trackedEvents[0].properties["primary_error_type"])
        assertEquals("timeout", analyticsManager.trackedEvents[0].properties["fallback_error_type"])
    }

    @Test
    fun `trackScanRetryFromError fires scan_retry_from_error with error_type`() {
        analyticsManager.trackScanRetryFromError(errorType = "network_error")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_retry_from_error", analyticsManager.trackedEvents[0].name)
        assertEquals("network_error", analyticsManager.trackedEvents[0].properties["error_type"])
    }

    @Test
    fun `trackScanManualEntryFromError fires scan_manual_entry_from_error with error_type`() {
        analyticsManager.trackScanManualEntryFromError(errorType = "timeout")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_manual_entry_from_error", analyticsManager.trackedEvents[0].name)
        assertEquals("timeout", analyticsManager.trackedEvents[0].properties["error_type"])
    }

    @Test
    fun `trackCreditDeducted fires credit_deducted event with new_balance and game_id`() {
        analyticsManager.trackCreditDeducted(newBalance = 4, gameId = "game-123")

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("credit_deducted", analyticsManager.trackedEvents[0].name)
        assertEquals("4", analyticsManager.trackedEvents[0].properties["new_balance"])
        assertEquals("game-123", analyticsManager.trackedEvents[0].properties["game_id"])
    }

    @Test
    fun `trackScanCompleted fires scan_completed event with game_id, game_name, and new_credit_balance`() {
        analyticsManager.trackScanCompleted(gameId = "game-456", gameName = "Catan", newCreditBalance = 2)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("scan_completed", analyticsManager.trackedEvents[0].name)
        assertEquals("game-456", analyticsManager.trackedEvents[0].properties["game_id"])
        assertEquals("Catan", analyticsManager.trackedEvents[0].properties["game_name"])
        assertEquals("2", analyticsManager.trackedEvents[0].properties["new_credit_balance"])
    }

    @Test
    fun `trackPaywallDisplayed fires paywall_displayed event with source and current_balance`() {
        analyticsManager.trackPaywallDisplayed(source = "scan_gate", currentBalance = 3)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("paywall_displayed", analyticsManager.trackedEvents[0].name)
        assertEquals("scan_gate", analyticsManager.trackedEvents[0].properties["source"])
        assertEquals("3", analyticsManager.trackedEvents[0].properties["current_balance"])
    }

    @Test
    fun `trackPaywallDisplayed fires event with zero balance`() {
        analyticsManager.trackPaywallDisplayed(source = "settings", currentBalance = 0)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("paywall_displayed", analyticsManager.trackedEvents[0].name)
        assertEquals("settings", analyticsManager.trackedEvents[0].properties["source"])
        assertEquals("0", analyticsManager.trackedEvents[0].properties["current_balance"])
    }

    @Test
    fun `trackPurchaseStarted fires purchase_started event with sku and credits`() {
        analyticsManager.trackPurchaseStarted(sku = "credits_3", credits = 3)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("purchase_started", analyticsManager.trackedEvents[0].name)
        assertEquals("credits_3", analyticsManager.trackedEvents[0].properties["sku"])
        assertEquals("3", analyticsManager.trackedEvents[0].properties["credits"])
    }

    @Test
    fun `trackPurchaseCompleted fires purchase_completed event with sku, credits_added, and new_balance`() {
        analyticsManager.trackPurchaseCompleted(sku = "credits_10", creditsAdded = 10, newBalance = 15)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("purchase_completed", analyticsManager.trackedEvents[0].name)
        assertEquals("credits_10", analyticsManager.trackedEvents[0].properties["sku"])
        assertEquals("10", analyticsManager.trackedEvents[0].properties["credits_added"])
        assertEquals("15", analyticsManager.trackedEvents[0].properties["new_balance"])
    }

    @Test
    fun `trackPurchaseFailed fires purchase_failed event with sku, error_code, and error_message`() {
        analyticsManager.trackPurchaseFailed(
            sku = "credits_1",
            errorCode = "USER_CANCELED",
            errorMessage = "User cancelled the purchase"
        )

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("purchase_failed", analyticsManager.trackedEvents[0].name)
        assertEquals("credits_1", analyticsManager.trackedEvents[0].properties["sku"])
        assertEquals("USER_CANCELED", analyticsManager.trackedEvents[0].properties["error_code"])
        assertEquals("User cancelled the purchase", analyticsManager.trackedEvents[0].properties["error_message"])
    }

    @Test
    fun `trackPurchaseRestored fires purchase_restored event with result and credits_restored`() {
        analyticsManager.trackPurchaseRestored(result = "success", creditsRestored = 4)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("purchase_restored", analyticsManager.trackedEvents[0].name)
        assertEquals("success", analyticsManager.trackedEvents[0].properties["result"])
        assertEquals("4", analyticsManager.trackedEvents[0].properties["credits_restored"])
    }

    @Test
    fun `trackPurchaseRestored fires event with zero credits when none restored`() {
        analyticsManager.trackPurchaseRestored(result = "none", creditsRestored = 0)

        assertEquals(1, analyticsManager.trackedEvents.size)
        assertEquals("purchase_restored", analyticsManager.trackedEvents[0].name)
        assertEquals("none", analyticsManager.trackedEvents[0].properties["result"])
        assertEquals("0", analyticsManager.trackedEvents[0].properties["credits_restored"])
    }
}
