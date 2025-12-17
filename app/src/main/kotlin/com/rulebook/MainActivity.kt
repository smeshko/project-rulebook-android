package com.rulebook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.startup.StartupViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Main entry point for the Rulebook app.
 *
 * Uses the AndroidX Splash Screen API to show a splash screen during app startup.
 * The splash screen is held until [StartupViewModel] determines the initial
 * navigation destination based on onboarding completion status.
 *
 * This prevents any flash of the wrong screen during cold start.
 */
class MainActivity : ComponentActivity() {

    private val startupViewModel: StartupViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE super.onCreate()
        val splashScreen = installSplashScreen()

        // Keep splash screen visible until startup destination is determined
        // This prevents flash of wrong screen during navigation decision
        splashScreen.setKeepOnScreenCondition {
            startupViewModel.isLoading.value
        }

        // Enable edge-to-edge after splash screen installation
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val startupDestination by startupViewModel.startupDestination.collectAsStateWithLifecycle()

            RulebookTheme {
                // Only render app once startup destination is determined
                // Splash screen is held until this point, so no flash occurs
                startupDestination?.let { destination ->
                    RulebookApp(startDestination = destination)
                }
            }
        }
    }
}
