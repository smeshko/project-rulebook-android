package com.rulebook.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Onboarding screen composable.
 *
 * Placeholder implementation that displays the screen name.
 * Full implementation with value proposition and getting started
 * screens will be added in Stories 3.2 and 3.3.
 *
 * @param onComplete Callback invoked when onboarding is completed.
 * @param modifier Optional modifier for the screen.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Onboarding Screen - Placeholder")
    }
}
