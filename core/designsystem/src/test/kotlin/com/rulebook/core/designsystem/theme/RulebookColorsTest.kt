package com.rulebook.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Tests for RulebookColors design tokens.
 *
 * Verifies that all color tokens match the design specification values
 * from the UX design specification document.
 */
class RulebookColorsTest {

    // =========================================================================
    // SURFACE PALETTE TESTS
    // =========================================================================

    @Test
    fun `surface palette light mode matches spec`() {
        assertEquals(Color(0xFFFFFFFF), SurfacePrimaryLight)
        assertEquals(Color(0xFFFFF9F0), SurfaceSecondaryLight)
        assertEquals(Color(0xFFF5E6D3), SurfaceTertiaryLight)
    }

    @Test
    fun `surface palette dark mode matches spec`() {
        assertEquals(Color(0xFF1C1C1E), SurfacePrimaryDark)
        assertEquals(Color(0xFF2C2C2E), SurfaceSecondaryDark)
        assertEquals(Color(0xFF3A3A3C), SurfaceTertiaryDark)
    }

    // =========================================================================
    // CONTENT PALETTE TESTS
    // =========================================================================

    @Test
    fun `content palette light mode matches spec`() {
        assertEquals(Color(0xFF000000), ContentPrimaryLight)
        assertEquals(Color(0xB2000000), ContentSecondaryLight) // 70% opacity
        assertEquals(Color(0x66000000), ContentTertiaryLight)  // 40% opacity
    }

    @Test
    fun `content palette dark mode matches spec`() {
        assertEquals(Color(0xFFFFFFFF), ContentPrimaryDark)
        assertEquals(Color(0xB2FFFFFF), ContentSecondaryDark) // 70% opacity
        assertEquals(Color(0x66FFFFFF), ContentTertiaryDark)  // 40% opacity
    }

    // =========================================================================
    // ACCENT PALETTE TESTS
    // =========================================================================

    @Test
    fun `orange accent colors match spec`() {
        assertEquals(Color(0xFFFF6B35), OrangeLight)
        assertEquals(Color(0xFFFF8C5F), OrangeDark)
    }

    @Test
    fun `blue accent colors match spec`() {
        assertEquals(Color(0xFF3498DB), BlueLight)
        assertEquals(Color(0xFF5DADE2), BlueDark)
    }

    @Test
    fun `yellow accent colors match spec`() {
        assertEquals(Color(0xFFFFD23F), YellowLight)
        assertEquals(Color(0xFFFFE066), YellowDark)
    }

    @Test
    fun `purple accent colors match spec`() {
        assertEquals(Color(0xFF7209B7), PurpleLight)
        assertEquals(Color(0xFF9D4EDD), PurpleDark)
    }

    @Test
    fun `pink accent colors match spec`() {
        assertEquals(Color(0xFFE91E63), PinkLight)
        assertEquals(Color(0xFFF06292), PinkDark)
    }

    @Test
    fun `green accent colors match spec`() {
        assertEquals(Color(0xFF2ECC71), GreenLight)
        assertEquals(Color(0xFF58D68D), GreenDark)
    }

    @Test
    fun `red accent colors match spec`() {
        assertEquals(Color(0xFFE74C3C), RedLight)
        assertEquals(Color(0xFFEC7063), RedDark)
    }

    // =========================================================================
    // EXTENDED COLOR SCHEME TESTS
    // =========================================================================

    @Test
    fun `light extended colors contain all surface colors`() {
        assertEquals(SurfacePrimaryLight, LightExtendedColors.surfacePrimary)
        assertEquals(SurfaceSecondaryLight, LightExtendedColors.surfaceSecondary)
        assertEquals(SurfaceTertiaryLight, LightExtendedColors.surfaceTertiary)
    }

    @Test
    fun `light extended colors contain all content colors`() {
        assertEquals(ContentPrimaryLight, LightExtendedColors.contentPrimary)
        assertEquals(ContentSecondaryLight, LightExtendedColors.contentSecondary)
        assertEquals(ContentTertiaryLight, LightExtendedColors.contentTertiary)
    }

