package com.rulebook.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rulebook.feature.settings.SettingsScreen

/**
 * Navigation host for the Rulebook app.
 *
 * Defines the navigation graph with all screen destinations.
 * Each destination is mapped to its corresponding screen composable.
 *
 * ## Predictive Back Gesture Support
 * This NavHost is configured to work with Android 14+ predictive back gestures:
 * - The `android:enableOnBackInvokedCallback="true"` flag in AndroidManifest.xml enables the feature
 * - Compose Navigation 2.8+ automatically handles predictive back with preview animations
 * - No explicit BackHandler needed - NavHost manages the back stack automatically
 * - Back gesture shows a preview of the previous screen while the user swipes
 * - Main screens (Library, Settings) let the system handle back (exits app)
 * - Detail screens (Camera, Rules, Purchase) automatically pop the back stack
 *
 * @param navController The navigation controller managing the back stack
 * @param modifier Optional modifier for the NavHost container
 * @param startDestination The initial destination when the app launches (defaults to Library)
 */
@Composable
fun RulebookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Route.Library.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Library - main screen showing saved rulebooks
        // Back gesture on main screen lets system handle it (exit app)
        composable(route = Route.Library.route) {
            LibraryPlaceholder()
        }

        // Settings - app configuration and preferences
        // Back gesture on main screen lets system handle it (exit app)
        composable(route = Route.Settings.route) {
            SettingsScreen()
        }

        // Camera - capture rulebook pages
        // Predictive back handled automatically by NavHost - shows preview during gesture
        composable(route = Route.Camera.route) {
            CameraPlaceholder()
        }

        // Onboarding - first-time user experience
        // Back gesture during onboarding should be handled by the screen
        composable(route = Route.Onboarding.route) {
            OnboardingPlaceholder()
        }

        // Purchase - premium features and subscriptions
        // Predictive back handled automatically by NavHost - shows preview during gesture
        composable(route = Route.Purchase.route) {
            PurchasePlaceholder()
        }

        // Rules - displays rules for a specific game with type-safe gameId argument
        // Deep link: rulebook://rules/{gameId}
        // Predictive back handled automatically by NavHost - shows preview during gesture
        // Note: When opened via deep link, Rules is the only destination.
        // NavHost handles this by finishing the activity when back stack is empty.
        composable(
            route = Route.Rules.route,
            arguments = listOf(
                navArgument(RulebookNavArgs.GAME_ID) {
                    type = NavType.StringType
                }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "${DeepLinkConfig.SCHEME}://${DeepLinkConfig.HOST_RULES}/{${RulebookNavArgs.GAME_ID}}"
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(RulebookNavArgs.GAME_ID) ?: ""
            RulesPlaceholder(gameId = gameId)
        }
    }
}
