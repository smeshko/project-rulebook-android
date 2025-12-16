package com.rulebook.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for NavController extension functions.
 *
 * Note: Full integration tests for NavController extensions require
 * instrumented tests with a real NavController. These tests verify
 * the route construction logic used by the extension functions.
 */
class NavControllerExtensionsTest {

    @Test
    fun `navigateToRules uses correct route with gameId`() {
        // Extension uses Route.Rules.createRoute which should produce correct path
        val gameId = "chess-classic"
        val expectedRoute = "rules/$gameId"
        assertEquals(expectedRoute, Route.Rules.createRoute(gameId))
    }

    @Test
    fun `navigateToLibrary uses Library route`() {
        // Extension navigates to Route.Library.route
        assertEquals("library", Route.Library.route)
    }

    @Test
    fun `navigateToSettings uses Settings route`() {
        // Extension navigates to Route.Settings.route
        assertEquals("settings", Route.Settings.route)
    }

    @Test
    fun `navigateToCamera uses Camera route`() {
        // Extension navigates to Route.Camera.route
        assertEquals("camera", Route.Camera.route)
    }

    @Test
    fun `navigateToOnboarding uses Onboarding route`() {
        // Extension navigates to Route.Onboarding.route
        assertEquals("onboarding", Route.Onboarding.route)
    }

    @Test
    fun `navigateToPurchase uses Purchase route`() {
        // Extension navigates to Route.Purchase.route
        assertEquals("purchase", Route.Purchase.route)
    }

    @Test
    fun `completeOnboarding navigates to Library route`() {
        // Extension navigates to Route.Library.route with popUpTo Onboarding
        assertEquals("library", Route.Library.route)
        assertEquals("onboarding", Route.Onboarding.route)
    }

    @Test
    fun `Rules createRoute handles various gameId formats`() {
        // Test different gameId formats
        assertEquals("rules/simple", Route.Rules.createRoute("simple"))
        assertEquals("rules/with-dashes", Route.Rules.createRoute("with-dashes"))
        assertEquals("rules/with_underscores", Route.Rules.createRoute("with_underscores"))
        assertEquals("rules/123numeric", Route.Rules.createRoute("123numeric"))
    }
}
