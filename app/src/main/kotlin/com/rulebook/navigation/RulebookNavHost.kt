package com.rulebook.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Navigation host for the Rulebook app.
 *
 * Defines the navigation graph with all screen destinations.
 * Each destination is mapped to its corresponding screen composable.
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
        composable(route = Route.Library.route) {
            LibraryPlaceholder()
        }

        // Settings - app configuration and preferences
        composable(route = Route.Settings.route) {
            SettingsPlaceholder()
        }

        // Camera - capture rulebook pages
        composable(route = Route.Camera.route) {
            CameraPlaceholder()
        }

        // Onboarding - first-time user experience
        composable(route = Route.Onboarding.route) {
            OnboardingPlaceholder()
        }

        // Purchase - premium features and subscriptions
        composable(route = Route.Purchase.route) {
            PurchasePlaceholder()
        }

        // Rules - displays rules for a specific game with type-safe gameId argument
        // Deep link: rulebook://rules/{gameId}
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