    @Test
    fun `light extended colors contain all accent colors`() {
        assertEquals(OrangeLight, LightExtendedColors.orange)
        assertEquals(BlueLight, LightExtendedColors.blue)
        assertEquals(YellowLight, LightExtendedColors.yellow)
        assertEquals(PurpleLight, LightExtendedColors.purple)
        assertEquals(PinkLight, LightExtendedColors.pink)
        assertEquals(GreenLight, LightExtendedColors.green)
        assertEquals(RedLight, LightExtendedColors.red)
    }

    @Test
    fun `dark extended colors contain all surface colors`() {
        assertEquals(SurfacePrimaryDark, DarkExtendedColors.surfacePrimary)
        assertEquals(SurfaceSecondaryDark, DarkExtendedColors.surfaceSecondary)
        assertEquals(SurfaceTertiaryDark, DarkExtendedColors.surfaceTertiary)
    }

    @Test
    fun `dark extended colors contain all content colors`() {
        assertEquals(ContentPrimaryDark, DarkExtendedColors.contentPrimary)
        assertEquals(ContentSecondaryDark, DarkExtendedColors.contentSecondary)
        assertEquals(ContentTertiaryDark, DarkExtendedColors.contentTertiary)
    }

    @Test
    fun `dark extended colors contain all accent colors`() {
        assertEquals(OrangeDark, DarkExtendedColors.orange)
        assertEquals(BlueDark, DarkExtendedColors.blue)
        assertEquals(YellowDark, DarkExtendedColors.yellow)
        assertEquals(PurpleDark, DarkExtendedColors.purple)
        assertEquals(PinkDark, DarkExtendedColors.pink)
        assertEquals(GreenDark, DarkExtendedColors.green)
        assertEquals(RedDark, DarkExtendedColors.red)
    }

    // =========================================================================
    // MATERIAL COLOR SCHEME TESTS
    // =========================================================================

    @Test
    fun `light color scheme has correct primary color`() {
        val scheme = rulebookLightColorScheme()
        assertEquals(PinkLight, scheme.primary)
    }

    @Test
    fun `light color scheme has correct secondary color`() {
        val scheme = rulebookLightColorScheme()
        assertEquals(BlueLight, scheme.secondary)
    }

    @Test
    fun `light color scheme has correct tertiary color`() {
        val scheme = rulebookLightColorScheme()
        assertEquals(OrangeLight, scheme.tertiary)
    }

    @Test
    fun `light color scheme has correct error color`() {
        val scheme = rulebookLightColorScheme()
        assertEquals(RedLight, scheme.error)
    }

    @Test
    fun `light color scheme has correct surface colors`() {
        val scheme = rulebookLightColorScheme()
        assertEquals(SurfacePrimaryLight, scheme.surface)
        assertEquals(SurfaceSecondaryLight, scheme.background)
    }

    @Test
    fun `dark color scheme has correct primary color`() {
        val scheme = rulebookDarkColorScheme()
        assertEquals(PinkDark, scheme.primary)
    }

    @Test
    fun `dark color scheme has correct secondary color`() {
        val scheme = rulebookDarkColorScheme()
        assertEquals(BlueDark, scheme.secondary)
    }

    @Test
    fun `dark color scheme has correct tertiary color`() {
        val scheme = rulebookDarkColorScheme()
        assertEquals(OrangeDark, scheme.tertiary)
    }

    @Test
    fun `dark color scheme has correct error color`() {
        val scheme = rulebookDarkColorScheme()
        assertEquals(RedDark, scheme.error)
    }

    @Test
    fun `dark color scheme has correct surface colors`() {
        val scheme = rulebookDarkColorScheme()
        assertEquals(SurfaceSecondaryDark, scheme.surface)
        assertEquals(SurfacePrimaryDark, scheme.background)
    }

    @Test
    fun `light and dark schemes are different`() {
        val light = rulebookLightColorScheme()
        val dark = rulebookDarkColorScheme()
        assertNotEquals(light.primary, dark.primary)
        assertNotEquals(light.background, dark.background)
        assertNotEquals(light.surface, dark.surface)
    }
}
