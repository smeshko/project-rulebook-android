# Haptic Feedback Toggle

**Date:** 2026-03-19
**Related Files:**
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/HapticsPreferencesSource.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsScreen.kt`
- `core/common/src/main/kotlin/com/rulebook/core/common/HapticUtils.kt`

## Overview

Added a user-configurable haptic feedback toggle to the Settings screen that controls vibration behavior throughout the app. When disabled, no haptic feedback is triggered from interactions like photo capture or long-press actions. The implementation demonstrates a reusable preference pattern (`HapticsPreferencesSource`) that integrates DataStore with UI state management and module-wide feature gating.

## What Was Built

- **HapticsPreferencesSource interface** - Centralized preference source following the `ThemePreferencesSource` pattern
- **Settings toggle UI** - New toggle control in the Feedback section of Settings
- **Preference persistence** - DataStore-backed persistence via `RulebookPreferences`
- **App-wide integration** - Haptics-aware utility functions and camera module integration
- **Farewell vibration** - Special UX pattern: one final haptic vibration fires when disabling

## Technical Implementation

### Key Files

- `HapticsPreferencesSource.kt` - Interface defining the preference contract (`hapticsEnabled: Flow<Boolean>`, `setHapticsEnabled()`)
- `RulebookPreferences.kt` - Implementation of HapticsPreferencesSource using DataStore `booleanPreferencesKey("haptics_enabled")`
- `DataModule.kt` - DI registration: `single<HapticsPreferencesSource> { get<RulebookPreferences>() }`
- `SettingsViewModel.kt` - Collects preference flow, exposes as UI state, persists user changes
- `HapticUtils.kt` - New overloads accepting `hapticsEnabled: Boolean` parameter
- `CameraScreen.kt` - Injects `HapticsPreferencesSource` via `koinInject()`, passes enabled state to haptic callbacks

### Key Patterns

- **Preference Source Pattern**: Abstract preference sources (like `ThemePreferencesSource`) using Flow for reactive updates and suspend functions for persistence. Allows decoupling UI from storage layer.

- **Feature Gating Pattern**: Pass `hapticsEnabled: Boolean` parameter through function callsites. When `false`, haptic methods become no-ops. Preserves backward compatibility via default parameter values.

- **Farewell Vibration UX**: When toggling FROM enabled→disabled, fire one haptic vibration BEFORE persisting the disabled state. This gives users tactile feedback that they've turned off haptics. Use `LocalView.current` to bypass the preference check for this special case.

- **Flow-based State Synchronization**: SettingsViewModel collects `hapticsEnabled` flow in `init` block with `.catch { emit(true) }` fallback, ensuring UI always has valid state even if preference source fails.

### Code Examples

**Using the preference source in a feature module:**
```kotlin
// Inject in ViewModel/Composable
private val hapticsPreferencesSource: HapticsPreferencesSource = koinInject()

// Collect preference state
LaunchedEffect(Unit) {
    hapticsPreferencesSource.hapticsEnabled.collect { enabled ->
        _uiState.update { it.copy(isHapticsEnabled = enabled) }
    }
}

// Respond to user toggle
fun onHapticsToggle(enabled: Boolean) {
    viewModelScope.launch {
        hapticsPreferencesSource.setHapticsEnabled(enabled)
    }
}
```

**Using haptics with preference awareness:**
```kotlin
// New overload respects the preference
HapticUtils.performCaptureHaptic(view, hapticsEnabled = isHapticsEnabled)

// Farewell haptic bypasses preference for special UX
if (currentlyEnabled && !newValue) {
    HapticUtils.performCaptureHaptic(view) // No hapticsEnabled param = ignores preference
}
```

## How to Use

1. **Add haptics to a new action:**
   - Inject `HapticsPreferencesSource` in your ViewModel/screen
   - Collect `hapticsEnabled` flow to get current state
   - Call `HapticUtils.performXxxHaptic(view, hapticsEnabled = state.isHapticsEnabled)`

2. **Add preference to Settings:**
   - Create preference source following `HapticsPreferencesSource` pattern
   - Register in `DataModule` as `single<YourPreferenceSource> { get<RulebookPreferences>() }`
   - Add to `SettingsViewModel` constructor and init block
   - Update `SettingsScreen` to render toggle control

3. **Implement farewell feedback for toggle-off:**
   - Wrap callback in `if (currentlyEnabled && !newValue) { fireHaptic() }`
   - Use original `HapticUtils` method without `hapticsEnabled` parameter

## Configuration

| Option | Storage | Default | Description |
|--------|---------|---------|-------------|
| `haptics_enabled` | DataStore (BooleanPreferencesKey) | `true` | Whether haptic feedback is enabled |

## Notes

- The `hapticsEnabled` parameter in HapticUtils overloads defaults to `true`, preserving backward compatibility for legacy callsites
- Preference propagation uses Flow and `.catch { emit(true) }` fallback, so UI never sees null/missing state
- The farewell vibration is a special case that bypasses the preference check—intentional UX affordance
- All haptic callsites should eventually migrate to pass the `hapticsEnabled` parameter for complete preference respect
- Future preferences (brightness, sound, animations) should follow this same `PreferencesSource` interface pattern
