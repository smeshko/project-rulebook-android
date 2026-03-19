# App Shortcuts: Deep Link Navigation with MVI Preference Pattern

**Date:** 2026-03-19
**Related Files:**
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt`

## Overview

Implemented app shortcuts (RULE-239) that enable users to long-press the launcher icon and quickly access the Camera screen ("Scan Game" shortcut). This required ensuring the camera module correctly handles warm-start deep link navigation and user preference state.

The key implementation pattern: **Move user preference observation from Composable level to ViewModel level** to enable proper state management across warm/cold start scenarios, particularly for deep-link navigation.

## What Was Built

- **App Shortcuts Infrastructure**: Static shortcut with adaptive icon + deep link routing
- **Deep Link Handling**: Intent filters and NavHost integration to route `rulebook://camera` to Camera screen
- **Warm-Start Navigation**: `MainActivity.onNewIntent()` with `singleTop` launch mode to handle shortcut re-taps
- **Camera State Refactoring**: Preference observation moved to ViewModel for MVI compliance

## Technical Implementation

### Key Files

- **`CameraViewModel.kt`**:
  - Accepts `HapticsPreferencesSource` dependency
  - Observes haptics preference in `init` block with flow operations
  - Emits preference state to `CameraUiState` for UI consumption

- **`CameraScreen.kt`**:
  - Retrieves `uiState.hapticsEnabled` from ViewModel instead of direct koinInject
  - Passes to `rememberCaptureHapticFeedback()` for haptic feedback configuration

- **`CameraUiState.kt`**:
  - Added `hapticsEnabled: Boolean = true` property to centralize preference state

### Key Patterns

**Pattern 1: ViewModel Preference Observation (MVI)**

Instead of:
```kotlin
// ❌ ANTI-PATTERN: Direct dependency injection in Composable
@Composable
fun CameraScreen() {
    val hapticsPreferencesSource = koinInject<HapticsPreferencesSource>()
    val hapticsEnabled by hapticsPreferencesSource.hapticsEnabled
        .collectAsStateWithLifecycle(initialValue = true)
    // ...
}
```

Do this:
```kotlin
// ✅ CORRECT: Observe in ViewModel, expose via State
class CameraViewModel(
    private val hapticsPreferencesSource: HapticsPreferencesSource
) : ViewModel() {
    init {
        hapticsPreferencesSource.hapticsEnabled
            .catch { emit(true) }  // Default to enabled on error
            .onEach { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
            .launchIn(viewModelScope)
    }
}

@Composable
fun CameraScreen(viewModel: CameraViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hapticFeedback = rememberCaptureHapticFeedback(
        hapticsEnabled = uiState.hapticsEnabled
    )
}
```

**Why This Matters:**
- **Testability**: Can inject fake preference source into ViewModel for unit tests
- **Warm-Start Reliability**: State is preserved across `onNewIntent()` calls without re-fetching
- **Lifecycle Safety**: Preference observation is scoped to ViewModel lifecycle, not Composable recomposition
- **Error Handling**: Centralized error handling (`.catch { emit(true) }`) defaults to safe behavior

**Pattern 2: Preference Source Error Handling**

```kotlin
// Always use .catch { emit(defaultValue) } for DataStore flows
hapticsPreferencesSource.hapticsEnabled
    .catch { emit(true) }  // Default to haptics ON if DataStore fails
    .onEach { enabled ->
        _uiState.update { it.copy(hapticsEnabled = enabled) }
    }
    .launchIn(viewModelScope)
```

This ensures:
- UI remains functional if DataStore becomes temporarily unavailable
- Haptics are ON by default (safe for user experience)
- Error doesn't crash the navigation flow when deep link launches camera

## How to Use

### When Launching Camera via Deep Link or Shortcut

1. **Shortcut Intent** → Android launcher sends `Intent` with deep link `rulebook://camera`
2. **Intent Filter** → `MainActivity` catches intent, extracts data URI
3. **NavHost** → Composes `composable(route = "camera", deepLinks = [...])` to match `rulebook://camera`
4. **ViewModel Initialization** → CameraViewModel initializes, observes `hapticsEnabled` preference
5. **State Observation** → CameraScreen collects `uiState.hapticsEnabled` and applies to UI

### Cold Start vs Warm Start

| Scenario | Path | Key Pattern |
|----------|------|-------------|
| **Cold Start** (app not running) | Shortcut → `onCreate()` → NavHost matches deep link → Camera composes → ViewModel initializes | Normal flow |
| **Warm Start** (app running) | Shortcut → `onNewIntent()` → `setIntent(newIntent)` → NavHost re-reads intent → ViewModel restored | `singleTop` + `onNewIntent()` |

The MVI pattern (preference in ViewModel state, not Composable state) ensures both paths work identically.

## Configuration

### Preference Source Interface

```kotlin
interface HapticsPreferencesSource {
    val hapticsEnabled: Flow<Boolean>
    suspend fun setHapticsEnabled(enabled: Boolean)
}
```

### Testing with Fake Implementation

```kotlin
class FakeHapticsPreferencesSource(
    initialEnabled: Boolean = true
) : HapticsPreferencesSource {
    private val _hapticsEnabled = MutableStateFlow(initialEnabled)
    override val hapticsEnabled: Flow<Boolean> = _hapticsEnabled

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
    }
}

// In ViewModel test:
@Before
fun setup() {
    fakeHapticsPreferencesSource = FakeHapticsPreferencesSource()
    viewModel = CameraViewModel(
        creditRepository = fakeCreditRepository,
        analyticsManager = fakeAnalyticsManager,
        hapticsPreferencesSource = fakeHapticsPreferencesSource
    )
}
```

## Notes

- **Warm-Start Reliability**: The `singleTop` launch mode with `onNewIntent()` is critical for shortcut reliability. Without it, rapid shortcut taps may create new Activity instances instead of reusing the existing one.
- **Error Resilience**: The `.catch { emit(true) }` pattern is defensive — it ensures haptics remain ON if DataStore is unavailable, which is a safe default for user experience.
- **Preference Scope**: All user preference observations should follow this ViewModel pattern for consistency, not just haptics.
- **Deep Link Testing**: When testing deep link navigation, ensure ViewModel initialization completes before assertions, as preference flows may take a frame to settle.
- **Future Preference Sources**: When adding new preferences (theme, language, etc.), replicate this exact pattern: ViewModel constructor parameter + `init` block with `catch/emit` error handling.
