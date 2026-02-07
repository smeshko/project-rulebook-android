package com.rulebook.navigation

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
     * Generation screen - shows scan/generation progress with phase indicator.
     *
     * This route requires an imageUri argument identifying the image to process.
     */
    data object Generation : Route("generation/{${RulebookNavArgs.IMAGE_URI}}") {
        /**
         * Creates the full route string with the provided image URI.
         *
         * @param imageUri The URI of the image to process (URL-encoded)
         * @return The complete route string for navigation
         */
        fun createRoute(imageUri: String): String = "generation/${java.net.URLEncoder.encode(imageUri, "UTF-8")}"
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

    /** Argument key for image URI in Generation route */
    const val IMAGE_URI = "imageUri"
}
