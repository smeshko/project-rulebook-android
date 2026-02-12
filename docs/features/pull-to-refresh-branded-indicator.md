# Pull-to-Refresh Branded Indicator

**Date:** 2026-02-13
**Story:** RULE-221 - Story 7.6: Pull-to-Refresh (Future Proofing)
**Related Files:**
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryScreen.kt`
- `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookColors.kt`

## Overview

Customized the Material 3 pull-to-refresh indicator on the Library screen with brand-consistent pink coloring (#E91E63 light / #F06292 dark). This feature demonstrates how to extend Material 3's `PullToRefreshBox` component with custom styling while preserving platform conventions. The implementation uses the `PullToRefreshDefaults.Indicator` with overridden colors to maintain the standard Material 3 circular progress behavior while aligning with the brutalist design system.

## What Was Built

- **Custom Pull-to-Refresh Indicator** - Branded pink spinner color with surfacePrimary background circle
- **Material 3 State Management** - `rememberPullToRefreshState()` for indicator lifecycle
- **Color Token Integration** - Using `RulebookTheme.colors.pink` and `surfacePrimary` for theme-aware styling
- **Gesture Integration** - Indicator appears when user pulls down on the Library grid/empty/error states
- **Visual Feedback** - Smooth Material 3 animated circular progress while refreshing

## Technical Implementation

### Key Files

- **`LibraryScreen.kt`** - Added custom `indicator` parameter to existing `PullToRefreshBox` with branded colors

### Key Patterns

#### 1. **Material 3 Pull-to-Refresh Customization Pattern**

Material 3's `PullToRefreshBox` provides a standard indicator, but you can override it with custom styling:

```kotlin
val pullToRefreshState = rememberPullToRefreshState()

PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    modifier = Modifier.fillMaxSize(),
    state = pullToRefreshState,
    indicator = {
        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            state = pullToRefreshState,
            color = RulebookTheme.colors.pink,           // Spinner arc color
            containerColor = RulebookTheme.colors.surfacePrimary  // Background circle
        )
    }
) {
    // Content
}
```

**Key details:**
- `state` parameter: Required for both `PullToRefreshBox` and `Indicator` — must be the same instance
- `color` parameter: The spinning progress arc color (uses `pink` for brutalist branding)
- `containerColor` parameter: The circular background color (uses `surfacePrimary` for theme awareness)
- `isRefreshing` parameter: Passed to indicator to control animation state
- `modifier.align(Alignment.TopCenter)`: Positions indicator at the top of the content area (Material 3 convention)

#### 2. **State Management with rememberPullToRefreshState()**

The `PullToRefreshState` manages indicator animation and positioning:

```kotlin
val pullToRefreshState = rememberPullToRefreshState()
// State is automatically recomposed when pull gesture changes
// Persists across recompositions within the same scope
```

**Why this matters:**
- The state object tracks the pull progress (0.0 to 1.0)
- Animation curves are handled by Material 3
- Both `PullToRefreshBox` and `Indicator` need this shared state for coordination

#### 3. **Theme-Aware Color Integration**

Colors are resolved from the design system at composition time:

```kotlin
color = RulebookTheme.colors.pink  // Automatically light/dark theme aware
```

**Color tokens used:**
- `pink`: #E91E63 (light mode) / #F06292 (dark mode) — Spinner arc
- `surfacePrimary`: White (light) / Dark surface color (dark) — Background circle

This ensures the indicator respects the app's theme without extra logic.

#### 4. **Gesture Integration - No Code Changes**

The pull-to-refresh gesture is handled entirely by `PullToRefreshBox`:
- User pulls down → `onRefresh` callback fires
- `isRefreshing` state becomes true → Indicator shows and animates
- Refresh completes → `isRefreshing` becomes false → Indicator hides

The indicator customization doesn't change this flow — it only affects the visual appearance.

### Code Examples

**Basic Custom Indicator (what was implemented):**
```kotlin
// In LibraryScreenContent composable
val pullToRefreshState = rememberPullToRefreshState()

PullToRefreshBox(
    isRefreshing = uiState.isRefreshing,
    onRefresh = onRefresh,
    modifier = Modifier.fillMaxSize(),
    state = pullToRefreshState,
    indicator = {
        PullToRefreshDefaults.Indicator(
            modifier = Modifier.align(Alignment.TopCenter),
            isRefreshing = uiState.isRefreshing,
            state = pullToRefreshState,
            color = RulebookTheme.colors.pink,
            containerColor = RulebookTheme.colors.surfacePrimary
        )
    }
) {
    // Grid or empty/error state
}
```

**Extending with Custom Indicator Component:**
If you need more complex customization, create a reusable composable:

```kotlin
@Composable
fun BrutalistPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    PullToRefreshDefaults.Indicator(
        modifier = modifier.align(Alignment.TopCenter),
        isRefreshing = isRefreshing,
        state = state,
        color = RulebookTheme.colors.pink,
        containerColor = RulebookTheme.colors.surfacePrimary
    )
}

