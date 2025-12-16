package com.rulebook.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarDestinationTest {

    @Test
    fun `Library destination has correct route`() {
        assertEquals(Route.Library.route, BottomBarDestination.Library.route)
    }

    @Test
    fun `Library destination has correct label`() {
        assertEquals("Library", BottomBarDestination.Library.label)
    }

    @Test
    fun `Library destination has outlined icon for unselected state`() {
        assertEquals(Icons.Outlined.GridView, BottomBarDestination.Library.icon)
    }

    @Test
    fun `Library destination has filled icon for selected state`() {
        assertEquals(Icons.Filled.GridView, BottomBarDestination.Library.selectedIcon)
    }

    @Test
    fun `Settings destination has correct route`() {
        assertEquals(Route.Settings.route, BottomBarDestination.Settings.route)
    }

    @Test
    fun `Settings destination has correct label`() {
        assertEquals("Settings", BottomBarDestination.Settings.label)
    }

    @Test
    fun `Settings destination has outlined icon for unselected state`() {
        assertEquals(Icons.Outlined.Settings, BottomBarDestination.Settings.icon)
    }

    @Test
    fun `Settings destination has filled icon for selected state`() {
        assertEquals(Icons.Filled.Settings, BottomBarDestination.Settings.selectedIcon)
    }

    // Note: Tests for BottomBarDestination.items are in instrumented tests
    // because the companion object initialization triggers Android framework code.
    // Unit tests verify individual destinations directly.
}
