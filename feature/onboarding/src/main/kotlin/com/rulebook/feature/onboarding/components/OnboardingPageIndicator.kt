package com.rulebook.feature.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * Brutalist-styled page indicator for onboarding screens.
 *
 * Displays a row of dots indicating total pages and current position.
 * Uses sharp-edged squares with slight rounding for brutalist aesthetic.
 *
 * Features:
 * - Animated size transitions between active/inactive states
 * - Animated color transitions
 * - Accessibility support with content description
 * - Brutalist styling with square-ish dots (2dp corner radius)
 *
 * @param pageCount Total number of pages in the pager.
 * @param currentPage Currently displayed page index (0-based).
 * @param modifier Optional modifier for the indicator row.
 * @param activeColor Color for the active page dot (default: primary).
 * @param inactiveColor Color for inactive page dots (default: surfaceVariant).
 */
@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier.semantics {
            contentDescription = "Page ${currentPage + 1} of $pageCount"
        },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val isActive = page == currentPage

            // Animated size for smooth transitions
            val size by animateDpAsState(
                targetValue = if (isActive) 12.dp else 8.dp,
                animationSpec = tween(durationMillis = 150),
                label = "indicator_size"
            )

            // Animated color for smooth transitions
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 150),
                label = "indicator_color"
            )

            // Brutalist: square with slight rounding (2dp radius)
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Preview(showBackground = true, name = "Page Indicator - Page 1")
@Composable
private fun OnboardingPageIndicatorPage1Preview() {
    RulebookTheme {
        OnboardingPageIndicator(
            pageCount = 2,
            currentPage = 0
        )
    }
}

@Preview(showBackground = true, name = "Page Indicator - Page 2")
@Composable
private fun OnboardingPageIndicatorPage2Preview() {
    RulebookTheme {
        OnboardingPageIndicator(
            pageCount = 2,
            currentPage = 1
        )
    }
}
