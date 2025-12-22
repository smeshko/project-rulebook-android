package com.rulebook.navigation

import android.net.Uri
import androidx.core.net.toUri
import androidx.navigation.NavBackStackEntry

/**
 * Sealed class defining all navigation routes in the Rulebook app.
 *
 * Each route object represents a unique destination in the navigation graph.
 * Routes with arguments provide helper functions to create the full route string.
 *
 * @property route The route string used by the navigation system
 */
sealed class Route(val route: String) {
    /** Library screen - main screen showing saved rulebooks */
    data object Library : Route("library")

    /** Settings screen - app configuration and preferences */
    data object Settings : Route("settings")

    /** Camera screen - capture rulebook pages */
    data object Camera : Route("camera")

    /** Onboarding screen - first-time user experience */
    data object Onboarding : Route("onboarding")

    /** Purchase screen - premium features and subscriptions */
    data object Purchase : Route("purchase")

    /**
     * Processing screen - game recognition and rules generation flow.
     *
     * This route requires an imageUri argument containing the captured photo
     * that will be processed for game recognition and rules generation.
     *
     * Part of Story 5.1 - credit-gated navigation to processing flow.
     */
    data object Processing : Route("processing/{${RulebookNavArgs.IMAGE_URI}}") {
        /**
         * Creates the full route string with the provided image URI.
         *
         * The URI is URL-encoded to safely pass it as a route parameter.
         *
         * @param imageUri The URI of the captured image to process
         * @return The complete route string for navigation
         */
        fun createRoute(imageUri: Uri): String {
            val encodedUri = Uri.encode(imageUri.toString())
            return "processing/$encodedUri"
        }

        /**
         * Extracts the image URI from the navigation back stack entry.
         *
         * @param backStackEntry The navigation back stack entry containing route arguments
         * @return The decoded image URI
         * @throws IllegalArgumentException if the IMAGE_URI argument is missing
         */
        fun getImageUri(backStackEntry: NavBackStackEntry): Uri {
            val uriString = backStackEntry.arguments?.getString(RulebookNavArgs.IMAGE_URI)
                ?: throw IllegalArgumentException("Image URI not found in route arguments")
            return uriString.toUri()
        }
    }

    /**
     * Rules screen - displays rules for a specific game.
     *
     * This route requires a gameId argument to identify which game's rules to display.
     */
    data object Rules : Route("rules/{${RulebookNavArgs.GAME_ID}}") {
        /**
         * Creates the full route string with the provided game ID.
         *
         * @param gameId The unique identifier of the game
         * @return The complete route string for navigation
         */
        fun createRoute(gameId: String): String = "rules/$gameId"
    }
}

/**
 * Navigation argument keys used across the app.
 *
 * Centralizes argument key definitions to ensure consistency
 * between route definitions and argument extraction.
 */
object RulebookNavArgs {
    /** Argument key for game identifier in Rules route */
    const val GAME_ID = "gameId"

    /** Argument key for image URI in Processing route (Story 5.1) */
    const val IMAGE_URI = "imageUri"
}
