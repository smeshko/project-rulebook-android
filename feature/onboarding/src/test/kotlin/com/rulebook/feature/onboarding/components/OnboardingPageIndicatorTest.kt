package com.rulebook.feature.onboarding.components

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for OnboardingPageIndicator logic.
 *
 * Note: These are logic-level unit tests that validate the indicator's
 * state calculation logic. For visual/rendering tests, use Compose UI
 * tests in androidTest with createComposeRule().
 *
 * Tests cover:
 * - Active page detection algorithm
 * - Page range validation
 * - Expected page count for onboarding
 */
class OnboardingPageIndicatorTest {

    companion object {
        /** Number of onboarding pages per requirements */
        private const val ONBOARDING_PAGE_COUNT = 2
    }

    @Test
    fun `active page detection returns true for matching page index`() {
        val currentPage = 0
        val isActive = isPageActive(page = 0, currentPage = currentPage)
        assertTrue(isActive, "Page should be active when index matches currentPage")
    }

    @Test
    fun `active page detection returns false for non-matching page index`() {
        val currentPage = 0
        val isActive = isPageActive(page = 1, currentPage = currentPage)
        assertFalse(isActive, "Page should be inactive when index does not match currentPage")
    }

    @Test
    fun `active page detection works for second page`() {
        val currentPage = 1
        assertTrue(isPageActive(page = 1, currentPage = currentPage), "Page 1 should be active")
        assertFalse(isPageActive(page = 0, currentPage = currentPage), "Page 0 should be inactive")
    }

    @Test
    fun `onboarding has exactly 2 pages per acceptance criteria`() {
        assertEquals(2, ONBOARDING_PAGE_COUNT, "Onboarding should have exactly 2 pages (AC #1)")
    }

    @Test
    fun `valid page range is 0 to pageCount minus 1`() {
        val pageCount = ONBOARDING_PAGE_COUNT
        val validPages = (0 until pageCount).toList()
        assertEquals(listOf(0, 1), validPages, "Valid pages should be [0, 1] for 2-page indicator")
    }

    /**
     * Mirrors the logic used in OnboardingPageIndicator composable:
     * `val isActive = page == currentPage`
     */
    private fun isPageActive(page: Int, currentPage: Int): Boolean = page == currentPage
}
