package com.rulebook

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.rulebook.navigation.Route
import com.rulebook.navigation.RulebookNavHost
import com.rulebook.navigation.RulebookScaffold
import com.rulebook.startup.StartupDestination

/**
 * Root composable for the Rulebook app.
 *
 * This composable:
 * - Configures theme-aware status bar icons (light/dark)
 * - Applies WindowInsets for proper edge-to-edge content layout
 * - Provides the RulebookScaffold structure with bottom bar, FAB, and navigation
 *
 * @param startDestination The initial navigation destination based on onboarding status.
 * @param modifier Optional modifier for the root composable
 */
@Composable
fun RulebookApp(
    startDestination: StartupDestination,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val navController = rememberNavController()

    // Map StartupDestination to navigation route
    val startRoute = when (startDestination) {
        StartupDestination.Onboarding -> Route.Onboarding.route
        StartupDestination.Library -> Route.Library.route
    }

    // Configure status bar and navigation bar icon colors based on theme
    // Note: enableEdgeToEdge() in MainActivity handles transparent bars
    ConfigureSystemBars(isDarkTheme = isDarkTheme)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        RulebookScaffold(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            // Navigation host with proper insets applied
            RulebookNavHost(
                navController = navController,
                startDestination = startRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
