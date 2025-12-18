# Story 4.2: Photo Capture Button with Haptic Feedback

Status: in-review

## Story

As a user,
I want to tap a button to capture my photo,
so that I can submit my game box image for analysis.

## Acceptance Criteria

1. **Given** the camera preview is active
   **When** the user taps the capture button
   **Then** a photo is captured from the current preview

2. **Given** the capture button is tapped
   **When** the photo is captured
   **Then** haptic feedback confirms the capture (medium click)

3. **Given** the photo is captured
   **When** processing completes
   **Then** the captured image is passed to the next step (processing)

4. **Given** the camera screen is displayed
   **When** viewing the capture button
   **Then** the capture button is prominent and easy to tap (48dp+)

5. **Given** the design system is applied
   **When** viewing the capture button
   **Then** the capture button has brutalist styling (circle with border)

6. **Given** a photo capture is in progress
   **When** the user taps the capture button again
   **Then** the button is disabled to prevent double-tap

## Tasks / Subtasks

- [x] Task 1: Add ImageCapture Use Case to CameraX (AC: #1)
  - [x] Configure ImageCapture use case in CameraViewModel
  - [x] Bind ImageCapture to ProcessCameraProvider with Preview
  - [x] Create `capturePhoto()` function in ViewModel
  - [x] Handle image capture callback

- [x] Task 2: Create CaptureButton Composable (AC: #4, #5)
  - [x] Create `CaptureButton.kt` in `feature/camera/components/`
  - [x] Apply brutalist styling: large circle (72dp), thick border (4dp)
  - [x] Use design system colors: background, border from theme
  - [x] Ensure minimum touch target of 48dp (actual 72dp for prominence)

- [x] Task 3: Implement Haptic Feedback (AC: #2)
  - [x] Get Vibrator service from context
  - [x] Create haptic feedback on capture (VibrationEffect.EFFECT_CLICK)
  - [x] Respect system haptic setting (check Settings)
  - [x] Handle devices without vibration capability

- [x] Task 4: Handle Capture State (AC: #6)
  - [x] Add `isCapturing` to CameraUiState
  - [x] Disable button during capture (visual + interaction)
  - [x] Show brief loading indicator during capture
  - [x] Re-enable button after capture completes or fails

- [x] Task 5: Save Captured Image (AC: #1, #3)
  - [x] Save image to temporary cache file
  - [x] Use `OutputFileOptions` for file configuration
  - [x] Handle rotation/orientation from EXIF
  - [x] Return file URI/path for next step

- [x] Task 6: Wire Capture to Processing Flow (AC: #3)
  - [x] Add captured image path to UiState
  - [x] Trigger navigation to processing screen on success
  - [x] Handle capture failures gracefully with error state

## Dev Notes

### ImageCapture Configuration
```kotlin
// ImageCapture use case
val imageCapture = ImageCapture.Builder()
    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
    .setTargetRotation(rotation)
    .build()

// Bind both Preview and ImageCapture
cameraProvider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview,
    imageCapture
)
```

### Capture Implementation
```kotlin
fun capturePhoto(
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = File(
        outputDirectory,
        "IMG_${System.currentTimeMillis()}.jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onImageCaptured(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}
```

### Haptic Feedback
```kotlin
@Composable
fun rememberHapticFeedback(): () -> Unit {
    val context = LocalContext.current
    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    return {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        }
    }
}
```

### Brutalist Button Styling
```kotlin
@Composable
fun CaptureButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .border(
                width = 4.dp,
                color = RulebookTheme.colors.onBackground,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(RulebookTheme.colors.background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Inner circle for visual effect
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = RulebookTheme.colors.onBackground,
                    shape = CircleShape
                )
        )
    }
}
```

### Project Structure Notes

- Capture button: `feature/camera/components/CaptureButton.kt`
- Haptic helper: `core/common/HapticUtils.kt` (reusable)
- Camera temp directory: Use `context.cacheDir` for temporary images

### References

- [Source: docs/architecture.md#CameraX] - CameraX ImageCapture use case
- [Source: docs/prd.md#FR5] - Users can capture photos using device camera
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.2] - Full story definition
- [Source: docs/architecture.md#Brutalist Modifier Extensions] - Thick borders, bold shadows

### Testing Requirements

- Unit test ViewModel capture state transitions
- UI test for button enabled/disabled states
- Manual test haptic feedback on physical device
- Test capture on various devices with different camera capabilities

### Dependencies

- **Prerequisites:** Story 4.1 (camera preview must be active)
- **Blocks:** Story 4.7 (image compression needs captured image)

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Added ImageCapture use case to CameraX. Updated CameraUiState with isCapturing and capturedImageUri fields. Added capture state management methods to CameraViewModel (onCaptureStarted, onCaptureSuccess, onCaptureError, clearCapturedImage). Updated CameraPreview to bind ImageCapture with Preview and expose capture functionality via callback.
- Task 2: Created CaptureButton composable with brutalist styling (72dp circle, 4dp border, inner 56dp circle). Uses white colors for visibility on camera preview. Includes enabled/disabled state with alpha dimming.
- Task 3: Implemented haptic feedback utility with HapticUtils in core/common. Uses View.performHapticFeedback with CONFIRM constant (API 30+) or KEYBOARD_TAP for older APIs. Automatically respects system haptic settings. Created rememberCaptureHapticFeedback() composable helper in camera module.
- Task 4: Added isCapturing parameter to CaptureButton. When capturing, shows CircularProgressIndicator instead of inner circle, disables button interaction, and updates content description to "Capturing photo". State was already added in Task 1.
- Task 5: Save functionality was implemented in Task 1 - capturePhoto saves to context.cacheDir with OutputFileOptions. CameraX automatically handles EXIF orientation. URI returned via onImageCaptured callback.
- Task 6: Integrated all components in CameraScreen. Added CaptureButton positioned at bottom center with safeDrawingPadding. Wired haptic feedback to trigger on capture. Added onPhotoCaptured callback to notify parent when capture succeeds. Used LaunchedEffect to handle captured URI and notify parent.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/components/CameraPreview.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/components/CaptureButton.kt (new)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/util/CameraHaptics.kt (new)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)
- feature/camera/src/androidTest/kotlin/com/rulebook/feature/camera/CaptureButtonTest.kt (new)
- core/common/src/main/kotlin/com/rulebook/core/common/HapticUtils.kt (new)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** Story 4.7
- **Can Parallel With:** Story 4.3, Story 4.4, Story 4.5, Story 4.6, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera preview to be active for capture functionality
