# Theme Selection (Light/Dark/System Modes)

**Date:** 2026-03-19
**Story:** RULE-234 (Story 9.1: Theme Selection)
**Related Files:**
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/ThemePreferencesSource.kt`
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/RulebookPreferences.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsThemeRow.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt`
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsScreen.kt`
- `app/src/main/kotlin/com/rulebook/MainActivity.kt`
- `app/src/main/kotlin/com/rulebook/RulebookApp.kt`

## Overview

Story 9.1 implements a theme selection system that allows users to choose between Light, Dark, and System (default) themes. The implementation introduces a reusable `ThemePreferencesSource` interface pattern for preference management, persists theme choices to DataStore, and applies theme changes instantly across the entire app without requiring a restart. System mode automatically follows the device's dark mode setting.

## What Was Built

### 1. **Theme Preference Source Interface**
A new abstraction layer (`ThemePreferencesSource`) for managing theme preferences:
- Follows the same pattern as `SortPreferencesSource` - a reusable preference source pattern
- Exposes `themeMode: Flow<ThemeMode>` for reactive theme observation
- Provides `setThemeMode(mode: ThemeMode)` for persistence
- Implemented by `RulebookPreferences` in DataStore layer

### 2. **Theme Mode Enum**
Three distinct theme modes in `ThemeMode` enum:
- `LIGHT`: Force light theme
- `DARK`: Force dark theme
- `SYSTEM`: Default - follows device dark mode setting

### 3. **Theme Selection UI in Settings**
The Appearance section in SettingsScreen displays three radio-button rows:
- Light mode row with sun icon
- Dark mode row with moon icon
- System mode row with auto/settings icon
- Selected mode shown with filled pink radio circle
- Instant visual feedback with animated color transitions

### 4. **SettingsThemeRow Component**
A reusable row composable for theme mode selection:
- Icon box with brutalist orange background
- Radio button indicator (filled when selected)
- Follows the same pattern as other settings rows
- Supports accessibility with semantic labels

### 5. **Root App Theme Application**
Theme resolution at application root:
- `MainActivity` collects `themeMode` Flow from DataStore
- Resolves to boolean `darkTheme` parameter:
  - `LIGHT` → `false`
  - `DARK` → `true`
  - `SYSTEM` → `isSystemInDarkTheme()`
- Passes `darkTheme` to `RulebookApp` which applies `RulebookTheme`
- Changes apply instantly without app restart

### 6. **DataStore Persistence**
Theme preference stored in DataStore with:
- Key: `stringPreferencesKey("theme_mode")`
- Default: `ThemeMode.SYSTEM`
- Survives app restarts
- Uses atomic edit blocks for safety

## Technical Implementation

### Key Files

**Preference Source Abstraction:**
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/ThemePreferencesSource.kt` - Interface defining theme preference contract
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/RulebookPreferences.kt` - DataStore implementation with `themeMode` Flow and `setThemeMode()` method

**Settings UI:**
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/components/SettingsThemeRow.kt` - Reusable row composable for individual theme option
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsScreen.kt` - Appearance section with three theme rows
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt` - Collects theme mode from preferences and handles `onThemeSelected()` updates

**Application Root:**
- `app/src/main/kotlin/com/rulebook/MainActivity.kt` - Collects `themeMode` Flow and resolves to `darkTheme: Boolean`
- `app/src/main/kotlin/com/rulebook/RulebookApp.kt` - Accepts `darkTheme` parameter and applies via `RulebookTheme()`

**Dependency Injection:**
- `core/data/src/main/kotlin/com/rulebook/core/data/di/DataModule.kt` - Provides `ThemePreferencesSource` (singleton `RulebookPreferences`)
- `feature/settings/src/main/kotlin/com/rulebook/feature/settings/di/SettingsModule.kt` - Injects `ThemePreferencesSource` into `SettingsViewModel`

### Key Patterns

**Preference Source Pattern:**
A reusable abstraction for preferences that can be extended for other settings:

```kotlin
// Interface contract
interface ThemePreferencesSource {
    val themeMode: Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}

// DataStore implementation
class RulebookPreferences : ThemePreferencesSource {
    override val themeMode: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            ThemeMode.valueOf(prefs[THEME_MODE_KEY] ?: "SYSTEM")
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }
}
```

