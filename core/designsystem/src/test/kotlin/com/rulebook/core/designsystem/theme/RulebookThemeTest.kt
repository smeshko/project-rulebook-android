package com.rulebook.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Tests for RulebookTheme configuration.
 *
 * Note: Composable tests would require androidTest with Compose UI testing.
 * These unit tests verify the theme token configurations are properly set up.
 */
class RulebookThemeTest {

    // =========================================================================
    // COLOR CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun `light extended colors are properly configured`() {
        assertNotNull(LightExtendedColors)
        assertEquals(SurfacePrimaryLight, LightExtendedColors.surfacePrimary)
        assertEquals(PinkLight, LightExtendedColors.pink)
    }

    @Test
    fun `dark extended colors are properly configured`() {
        assertNotNull(DarkExtendedColors)
        assertEquals(SurfacePrimaryDark, DarkExtendedColors.surfacePrimary)
        assertEquals(PinkDark, DarkExtendedColors.pink)
    }

    @Test
    fun `light color scheme uses pink as primary`() {
        val colorScheme = rulebookLightColorScheme()
        assertEquals(PinkLight, colorScheme.primary)
    }

    @Test
    fun `dark color scheme uses pink as primary`() {
        val colorScheme = rulebookDarkColorScheme()
        assertEquals(PinkDark, colorScheme.primary)
    }

    // =========================================================================
    // TYPOGRAPHY CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun `extended typography instance is properly configured`() {
        assertNotNull(RulebookExtendedTypographyInstance)
        assertEquals(DisplayLargeTitle, RulebookExtendedTypographyInstance.displayLargeTitle)
        assertEquals(BrutalistTitle, RulebookExtendedTypographyInstance.brutalistTitle)
    }

    @Test
    fun `material typography uses display large title`() {
        assertEquals(DisplayLargeTitle, RulebookTypography.displayLarge)
    }

    @Test
    fun `material typography uses brutalist styles for headlines`() {
        assertEquals(BrutalistTitle, RulebookTypography.headlineLarge)
        assertEquals(BrutalistSectionTitle, RulebookTypography.headlineMedium)
    }

    // =========================================================================
    // SPACING CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun `spacing object is properly configured`() {
        assertNotNull(RulebookSpacing)
        assertEquals(SpacingMd, RulebookSpacing.md)
        assertEquals(BrutalistBorderWidth, RulebookSpacing.borderWidth)
    }

    @Test
    fun `spacing has correct brutalist values`() {
        assertEquals(BrutalistCornerRadius, RulebookSpacing.cornerRadius)
        assertEquals(BrutalistShadowOffset, RulebookSpacing.shadowOffset)
    }

    // =========================================================================
    // SHAPES CONFIGURATION TESTS
    // =========================================================================

    @Test
    fun `extended shapes instance is properly configured`() {
        assertNotNull(RulebookExtendedShapesInstance)
        assertEquals(ShapeMedium, RulebookExtendedShapesInstance.medium)
    }

    @Test
    fun `material shapes have zero corners`() {
        // All shapes should have 0dp corners for brutalist aesthetic
        assertEquals(RoundedCornerShape(0.dp), RulebookShapes.extraSmall)
        assertEquals(RoundedCornerShape(0.dp), RulebookShapes.small)
        assertEquals(RoundedCornerShape(0.dp), RulebookShapes.medium)
        assertEquals(RoundedCornerShape(0.dp), RulebookShapes.large)
        assertEquals(RoundedCornerShape(0.dp), RulebookShapes.extraLarge)
    }

    @Test
    fun `extended shapes use RectangleShape`() {
        // All extended shapes should be RectangleShape for brutalist aesthetic
        assertEquals(RectangleShape, RulebookExtendedShapesInstance.extraSmall)
        assertEquals(RectangleShape, RulebookExtendedShapesInstance.small)
        assertEquals(RectangleShape, RulebookExtendedShapesInstance.medium)
        assertEquals(RectangleShape, RulebookExtendedShapesInstance.large)
        assertEquals(RectangleShape, RulebookExtendedShapesInstance.extraLarge)
    }

    // =========================================================================
    // COMPOSITION LOCAL TESTS
    // =========================================================================

    @Test
    fun `LocalRulebookColors is not null`() {
        // CompositionLocal itself should exist
        assertNotNull(LocalRulebookColors)
    }

    @Test
    fun `LocalRulebookTypography is not null`() {
        // CompositionLocal itself should exist
        assertNotNull(LocalRulebookTypography)
    }

    @Test
    fun `LocalRulebookSpacing is not null`() {
        // CompositionLocal itself should exist
        assertNotNull(LocalRulebookSpacing)
    }

    @Test
    fun `LocalRulebookShapes is not null`() {
        // CompositionLocal itself should exist
        assertNotNull(LocalRulebookShapes)
    }

    // =========================================================================
    // THEME INTEGRATION TESTS
    // =========================================================================

    @Test
    fun `all design tokens are available and consistent`() {
        // Verify colors
        assertNotNull(LightExtendedColors)
        assertNotNull(DarkExtendedColors)
        assertNotNull(rulebookLightColorScheme())
        assertNotNull(rulebookDarkColorScheme())

        // Verify typography
        assertNotNull(RulebookTypography)
        assertNotNull(RulebookExtendedTypographyInstance)

        // Verify spacing
        assertNotNull(RulebookSpacing)

        // Verify shapes
        assertNotNull(RulebookShapes)
        assertNotNull(RulebookExtendedShapesInstance)
    }

    @Test
    fun `theme tokens follow brutalist aesthetic`() {
        // Corner radius is 0dp
        assertEquals(BrutalistCornerRadius, RulebookSpacing.cornerRadius)

        // Border width is 3dp standard
        assertEquals(BrutalistBorderWidth, RulebookSpacing.borderWidth)

        // Shadow offset is 4dp
        assertEquals(BrutalistShadowOffset, RulebookSpacing.shadowOffset)
    }
}
