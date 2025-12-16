package com.rulebook.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Placeholder screens for navigation destinations.
 *
 * These temporary screens are used during the navigation setup phase
 * and will be replaced by actual feature screens in subsequent stories.
 *
 * Each placeholder displays the screen name and notes when the actual
 * implementation is expected.
 */

/**
 * Placeholder for Library screen - main screen showing saved rulebooks.
 */
@Composable
fun LibraryPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Library",
        subtitle = "Coming in Story 2.5",
        modifier = modifier
    )
}

/**
 * Placeholder for Settings screen - app configuration and preferences.
 */
@Composable
fun SettingsPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Settings",
        subtitle = "Coming in Story 2.6",
        modifier = modifier
    )
}

/**
 * Placeholder for Camera screen - capture rulebook pages.
 */
@Composable
fun CameraPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Camera",
        subtitle = "Coming in Epic 3",
        modifier = modifier
    )
}

/**
 * Placeholder for Onboarding screen - first-time user experience.
 */
@Composable
fun OnboardingPlaceholder(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Onboarding",
        subtitle = "Coming in Epic 6",
        modifier = modifier
    )
}

/**
 * Placeholder for Purchase screen - premium features and subscriptions.
 */
@Composable
fun PurchasePlaceholder(modifier: Modifier = Modifier) {
    PlaceholderContent(
        title = "Purchase",
        subtitle = "Coming in Epic 6",
        modifier = modifier
    )
}

/**
 * Placeholder for Rules screen - displays rules for a specific game.
 *
 * @param gameId The unique identifier of the game (displayed for debugging)
 */
@Composable
fun RulesPlaceholder(
    gameId: String,
    modifier: Modifier = Modifier
) {
    PlaceholderContent(
        title = "Rules",
        subtitle = "Game: $gameId\nComing in Epic 4",
        modifier = modifier
    )
}

/**
 * Base placeholder content composable.
 *
 * @param title The screen title to display
 * @param subtitle Additional context about when the screen will be implemented
 * @param modifier Optional modifier for the container
 */
@Composable
private fun PlaceholderContent(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}
