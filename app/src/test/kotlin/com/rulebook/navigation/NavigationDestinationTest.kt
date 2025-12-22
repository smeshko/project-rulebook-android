package com.rulebook.navigation

import androidx.core.net.toUri
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationDestinationTest {

    @Test
    fun `Library route has correct route string`() {
        assertEquals("library", Route.Library.route)
    }

    @Test
    fun `Settings route has correct route string`() {
        assertEquals("settings", Route.Settings.route)
    }

    @Test
    fun `Camera route has correct route string`() {
        assertEquals("camera", Route.Camera.route)
    }

    @Test
    fun `Onboarding route has correct route string`() {
        assertEquals("onboarding", Route.Onboarding.route)
    }

    @Test
    fun `Purchase route has correct route string`() {
        assertEquals("purchase", Route.Purchase.route)
    }

    @Test
    fun `Rules route has correct route pattern with argument placeholder`() {
        assertEquals("rules/{gameId}", Route.Rules.route)
    }

    @Test
    fun `Rules createRoute replaces gameId placeholder correctly`() {
        val gameId = "chess-2024"
        val expectedRoute = "rules/chess-2024"
        assertEquals(expectedRoute, Route.Rules.createRoute(gameId))
    }

    @Test
    fun `Rules createRoute handles special characters in gameId`() {
        val gameId = "game_with_underscore"
        assertEquals("rules/game_with_underscore", Route.Rules.createRoute(gameId))
    }

    @Test
    fun `RulebookNavArgs GAME_ID has correct value`() {
        assertEquals("gameId", RulebookNavArgs.GAME_ID)
    }

    // =========================================================================
    // Processing Route Tests (Story 5.1)
    // =========================================================================

    @Test
    fun `Processing route has correct route pattern with imageUri placeholder`() {
        assertEquals("processing/{imageUri}", Route.Processing.route)
    }

    @Test
    fun `RulebookNavArgs IMAGE_URI has correct value`() {
        assertEquals("imageUri", RulebookNavArgs.IMAGE_URI)
    }

    // Note: createRoute() and getImageUri() are tested in instrumentation tests
    // as they require Android runtime context (Uri.parse, Uri.encode)
}