// Usage
PullToRefreshBox(
    ...,
    indicator = {
        BrutalistPullToRefreshIndicator(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing
        )
    }
)
```

## How to Use

### Adding Custom Pull-to-Refresh to Another Screen

1. **Create the state:** `val pullToRefreshState = rememberPullToRefreshState()`
2. **Add `state` and `indicator` parameters** to existing `PullToRefreshBox`
3. **Define the indicator** using `PullToRefreshDefaults.Indicator` with your brand colors
4. **Ensure `isRefreshing` state** is connected to your ViewModel
5. **Test in preview** with `isRefreshing = true` state

### Customizing Indicator Colors

Use any color tokens from your design system:

```kotlin
PullToRefreshDefaults.Indicator(
    color = RulebookTheme.colors.accentColor,      // Change spinner color
    containerColor = RulebookTheme.colors.surface   // Change background color
)
```

### Customizing Indicator Position

Adjust alignment if you want the indicator elsewhere:

```kotlin
PullToRefreshDefaults.Indicator(
    modifier = Modifier.align(Alignment.Center)    // Center instead of top
)
```

## Configuration

### Theme Integration

The indicator automatically respects light/dark theme:

| Property | Token | Light | Dark |
|----------|-------|-------|------|
| Spinner Color | `pink` | #E91E63 | #F06292 |
| Background | `surfacePrimary` | White | Dark Surface |
| Indicator Size | Material 3 default | 40dp | 40dp |

### Design System Requirements

- **@OptIn(ExperimentalMaterial3Api::class)** annotation required (already applied at `LibraryScreenContent`)
- **Imports needed:**
  - `androidx.compose.material3.pulltorefresh.PullToRefreshDefaults`
  - `androidx.compose.material3.pulltorefresh.rememberPullToRefreshState`

### Material 3 Dependency

- **Compose BOM:** 2024.11.00 or later
- **Material 3:** Included in Compose BOM, no separate dependency

## Notes

### Design Decision: Platform Conventions

This implementation **preserves Material 3 platform conventions** rather than creating a fully custom indicator:
- Uses `PullToRefreshDefaults.Indicator` instead of a custom composable
- Only overrides colors, not the animation or layout
- Maintains standard Material 3 circular progress appearance
- Respects Material 3 pull gesture behavior

**Why:** The UX spec states "Preserve Android Conventions" — Material 3 pull-to-refresh is the Android standard, so we customize it rather than replace it.

### Future Considerations

**Cloud Sync Integration:**
Currently, the refresh triggers a local Room Flow re-emission (no-op for visual feedback). When cloud sync is implemented, change `LibraryViewModel.refresh()` to call the cloud sync API instead of re-collecting local data.

```kotlin
// Future: when cloud sync is available
fun refresh() = viewModelScope.launch {
    try {
        _uiState.update { it.copy(isRefreshing = true) }
        cloudSyncRepository.syncLibrary()  // Replace Room re-collect
    } finally {
        delay(300)  // Keep visual feedback visible
        _uiState.update { it.copy(isRefreshing = false) }
    }
}
```

The indicator will work unchanged — only the refresh action changes.

### Testing Notes

- **ViewModel test:** `refresh shows indicator then clears it` validates state lifecycle
- **Preview:** `LibraryScreenRefreshingLightPreview` shows the branded indicator in action
- **UI testing:** No special tests needed — Material 3 gesture detection is well-tested upstream

### Performance Considerations

- `rememberPullToRefreshState()` is lightweight and recreated only on recomposition
- No additional state flow or observers added
- Indicator animation is GPU-accelerated by Compose
- No performance impact vs. default Material 3 indicator

### Related Material 3 Documentation

- [Pull to Refresh | Android Developers](https://developer.android.com/develop/ui/compose/components/pull-to-refresh)
- [PullToRefreshBox | Kotlin API Docs](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3.pulltorefresh/-pull-to-refresh-box.html)
- [PullToRefreshDefaults | Kotlin API Docs](https://kotlinlang.org/api/compose-multiplatform/material3/androidx.compose.material3.pulltorefresh/-pull-to-refresh-defaults.html)

---

## Related Features

- **Story 7.1 (RULE-216):** Game Grid Display - Established the Library screen layout and initial PullToRefreshBox
- **Story 7.2 (RULE-217):** Sort Options Dropdown - Added reactive flow pattern for library data
- **Story 7.5 (RULE-220):** Long-Press Context Menu - Enhanced game card interactions
- **Core Design System:** RulebookTheme.colors for theme-aware color tokens
