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
}