This pattern allows:
- Easy testing with fake implementations
- Future swapping of preference storage (DataStore → encrypted preferences, etc.)
- Clear separation of concerns between UI and data layers
- Extension for other preferences (sort mode, display preferences, etc.)

**Root App Theme Resolution:**
Theme is resolved at the highest level where it can be applied to all content:

```kotlin
// In MainActivity - collect and resolve theme
val darkTheme by remember {
    themePreferencesSource.themeMode.map { mode ->
        when (mode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}.collectAsState(initial = true)

// Pass to composable
RulebookApp(darkTheme = darkTheme)
```

This approach:
- Centralizes theme logic in one place
- Allows instant updates when user changes theme
- Properly handles System mode detection
- Avoids theme flickers by controlling at app root

**Settings Theme Row Component:**
```kotlin
@Composable
fun SettingsThemeRow(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RulebookListRow(
        onClick = onClick,
        modifier = modifier,
        accessoryView = {
            RadioButton(selected = isSelected, onClick = null)
        }
    ) {
        // Theme icon (sun/moon/auto)
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(brutalist.orange)
        ) {
            Icon(imageVector = getThemeIcon(mode), contentDescription = null)
        }
        // Theme name
        Text(getThemeName(mode))
    }
}
```

Reusable for any preference that requires radio-button selection.

## How to Use

### For Users

1. Open Settings from the app menu
2. Navigate to the Appearance section
3. Tap one of three theme options:
   - **Light**: Always shows light theme
   - **Dark**: Always shows dark theme
   - **System**: Automatically follows device dark mode setting
4. Theme changes instantly - no restart needed
5. Selection persists across app launches

### For Developers

**Accessing the Current Theme:**
```kotlin
// In ViewModel
val themeMode: StateFlow<ThemeMode> = themePreferencesSource.themeMode
    .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.SYSTEM)

// In Composable
val themeMode by themePreferencesSource.themeMode
    .collectAsState(initial = ThemeMode.SYSTEM)
```

**Changing Theme Programmatically:**
```kotlin
// In ViewModel
suspend fun onThemeSelected(mode: ThemeMode) {
    themePreferencesSource.setThemeMode(mode)
}
```

**Adding a New Preference Using This Pattern:**
The `ThemePreferencesSource` interface pattern can be extended for other preference types:

1. Create interface: `MyFeaturePreferencesSource`
2. Implement in `RulebookPreferences`
3. Inject into ViewModels
4. Add UI for selection

Example: Sort order preferences already use this pattern via `SortPreferencesSource`.

## Configuration

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `theme_mode` | String (DataStore) | `"SYSTEM"` | Persisted theme mode preference |
| `isSystemInDarkTheme()` | Boolean | Device setting | System mode detection (API 28+) |

## Tests

The implementation includes comprehensive unit tests in `SettingsViewModelTest`:
- `initial state has expected default values` - Verifies SYSTEM is default
- `uiState exposes immutable state flow` - State immutability
- `credit balance updates when preference changes` - ViewModel update flow
- `onThemeSelected updates themeMode state for all three modes` - All theme modes work
- `theme mode updates when preference changes externally` - External change detection
- `onThemeSelected persists to preferences source` - DataStore persistence
- `onHapticsToggle updates isHapticsEnabled state` - Unrelated setting unchanged

All tests pass with `FakeThemePreferencesSource` for isolation.

## Notes

### System Mode Detection
- Uses `isSystemInDarkTheme()` from `androidx.compose.foundation.isSystemInDarkTheme()`
- Works on API 28+ (respects device dark mode setting)
- On older APIs, defaults to `false` (light theme)

### Instant Updates Without Restart
- Theme applied at app root in `MainActivity`
- `MainActivity` collects the Flow and triggers recomposition on changes
- No Activity restart needed - the composable tree recomposes with new `darkTheme` parameter
- Users see instant visual feedback

### Future Extensions
This pattern can be extended for:
- Display density preferences
- Text size preferences
- Color theme customization (beyond light/dark)
- Animation/motion preferences for accessibility

### Known Limitations
- System mode detection respects device dark mode, not time-based switching (future enhancement)
- No per-screen theme overrides (all-or-nothing at app level)
