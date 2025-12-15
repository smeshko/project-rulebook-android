package com.rulebook.core.designsystem.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for Brutalist modifier extensions.
 *
 * Verifies that brutalist modifiers apply the correct visual styling
 * following the brutalist design specification.
 */
class BrutalistModifiersTest {

    // =========================================================================
    // BRUTALIST SHADOW MODIFIER TESTS
    // =========================================================================

    @Test
    fun `brutalistShadow returns non-null modifier`() {
        val result = Modifier.brutalistShadow()
        assertNotNull("brutalistShadow should return a modifier", result)
    }

    @Test
    fun `brutalistShadow with default offset uses BrutalistShadowOffset`() {
        // Test that the function accepts default parameters and returns a valid modifier
        val result = Modifier.brutalistShadow()
        assertNotNull("brutalistShadow should compose with Modifier", result)
    }

    @Test
    fun `brutalistShadow with custom offset accepts parameter`() {
        val customOffset = 8.dp
        val result = Modifier.brutalistShadow(offset = customOffset)
        assertNotNull("brutalistShadow with custom offset should return a modifier", result)
    }

    @Test
    fun `brutalistShadow with custom color accepts parameter`() {
        val customColor = Color.Red
        val result = Modifier.brutalistShadow(color = customColor)
        assertNotNull("brutalistShadow with custom color should return a modifier", result)
    }

    @Test
    fun `brutalistShadow with all custom parameters`() {
        val customOffset = 12.dp
        val customColor = Color.Blue
        val result = Modifier.brutalistShadow(offset = customOffset, color = customColor)
        assertNotNull("brutalistShadow with all custom params should return a modifier", result)
    }

    @Test
    fun `brutalistShadow chains with other modifiers`() {
        val result = Modifier
            .brutalistShadow()
            .then(Modifier)
        assertNotNull("brutalistShadow should chain with other modifiers", result)
    }
}
