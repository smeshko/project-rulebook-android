package com.rulebook.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions

/**
 * Extension functions for type-safe navigation in the Rulebook app.
 *
 * These functions provide a clean API for navigating between screens
 * while ensuring proper route construction and navigation behavior.
 */

/**
 * Navigates to the Rules screen for a specific game.
 *
 * @param gameId The unique identifier of the game to display rules for
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToRules(gameId: String, navOptions: NavOptions? = null) {
    navigate(Route.Rules.createRoute(gameId), navOptions)
}

/**
 * Navigates to the Library screen, clearing the back stack to this destination.
 *
 * This is useful when returning to the Library from a deep navigation flow,
 * ensuring the user doesn't have a long back stack to navigate through.
 */
fun NavController.navigateToLibrary() {
    navigate(Route.Library.route) {
        popUpTo(Route.Library.route) { inclusive = true }
    }
}

/**
 * Navigates to the Settings screen.
 *
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToSettings(navOptions: NavOptions? = null) {
    navigate(Route.Settings.route, navOptions)
}

/**
 * Navigates to the Camera screen.
 *
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToCamera(navOptions: NavOptions? = null) {
    navigate(Route.Camera.route, navOptions)
}

/**
 * Navigates to the Onboarding screen.
 *
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) {
    navigate(Route.Onboarding.route, navOptions)
}

/**
 * Navigates to the Purchase screen.
 *
 * @param source The navigation source that triggered the paywall (e.g., "scan_gate", "settings")
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToPurchase(source: String = "unknown", navOptions: NavOptions? = null) {
    navigate(Route.Purchase.createRoute(source), navOptions)
}

/**
 * Completes onboarding and navigates to Library, removing onboarding from back stack.
 *
 * This ensures users cannot navigate back to onboarding after completing it.
 */
fun NavController.completeOnboarding() {
    navigate(Route.Library.route) {
        popUpTo(Route.Onboarding.route) { inclusive = true }
    }
}

/**
 * Navigates to a bottom bar destination with state preservation.
 *
 * This function implements the recommended navigation pattern for bottom navigation bars:
 * - Pops up to the start destination to avoid building up the back stack
 * - Uses launchSingleTop to avoid multiple copies of the same destination
 * - Saves and restores state when switching between tabs
 *
 * @param route The route to navigate to (should be a bottom bar destination)
 */
fun NavController.navigateToBottomBarDestination(route: String) {
    navigate(route) {
        // Pop up to start destination to avoid building up back stack
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of the same destination
        launchSingleTop = true
        // Restore state when navigating back to a previously visited destination
        restoreState = true
    }
}
