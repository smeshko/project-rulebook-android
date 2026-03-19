# Edge-to-Edge Display Cutout Handling in Immersive Mode

**Date:** 2026-03-19
**Related Files:** `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt`

## Overview

When building full-screen immersive camera UIs on modern Android devices with display cutouts (notches, punch holes), the standard `statusBarsPadding()` modifier returns 0 in immersive mode because system bars are hidden. However, display cutout insets remain valid and must be respected to prevent UI elements from overlapping the cutout area. This document explains the pattern and when to use `safeDrawingPadding()` instead.

## What Was Built

- Proper display cutout handling in the immersive Camera screen
- Top controls Row positioned safely below notch/punch-hole using `safeDrawingPadding()`
- Bottom controls Row similarly protected from display cutout areas
- Clear documentation of the padding strategy in code comments

## Technical Implementation

### Key Files

- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt`: Camera screen composable with immersive mode and display cutout handling

### Key Patterns

**Display Cutout Handling Pattern**: When building immersive full-screen UIs, use `safeDrawingPadding()` for overlay controls instead of `statusBarsPadding()`.

**Why This Matters:**
- `statusBarsPadding()` = queries `WindowInsets.statusBars` only
  - Returns 0 in immersive mode (system bars hidden)
  - Ignores display cutout insets
  - Result: UI overlaps notch/punch-hole ❌

- `safeDrawingPadding()` = queries `WindowInsets.systemBars + displayCutout + ime`
  - In immersive mode: systemBars=0, ime=0 (no keyboard) → effectively just displayCutout padding
  - Protects from notches even when system bars are hidden ✅

**WindowInsets Consumption Model:**
The `RulebookScaffold` declares `contentWindowInsets = WindowInsets.systemBars` which **consumes** the insets. Child composables that query insets receive 0. This is why:
- Regular screens rely on `Scaffold`'s `innerPadding` parameter
- Full-screen immersive screens must use safe drawing padding directly

### Code Examples

**Top controls Row in Camera screen (lines 341-366):**
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .safeDrawingPadding()  // Protects from notch/punch-hole
        .padding(16.dp),        // Additional breathing room
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    CloseButton(onClick = onNavigateBack)

    if (uiState.hasFlashUnit) {
        FlashToggle(flashMode = uiState.flashMode, onToggle = { viewModel.cycleFlashMode() })
    } else {
        Spacer(modifier = Modifier.size(48.dp))
    }

    CreditsDisplay(creditCount = uiState.creditBalance)
}
```

**Bottom controls Box (lines 370-375):**
```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()   // Protects from bottom cutout/gesture nav
        .padding(bottom = 48.dp),
    contentAlignment = Alignment.BottomCenter
) {
    // Capture, Gallery, Spacer buttons
}
```

## How to Use

**When to apply this pattern:**

1. You're building a full-screen immersive UI that hides system bars
2. The device may have display cutouts (notches, punch holes)
3. You have overlay controls (buttons, indicators) that must be visible

**Steps:**

1. Enable immersive mode with `ImmersiveMode()` composable or equivalent
2. For overlay controls that should avoid cutouts, use `safeDrawingPadding()` instead of `statusBarsPadding()`
3. Add additional `.padding()` after `safeDrawingPadding()` for breathing room
4. Position controls with `.align()` (TopCenter, BottomCenter, etc.)

Example:
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .align(Alignment.TopCenter)
        .safeDrawingPadding()      // Respect display cutout
        .padding(16.dp),           // Breathing room
    ...
) {
    // Control buttons
}
```

## Configuration

No configuration needed. This is a Compose layout pattern applied via modifiers.

| Modifier | Use Case | Immersive Mode Behavior |
|----------|----------|------------------------|
| `statusBarsPadding()` | Regular screens inside Scaffold | Returns 0 (insets consumed by Scaffold) |
| `safeDrawingPadding()` | Full-screen immersive overlay controls | Respects displayCutout + ime insets |
| `systemBarsPadding()` | Deprecated, use safeDrawingPadding | Includes system bars (not useful in immersive) |

## Notes

- **Display Cutout Insets:** Android separates `statusBars` insets from `displayCutout` insets. Hiding system bars doesn't invalidate display cutout information.
- **Testing:** Test on actual devices with notches (Pixel 3+, iPhone notch simulation) and gesture navigation bars (Android 10+)
- **Gesture Navigation:** The bottom controls automatically account for gesture nav inset height via `safeDrawingPadding()`
- **IME Handling:** If the screen can show a keyboard, `safeDrawingPadding()` protects from that too
- **3-Button Navigation:** Taller nav bar is accounted for by `safeDrawingPadding()`
- **Immersive Mode Lifecycle:** Use `DisposableEffect` to restore system bars when navigating away from immersive screens

## Related Stories

- Story 2.1: Edge-to-edge enablement via `enableEdgeToEdge()` in MainActivity
- Story 4.10: Camera screen implementation with immersive mode
- Story 9.8: Edge-to-edge polish across all screens

## Further Reading

- [Android WindowInsets Documentation](https://developer.android.com/develop/ui/compose/layouts/insets)
- [Display Cutout Handling](https://developer.android.com/develop/ui/compose/layouts/insets#display-cutout)
- [Jetpack Compose Insets Modifiers](https://developer.android.com/develop/ui/compose/layouts/insets#modifiers)
