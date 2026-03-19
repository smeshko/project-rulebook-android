package com.rulebook.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.common.HapticUtils
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.settings.components.ClearDataConfirmationDialog
import com.rulebook.feature.settings.components.SettingsCreditRow
import com.rulebook.feature.settings.components.SettingsIconInfoRow
import com.rulebook.feature.settings.components.SettingsIconLinkRow
import com.rulebook.feature.settings.components.SettingsSectionHeader
import com.rulebook.feature.settings.components.SettingsThemeRow
import com.rulebook.feature.settings.components.SettingsToggleRow
import org.koin.androidx.compose.koinViewModel

private const val PRIVACY_POLICY_URL = "https://rulebook.app/privacy"
private const val TERMS_OF_SERVICE_URL = "https://rulebook.app/terms"

/**
 * Settings screen displaying app configuration options.
 *
 * @param onNavigateToPaywall Callback invoked when the user taps the credit balance row.
 * @param onNavigateToOnboarding Callback invoked after data is cleared to return to onboarding.
 * @param viewModel The ViewModel managing settings state.
 * @param modifier Modifier to be applied to the screen.
 */
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Read version info from PackageManager and populate UI state
    LaunchedEffect(Unit) {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        if (packageInfo != null) {
            val versionName = packageInfo.versionName ?: "1.0.0"
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            viewModel.updateVersionInfo(versionName, versionCode)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val state = viewModel.uiState.value
            val versionSubject = "Rulebook Android v${state.appVersion} (${state.appVersionCode})"
            when (event) {
                SettingsEvent.ContactSupport -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@rulebook.app")
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "$versionSubject - Support"
                        )
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        // No email client available — silently ignore
                    }
                }
                SettingsEvent.ReportBug -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@rulebook.app")
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "$versionSubject - Bug"
                        )
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        // No email client available — silently ignore
                    }
                }
                SettingsEvent.RateApp -> {
                    val packageName = context.packageName
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        )
                    } catch (_: ActivityNotFoundException) {
                        try {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        } catch (_: ActivityNotFoundException) {
                            // No Play Store or browser available — silently ignore
                        }
                    }
                }
                SettingsEvent.OpenPrivacyPolicy -> {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                        )
                    } catch (_: ActivityNotFoundException) {
                        // No browser available — silently ignore
                    }
                }
                SettingsEvent.OpenTermsOfService -> {
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_OF_SERVICE_URL))
                        )
                    } catch (_: ActivityNotFoundException) {
                        // No browser available — silently ignore
                    }
                }
                is SettingsEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                SettingsEvent.NavigateToOnboarding -> {
                    onNavigateToOnboarding()
                }
            }
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCreditsTap = onNavigateToPaywall,
        onThemeSelected = viewModel::onThemeSelected,
        onHapticsToggle = { enabled ->
            // Farewell vibration: fire one last haptic before disabling
            if (!enabled && uiState.isHapticsEnabled) {
                HapticUtils.performCaptureHaptic(view)
            }
            viewModel.onHapticsToggle(enabled)
        },
        onShowClearConfirmation = viewModel::onShowClearConfirmation,
        onDismissClearConfirmation = viewModel::onDismissClearConfirmation,
        onClearData = {
            HapticUtils.performCaptureHaptic(view, uiState.isHapticsEnabled)
            viewModel.onClearData()
        },
        onContactUs = viewModel::onContactUs,
        onReportBug = viewModel::onReportBug,
        onRateApp = viewModel::onRateApp,
        onPrivacyPolicy = viewModel::onPrivacyPolicy,
        onTermsOfService = viewModel::onTermsOfService,
        modifier = modifier
    )
}

