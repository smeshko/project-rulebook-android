package com.rulebook.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.datastore.ThemeMode
import com.rulebook.core.designsystem.component.ButtonVariant
import com.rulebook.core.designsystem.component.RulebookButton
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import com.rulebook.feature.settings.components.SettingsCreditRow
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

    SettingsScreenContent(
        uiState = uiState,
        onCreditsTap = onNavigateToPaywall,
        onThemeSelected = viewModel::onThemeSelected,
        onHapticsToggle = viewModel::onHapticsToggle,
        onClearData = viewModel::onClearData,
        onContactUs = viewModel::onContactUs,
        onRateApp = viewModel::onRateApp,
        onShareApp = viewModel::onShareApp,
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
    onRateApp: () -> Unit,
    onShareApp: () -> Unit,
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
                SettingsThemeRow(
                    label = "Light",
                    icon = Icons.Outlined.LightMode,
                    isSelected = uiState.themeMode == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )
            }
            item {
                SettingsThemeRow(
                    label = "Dark",
                    icon = Icons.Outlined.DarkMode,
                    isSelected = uiState.themeMode == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )
            }
            item {
                SettingsThemeRow(
                    label = "System",
                    icon = Icons.Outlined.BrightnessAuto,
                    isSelected = uiState.themeMode == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )
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
                SettingsLinkRow(
                    label = "Contact Us",
                    onClick = onContactUs
                )
            }
            item {
                SettingsLinkRow(
                    label = "Rate the App",
                    onClick = onRateApp
                )
            }
            item {
                SettingsLinkRow(
                    label = "Share",
                    onClick = onShareApp
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
            onRateApp = {},
            onShareApp = {},
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
            onRateApp = {},
            onShareApp = {},
            onPrivacyPolicy = {},
            onTermsOfService = {}
        )
    }
}
