# Story 4.1: Camera Screen with CameraX Preview

Status: in-progress

## Story

As a user,
I want to see a live camera preview when I tap the scan button,
so that I can frame my game box photo.

## Acceptance Criteria

1. **Given** the user taps the camera FAB (FR5)
   **When** the camera screen opens
   **Then** a live camera preview fills the screen

2. **Given** the camera screen is opening
   **When** the preview initializes
   **Then** the preview starts within 1 second (NFR3)

3. **Given** the camera is initializing
   **When** selecting the camera
   **Then** the preview uses the rear camera by default

4. **Given** the camera screen is displayed
   **When** viewing the layout
   **Then** the screen is full-screen (no bottom nav)

5. **Given** the app is resumed from background
   **When** the camera screen is active
   **Then** the camera preview resumes properly without errors

## Tasks / Subtasks

- [x] Task 1: Create Camera Feature Module Structure (AC: #1)
  - [x] Create `feature/camera/` module directory
  - [x] Add `build.gradle.kts` with CameraX dependencies
  - [x] Create module package structure: `com.rulebook.feature.camera`
  - [x] Register module in `settings.gradle.kts`

- [x] Task 2: Add CameraX Dependencies (AC: #1, #2)
  - [x] Add CameraX dependencies to version catalog (`libs.versions.toml`)
    - `androidx.camera:camera-core:1.4.1`
    - `androidx.camera:camera-camera2:1.4.1`
    - `androidx.camera:camera-lifecycle:1.4.1`
    - `androidx.camera:camera-view:1.4.1`
  - [x] Add Accompanist Permissions dependency for runtime permissions

- [x] Task 3: Implement CameraScreen Composable (AC: #1, #3, #4)
  - [x] Create `CameraScreen.kt` with full-screen layout
  - [x] Create `CameraUiState.kt` data class for UI state
  - [x] Create `CameraViewModel.kt` for camera state management
  - [x] Hide system bars for immersive experience (edge-to-edge)
  - [x] No bottom navigation visible

- [x] Task 4: Implement CameraX Preview (AC: #1, #2, #3)
  - [x] Create `CameraPreview.kt` composable using AndroidView + PreviewView
  - [x] Configure Preview use case with rear camera selector
  - [x] Bind Preview to ProcessCameraProvider
  - [x] Set up lifecycle-aware camera binding

- [x] Task 5: Handle Camera Lifecycle (AC: #5)
  - [x] Use `DisposableEffect` for camera cleanup on exit
  - [x] Handle configuration changes (rotation)
  - [x] Properly release camera on pause/stop
  - [x] Resume camera on return to foreground

- [x] Task 6: Register Camera Navigation (AC: #1)
  - [x] Add `CameraDestination` to navigation graph
  - [x] Create navigation extension function `navigateToCamera()`
  - [x] Update FAB click handler in MainActivity to navigate to camera

- [x] Task 7: Set Up Koin DI for Camera Module (AC: #1)
  - [x] Create `CameraModule.kt` with Koin definitions
  - [x] Register CameraViewModel in module
  - [x] Add camera module to main Koin configuration

## Dev Notes

### Architecture Pattern
- Follow MVI pattern: CameraViewModel exposes `StateFlow<CameraUiState>`
- CameraScreen observes state and renders UI
- Camera operations are side effects triggered by ViewModel

### CameraX Configuration
```kotlin
// Preview use case setup
val preview = Preview.Builder()
    .setTargetRotation(rotation)
    .build()

// Camera selector for rear camera
val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

// Bind to lifecycle
cameraProvider.bindToLifecycle(
    lifecycleOwner,
    cameraSelector,
    preview
)
```

### PreviewView Integration
```kotlin
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPreviewReady: () -> Unit
) {
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
```

### Lifecycle Handling
- Use `LifecycleOwner` from `LocalLifecycleOwner.current`
- Camera must be bound to lifecycle for automatic pause/resume
- Clean up with `cameraProvider.unbindAll()` on dispose

### Project Structure Notes

- Feature module location: `feature/camera/`
- Main screen: `CameraScreen.kt`
- ViewModel: `CameraViewModel.kt`
- UI State: `CameraUiState.kt`
- Preview composable: `components/CameraPreview.kt`
- Navigation: `navigation/CameraNavigation.kt`
- DI: `di/CameraModule.kt`

### References

- [Source: docs/architecture.md#Module Structure] - Camera module in feature/ folder
- [Source: docs/architecture.md#CameraX] - CameraX 1.4.x with Preview use case
- [Source: docs/prd.md#FR5] - Users can capture photos using device camera
- [Source: docs/prd.md#NFR3] - Camera preview starts in <1 second
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.1] - Full story definition

### Testing Requirements

- Unit test CameraViewModel state management
- UI test for camera screen layout (without actual camera)
- Manual test on physical device for camera preview

### Dependencies

- **Prerequisites:** Epic 2 (navigation), Epic 1 (design system)
- **Blocks:** Stories 4.2, 4.3, 4.4, 4.5, 4.6, 4.8, 4.9, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Camera feature module structure already existed from initial project setup. Module directory, build.gradle.kts with CameraX deps, package structure, and settings.gradle.kts registration were already in place. Added components/ and navigation/ subdirectories for upcoming tasks.
- Task 2: Updated CameraX dependencies from 1.4.0 to 1.4.1. Added Accompanist Permissions library (v0.36.0) to version catalog and camera module.
- Task 3: Implemented CameraScreen with full-screen layout, immersive mode (hidden system bars), CameraUiState data class, and CameraViewModel with MVI pattern. Unit tests for CameraViewModel added.
- Task 4: Implemented CameraPreview composable using AndroidView + PreviewView, configured for rear camera, lifecycle-aware binding via ProcessCameraProvider.
- Task 5: Camera lifecycle handled via DisposableEffect in CameraPreview - unbindAll on dispose, camera bound to lifecycle owner for automatic pause/resume on app backgrounding.
- Task 6: Created CameraNavigation.kt with route constant and navigation extensions. Updated RulebookNavHost to use CameraScreen instead of placeholder. FAB navigation to camera was already working via RulebookScaffold.
- Task 7: Updated CameraModule.kt to register CameraViewModel. Module was already registered in AppModule from initial project setup.

### File List

**New Files:**
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/components/CameraPreview.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/navigation/CameraNavigation.kt`
- `feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt`

**Modified Files:**
- `gradle/libs.versions.toml` - Updated CameraX to 1.4.1, added Accompanist Permissions
- `feature/camera/build.gradle.kts` - Added Accompanist Permissions and test dependencies
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt` - Full implementation
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/di/CameraModule.kt` - Added CameraViewModel
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` - Uses CameraScreen
- `docs/sprint-artifacts/sprint-status.yaml` - Added Epic 4 and Story 4-1 tracking

## Epic Dependencies

- **Depends On:** None (Wave 1 - Foundation)
- **Blocks:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.6, Story 4.8, Story 4.9, Story 4.10
- **Can Parallel With:** None

### Dependency Rationale
- All other Epic 4 stories depend on this foundation story as camera preview must be active for capture, flash, zoom, focus, gallery, credits display, permissions, and navigation
