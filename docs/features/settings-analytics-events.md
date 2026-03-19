# Settings Analytics Events Integration

**Date:** 2026-03-19
**Related Files:** `feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt`, `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt`

## Overview

This feature documents how to integrate analytics tracking for user settings changes. It establishes a resilient, non-blocking pattern for analytics that ensures failures in analytics don't impact user experience.

## What Was Built

- **Analytics tracking for theme selection** - Tracks when users change theme preference (light/dark/system)
- **Analytics tracking for haptics toggle** - Tracks when users enable/disable haptic feedback
- **Analytics tracking for data clearing** - Tracks when users clear app data (with game count)
- **Analytics tracking for support links** - Tracks which support/legal links users tap (contact, bug report, rate, privacy, terms)
- **Resilient analytics pattern** - All analytics calls wrapped in try-catch to prevent blocking user actions
- **Test fixtures for analytics** - FakeAnalyticsManager for unit testing analytics integration

## Technical Implementation

### Key Files

- `SettingsViewModel.kt`: Implements analytics calls for all settings actions
- `AnalyticsManager.kt`: Interface with convenience methods for settings analytics events
- `SettingsViewModelTest.kt`: Unit tests with FakeAnalyticsManager test fixture

### Key Patterns

#### 1. **Synchronous Analytics Calls with Try-Catch**

Analytics calls are made synchronously (not in viewModelScope.launch) and wrapped in try-catch to ensure they never block user actions:

```kotlin
try {
    analyticsManager.trackSettingsThemeChanged(theme.name.lowercase(), previousTheme.name.lowercase())
} catch (_: Exception) {
    // Analytics must never block user actions
}
```

**Why:** If AnalyticsManager throws (network error, initialization failure), the user's theme change completes normally. Analytics failures are silently absorbed.

#### 2. **State Capture Before Update**

For tracking state transitions, capture the previous value before updating:

```kotlin
fun onThemeSelected(mode: ThemeMode) {
    val previousTheme = _uiState.value.themeMode  // Capture BEFORE update
    _uiState.update { it.copy(themeMode = mode) }
    try {
        analyticsManager.trackSettingsThemeChanged(mode, previousTheme)
    } catch (_: Exception) {
        // Analytics must never block user actions
    }
    // Then persist the change asynchronously
    viewModelScope.launch {
        themePreferencesSource.setThemeMode(mode)
    }
}
```

**Why:** You need the previous state for tracking "changed from X to Y" events. Capture it immediately before the update.

#### 3. **Analytics After Successful Data Operations**

When analytics depend on app state (like game count for clear-data event), fetch the data first, then only track on success:

```kotlin
fun onClearData() {
    _uiState.update { it.copy(showClearConfirmation = false) }
    viewModelScope.launch {
        // Get game count for analytics context
        val gamesCount = when (val result = gameRepository.getGames()) {
            is Result.Success -> result.data.size
            is Result.Error -> 0  // Default to 0 if fetch fails
        }

        // Attempt to clear database (critical operation)
        try {
            clearDatabase()
        } catch (_: Exception) {
            _events.send(SettingsEvent.ShowSnackbar("Failed to clear data"))
            return@launch  // Stop here on failure - don't track analytics
        }

        // ... reset preferences ...

        // Now that critical operation succeeded, track analytics
        try {
            analyticsManager.trackSettingsDataCleared(gamesCount)
        } catch (_: Exception) {
            // Analytics must never block user actions
        }
    }
}
```

**Why:** Only track destructive operations in analytics after they succeed. If clearDatabase fails, we don't track the event. This ensures analytics faithfully represents actual user actions.

#### 4. **Convenience Methods on AnalyticsManager**

Add domain-specific convenience methods to AnalyticsManager as default methods on the interface:

```kotlin
interface AnalyticsManager {
    fun trackEvent(name: String, properties: Map<String, String>)

    // Settings analytics convenience methods
    fun trackSettingsThemeChanged(theme: String, previousTheme: String) =
        trackEvent("settings_theme_changed", mapOf(
            "theme" to theme,
            "previous_theme" to previousTheme
        ))

    fun trackSettingsHapticsChanged(enabled: Boolean) =
        trackEvent("settings_haptics_changed", mapOf("enabled" to enabled.toString()))
}
```

**Why:** Reduces boilerplate in ViewModels, centralizes event structure, makes it easier to add properties later.

### Code Examples

#### Example 1: Simple Boolean Toggle with Analytics

