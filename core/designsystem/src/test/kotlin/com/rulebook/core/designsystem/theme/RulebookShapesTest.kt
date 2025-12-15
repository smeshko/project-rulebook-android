package com.rulebook.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for RulebookShapes design tokens.
 *
 * Verifies that all shapes have 0dp corner radius for the brutalist aesthetic.
 */
class RulebookShapesTest {

    // =========================================================================
    // SHAPE DEFINITION TESTS
    // =========================================================================

    @Test
    fun `extra small shape is rectangle (0dp corners)`() {
        assertEquals(RectangleShape, ShapeExtraSmall)
    }

    @Test
    fun `small shape is rectangle (0dp corners)`() {
        assertEquals(RectangleShape, ShapeSmall)
    }

    @Test
    fun `medium shape is rectangle (0dp corners)`() {
        assertEquals(RectangleShape, ShapeMedium)
    }

    @Test
    fun `large shape is rectangle (0dp corners)`() {
        assertEquals(RectangleShape, ShapeLarge)
    }

    @Test
    fun `extra large shape is rectangle (0dp corners)`() {
        assertEquals(RectangleShape, ShapeExtraLarge)
    }

    // =========================================================================
    // MATERIAL SHAPES TESTS
    // =========================================================================

    @Test
    fun `material shapes extraSmall has 0dp corners`() {
        val expected = RoundedCornerShape(0.dp)
        assertEquals(expected, RulebookShapes.extraSmall)
    }

    @Test
    fun `material shapes small has 0dp corners`() {
        val expected = RoundedCornerShape(0.dp)
        assertEquals(expected, RulebookShapes.small)
    }

    @Test
    fun `material shapes medium has 0dp corners`() {
        val expected = RoundedCornerShape(0.dp)
        assertEquals(expected, RulebookShapes.medium)
    }

    @Test
    fun `material shapes large has 0dp corners`() {
        val expected = RoundedCornerShape(0.dp)
        assertEquals(expected, RulebookShapes.large)
    }

    @Test
    fun `material shapes extraLarge has 0dp corners`() {
        val expected = RoundedCornerShape(0.dp)
        assertEquals(expected, RulebookShapes.extraLarge)
    }

    // =========================================================================
    // EXTENDED SHAPES TESTS
    // =========================================================================

    @Test
    fun `extended shapes contains extra small`() {
        assertEquals(ShapeExtraSmall, RulebookExtendedShapesInstance.extraSmall)
    }

    @Test
    fun `extended shapes contains small`() {
        assertEquals(ShapeSmall, RulebookExtendedShapesInstance.small)
    }

    @Test
    fun `extended shapes contains medium`() {
        assertEquals(ShapeMedium, RulebookExtendedShapesInstance.medium)
    }

    @Test
    fun `extended shapes contains large`() {
        assertEquals(ShapeLarge, RulebookExtendedShapesInstance.large)
    }

    @Test
    fun `extended shapes contains extra large`() {
        assertEquals(ShapeExtraLarge, RulebookExtendedShapesInstance.extraLarge)
    }

    // =========================================================================
    // BRUTALIST AESTHETIC VERIFICATION
    // =========================================================================

    @Test
    fun `all shape definitions use rectangle shape for brutalist aesthetic`() {
        // All shapes should be RectangleShape for brutalist sharp corners
        assertEquals(RectangleShape, ShapeExtraSmall)
        assertEquals(RectangleShape, ShapeSmall)
        assertEquals(RectangleShape, ShapeMedium)
        assertEquals(RectangleShape, ShapeLarge)
        assertEquals(RectangleShape, ShapeExtraLarge)
    }

    @Test
    fun `all material shapes use 0dp corner radius`() {
        // All Material shapes should have 0dp corners for brutalist aesthetic
        val zeroCorner = RoundedCornerShape(0.dp)
        assertEquals(zeroCorner, RulebookShapes.extraSmall)
        assertEquals(zeroCorner, RulebookShapes.small)
        assertEquals(zeroCorner, RulebookShapes.medium)
        assertEquals(zeroCorner, RulebookShapes.large)
        assertEquals(zeroCorner, RulebookShapes.extraLarge)
    }
}
