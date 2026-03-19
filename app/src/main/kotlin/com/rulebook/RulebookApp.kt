package com.rulebook

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rulebook.navigation.Route
import com.rulebook.navigation.RulebookNavHost
import com.rulebook.navigation.RulebookScaffold
import com.rulebook.startup.StartupDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Root composable for the Rulebook app.
 *
 * This composable:
 * - Configures theme-aware status bar icons (light/dark)
 * - Applies WindowInsets for proper edge-to-edge content layout
 * - Provides the RulebookScaffold structure with bottom bar, FAB, and navigation
 *
 * @param startDestination The initial navigation destination based on onboarding status.
 * @param darkTheme Whether the app is currently in dark theme, resolved at MainActivity level.
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun RulebookApp(
    startDestination: StartupDestination,
    darkTheme: Boolean = isSystemInDarkTheme(),
    newIntentFlow: Flow<Intent> = emptyFlow(),
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    // Handle deep links from onNewIntent (warm-start shortcut navigation).
    // NavHost only processes the Activity's intent on initial composition;
    // subsequent intents (singleTop) must be forwarded explicitly.
    LaunchedEffect(Unit) {
        newIntentFlow.collect { intent ->
            navController.handleDeepLink(intent)
        }
    }

    // Map StartupDestination to navigation route
    val startRoute = when (startDestination) {
        StartupDestination.Onboarding -> Route.Onboarding.route
        StartupDestination.Library -> Route.Library.route
    }

    // Configure status bar and navigation bar icon colors based on theme
    // Note: enableEdgeToEdge() in MainActivity handles transparent bars
    ConfigureSystemBars(isDarkTheme = darkTheme)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RulebookScaffold(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            // Track current route to conditionally apply padding
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            // Routes that should be truly full-screen (no scaffold padding)
            val isFullScreenRoute = currentRoute == Route.Camera.route

            // Navigation host with conditional insets
            // Full-screen routes (like Camera) get zero padding for immersive experience
            RulebookNavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullScreenRoute) PaddingValues(0.dp) else innerPadding)
            )
        }
    }
}

/**
 * Configures system bar icon colors based on theme.
 *
 * Sets appropriate icon colors for status bar and navigation bar:
 * - Light theme: Dark icons for visibility on light backgrounds
 * - Dark theme: Light icons for visibility on dark backgrounds
 *
 * Note: Transparent bars are configured via enableEdgeToEdge() in MainActivity.
 *
 * @param isDarkTheme Whether the app is currently using dark theme
 */
@Composable
private fun ConfigureSystemBars(isDarkTheme: Boolean) {
    val view = LocalView.current

    // Skip in preview/edit mode
    if (view.isInEditMode) return

    SideEffect {
        val window = (view.context as Activity).window

        // Configure icon colors based on theme
        // Light theme = dark icons, Dark theme = light icons
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !isDarkTheme
            isAppearanceLightNavigationBars = !isDarkTheme
        }
    }
}
