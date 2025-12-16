package com.rulebook.core.designsystem.component

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulebookFAB component.
 *
 * These tests verify the component's API contract and existence.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class RulebookFABTest {

    // =========================================================================
    // RULEBOOKFAB COMPOSABLE TESTS
    // =========================================================================

    @Test
    fun `RulebookFAB class exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookFABKt")
        assertNotNull("RulebookFAB composable should exist", clazz)
    }

    @Test
    fun `RulebookFAB has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookFABKt")
        val methods = clazz.declaredMethods

        val hasFABMethod = methods.any { method ->
            method.name.contains("RulebookFAB")
        }
        assertTrue("RulebookFAB composable function should exist", hasFABMethod)
    }
}
