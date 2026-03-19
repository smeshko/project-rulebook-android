# Predictive Back Gesture Refinement

**Story:** 9.7
**Epic:** 9 — Settings & Platform Polish
**Status:** Done
**Platform:** Android 14+ (API 34+)

## Overview

Ensures all screens support Android's predictive back gesture, providing users with a native preview animation of the previous screen while swiping back. No screen traps the user without a back navigation path.

## Architecture Decisions

### BackHandler Removal from Camera (Story 9.7)

**Problem:** Story 4.10 added an explicit `BackHandler` to `CameraScreen` for close button consistency. This conflicted with the predictive back preview mechanism identified in Story 2.8.

**Root Cause:** `BackHandler` from `androidx.activity:activity-compose` registers an `OnBackPressedCallback`. When present, it intercepts the back event *before* NavHost's `OnBackInvokedCallback` integration can show the preview animation. The user sees no preview — just an immediate back navigation.

**Solution:** Remove the explicit `BackHandler` from `CameraScreen`. NavHost automatically handles back navigation via `popBackStack()`. Camera resource cleanup is unaffected because it uses `DisposableEffect`:
- `ImmersiveMode()` composable in `CameraScreen.kt` — restores system bars on dispose
- `CameraPreview.kt` — unbinds camera use cases and cleans up torch on dispose

**Principle (from Story 2.8 retro):** "Don't add manual BackHandler when predictive back is desired."

### Conditional BackHandler in Onboarding (Story 9.7)

**Problem:** Onboarding uses a `HorizontalPager` with 2 pages. Without a `BackHandler`, swiping back on page 1 exits the app rather than navigating to page 0.

**Solution:** Add a conditional `BackHandler` that is only enabled when on page 1+:

```kotlin
BackHandler(enabled = pagerState.targetPage > 0) {
    coroutineScope.launch {
        pagerState.animateScrollToPage(pagerState.targetPage - 1)
    }
}
```

Uses `targetPage` (not `currentPage`) to correctly handle in-flight pager animations — during a programmatic scroll from page 0→1, `currentPage` is still 0 until settled.

When `enabled = false` (page 0), the system handles back and exits the app. When `enabled = true` (page 1+), the handler animates back to the previous page.

This is the **only** screen that legitimately uses `BackHandler` because it has in-screen navigation (pager pages) that the NavHost back stack does not model.

## Screen Back Navigation Matrix

| Screen | Back Target | Predictive Preview | BackHandler |
|---|---|---|---|
| Library | Exit app | System default | None |
| Settings | Exit app | System default | None |
| Camera | Library (or deep link exit) | Library preview | **None (removed)** |
| Rules | Library (or deep link exit) | Library preview | None |
| Purchase | Previous screen | Previous screen | None |
| Generation | Camera | Camera preview | None |
| Onboarding (page 0) | Exit app | System default | Disabled |
| Onboarding (page 1) | Page 0 | N/A (in-pager nav) | **Enabled** |

## Technical Configuration

### Manifest

`android:enableOnBackInvokedCallback="true"` in `app/src/main/AndroidManifest.xml` opts into the predictive back system. Without this flag, Android does not provide the preview animation.

### Navigation Library

Compose Navigation 2.8.4 (`androidx.navigation:navigation-compose:2.8.4`) automatically integrates with `OnBackInvokedCallback` when `enableOnBackInvokedCallback="true"` is set. The `NavHost` composable registers a callback that provides the preview animation showing the previous destination.

### Deep Link Edge Cases

Both `rulebook://camera` (app shortcut) and `rulebook://rules/{gameId}` may launch with an empty back stack. When NavHost's back stack is empty and back is pressed, the system exits the activity — no trapping occurs.

## Files Changed

| File | Change |
|---|---|
| `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt` | Removed explicit `BackHandler` and `BackHandler` import |
| `feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingScreen.kt` | Added conditional `BackHandler` for pager page navigation |
| `app/src/test/kotlin/com/rulebook/navigation/PredictiveBackConfigTest.kt` | Added tests and expanded manual testing checklist |

## Manual Testing

See `PredictiveBackConfigTest.kt` KDoc for the complete manual testing checklist.

Requires:
- Android 14+ physical device (API 34+)
- Gesture navigation enabled: Settings > System > Gestures > System navigation

Key test cases:
1. Camera: swipe back → see Library preview → complete → land on Library
2. Onboarding page 1: swipe back → animate to page 0
3. Onboarding page 0: swipe back → app exits
4. Deep link to camera: swipe back → app exits

## Related Stories

- **Story 2.8:** Original predictive back implementation — established BackHandler-free pattern
- **Story 4.10:** Re-added BackHandler to Camera — inadvertently reintroduced the conflict
- **Story 9.6:** Added `rulebook://camera` deep link — created empty-back-stack edge case
