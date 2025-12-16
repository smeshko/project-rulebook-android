package com.rulebook.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

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
            // Placeholder - actual screen will be provided in Task 5
        }

        // Settings - app configuration and preferences
        composable(route = Route.Settings.route) {
            // Placeholder - actual screen will be provided in Task 5
        }

        // Camera - capture rulebook pages
        composable(route = Route.Camera.route) {
            // Placeholder - actual screen will be provided in Task 5
        }

        // Onboarding - first-time user experience
        composable(route = Route.Onboarding.route) {
            // Placeholder - actual screen will be provided in Task 5
        }

        // Purchase - premium features and subscriptions
        composable(route = Route.Purchase.route) {
            // Placeholder - actual screen will be provided in Task 5
        }

        // Rules - displays rules for a specific game
        // Note: Type-safe arguments will be added in Task 3
        composable(route = Route.Rules.route) {
            // Placeholder - actual screen will be provided in Task 5
        }
    }
}
