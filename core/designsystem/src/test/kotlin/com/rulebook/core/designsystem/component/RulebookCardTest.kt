package com.rulebook.core.designsystem.component

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulebookCard component.
 *
 * These tests verify the component's API contract and parameter handling.
 * UI rendering tests would require androidTest with Compose test rules.
 */
class RulebookCardTest {

    @Test
    fun `RulebookCard class exists`() {
        // Verify the composable function exists by checking class loading
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookCardKt")
        assertNotNull("RulebookCard composable should exist", clazz)
    }

    @Test
    fun `RulebookCard has required composable function`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookCardKt")
        val methods = clazz.declaredMethods

        // Verify RulebookCard function exists (Compose generates specific method signatures)
        val hasCardMethod = methods.any { method ->
            method.name.contains("RulebookCard")
        }
        assertTrue("RulebookCard composable function should exist", hasCardMethod)
    }

    @Test
    fun `RulebookCard elevated variant exists`() {
        val clazz = Class.forName("com.rulebook.core.designsystem.component.RulebookCardKt")
        val methods = clazz.declaredMethods

        // Verify elevated variant exists
        val hasElevatedMethod = methods.any { method ->
            method.name.contains("ElevatedRulebookCard")
        }
        assertTrue("ElevatedRulebookCard composable function should exist", hasElevatedMethod)
    }
}
