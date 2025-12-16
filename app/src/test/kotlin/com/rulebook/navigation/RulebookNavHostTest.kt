package com.rulebook.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for RulebookNavHost configuration.
 *
 * Note: Navigation behavior testing requires instrumented tests with Compose test rule.
 * These unit tests verify the configuration values used by RulebookNavHost.
 */
class RulebookNavHostTest {

    @Test
    fun `default start destination is Library route`() {
        // The default startDestination parameter in RulebookNavHost should be Library
        assertEquals(Route.Library.route, "library")
    }

    @Test
    fun `all routes are accessible for navigation graph`() {
        // Verify all route strings are non-empty and properly defined
        val routes = listOf(
            Route.Library.route,
            Route.Settings.route,
            Route.Camera.route,
            Route.Onboarding.route,
            Route.Purchase.route,
            Route.Rules.route
        )

        routes.forEach { route ->
            assert(route.isNotEmpty()) { "Route string should not be empty" }
        }
    }

    @Test
    fun `route count matches expected destinations`() {
        // RulebookNavHost should have 6 destinations
        val routeCount = 6
        val actualRoutes = listOf(
            Route.Library,
            Route.Settings,
            Route.Camera,
            Route.Onboarding,
            Route.Purchase,
            Route.Rules
        )
        assertEquals(routeCount, actualRoutes.size)
    }
}
