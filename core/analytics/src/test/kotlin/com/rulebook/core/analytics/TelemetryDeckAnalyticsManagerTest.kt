package com.rulebook.core.analytics

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TelemetryDeckAnalyticsManager.
 *
 * Note: Full integration tests require Android instrumentation tests
 * since TelemetryDeck SDK requires Android Context. These tests verify
 * the implementation structure and interface compliance.
 */
class TelemetryDeckAnalyticsManagerTest {

    @Test
    fun `TelemetryDeckAnalyticsManager implements AnalyticsManager interface`() {
        // Verify class implements the interface
        assertTrue(AnalyticsManager::class.java.isAssignableFrom(TelemetryDeckAnalyticsManager::class.java))
    }

    @Test
    fun `trackEvent method signature matches interface`() {
        val method = TelemetryDeckAnalyticsManager::class.java.getMethod(
            "trackEvent",
            String::class.java,
            Map::class.java
        )
        assertNotNull(method)
    }

    @Test
    fun `trackScreenView method signature matches interface`() {
        val method = TelemetryDeckAnalyticsManager::class.java.getMethod(
            "trackScreenView",
            String::class.java
        )
        assertNotNull(method)
    }
}
