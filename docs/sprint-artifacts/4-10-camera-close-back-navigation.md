# Story 4.10: Camera Close/Back Navigation

Status: in-progress

## Story

As a user,
I want to close the camera and return to the library,
so that I can exit if I change my mind.

## Acceptance Criteria

1. **Given** the camera screen is displayed
   **When** the user taps back or the close button
   **Then** the camera is released properly

2. **Given** the user navigates away from camera
   **When** returning to the previous screen
   **Then** user returns to the previous screen (Library)

3. **Given** the camera is closing
   **When** cleanup occurs
   **Then** no resources leak (camera, memory)

4. **Given** predictive back gesture is enabled
   **When** the user swipes back partially
   **Then** predictive back gesture shows library preview

5. **Given** the camera screen is displayed
   **When** viewing the layout
   **Then** close button (X) is visible in top corner

## Tasks / Subtasks

- [ ] Task 1: Create Close Button (AC: #1, #5)
  - [ ] Create `CloseButton.kt` or use IconButton with close icon
  - [ ] Position in top-left or top-right corner
  - [ ] Apply semi-transparent background for visibility
  - [ ] Wire to navigation callback

- [ ] Task 2: Implement Camera Cleanup (AC: #1, #3)
  - [ ] Release CameraProvider with `unbindAll()`
  - [ ] Use `DisposableEffect` for guaranteed cleanup
  - [ ] Cancel any pending capture operations
  - [ ] Clear any temporary files if needed

- [ ] Task 3: Handle Back Navigation (AC: #1, #2)
  - [ ] Wire close button to `onNavigateBack` callback
  - [ ] Ensure system back button also navigates back
  - [ ] Use `BackHandler` if custom back logic needed

- [ ] Task 4: Support Predictive Back Gesture (AC: #4)
  - [ ] Ensure activity uses `android:enableOnBackInvokedCallback="true"`
  - [ ] Let system handle predictive back animation
  - [ ] Camera preview should remain visible during gesture

- [ ] Task 5: Clean Up Resources on Dispose (AC: #3)
  - [ ] Add `DisposableEffect` with camera cleanup
  - [ ] Release ImageCapture callbacks
  - [ ] Clear ViewModel state if needed
  - [ ] Log cleanup for debugging

- [ ] Task 6: Test Resource Cleanup (AC: #3)
  - [ ] Verify camera released on back navigation
  - [ ] Verify no memory leaks with LeakCanary
  - [ ] Test rapid open/close cycles

## Dev Notes

### Close Button Implementation
```kotlin
@Composable
fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(48.dp)
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close camera",
            tint = Color.White
        )
    }
}
```

### Camera Cleanup with DisposableEffect
```kotlin
@Composable
fun CameraContent(
    cameraProvider: ProcessCameraProvider,
    // ... other params
) {
    DisposableEffect(Unit) {
        onDispose {
            // Release all camera use cases
            cameraProvider.unbindAll()
            Log.d("CameraScreen", "Camera resources released")
        }
    }

    // Camera preview content...
}
```

### Camera Screen with Navigation
```kotlin
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProcessing: (Uri) -> Unit,
    viewModel: CameraViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle system back button
    BackHandler {
        onNavigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview...

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CloseButton(onClick = onNavigateBack)

            // Flash toggle, credits, etc...
        }

        // Bottom controls...
    }
}
```

### Predictive Back Support
The app should already have predictive back enabled from Epic 2 (Story 2.8). Ensure:

```xml
<!-- In AndroidManifest.xml -->
<application
    android:enableOnBackInvokedCallback="true"
    ...>
```

For camera screen specifically:
- Don't override `BackHandler` unnecessarily
- Let system handle predictive back animation
- Camera preview will show behind the back gesture

### ViewModel Cleanup
```kotlin
class CameraViewModel(
    // ...
) : ViewModel() {

    // Cancel any pending operations on clear
    override fun onCleared() {
        super.onCleared()
        // Cancel any pending capture jobs
        // Clear temporary state
    }
}
```

### Camera Overlay Layout (Complete)
```kotlin
@Composable
fun CameraOverlay(
    onClose: () -> Unit,
    onCapture: () -> Unit,
    onGallery: () -> Unit,
    hasFlashUnit: Boolean,
    flashMode: FlashMode,
    onFlashToggle: () -> Unit,
    creditBalance: Int,
    isCapturing: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Top row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CloseButton(onClick = onClose)

            if (hasFlashUnit) {
                FlashToggle(flashMode = flashMode, onToggle = onFlashToggle)
            }

            CreditsDisplay(creditCount = creditBalance)
        }

        // Bottom controls
        CameraControls(
            onCapture = onCapture,
            onGalleryClick = onGallery,
            isCapturing = isCapturing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}
```

### Project Structure Notes

- Close button: `feature/camera/components/CloseButton.kt`
- Camera overlay: `feature/camera/components/CameraOverlay.kt`
- Integration: Update `CameraScreen.kt`

### References

- [Source: docs/architecture.md#Navigation] - Compose Navigation for back handling
- [Source: docs/prd.md#FR49] - System supports predictive back gesture navigation
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.10] - Full story definition
- [Source: docs/sprint-artifacts/2-8-predictive-back-gesture.md] - Predictive back from Epic 2

### Testing Requirements

- Test close button navigates back to library
- Test system back button works correctly
- Test camera resources released (check logs/LeakCanary)
- Test predictive back gesture shows library preview
- Test no memory leaks with rapid navigation

### Dependencies

- **Prerequisites:** Story 4.1 (camera screen), Epic 2 (predictive back)
- **Parallel with:** Stories 4.2, 4.3, 4.4, 4.5, 4.6, 4.8, 4.9

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

### File List

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** None
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.6, Story 4.8, Story 4.9

### Dependency Rationale
- Story 4.1: Requires camera screen for close/back navigation functionality
