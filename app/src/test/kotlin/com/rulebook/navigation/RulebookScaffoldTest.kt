package com.rulebook.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for RulebookScaffold visibility logic.
 *
 * Tests verify the visibility rules for bottom bar and FAB based on current route.
 * These are pure logic tests that don't require Android/Compose framework.
 *
 * Visibility Rules:
 * - Bottom bar visible on: Library, Settings
 * - FAB visible on: Library, Settings
 * - Both hidden on: Camera, Rules, Onboarding, Purchase
 */
class RulebookScaffoldTest {

    // =========================================================================
    // Main Routes - Bottom bar and FAB should be visible
    // =========================================================================

    @Test
    fun `bottom bar visible on Library route`() {
        assertTrue(shouldShowBottomBar(Route.Library.route))
    }

    @Test
    fun `bottom bar visible on Settings route`() {
        assertTrue(shouldShowBottomBar(Route.Settings.route))
    }

    @Test
    fun `FAB visible on Library route`() {
        assertTrue(shouldShowFab(Route.Library.route))
    }

    @Test
    fun `FAB visible on Settings route`() {
        assertTrue(shouldShowFab(Route.Settings.route))
    }

    // =========================================================================
    // Detail Routes - Bottom bar and FAB should be hidden
    // =========================================================================

    @Test
    fun `bottom bar hidden on Camera route`() {
        assertFalse(shouldShowBottomBar(Route.Camera.route))
    }

    @Test
    fun `bottom bar hidden on Rules route`() {
        assertFalse(shouldShowBottomBar(Route.Rules.route))
    }

    @Test
    fun `bottom bar hidden on Onboarding route`() {
        assertFalse(shouldShowBottomBar(Route.Onboarding.route))
    }

    @Test
    fun `bottom bar hidden on Purchase route`() {
        assertFalse(shouldShowBottomBar(Route.Purchase.route))
    }

    @Test
    fun `FAB hidden on Camera route`() {
        assertFalse(shouldShowFab(Route.Camera.route))
    }

    @Test
    fun `FAB hidden on Rules route`() {
        assertFalse(shouldShowFab(Route.Rules.route))
    }

    @Test
    fun `FAB hidden on Onboarding route`() {
        assertFalse(shouldShowFab(Route.Onboarding.route))
    }

    @Test
    fun `FAB hidden on Purchase route`() {
        assertFalse(shouldShowFab(Route.Purchase.route))
    }

    // =========================================================================
    // Edge Cases
    // =========================================================================

    @Test
    fun `bottom bar hidden for null route`() {
        assertFalse(shouldShowBottomBar(null))
    }

    @Test
    fun `FAB hidden for null route`() {
        assertFalse(shouldShowFab(null))
    }

    @Test
    fun `bottom bar hidden for unknown route`() {
        assertFalse(shouldShowBottomBar("unknown/route"))
    }

    @Test
    fun `FAB hidden for unknown route`() {
        assertFalse(shouldShowFab("unknown/route"))
    }

    @Test
    fun `bottom bar hidden for Rules with gameId`() {
        // When navigating to rules/someGameId, should still be hidden
        assertFalse(shouldShowBottomBar("rules/game123"))
    }

    @Test
    fun `FAB hidden for Rules with gameId`() {
        assertFalse(shouldShowFab("rules/game123"))
    }
}
