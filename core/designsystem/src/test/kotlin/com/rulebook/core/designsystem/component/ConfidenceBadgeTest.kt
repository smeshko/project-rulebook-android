package com.rulebook.core.designsystem.component

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for ConfidenceBadge component.
 *
 * Verifies color threshold logic and component API existence.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class ConfidenceBadgeTest {

    private val green = Color(0xFF2ECC71)
    private val yellow = Color(0xFFFFD23F)
    private val red = Color(0xFFE74C3C)

    // =========================================================================
    // COLOR THRESHOLD TESTS
    // =========================================================================

    @Test
    fun `confidence above 80 percent returns green`() {
        val color = confidenceBadgeColor(0.92f, green, yellow, red)
        assertEquals(green, color)
    }

    @Test
    fun `confidence at exactly 80 percent returns yellow`() {
        // 0.80 is NOT > 0.80, so it falls into the yellow range (50-80)
        val color = confidenceBadgeColor(0.80f, green, yellow, red)
        assertEquals(yellow, color)
    }

    @Test
    fun `confidence at 81 percent returns green`() {
        val color = confidenceBadgeColor(0.81f, green, yellow, red)
        assertEquals(green, color)
    }

    @Test
    fun `confidence at 50 percent returns yellow`() {
        val color = confidenceBadgeColor(0.50f, green, yellow, red)
        assertEquals(yellow, color)
    }

    @Test
    fun `confidence at 49 percent returns red`() {
        val color = confidenceBadgeColor(0.49f, green, yellow, red)
        assertEquals(red, color)
    }

    @Test
    fun `confidence below 50 percent returns red`() {
        val color = confidenceBadgeColor(0.30f, green, yellow, red)
        assertEquals(red, color)
    }

    @Test
    fun `confidence at 0 percent returns red`() {
        val color = confidenceBadgeColor(0.0f, green, yellow, red)
        assertEquals(red, color)
    }

    @Test
    fun `confidence at 100 percent returns green`() {
        val color = confidenceBadgeColor(1.0f, green, yellow, red)
        assertEquals(green, color)
    }

    @Test
    fun `confidence at 79_9 percent returns yellow`() {
        val color = confidenceBadgeColor(0.799f, green, yellow, red)
        assertEquals(yellow, color)
    }

    @Test
    fun `confidence at 80_1 percent returns green`() {
        val color = confidenceBadgeColor(0.801f, green, yellow, red)
        assertEquals(green, color)
    }

    // =========================================================================
    // COMPOSABLE API TESTS
    // =========================================================================

    @Test
    fun `ConfidenceBadge composable class exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.ConfidenceBadgeKt")
        assertNotNull("ConfidenceBadge composable should exist", clazz)
    }

    @Test
    fun `ConfidenceBadge has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.ConfidenceBadgeKt")
        val methods = clazz.declaredMethods

        val hasMethod = methods.any { method ->
            method.name.contains("ConfidenceBadge")
        }
        assertTrue("ConfidenceBadge composable function should exist", hasMethod)
    }
}
