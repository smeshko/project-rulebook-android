package com.rulebook.feature.onboarding

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for OnboardingScreen composable.
 *
 * Tests cover:
 * - HorizontalPager with 2 pages
 * - OnboardingPage sealed class structure
 * - Page content definitions
 */
class OnboardingScreenTest {

    @Test
    fun `OnboardingPage values count should be 2`() {
        val pages = OnboardingPage.entries
        assertEquals(2, pages.size, "Onboarding should have exactly 2 pages")
    }

    @Test
    fun `OnboardingPage ValueProposition should be first page`() {
        val pages = OnboardingPage.entries
        assertEquals(OnboardingPage.ValueProposition, pages[0])
    }

    @Test
    fun `OnboardingPage GettingStarted should be second page`() {
        val pages = OnboardingPage.entries
        assertEquals(OnboardingPage.GettingStarted, pages[1])
    }

    @Test
    fun `OnboardingPage ValueProposition should have correct headline`() {
        assertEquals("Scan any game box", OnboardingPage.ValueProposition.headline)
    }

    @Test
    fun `OnboardingPage ValueProposition should have correct subtext`() {
        assertEquals(
            "Point your camera at a board game and get the rules instantly",
            OnboardingPage.ValueProposition.subtext
        )
    }

    @Test
    fun `OnboardingPage GettingStarted should have correct headline`() {
        assertEquals("Get started for free", OnboardingPage.GettingStarted.headline)
    }

    @Test
    fun `OnboardingPage GettingStarted should have correct subtext`() {
        assertEquals(
            "Your first game is on us. Additional scans available via credit packs.",
            OnboardingPage.GettingStarted.subtext
        )
    }
}
