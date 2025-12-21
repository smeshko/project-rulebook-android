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
 *
 * ## Predictive Back Gesture Support
 * Compose Navigation 2.8+ automatically handles predictive back gestures
 * when `android:enableOnBackInvokedCallback="true"` is set in the manifest.
 * The NavHost manages back navigation with preview animations - no explicit
 * BackHandler is needed for standard back-to-previous-screen behavior.
 */

/**
 * Placeholder for Library screen - main screen showing saved rulebooks.
 *
 * As a main/root screen, back navigation is handled by the system (exits app).
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
 *
 * As a main/root screen, back navigation is handled by the system (exits app).
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
 *
 * Supports predictive back gesture - swipe from left edge shows preview
 * of previous screen before navigating back. Compose Navigation 2.8+
 * handles the preview animation automatically via NavHost.
 */
@Composable
fun CameraPlaceholder(modifier: Modifier = Modifier) {
    // No BackHandler needed - Compose Navigation handles predictive back
    // with preview animations automatically when using NavHost
    PlaceholderContent(
        title = "Camera",
        subtitle = "Coming in Epic 3",
        modifier = modifier
    )
}

/**
 * Placeholder for Onboarding screen - first-time user experience.
 *
 * Onboarding typically handles back navigation specially (e.g., go to previous step
 * or show confirmation dialog). This placeholder uses default system behavior.
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
 *
 * Supports predictive back gesture - swipe from left edge shows preview
 * of previous screen before navigating back. Compose Navigation 2.8+
 * handles the preview animation automatically via NavHost.
 */
@Composable
fun PurchasePlaceholder(modifier: Modifier = Modifier) {
    // No BackHandler needed - Compose Navigation handles predictive back
    // with preview animations automatically when using NavHost
    PlaceholderContent(
        title = "Purchase",
        subtitle = "Coming in Epic 6",
        modifier = modifier
    )
}

/**
 * Placeholder for Rules screen - displays rules for a specific game.
 *
 * Supports predictive back gesture - swipe from left edge shows preview
 * of previous screen before navigating back. Compose Navigation 2.8+
 * handles the preview animation automatically via NavHost.
 *
 * @param gameId The unique identifier of the game (displayed for debugging)
 */
@Composable
fun RulesPlaceholder(
    gameId: String,
    modifier: Modifier = Modifier
) {
    // No BackHandler needed - Compose Navigation handles predictive back
    // with preview animations automatically when using NavHost
    PlaceholderContent(
        title = "Rules",
        subtitle = "Game: $gameId\nComing in Epic 4",
        modifier = modifier
    )
}

/**
 * Placeholder for Scan Processing screen - displays AI recognition progress.
 *
 * Supports predictive back gesture - swipe from left edge shows preview
 * of previous screen before navigating back. Compose Navigation 2.8+
 * handles the preview animation automatically via NavHost.
 *
 * @param imageUri The URI of the image being processed (displayed for debugging)
 */
@Composable
fun ScanProcessingPlaceholder(
    imageUri: String,
    modifier: Modifier = Modifier
) {
    // No BackHandler needed - Compose Navigation handles predictive back
    // with preview animations automatically when using NavHost
    PlaceholderContent(
        title = "Scan Processing",
        subtitle = "Image: ${imageUri.take(40)}...\nComing in Story 5.2",
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
