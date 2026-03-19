package com.rulebook.feature.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.rulebook.feature.settings.components.SettingsCreditRow
import com.rulebook.feature.settings.components.SettingsIconLinkRow
import com.rulebook.feature.settings.components.SettingsInfoRow
import com.rulebook.feature.settings.components.SettingsLinkRow
import com.rulebook.feature.settings.components.SettingsSectionHeader
import com.rulebook.feature.settings.components.SettingsThemeRow
import com.rulebook.feature.settings.components.SettingsToggleRow
import org.koin.androidx.compose.koinViewModel

/**
 * Settings screen displaying app configuration options.
 *
 * @param onNavigateToPaywall Callback invoked when the user taps the credit balance row.
 * @param viewModel The ViewModel managing settings state.
 * @param modifier Modifier to be applied to the screen.
 */
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val appVersion = viewModel.uiState.value.appVersion
            when (event) {
                SettingsEvent.ContactSupport -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@rulebook.app")
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Rulebook Android v$appVersion - Support"
                        )
                    }
                    context.startActivity(intent)
                }
                SettingsEvent.ReportBug -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@rulebook.app")
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Rulebook Android v$appVersion - Bug"
                        )
                    }
                    context.startActivity(intent)
                }
                SettingsEvent.RateApp -> {
                    val packageName = context.packageName
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        )
                    } catch (e: ActivityNotFoundException) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                            )
                        )
                    }
                }
            }
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        onCreditsTap = onNavigateToPaywall,
        onThemeSelected = viewModel::onThemeSelected,
        onHapticsToggle = { enabled ->
            // Farewell vibration: fire one last haptic before disabling
            if (!enabled && uiState.isHapticsEnabled) {
                HapticUtils.performCaptureHaptic(view)
            }
            viewModel.onHapticsToggle(enabled)
        },
        onClearData = viewModel::onClearData,
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
    onCreditsTap: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onClearData: () -> Unit,
    onContactUs: () -> Unit,
    onReportBug: () -> Unit,
    onRateApp: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onTermsOfService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
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
                SettingsInfoRow(
                    label = "Version",
                    value = uiState.appVersion
                )
            }
            item {
                SettingsLinkRow(
                    label = "Privacy Policy",
                    onClick = onPrivacyPolicy
                )
            }
            item {
                SettingsLinkRow(
                    label = "Terms of Service",
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
                    onClick = onClearData,
                    variant = ButtonVariant.Destructive,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
            onClearData = {},
            onContactUs = {},
            onReportBug = {},
            onRateApp = {},
            onPrivacyPolicy = {},
            onTermsOfService = {}
        )
    }
}