@Composable
internal fun SettingsScreenContent(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onCreditsTap: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onShowClearConfirmation: () -> Unit,
    onDismissClearConfirmation: () -> Unit,
    onClearData: () -> Unit,
    onContactUs: () -> Unit,
    onReportBug: () -> Unit,
    onRateApp: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            RulebookHeaderBar(title = "Settings")

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credits Section
                item {
                    SettingsSectionHeader(title = "Credits")
                }
                item {
                    SettingsCreditRow(
                        creditCount = uiState.creditBalance,
                        onClick = onCreditsTap
                    )
                }

                // Appearance Section
                item {
                    SettingsSectionHeader(
                        title = "Appearance",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    Column(
                        modifier = Modifier.selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsThemeRow(
                            label = "Light",
                            icon = Icons.Outlined.LightMode,
                            isSelected = uiState.themeMode == ThemeMode.LIGHT,
                            onClick = { onThemeSelected(ThemeMode.LIGHT) }
                        )
                        SettingsThemeRow(
                            label = "Dark",
                            icon = Icons.Outlined.DarkMode,
                            isSelected = uiState.themeMode == ThemeMode.DARK,
                            onClick = { onThemeSelected(ThemeMode.DARK) }
                        )
                        SettingsThemeRow(
                            label = "System",
                            icon = Icons.Outlined.BrightnessAuto,
                            isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                            onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                        )
                    }
                }

                // Feedback Section
                item {
                    SettingsSectionHeader(
                        title = "Feedback",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    SettingsToggleRow(
                        label = "Haptic Feedback",
                        checked = uiState.isHapticsEnabled,
                        onCheckedChange = onHapticsToggle
                    )
                }

                // Support Section
                item {
                    SettingsSectionHeader(
                        title = "Support",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    SettingsIconLinkRow(
                        label = "Contact Us",
                        icon = Icons.Outlined.Email,
                        iconTint = RulebookTheme.colors.blue,
                        onClick = onContactUs
                    )
                }
                item {
                    SettingsIconLinkRow(
                        label = "Report a Bug",
                        icon = Icons.Outlined.BugReport,
                        iconTint = RulebookTheme.colors.orange,
                        onClick = onReportBug
                    )
                }
                item {
                    SettingsIconLinkRow(
                        label = "Rate the App",
                        icon = Icons.Outlined.Star,
                        iconTint = RulebookTheme.colors.yellow,
                        onClick = onRateApp
                    )
                }

                // About Section
                item {
                    SettingsSectionHeader(
                        title = "About",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    SettingsIconInfoRow(
                        label = "Version",
                        value = "${uiState.appVersion} (build ${uiState.appVersionCode})",
                        icon = Icons.Outlined.Info,
                        iconTint = RulebookTheme.colors.contentSecondary
                    )
                }
                item {
                    SettingsIconLinkRow(
                        label = "Privacy Policy",
                        icon = Icons.Outlined.Shield,
                        iconTint = RulebookTheme.colors.blue,
                        onClick = onPrivacyPolicy
                    )
                }
                item {
                    SettingsIconLinkRow(
                        label = "Terms of Service",
                        icon = Icons.Outlined.Description,
                        iconTint = RulebookTheme.colors.blue,
                        onClick = onTermsOfService
                    )
                }

                // Data Section
                item {
                    SettingsSectionHeader(
                        title = "Data",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                item {
                    RulebookButton(
                        text = "Clear All Data",
                        onClick = onShowClearConfirmation,
                        variant = ButtonVariant.Destructive,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.showClearConfirmation) {
            ClearDataConfirmationDialog(
                onConfirm = onClearData,
                onDismiss = onDismissClearConfirmation
            )
        }
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Settings Screen - Light")
@Composable
private fun SettingsScreenLightPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsScreenContent(
            uiState = SettingsUiState(creditBalance = 5, themeMode = ThemeMode.SYSTEM),
            onCreditsTap = {},
            onThemeSelected = {},
            onHapticsToggle = {},
            onShowClearConfirmation = {},
            onDismissClearConfirmation = {},
            onClearData = {},
            onContactUs = {},
            onReportBug = {},
            onRateApp = {},
            onPrivacyPolicy = {},
            onTermsOfService = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen - Dark")
@Composable
private fun SettingsScreenDarkPreview() {
    RulebookTheme(darkTheme = true) {
        SettingsScreenContent(
            uiState = SettingsUiState(creditBalance = 5, themeMode = ThemeMode.DARK),
            onCreditsTap = {},
            onThemeSelected = {},
            onHapticsToggle = {},
            onShowClearConfirmation = {},
            onDismissClearConfirmation = {},
            onClearData = {},
            onContactUs = {},
            onReportBug = {},
            onRateApp = {},
            onPrivacyPolicy = {},
            onTermsOfService = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen - Clear Confirmation Dialog")
@Composable
private fun SettingsScreenClearConfirmationPreview() {
    RulebookTheme(darkTheme = false) {
        SettingsScreenContent(
            uiState = SettingsUiState(creditBalance = 5, showClearConfirmation = true),
            onCreditsTap = {},
            onThemeSelected = {},
            onHapticsToggle = {},
            onShowClearConfirmation = {},
            onDismissClearConfirmation = {},
            onClearData = {},
            onContactUs = {},
            onReportBug = {},
            onRateApp = {},
            onPrivacyPolicy = {},
            onTermsOfService = {}
        )
    }
}
