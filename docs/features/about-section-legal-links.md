# About Section: Version Display and Legal Links

**Date:** 2026-03-19
**Story:** RULE-237: Story 9.4: About Section (Version & Legal)
**Related Files:** SettingsScreen.kt, SettingsEvent.kt, SettingsViewModel.kt, SettingsIconInfoRow.kt, SettingsIconLinkRow.kt

## Overview

This feature implements the About section in Settings, displaying the app version dynamically and providing links to Privacy Policy and Terms of Service. It establishes a pattern for safe external link navigation via Intent.ACTION_VIEW with graceful error handling.

## What Was Built

- **About Section UI**: Displays app version (X.Y.Z format with build number) and legal policy links
- **SettingsIconInfoRow Component**: Non-clickable settings row with icon, label, and secondary-colored value text
- **SettingsIconLinkRow Component**: Clickable settings row with icon, label, and chevron accessory (reused from previous design)
- **Event-Based Legal Link Navigation**: Two new events (OpenPrivacyPolicy, OpenTermsOfService) for safe intent handling
- **Safe External Link Opening**: Intent.ACTION_VIEW with ActivityNotFoundException error handling

## Technical Implementation

### Key Files

- `SettingsScreen.kt` - Main screen composable that:
  - Reads version info from PackageManager at composition time
  - Handles legal link events with try-catch for ActivityNotFoundException
  - Opens URLs with Intent.ACTION_VIEW (fallback chain for Play Store app rating)

- `SettingsIconInfoRow.kt` - New composable component that:
  - Displays icon box, label, and value text
  - Uses brutalist design system styling (3dp border, surface background)
  - Shows value in secondary color as read-only information

- `SettingsEvent.kt` - Sealed interface with:
  - `OpenPrivacyPolicy` - Event to open privacy policy URL
  - `OpenTermsOfService` - Event to open terms of service URL
  - Other existing events: ContactSupport, ReportBug, RateApp

- `SettingsViewModel.kt` - View model that:
  - Sends events through Channel when legal link callbacks are invoked
  - Updates UI state with version info (appVersion, appVersionCode)

### Key Patterns

**Event-Based Intent Navigation Pattern**
- Legal link clicks emit events instead of directly calling startActivity
- This decouples UI from Android context and makes the ViewModel testable
- Event handlers in LaunchedEffect block have access to context and can handle errors gracefully

```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            SettingsEvent.OpenPrivacyPolicy -> {
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                } catch (_: ActivityNotFoundException) {
                    // Graceful degradation - no browser available
                }
            }
            // ... other events
        }
    }
}
```

**Runtime PackageManager Version Reading**
- Version info is read from PackageManager at composition time (LaunchedEffect)
- Handles both current API (longVersionCode) and legacy API (versionCode) for compatibility
- Defaults to "1.0.0" and catches NameNotFoundException gracefully

```kotlin
val packageInfo = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
} catch (_: PackageManager.NameNotFoundException) {
    null
}
```

**Brutalist Icon Box Pattern**
- Non-clickable information row with colored icon box
- Icon box is 32dp square with 24dp icon centered inside
- Uses design system colors for semantic meaning:
  - Gray (contentSecondary) for informational rows (version)
  - Blue for action rows (Privacy Policy, Terms of Service)

### Code Examples

**Using SettingsIconInfoRow for Version Display:**
```kotlin
SettingsIconInfoRow(
    label = "Version",
    value = "2.5.1 (build 42)",
    icon = Icons.Outlined.Info,
    iconTint = RulebookTheme.colors.contentSecondary
)
```

**Using SettingsIconLinkRow for Legal Links:**
```kotlin
SettingsIconLinkRow(
    label = "Privacy Policy",
    icon = Icons.Outlined.Shield,
    iconTint = RulebookTheme.colors.contentTertiary,
    onClick = viewModel::onPrivacyPolicy
)
```

**Handling Legal Link Events:**
```kotlin
SettingsEvent.OpenPrivacyPolicy -> {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
    } catch (_: ActivityNotFoundException) {
        // User doesn't have a browser installed - gracefully degrade
    }
}
```

## How to Use

1. **Adding a New Legal Link Row:**
   - Add onClick callback to SettingsViewModel (e.g., `fun onNewPolicy()`)
   - Add new SettingsEvent in SettingsEvent.kt
   - Emit event in callback via `_events.send(SettingsEvent.NewPolicy)`
   - Add event handler in SettingsScreen's LaunchedEffect
   - Add SettingsIconLinkRow with appropriate icon and callback

2. **Extending the About Section:**
   - Use SettingsIconInfoRow for non-clickable information
   - Use SettingsIconLinkRow for clickable links
   - Both use the same brutalist styling and spacing patterns

3. **Testing Legal Links:**
   - ViewModel tests verify events are emitted via `viewModel.events.first()`
   - No direct intent testing needed (kept in Screen, not ViewModel)

## Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| PRIVACY_POLICY_URL | https://rulebook.app/privacy | Privacy policy link |
| TERMS_OF_SERVICE_URL | https://rulebook.app/terms | Terms of service link |
| Icon box size | 32dp | Touch target compliance |
| Minimum row height | 64dp | Accessibility touch target |

## Notes

- **Error Handling**: ActivityNotFoundException is silently caught - if no browser is available, the action simply doesn't execute. Consider logging in production if needed.
- **Version Format**: Displayed as "Version X.Y.Z (build N)" where X.Y.Z is versionName and N is versionCode
- **PackageManager Caching**: Version info is read once per screen composition. No caching or periodic updates.
- **Future Enhancement**: Consider moving URLs to BuildConfig or a remote config service if they change frequently

## Related Documentation

- **Theme Selection Pattern**: See `docs/features/theme-selection-light-dark-system.md` for settings UI patterns with preference sources
- **Haptic Feedback Pattern**: See `docs/features/haptic-feedback-toggle.md` for extending Settings with new toggles