```kotlin
fun onHapticsToggle(enabled: Boolean) {
    _uiState.update { it.copy(isHapticsEnabled = enabled) }
    try {
        analyticsManager.trackSettingsHapticsChanged(enabled)
    } catch (_: Exception) {
        // Analytics must never block user actions
    }
    viewModelScope.launch {
        hapticsPreferencesSource.setHapticsEnabled(enabled)
    }
}
```

#### Example 2: State Transition Analytics

```kotlin
fun onThemeSelected(mode: ThemeMode) {
    val previousTheme = _uiState.value.themeMode
    _uiState.update { it.copy(themeMode = mode) }
    try {
        analyticsManager.trackSettingsThemeChanged(
            mode.name.lowercase(),
            previousTheme.name.lowercase()
        )
    } catch (_: Exception) {
        // Analytics must never block user actions
    }
    viewModelScope.launch {
        themePreferencesSource.setThemeMode(mode)
    }
}
```

#### Example 3: Testing with FakeAnalyticsManager

```kotlin
class FakeAnalyticsManager : AnalyticsManager {
    data class TrackedEvent(val name: String, val properties: Map<String, String>)

    private val _trackedEvents = mutableListOf<TrackedEvent>()
    val trackedEvents: List<TrackedEvent> get() = _trackedEvents.toList()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        _trackedEvents.add(TrackedEvent(name, properties))
    }
}

// In test
@Test
fun `onThemeSelected tracks event with correct properties`() = runTest {
    val fakeAnalytics = FakeAnalyticsManager()
    val viewModel = SettingsViewModel(
        ...,
        analyticsManager = fakeAnalytics
    )

    viewModel.onThemeSelected(ThemeMode.DARK)
    advanceUntilIdle()

    val event = fakeAnalytics.trackedEvents.first { it.name == "settings_theme_changed" }
    assertEquals("dark", event.properties["theme"])
    assertEquals("system", event.properties["previous_theme"])
}
```

## How to Use

### When Adding Analytics to Another Feature

1. **Add convenience methods to AnalyticsManager** interface for your feature events:
   ```kotlin
   interface AnalyticsManager {
       // Existing methods...

       // Your feature convenience methods
       fun trackYourFeatureAction(param1: String, param2: String) =
           trackEvent("your_feature_action", mapOf(
               "param1" to param1,
               "param2" to param2
           ))
   }
   ```

2. **Inject AnalyticsManager into your ViewModel**:
   ```kotlin
   class YourViewModel(
       // ... other dependencies ...
       private val analyticsManager: AnalyticsManager
   ) : ViewModel()
   ```

3. **Wrap analytics calls in try-catch**:
   ```kotlin
   fun onYourAction(value: String) {
       _uiState.update { it.copy(someField = value) }
       try {
           analyticsManager.trackYourFeatureAction(value)
       } catch (_: Exception) {
           // Analytics must never block user actions
       }
   }
   ```

4. **Write tests using FakeAnalyticsManager**:
   ```kotlin
   @Test
   fun `onYourAction tracks correct event`() = runTest {
       val fakeAnalytics = FakeAnalyticsManager()
       val viewModel = YourViewModel(..., analyticsManager = fakeAnalytics)

       viewModel.onYourAction("test")

       val event = fakeAnalytics.trackedEvents.first()
       assertEquals("your_feature_action", event.name)
   }
   ```

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `trackSettingsThemeChanged` | convenience method | enabled | Tracks theme mode selection with previous/new values |
| `trackSettingsHapticsChanged` | convenience method | enabled | Tracks haptics toggle with enabled flag |
| `trackSettingsDataCleared` | convenience method | enabled | Tracks data clear with game count before clearing |
| `trackSettingsSupportTapped` | convenience method | enabled | Tracks support link taps with link identifier |

## Notes

- **Events must not block user actions** - All AnalyticsManager calls are wrapped in try-catch
- **Event names match iOS** - iOS event names are identical for cross-platform consistency (e.g., `settings_theme_changed`)
- **No PII in events** - Properties contain only non-identifying data (theme names, enabled flags, link names, counts)
- **Property values are strings** - All properties in the `Map<String, String>` are converted to strings (e.g., `Boolean` becomes `"true"` or `"false"`)
- **State capture before update** - For transition events, always capture previous state before updating UI state
- **Test fixture pattern** - Use FakeAnalyticsManager in tests to verify events without network calls or actual analytics backend
- **Future expansion** - When AnalyticsManager implementation changes (e.g., adding user context), all analytics calls automatically benefit from the update

