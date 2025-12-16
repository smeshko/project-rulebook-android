package com.rulebook.navigation

import androidx.navigation.NavController
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
 * @param navOptions Optional navigation options for custom behavior
 */
fun NavController.navigateToPurchase(navOptions: NavOptions? = null) {
    navigate(Route.Purchase.route, navOptions)
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
