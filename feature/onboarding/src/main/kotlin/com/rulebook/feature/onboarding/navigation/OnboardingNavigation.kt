package com.rulebook.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.rulebook.feature.onboarding.OnboardingScreen

/**
 * Route constant for onboarding navigation.
 */
const val ONBOARDING_ROUTE = "onboarding"

/**
 * Adds the onboarding screen to the navigation graph.
 *
 * @param onOnboardingComplete Callback invoked when onboarding is completed.
 *                            Typically navigates to the Library and clears the back stack.
 */
fun NavGraphBuilder.onboardingScreen(
    onOnboardingComplete: () -> Unit
) {
    composable(route = ONBOARDING_ROUTE) {
        OnboardingScreen(
            onComplete = onOnboardingComplete
        )
    }
}

/**
 * Navigates to the onboarding screen.
 *
 * Clears the entire back stack to make onboarding the root destination.
 */
fun NavController.navigateToOnboarding() {
    navigate(ONBOARDING_ROUTE) {
        popUpTo(0) { inclusive = true }
    }
}

/**
 * Navigates from onboarding to library, clearing the back stack.
 *
 * Used when onboarding completes to replace the onboarding screen
 * with the library as the new root.
 */
fun NavController.navigateToLibraryFromOnboarding() {
    navigate("library") {
        popUpTo(ONBOARDING_ROUTE) { inclusive = true }
    }
}
