package com.rulebook.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulebookButton component and ButtonVariant enum.
 *
 * These tests verify the component's API contract and parameter handling.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class RulebookButtonTest {

    // =========================================================================
    // BUTTONVARIANT ENUM TESTS
    // =========================================================================

    @Test
    fun `ButtonVariant enum exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.ButtonVariant")
        assertNotNull("ButtonVariant enum should exist", clazz)
        assertTrue("ButtonVariant should be an enum", clazz.isEnum)
    }

    @Test
    fun `ButtonVariant has Primary variant`() {
        val variant = ButtonVariant.Primary
        assertNotNull("Primary variant should exist", variant)
        assertEquals("Primary", variant.name)
    }

    @Test
    fun `ButtonVariant has Secondary variant`() {
        val variant = ButtonVariant.Secondary
        assertNotNull("Secondary variant should exist", variant)
        assertEquals("Secondary", variant.name)
    }

    @Test
    fun `ButtonVariant has Destructive variant`() {
        val variant = ButtonVariant.Destructive
        assertNotNull("Destructive variant should exist", variant)
        assertEquals("Destructive", variant.name)
    }

    @Test
    fun `ButtonVariant has exactly three variants`() {
        val variants = ButtonVariant.entries
        assertEquals("ButtonVariant should have exactly 3 variants", 3, variants.size)
    }

    // =========================================================================
    // RULEBOOKBUTTON COMPOSABLE TESTS
    // =========================================================================

    @Test
    fun `RulebookButton class exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookButtonKt")
        assertNotNull("RulebookButton composable should exist", clazz)
    }

    @Test
    fun `RulebookButton has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookButtonKt")
        val methods = clazz.declaredMethods

        val hasButtonMethod = methods.any { method ->
            method.name.contains("RulebookButton")
        }
        assertTrue("RulebookButton composable function should exist", hasButtonMethod)
    }
}
