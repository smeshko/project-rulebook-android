package com.rulebook.navigation

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

    @Test
    fun `ScanProcessing route has correct route pattern with argument placeholder`() {
        assertEquals("scan_processing/{imageUri}", Route.ScanProcessing.route)
    }

    @Test
    fun `ScanProcessing createRoute replaces imageUri placeholder correctly`() {
        val imageUri = "content://media/image/123"
        val expectedRoute = "scan_processing/content://media/image/123"
        assertEquals(expectedRoute, Route.ScanProcessing.createRoute(imageUri))
    }

    @Test
    fun `ScanProcessing createRoute handles URL-encoded URIs`() {
        val imageUri = "content%3A%2F%2Fmedia%2Fimage%2F123"
        assertEquals("scan_processing/content%3A%2F%2Fmedia%2Fimage%2F123", Route.ScanProcessing.createRoute(imageUri))
    }

    @Test
    fun `RulebookNavArgs IMAGE_URI has correct value`() {
        assertEquals("imageUri", RulebookNavArgs.IMAGE_URI)
    }
}
