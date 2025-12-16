package com.rulebook.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Sealed class defining destinations shown in the bottom navigation bar.
 *
 * Each destination contains the route, icons for selected/unselected states,
 * and a display label. This provides a type-safe way to define and iterate
 * over bottom bar destinations.
 *
 * @property route The navigation route string for this destination
 * @property icon The icon displayed when this destination is not selected (outlined)
 * @property selectedIcon The icon displayed when this destination is selected (filled)
 * @property label The text label displayed below the icon
 */
sealed class BottomBarDestination(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    /**
     * Library destination - main screen showing saved rulebooks.
     * Uses GridView icons for visual consistency with grid-based content.
     */
    data object Library : BottomBarDestination(
        route = Route.Library.route,
        icon = Icons.Outlined.GridView,
        selectedIcon = Icons.Filled.GridView,
        label = "Library"
    )

    /**
     * Settings destination - app configuration and preferences.
     * Uses Settings/gear icons as standard convention for settings.
     */
    data object Settings : BottomBarDestination(
        route = Route.Settings.route,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        label = "Settings"
    )

    companion object {
        /**
         * Ordered list of all bottom bar destinations.
         * The order determines the display order in the bottom navigation bar.
         */
        val items: List<BottomBarDestination> = listOf(Library, Settings)
    }
}
