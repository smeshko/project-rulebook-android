package com.rulebook.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.feature.onboarding.components.OnboardingBottomSection
import com.rulebook.feature.onboarding.components.OnboardingPage1Content
import com.rulebook.feature.onboarding.components.OnboardingPage2Content
import org.koin.androidx.compose.koinViewModel

/**
 * Onboarding screen composable with HorizontalPager for 2 pages.
 *
 * Implements a full-screen onboarding flow with:
 * - HorizontalPager containing 2 pages (Value Proposition and Getting Started)
 * - Skip button in top-right corner
 * - Bottom navigation with Next/Get Started button
 * - Edge-to-edge display with proper insets
 *
 * Navigation events are handled through the ViewModel which persists onboarding
 * completion state and emits navigation events.
 *
 * @param onComplete Callback invoked when onboarding is completed (navigates to Library).
 * @param modifier Optional modifier for the screen.
 * @param viewModel The ViewModel managing onboarding state.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.entries.size })
    val haptic = LocalHapticFeedback.current

    // Track previous page to detect actual page changes vs initial composition
    val previousPage = remember { mutableIntStateOf(-1) }

    // Handle navigation events from ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                OnboardingNavigationEvent.NavigateToLibrary -> onComplete()
            }
        }
    }

    // Sync pager state with ViewModel when currentPage changes
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    // Sync ViewModel with pager state when user swipes and provide haptic feedback
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
        // Haptic feedback on page change (but not on initial composition)
        // Using previousPage tracking ensures haptic fires when swiping in any direction
        if (previousPage.intValue != -1 && previousPage.intValue != pagerState.currentPage) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previousPage.intValue = pagerState.currentPage
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content - HorizontalPager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingPage1Content()
                1 -> OnboardingPage2Content()
            }
        }

        // Skip button - top right with status bar padding
        TextButton(
            onClick = { viewModel.onSkipClicked() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bottom section with Next button and page indicator
        OnboardingBottomSection(
            currentPage = pagerState.currentPage,
            pageCount = OnboardingPage.entries.size,
            onNextClick = {
                if (pagerState.currentPage < OnboardingPage.entries.size - 1) {
                    viewModel.onNextClicked()
                } else {
                    viewModel.onGetStartedClicked()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
