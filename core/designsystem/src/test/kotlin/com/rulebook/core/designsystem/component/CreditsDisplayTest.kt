package com.rulebook.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for CreditsDisplay component and CreditState enum.
 *
 * These tests verify the component's API contract and enum behavior.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class CreditsDisplayTest {

    // =========================================================================
    // CREDITSTATE ENUM TESTS
    // =========================================================================

    @Test
    fun `CreditState enum exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.CreditState")
        assertNotNull("CreditState enum should exist", clazz)
        assertTrue("CreditState should be an enum", clazz.isEnum)
    }

    @Test
    fun `CreditState has Normal state`() {
        val state = CreditState.Normal
        assertNotNull("Normal state should exist", state)
        assertEquals("Normal", state.name)
    }

    @Test
    fun `CreditState has Low state`() {
        val state = CreditState.Low
        assertNotNull("Low state should exist", state)
        assertEquals("Low", state.name)
    }

    @Test
    fun `CreditState has Empty state`() {
        val state = CreditState.Empty
        assertNotNull("Empty state should exist", state)
        assertEquals("Empty", state.name)
    }

    @Test
    fun `CreditState has exactly three states`() {
        val states = CreditState.entries
        assertEquals("CreditState should have exactly 3 states", 3, states.size)
    }

    // =========================================================================
    // CREDITSDISPLAY COMPOSABLE TESTS
    // =========================================================================

    @Test
    fun `CreditsDisplay class exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.CreditsDisplayKt")
        assertNotNull("CreditsDisplay composable should exist", clazz)
    }

    @Test
    fun `CreditsDisplay has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.CreditsDisplayKt")
        val methods = clazz.declaredMethods

        val hasDisplayMethod = methods.any { method ->
            method.name.contains("CreditsDisplay")
        }
        assertTrue("CreditsDisplay composable function should exist", hasDisplayMethod)
    }
}
