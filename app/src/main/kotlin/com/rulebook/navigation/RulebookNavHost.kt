package com.rulebook.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.rulebook.feature.camera.CameraScreen
import com.rulebook.feature.library.LibraryScreen
import com.rulebook.feature.onboarding.OnboardingScreen
import com.rulebook.feature.settings.SettingsScreen

/**
 * Animation duration for screen transitions.
 */
private const val TRANSITION_DURATION_MS = 300

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
        modifier = modifier,
        // Default transitions - fade for most screens
        enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
    ) {
        // Library - main screen showing saved rulebooks
        // Back gesture on main screen lets system handle it (exit app)
        // Uses default fade transition
        composable(
            route = Route.Library.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            LibraryScreen(
                onNavigateToCamera = { navController.navigate(Route.Camera.route) },
                onNavigateToRules = { gameId -> navController.navigate(Route.Rules.createRoute(gameId)) }
            )
        }

        // Settings - app configuration and preferences
        // Back gesture on main screen lets system handle it (exit app)
        // Uses default fade transition
        composable(
            route = Route.Settings.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            SettingsScreen()
        }

        // Camera - capture rulebook pages
        // Full-screen camera preview with immersive mode (hidden system bars)
        // Predictive back handled automatically by NavHost - shows preview during gesture
        // Uses slide transition for detail screen
        composable(
            route = Route.Camera.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            CameraScreen()
        }

        // Onboarding - first-time user experience
        // Can be a start destination for new users
        // Uses fade transition (consistent with other start destinations)
        composable(
            route = Route.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            OnboardingScreen(
                onComplete = {
                    // Navigate to Library and clear onboarding from back stack
                    navController.navigate(Route.Library.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Purchase - premium features and subscriptions
        // Predictive back handled automatically by NavHost - shows preview during gesture
        // Uses slide transition for detail screen
        composable(
            route = Route.Purchase.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            PurchasePlaceholder()
        }

        // Rules - displays rules for a specific game with type-safe gameId argument
        // Deep link: rulebook://rules/{gameId}
        // Predictive back handled automatically by NavHost - shows preview during gesture
        // Note: When opened via deep link, Rules is the only destination.
        // NavHost handles this by finishing the activity when back stack is empty.
        // Uses slide transition for detail screen
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
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(TRANSITION_DURATION_MS)) },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(RulebookNavArgs.GAME_ID) ?: ""
            RulesPlaceholder(gameId = gameId)
        }
    }
}
