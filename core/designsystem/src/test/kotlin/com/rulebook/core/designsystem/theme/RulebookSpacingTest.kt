package com.rulebook.core.designsystem.theme

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for RulebookSpacing design tokens.
 *
 * Verifies that all spacing values match the design specification values
 * from the UX design specification document.
 */
class RulebookSpacingTest {

    // =========================================================================
    // SPACING SCALE TESTS
    // =========================================================================

    @Test
    fun `xs spacing matches spec - 4dp`() {
        assertEquals(4.dp, SpacingXs)
    }

    @Test
    fun `sm spacing matches spec - 8dp`() {
        assertEquals(8.dp, SpacingSm)
    }

    @Test
    fun `md spacing matches spec - 16dp`() {
        assertEquals(16.dp, SpacingMd)
    }

    @Test
    fun `lg spacing matches spec - 24dp`() {
        assertEquals(24.dp, SpacingLg)
    }

    @Test
    fun `xl spacing matches spec - 32dp`() {
        assertEquals(32.dp, SpacingXl)
    }

    // =========================================================================
    // BRUTALIST SPECIFICATION TESTS
    // =========================================================================

    @Test
    fun `brutalist border width matches spec - 3dp`() {
        assertEquals(3.dp, BrutalistBorderWidth)
    }

    @Test
    fun `brutalist thick border width matches spec - 4dp`() {
        assertEquals(4.dp, BrutalistBorderWidthThick)
    }

    @Test
    fun `brutalist shadow offset matches spec - 4dp`() {
        assertEquals(4.dp, BrutalistShadowOffset)
    }

    @Test
    fun `brutalist shadow offset medium is 8dp`() {
        assertEquals(8.dp, BrutalistShadowOffsetMedium)
    }

    @Test
    fun `brutalist shadow offset large matches spec - 12dp`() {
        assertEquals(12.dp, BrutalistShadowOffsetLarge)
    }

    @Test
    fun `brutalist corner radius is 0dp - sharp corners`() {
        assertEquals(0.dp, BrutalistCornerRadius)
    }

    @Test
    fun `button horizontal padding matches spec - 20dp`() {
        assertEquals(20.dp, ButtonPaddingHorizontal)
    }

    // =========================================================================
    // LAYOUT SPECIFICATION TESTS
    // =========================================================================

    @Test
    fun `screen margin matches spec - 16dp`() {
        assertEquals(16.dp, ScreenMargin)
    }

    @Test
    fun `card padding matches spec - 16dp`() {
        assertEquals(16.dp, CardPadding)
    }

    @Test
    fun `grid spacing matches spec - 16dp`() {
        assertEquals(16.dp, GridSpacing)
    }

    @Test
    fun `tab bar height matches spec - 60dp`() {
        assertEquals(60.dp, TabBarHeight)
    }

    // =========================================================================
    // SPACING OBJECT TESTS
    // =========================================================================

    @Test
    fun `RulebookSpacing contains correct scale values`() {
        assertEquals(SpacingXs, RulebookSpacing.xs)
        assertEquals(SpacingSm, RulebookSpacing.sm)
        assertEquals(SpacingMd, RulebookSpacing.md)
        assertEquals(SpacingLg, RulebookSpacing.lg)
        assertEquals(SpacingXl, RulebookSpacing.xl)
    }

    @Test
    fun `RulebookSpacing contains correct brutalist values`() {
        assertEquals(BrutalistBorderWidth, RulebookSpacing.borderWidth)
        assertEquals(BrutalistBorderWidthThick, RulebookSpacing.borderWidthThick)
        assertEquals(BrutalistShadowOffset, RulebookSpacing.shadowOffset)
        assertEquals(BrutalistShadowOffsetMedium, RulebookSpacing.shadowOffsetMedium)
        assertEquals(BrutalistShadowOffsetLarge, RulebookSpacing.shadowOffsetLarge)
        assertEquals(BrutalistCornerRadius, RulebookSpacing.cornerRadius)
        assertEquals(ButtonPaddingHorizontal, RulebookSpacing.buttonPaddingHorizontal)
    }

    @Test
    fun `RulebookSpacing contains correct layout values`() {
        assertEquals(ScreenMargin, RulebookSpacing.screenMargin)
        assertEquals(CardPadding, RulebookSpacing.cardPadding)
        assertEquals(GridSpacing, RulebookSpacing.gridSpacing)
        assertEquals(TabBarHeight, RulebookSpacing.tabBarHeight)
    }

    // =========================================================================
    // SCALE HIERARCHY TESTS
    // =========================================================================

    @Test
    fun `spacing scale is in ascending order`() {
        assert(SpacingXs < SpacingSm)
        assert(SpacingSm < SpacingMd)
        assert(SpacingMd < SpacingLg)
        assert(SpacingLg < SpacingXl)
    }

    @Test
    fun `shadow offsets are in ascending order`() {
        assert(BrutalistShadowOffset < BrutalistShadowOffsetMedium)
        assert(BrutalistShadowOffsetMedium < BrutalistShadowOffsetLarge)
    }

    @Test
    fun `border widths standard is thinner than thick`() {
        assert(BrutalistBorderWidth < BrutalistBorderWidthThick)
    }
}
