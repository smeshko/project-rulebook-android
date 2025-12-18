# Story 4.3: Flash/Torch Control

Status: ready-for-dev

## Story

As a user,
I want to control the camera flash,
so that I can photograph game boxes in low-light conditions.

## Acceptance Criteria

1. **Given** the camera preview is active (FR6)
   **When** the user taps the flash toggle
   **Then** the flash mode cycles: Off → On → Auto → Off

2. **Given** the flash toggle is tapped
   **When** the mode changes
   **Then** the current mode is indicated by icon state

3. **Given** flash mode is set to "On"
   **When** the preview is active
   **Then** torch is activated immediately (not just on capture)

4. **Given** the camera screen is displayed
   **When** viewing the layout
   **Then** the flash toggle is positioned in the camera UI (top area)

5. **Given** the device does not have a flash unit
   **When** the camera screen loads
   **Then** the flash control is hidden

## Tasks / Subtasks

- [x] Task 1: Add Flash Mode State to CameraUiState (AC: #1, #2)
  - [x] Create `FlashMode` enum: OFF, ON, AUTO
  - [x] Add `flashMode: FlashMode` to CameraUiState
  - [x] Add `hasFlashUnit: Boolean` to CameraUiState
  - [x] Create `cycleFlashMode()` action in ViewModel

- [ ] Task 2: Check Flash Unit Availability (AC: #5)
  - [ ] Query `CameraInfo.hasFlashUnit()` after camera binding
  - [ ] Update `hasFlashUnit` in state
  - [ ] Conditionally show/hide flash toggle based on state

- [ ] Task 3: Create FlashToggle Composable (AC: #2, #4)
  - [ ] Create `FlashToggle.kt` in `feature/camera/components/`
  - [ ] Design three-state toggle icon (off/on/auto)
  - [ ] Position in top-left or top-right of camera overlay
  - [ ] Apply brutalist styling with clear tap target

- [ ] Task 4: Implement Flash Mode Icons (AC: #2)
  - [ ] Use Material icons: `FlashOff`, `FlashOn`, `FlashAuto`
  - [ ] Show current state clearly with icon and optional label
  - [ ] Add subtle animation on mode change

- [ ] Task 5: Configure ImageCapture Flash Mode (AC: #1)
  - [ ] Map `FlashMode` enum to `ImageCapture.FLASH_MODE_*`
  - [ ] Apply flash mode to ImageCapture use case
  - [ ] Update flash mode when state changes

- [ ] Task 6: Implement Torch Mode (AC: #3)
  - [ ] Get `Camera` instance from `cameraProvider.bindToLifecycle()`
  - [ ] Use `camera.cameraControl.enableTorch(true/false)`
  - [ ] Enable torch when flash mode is ON
  - [ ] Disable torch when flash mode is OFF or AUTO

- [ ] Task 7: Handle Flash Mode Persistence (AC: #1)
  - [ ] Optionally persist last flash mode to DataStore
  - [ ] Restore flash mode on camera screen open
  - [ ] Default to OFF if no preference saved

## Dev Notes

### Flash Mode Enum
```kotlin
enum class FlashMode {
    OFF,
    ON,
    AUTO;

    fun next(): FlashMode = when (this) {
        OFF -> ON
        ON -> AUTO
        AUTO -> OFF
    }

    fun toImageCaptureFlashMode(): Int = when (this) {
        OFF -> ImageCapture.FLASH_MODE_OFF
        ON -> ImageCapture.FLASH_MODE_ON
        AUTO -> ImageCapture.FLASH_MODE_AUTO
    }
}
```

### Check Flash Availability
```kotlin
// After binding camera
val camera = cameraProvider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageCapture
)

val hasFlash = camera.cameraInfo.hasFlashUnit()
```

### Torch Control
```kotlin
// Enable torch (continuous light)
camera.cameraControl.enableTorch(flashMode == FlashMode.ON)

// Note: For FLASH_MODE_AUTO, torch should be off (flash fires only on capture)
```

### Flash Toggle Composable
```kotlin
@Composable
fun FlashToggle(
    flashMode: FlashMode,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .size(48.dp)
            .background(
                color = Color.Black.copy(alpha = 0.3f),
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = when (flashMode) {
                FlashMode.OFF -> Icons.Default.FlashOff
                FlashMode.ON -> Icons.Default.FlashOn
                FlashMode.AUTO -> Icons.Default.FlashAuto
            },
            contentDescription = "Flash ${flashMode.name}",
            tint = Color.White
        )
    }
}
```

### Camera Overlay Layout
```kotlin
@Composable
fun CameraOverlay(
    hasFlashUnit: Boolean,
    flashMode: FlashMode,
    onFlashToggle: () -> Unit,
    // ... other controls
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top controls row
        if (hasFlashUnit) {
            FlashToggle(
                flashMode = flashMode,
                onToggle = onFlashToggle,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        // Other controls...
    }
}
```

### Project Structure Notes

- Flash toggle: `feature/camera/components/FlashToggle.kt`
- Flash mode enum: `feature/camera/FlashMode.kt`
- Integration: Update `CameraViewModel.kt` and `CameraScreen.kt`

### References

- [Source: docs/architecture.md#CameraX] - CameraX with camera control APIs
- [Source: docs/prd.md#FR6] - Users can control camera flash/torch during capture
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.3] - Full story definition

### Testing Requirements

- Test flash mode cycling (OFF → ON → AUTO → OFF)
- Test torch activation when mode is ON
- Test flash control hidden on devices without flash
- Manual test on physical device with/without flash

### Dependencies

- **Prerequisites:** Story 4.1 (camera preview must be active)
- **Parallel with:** Stories 4.4, 4.5, 4.6, 4.8, 4.9, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Created FlashMode enum with OFF/ON/AUTO states and cycling logic. Added flashMode and hasFlashUnit to CameraUiState. Added cycleFlashMode() and onFlashUnitAvailable() methods to CameraViewModel. All unit tests pass.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/FlashMode.kt (new)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/FlashModeTest.kt (new)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** None
- **Can Parallel With:** Story 4.2, Story 4.4, Story 4.5, Story 4.6, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires active camera for flash/torch control functionality
