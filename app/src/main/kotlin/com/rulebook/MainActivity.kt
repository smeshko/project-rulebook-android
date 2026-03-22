package com.rulebook

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.rulebook.core.billing.PendingCheckResult
import com.rulebook.core.billing.PendingPurchaseChecker
import com.rulebook.core.datastore.RulebookPreferences
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.startup.RecoveryEvent
import com.rulebook.startup.RefundEvent
import com.rulebook.startup.StartupViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
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
    private val pendingPurchaseChecker: PendingPurchaseChecker by inject()
    private val preferences: RulebookPreferences by inject()
    private val _newIntentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val newIntentFlow = _newIntentFlow.asSharedFlow()

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

        // Collect recovery events and show toast when credits are recovered from a previous purchase
        lifecycleScope.launch {
            startupViewModel.recoveryEvents.collect { event ->
                if (event is RecoveryEvent.CreditsRecovered) {
                    Toast.makeText(
                        this@MainActivity,
                        "${event.credits} credits from a previous purchase have been added!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Collect refund events and show toast when credits are revoked due to a refund
        lifecycleScope.launch {
            startupViewModel.refundEvents.collect { event ->
                if (event is RefundEvent.CreditsRevoked) {
                    Toast.makeText(
                        this@MainActivity,
                        "A previous purchase was refunded. ${event.credits} credits removed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        setContent {
            val startupDestination by startupViewModel.startupDestination.collectAsStateWithLifecycle()
            val themeMode by preferences.themeMode
                .catch { emit(ThemeMode.SYSTEM) }
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            RulebookTheme(darkTheme = darkTheme) {
                // Only render app once startup destination is determined
                // Splash screen is held until this point, so no flash occurs
                startupDestination?.let { destination ->
                    RulebookApp(
                        startDestination = destination,
                        darkTheme = darkTheme,
                        newIntentFlow = newIntentFlow
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Emit the new intent so RulebookApp can call navController.handleDeepLink
        // for warm-start deep link navigation (singleTop from app shortcut).
        _newIntentFlow.tryEmit(intent)
    }

    override fun onResume() {
        super.onResume()
        // Check if a pending purchase was resolved while app was in background.
        // This is a best-effort check — failures are logged and retried on next resume.
        lifecycleScope.launch {
            val result = pendingPurchaseChecker.checkAndResolve()
            if (result is PendingCheckResult.Resolved) {
                Toast.makeText(
                    this@MainActivity,
                    "Purchase approved! ${result.creditsAdded} credits added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
