package com.rulebook.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for RulebookBottomBar component logic.
 *
 * Note: Full UI tests for RulebookBottomBar require instrumented tests
 * with Compose testing framework. These tests verify the supporting
 * logic and configuration used by the bottom bar.
 *
 * Tests accessing BottomBarDestination.items are avoided in unit tests
 * because the companion object initialization triggers icon loading
 * which requires the Android framework.
 */
class RulebookBottomBarTest {

    @Test
    fun `Library destination has correct route`() {
        assertEquals("library", BottomBarDestination.Library.route)
    }

    @Test
    fun `Library destination has correct label`() {
        assertEquals("Library", BottomBarDestination.Library.label)
    }

    @Test
    fun `Library destination has icons configured`() {
        assertNotNull(BottomBarDestination.Library.icon)
        assertNotNull(BottomBarDestination.Library.selectedIcon)
    }

    @Test
    fun `Settings destination has correct route`() {
        assertEquals("settings", BottomBarDestination.Settings.route)
    }

    @Test
    fun `Settings destination has correct label`() {
        assertEquals("Settings", BottomBarDestination.Settings.label)
    }

    @Test
    fun `Settings destination has icons configured`() {
        assertNotNull(BottomBarDestination.Settings.icon)
        assertNotNull(BottomBarDestination.Settings.selectedIcon)
    }

    @Test
    fun `Library and Settings have distinct routes`() {
        assertNotEquals(
            BottomBarDestination.Library.route,
            BottomBarDestination.Settings.route
        )
    }

    @Test
    fun `Library and Settings have distinct labels`() {
        assertNotEquals(
            BottomBarDestination.Library.label,
            BottomBarDestination.Settings.label
        )
    }

    @Test
    fun `Library has different icons for selected and unselected states`() {
        assertNotEquals(
            BottomBarDestination.Library.icon,
            BottomBarDestination.Library.selectedIcon
        )
    }

    @Test
    fun `Settings has different icons for selected and unselected states`() {
        assertNotEquals(
            BottomBarDestination.Settings.icon,
            BottomBarDestination.Settings.selectedIcon
        )
    }
}
