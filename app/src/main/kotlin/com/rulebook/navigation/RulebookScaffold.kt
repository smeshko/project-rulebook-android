package com.rulebook.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rulebook.core.designsystem.component.RulebookFAB

/**
 * Routes where the bottom navigation bar should be visible.
 * Derived from BottomBarDestination to ensure a single source of truth.
 */
private val MAIN_ROUTES: Set<String> by lazy {
    BottomBarDestination.items.map { it.route }.toSet()
}

/**
 * Determines if the bottom navigation bar should be shown for the given route.
 *
 * @param currentRoute The current navigation route
 * @return true if bottom bar should be visible, false otherwise
 */
fun shouldShowBottomBar(currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    return currentRoute in MAIN_ROUTES
}

/**
 * Determines if the FAB should be shown for the given route.
 *
 * The FAB is visible on the same routes as the bottom bar (Library and Settings).
 *
 * @param currentRoute The current navigation route
 * @return true if FAB should be visible, false otherwise
 */
fun shouldShowFab(currentRoute: String?): Boolean {
    if (currentRoute == null) return false
    return currentRoute in MAIN_ROUTES
}

/**
 * RulebookScaffold - Main scaffold wrapper for the Rulebook app.
 *
 * Provides consistent layout structure with:
 * - Bottom navigation bar (visible on main routes, with slide animation)
 * - Floating action button for camera (visible on main routes, with scale animation)
 * - Content area with proper system bar insets
 *
 * ## Edge-to-Edge Support
 * This scaffold properly handles edge-to-edge display:
 * - Content respects system bars via WindowInsets.systemBars
 * - Bottom bar is positioned above the navigation bar
 * - FAB is positioned above the bottom bar
 * - innerPadding should be applied to content for proper layout
 *
 * ## Visibility Rules
 * | Route      | Bottom Bar | FAB |
 * |------------|------------|-----|
 * | Library    | ✓          | ✓   |
 * | Settings   | ✓          | ✓   |
 * | Camera     | ✗          | ✗   |
 * | Rules      | ✗          | ✗   |
 * | Onboarding | ✗          | ✗   |
 * | Purchase   | ✗          | ✗   |
 *
 * @param navController Navigation controller for tracking current route
 * @param modifier Modifier for the scaffold
 * @param content Content lambda receiving inner padding values
 */
@Composable
fun RulebookScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = shouldShowBottomBar(currentRoute)
    val showFab = shouldShowFab(currentRoute)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                RulebookBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigateToBottomBarDestination(route)
                    }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(animationSpec = tween(durationMillis = 200)),
                exit = scaleOut(animationSpec = tween(durationMillis = 200))
            ) {
                RulebookFAB(
                    onClick = { navController.navigate(Route.Camera.route) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        content(innerPadding)
    }
}
