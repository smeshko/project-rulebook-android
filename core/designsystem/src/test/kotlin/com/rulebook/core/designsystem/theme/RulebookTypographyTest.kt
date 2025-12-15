package com.rulebook.core.designsystem.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for RulebookTypography design tokens.
 *
 * Verifies that all typography styles match the design specification values
 * from the UX design specification document.
 */
class RulebookTypographyTest {

    // =========================================================================
    // DISPLAY STYLE TESTS
    // =========================================================================

    @Test
    fun `display large title matches spec - 34sp Bold`() {
        assertEquals(34.sp, DisplayLargeTitle.fontSize)
        assertEquals(FontWeight.Bold, DisplayLargeTitle.fontWeight)
    }

    @Test
    fun `display title matches spec - 28sp Bold`() {
        assertEquals(28.sp, DisplayTitle.fontSize)
        assertEquals(FontWeight.Bold, DisplayTitle.fontWeight)
    }

    @Test
    fun `display title 2 matches spec - 22sp SemiBold`() {
        assertEquals(22.sp, DisplayTitle2.fontSize)
        assertEquals(FontWeight.SemiBold, DisplayTitle2.fontWeight)
    }

    // =========================================================================
    // BRUTALIST STYLE TESTS
    // =========================================================================

    @Test
    fun `brutalist title matches spec - 24sp Black`() {
        assertEquals(24.sp, BrutalistTitle.fontSize)
        assertEquals(FontWeight.Black, BrutalistTitle.fontWeight)
    }

    @Test
    fun `brutalist section title matches spec - 16sp Black`() {
        assertEquals(16.sp, BrutalistSectionTitle.fontSize)
        assertEquals(FontWeight.Black, BrutalistSectionTitle.fontWeight)
    }

    @Test
    fun `brutalist button text matches spec - 14sp Black`() {
        assertEquals(14.sp, BrutalistButtonText.fontSize)
        assertEquals(FontWeight.Black, BrutalistButtonText.fontWeight)
    }

    // =========================================================================
    // BODY STYLE TESTS
    // =========================================================================

    @Test
    fun `body text matches spec - 17sp Regular`() {
        assertEquals(17.sp, BodyText.fontSize)
        assertEquals(FontWeight.Normal, BodyText.fontWeight)
    }

    @Test
    fun `body callout matches spec - 16sp Regular`() {
        assertEquals(16.sp, BodyCallout.fontSize)
        assertEquals(FontWeight.Normal, BodyCallout.fontWeight)
    }

    // =========================================================================
    // DETAIL STYLE TESTS
    // =========================================================================

    @Test
    fun `caption text matches spec - 12sp Regular`() {
        assertEquals(12.sp, CaptionText.fontSize)
        assertEquals(FontWeight.Normal, CaptionText.fontWeight)
    }

    // =========================================================================
    // EXTENDED TYPOGRAPHY TESTS
    // =========================================================================

    @Test
    fun `extended typography contains all display styles`() {
        val extended = RulebookExtendedTypographyInstance
        assertEquals(DisplayLargeTitle, extended.displayLargeTitle)
        assertEquals(DisplayTitle, extended.displayTitle)
        assertEquals(DisplayTitle2, extended.displayTitle2)
    }

    @Test
    fun `extended typography contains all brutalist styles`() {
        val extended = RulebookExtendedTypographyInstance
        assertEquals(BrutalistTitle, extended.brutalistTitle)
        assertEquals(BrutalistSectionTitle, extended.brutalistSectionTitle)
        assertEquals(BrutalistButtonText, extended.brutalistButtonText)
    }

    @Test
    fun `extended typography contains all body styles`() {
        val extended = RulebookExtendedTypographyInstance
        assertEquals(BodyText, extended.body)
        assertEquals(BodyCallout, extended.callout)
    }

    @Test
    fun `extended typography contains caption style`() {
        val extended = RulebookExtendedTypographyInstance
        assertEquals(CaptionText, extended.caption)
    }

    // =========================================================================
    // MATERIAL TYPOGRAPHY TESTS
    // =========================================================================

    @Test
    fun `material typography display styles are mapped correctly`() {
        assertEquals(DisplayLargeTitle, RulebookTypography.displayLarge)
        assertEquals(DisplayTitle, RulebookTypography.displayMedium)
        assertEquals(DisplayTitle2, RulebookTypography.displaySmall)
    }

    @Test
    fun `material typography headline styles use brutalist`() {
        assertEquals(BrutalistTitle, RulebookTypography.headlineLarge)
        assertEquals(BrutalistSectionTitle, RulebookTypography.headlineMedium)
        assertEquals(BrutalistButtonText, RulebookTypography.headlineSmall)
    }

    @Test
    fun `material typography body styles are mapped correctly`() {
        assertEquals(BodyText, RulebookTypography.bodyLarge)
        assertEquals(BodyCallout, RulebookTypography.bodyMedium)
        assertEquals(CaptionText, RulebookTypography.bodySmall)
    }

    @Test
    fun `material typography label large uses brutalist button text`() {
        assertEquals(BrutalistButtonText, RulebookTypography.labelLarge)
    }

    // =========================================================================
    // WEIGHT HIERARCHY TESTS
    // =========================================================================

    @Test
    fun `brutalist styles use Black weight (900)`() {
        assertEquals(FontWeight.Black, BrutalistTitle.fontWeight)
        assertEquals(FontWeight.Black, BrutalistSectionTitle.fontWeight)
        assertEquals(FontWeight.Black, BrutalistButtonText.fontWeight)
    }

    @Test
    fun `display styles use Bold or SemiBold weights`() {
        assertEquals(FontWeight.Bold, DisplayLargeTitle.fontWeight)
        assertEquals(FontWeight.Bold, DisplayTitle.fontWeight)
        assertEquals(FontWeight.SemiBold, DisplayTitle2.fontWeight)
    }

    @Test
    fun `body and caption styles use Normal weight`() {
        assertEquals(FontWeight.Normal, BodyText.fontWeight)
        assertEquals(FontWeight.Normal, BodyCallout.fontWeight)
        assertEquals(FontWeight.Normal, CaptionText.fontWeight)
    }

    // =========================================================================
    // SIZE HIERARCHY TESTS
    // =========================================================================

    @Test
    fun `display sizes are in descending order`() {
        assertTrue("DisplayLargeTitle should be larger than DisplayTitle",
            DisplayLargeTitle.fontSize > DisplayTitle.fontSize)
        assertTrue("DisplayTitle should be larger than DisplayTitle2",
            DisplayTitle.fontSize > DisplayTitle2.fontSize)
    }

    @Test
    fun `brutalist sizes are in descending order`() {
        assertTrue("BrutalistTitle should be larger than BrutalistSectionTitle",
            BrutalistTitle.fontSize > BrutalistSectionTitle.fontSize)
        assertTrue("BrutalistSectionTitle should be larger than BrutalistButtonText",
            BrutalistSectionTitle.fontSize > BrutalistButtonText.fontSize)
    }

    @Test
    fun `body sizes are larger than caption`() {
        assertTrue("BodyText should be larger than CaptionText",
            BodyText.fontSize > CaptionText.fontSize)
        assertTrue("BodyCallout should be larger than CaptionText",
            BodyCallout.fontSize > CaptionText.fontSize)
    }
}
