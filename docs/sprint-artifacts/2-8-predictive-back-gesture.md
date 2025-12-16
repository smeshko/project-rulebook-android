# Story 2.8: Predictive Back Gesture Support

Status: ready-for-dev

## Story

As a user,
I want the app to support Android's predictive back gesture,
So that navigation feels native and modern.

## Acceptance Criteria

1. **Given** the user performs a back gesture (FR49)
   **When** on a detail screen (Rules, Camera)
   **Then** a preview of the previous screen is shown during the gesture

2. **And** completing the gesture navigates back

3. **And** canceling the gesture returns to current screen

4. **And** the back gesture works from the left edge

5. **And** the animation follows system conventions

## Tasks / Subtasks

- [ ] Task 1: Enable predictive back in AndroidManifest (AC: #1)
  - [ ] Add `android:enableOnBackInvokedCallback="true"` to application tag
  - [ ] Verify targeting Android 14+ (API 34)
- [ ] Task 2: Handle back navigation in NavHost (AC: #2, #3)
  - [ ] Use BackHandler composable for custom back handling
  - [ ] Ensure navigation back stack is properly managed
  - [ ] Handle back on main screens (exit app or no-op)
- [ ] Task 3: Configure predictive back animations (AC: #1, #5)
  - [ ] Use system default predictive back animations
  - [ ] Verify preview shows during gesture
  - [ ] Test animation follows system conventions
- [ ] Task 4: Handle edge gestures (AC: #4)
  - [ ] Ensure left edge gesture triggers back
  - [ ] Avoid conflicts with drawer gestures (not used)
  - [ ] Test gesture sensitivity
- [ ] Task 5: Test on Android 14+ devices
  - [ ] Verify predictive back works on Camera screen
  - [ ] Verify predictive back works on Rules screen
  - [ ] Verify behavior degrades gracefully on older devices

## Dev Notes

### Architecture Context

- **Module:** app
- **Location:** AndroidManifest.xml, navigation components
- **API Level:** Requires Android 14+ (API 34) for predictive back

### Implementation Pattern

```xml
<!-- AndroidManifest.xml -->
<application
    android:name=".RulebookApplication"
    android:enableOnBackInvokedCallback="true"
    ... >
</application>
```

```kotlin
// Back handling in composable (if needed)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    ...
) {
    // BackHandler for custom back behavior
    BackHandler(enabled = true) {
        onNavigateBack()
    }

    // Screen content
}
```

### Predictive Back Requirements

| Requirement | Implementation |
|-------------|----------------|
| Enable callback | `android:enableOnBackInvokedCallback="true"` |
| Min API | 34 (Android 14) |
| Animation | System default |
| Gesture area | Left edge |

### Navigation Back Stack Behavior

```kotlin
// Main screens - pop or exit app
composable(Route.Library.route) {
    BackHandler {
        // On main screen, let system handle (exit app)
        // No custom handling needed
    }
    LibraryScreen(...)
}

// Detail screens - pop back stack
composable(Route.Camera.route) {
    CameraScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}

composable(Route.Rules.route) { backStackEntry ->
    RulesScreen(
        gameId = backStackEntry.arguments?.getString(RulebookNavArgs.GAME_ID) ?: "",
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Predictive Back Gesture Flow

1. User starts back gesture from left edge
2. System shows preview of previous screen
3. User continues gesture → navigates back
4. User cancels gesture → returns to current screen

### Backward Compatibility

- Predictive back is only available on Android 14+
- On older devices, standard back navigation works without preview
- No additional code needed for backward compatibility
- `enableOnBackInvokedCallback="true"` is ignored on older APIs

### Testing Notes

1. **Physical device required** - Emulators may not fully support predictive back
2. **Enable gesture navigation** - Settings > System > Gestures > System navigation
3. **Test scenarios:**
   - Camera screen back to Library
   - Rules screen back to Library/Camera
   - Onboarding complete back (should be disabled)
   - Settings tab back (no navigation, main screen)

### Potential Conflicts

- **Drawer gestures**: Not applicable (no drawer in this app)
- **Horizontal swipe**: Rules screen may have horizontal content - ensure no conflicts
- **Full-screen content**: Camera screen is full-screen - ensure edge detection works

### References

- [Source: docs/architecture.md#Platform Integration]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.8]
- [Android Developers: Predictive Back](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture)

## Dev Agent Record

### Context Reference
- Navigation Host from Story 2.2
- Compose Navigation BackHandler
- AndroidManifest configuration

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 2.2 (Navigation Host & Route Definitions)
- **Blocks:** None
- **Can Parallel With:** Stories 2.3, 2.4, 2.5, 2.6, 2.7

### Dependency Rationale
- Story 2.2: Requires navigation structure for back handling
- No blocking: This is an independent navigation enhancement
- Parallel: Can be developed alongside other stories after 2.2
