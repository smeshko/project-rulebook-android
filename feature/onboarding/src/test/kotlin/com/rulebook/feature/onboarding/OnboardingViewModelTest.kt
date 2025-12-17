package com.rulebook.feature.onboarding

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit tests for OnboardingViewModel.
 */
class OnboardingViewModelTest {

    @Test
    fun `initial currentPage should be 0`() = runTest {
        val viewModel = OnboardingViewModel()
        assertEquals(0, viewModel.currentPage.first())
    }

    @Test
    fun `onNextClicked should advance to page 1 from page 0`() = runTest {
        val viewModel = OnboardingViewModel()

        viewModel.onNextClicked()

        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onNextClicked should not advance beyond last page`() = runTest {
        val viewModel = OnboardingViewModel()

        // Advance to last page
        viewModel.onNextClicked()
        assertEquals(1, viewModel.currentPage.first())

        // Try to advance further - should stay on page 1
        viewModel.onNextClicked()
        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should update current page`() = runTest {
        val viewModel = OnboardingViewModel()

        viewModel.onPageChanged(1)

        assertEquals(1, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should not accept invalid page numbers`() = runTest {
        val viewModel = OnboardingViewModel()

        // Try negative page
        viewModel.onPageChanged(-1)
        assertEquals(0, viewModel.currentPage.first())

        // Try page beyond range
        viewModel.onPageChanged(5)
        assertEquals(0, viewModel.currentPage.first())
    }

    @Test
    fun `onPageChanged should accept page 0`() = runTest {
        val viewModel = OnboardingViewModel()
        viewModel.onPageChanged(1)

        viewModel.onPageChanged(0)

        assertEquals(0, viewModel.currentPage.first())
    }
}
