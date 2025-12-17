package com.rulebook.feature.onboarding.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Bottom section of the onboarding screen containing page indicator and action button.
 *
 * Features:
 * - Page indicator showing progress through onboarding
 * - Primary action button (Next on page 1, Get Started on page 2)
 * - Navigation bar padding for edge-to-edge display
 *
 * @param currentPage The currently displayed page index (0-based).
 * @param pageCount Total number of pages in the pager.
 * @param onNextClick Callback invoked when the action button is clicked.
 * @param modifier Optional modifier for the section.
 */
@Composable
fun OnboardingBottomSection(
    currentPage: Int,
    pageCount: Int,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLastPage = currentPage == pageCount - 1
    val buttonText = if (isLastPage) "Get Started" else "Next"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicator
        OnboardingPageIndicator(
            pageCount = pageCount,
            currentPage = currentPage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary action button
        RulebookButton(
            text = buttonText,
            onClick = onNextClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Bottom Section - Page 1")
@Composable
private fun OnboardingBottomSectionPage1Preview() {
    RulebookTheme {
        OnboardingBottomSection(
            currentPage = 0,
            pageCount = 2,
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Bottom Section - Page 2")
@Composable
private fun OnboardingBottomSectionPage2Preview() {
    RulebookTheme {
        OnboardingBottomSection(
            currentPage = 1,
            pageCount = 2,
            onNextClick = {}
        )
    }
}
