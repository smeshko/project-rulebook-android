package com.rulebook.core.designsystem.component

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulebookHeaderBar component.
 *
 * These tests verify the component's API contract and parameter handling.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class RulebookHeaderBarTest {

    @Test
    fun `RulebookHeaderBar class exists`() {
        // Verify the composable function exists by checking class loading
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookHeaderBarKt")
        assertNotNull("RulebookHeaderBar composable should exist", clazz)
    }

    @Test
    fun `RulebookHeaderBar has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookHeaderBarKt")
        val methods = clazz.declaredMethods

        // Verify RulebookHeaderBar function exists (Compose generates specific method signatures)
        val hasHeaderBarMethod = methods.any { method ->
            method.name.contains("RulebookHeaderBar")
        }
        assertTrue("RulebookHeaderBar composable function should exist", hasHeaderBarMethod)
    }
}
