# Story 4.4: Pinch-to-Zoom Gesture

Status: in-progress

## Story

As a user,
I want to zoom the camera with a pinch gesture,
so that I can focus on the game box from a distance.

## Acceptance Criteria

1. **Given** the camera preview is active (FR7)
   **When** the user performs a pinch gesture
   **Then** the camera zooms in (spread) or out (pinch)

2. **Given** the user is zooming
   **When** the gesture is active
   **Then** zoom is smooth and responsive

3. **Given** the user is zooming
   **When** reaching zoom limits
   **Then** zoom level is bounded (1x to max supported)

4. **Given** the user is zooming
   **When** the zoom level changes
   **Then** an optional zoom level indicator appears briefly

## Tasks / Subtasks

- [x] Task 1: Add Zoom State to CameraUiState (AC: #1, #3)
  - [x] Add `zoomRatio: Float` to CameraUiState (default 1.0f)
  - [x] Add `minZoomRatio: Float` to CameraUiState
  - [x] Add `maxZoomRatio: Float` to CameraUiState
  - [x] Add `showZoomIndicator: Boolean` to CameraUiState

- [x] Task 2: Get Zoom Bounds from CameraInfo (AC: #3)
  - [x] Query `cameraInfo.zoomState` after camera binding
  - [x] Extract `minZoomRatio` and `maxZoomRatio`
  - [x] Store bounds in ViewModel state

- [x] Task 3: Implement Pinch-to-Zoom Gesture Detection (AC: #1, #2)
  - [x] Add `pointerInput` modifier with `detectTransformGestures`
  - [x] Calculate new zoom ratio from gesture scale
  - [x] Clamp zoom ratio within bounds
  - [x] Update state with new zoom ratio

- [x] Task 4: Apply Zoom to Camera (AC: #1, #2)
  - [x] Use `camera.cameraControl.setZoomRatio(ratio)`
  - [x] Apply zoom immediately on gesture change
  - [x] Handle zoom change result (success/failure)

- [ ] Task 5: Create ZoomIndicator Composable (AC: #4)
  - [ ] Create `ZoomIndicator.kt` in `feature/camera/components/`
  - [ ] Display current zoom level (e.g., "1.5x")
  - [ ] Animate appearance and disappearance
  - [ ] Position unobtrusively (center or corner)

- [ ] Task 6: Implement Zoom Indicator Auto-Hide (AC: #4)
  - [ ] Show indicator when zoom changes
  - [ ] Auto-hide after 1.5-2 seconds of inactivity
  - [ ] Use `LaunchedEffect` with delay for hiding

## Dev Notes

### Zoom Gesture Detection
```kotlin
@Composable
fun ZoomablePreview(
    onZoomChange: (Float) -> Unit,
    currentZoom: Float,
    minZoom: Float,
    maxZoom: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    // Calculate new zoom based on gesture
                    val newZoom = (currentZoom * zoom).coerceIn(minZoom, maxZoom)
                    onZoomChange(newZoom)
                }
            }
    ) {
        content()
    }
}
```

### Alternative: ScaleGestureDetector (AndroidView)
```kotlin
// If using PreviewView directly with AndroidView
val scaleGestureDetector = ScaleGestureDetector(
    context,
    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newZoom = (currentZoom * scaleFactor).coerceIn(minZoom, maxZoom)
            cameraControl.setZoomRatio(newZoom)
            return true
        }
    }
)
```

### CameraX Zoom Control
```kotlin
// Get zoom state
val zoomState = camera.cameraInfo.zoomState.value
val minZoom = zoomState?.minZoomRatio ?: 1f
val maxZoom = zoomState?.maxZoomRatio ?: 1f

// Apply zoom
camera.cameraControl.setZoomRatio(newZoomRatio)
    .addListener({}, ContextCompat.getMainExecutor(context))
```

### Zoom Indicator Composable
```kotlin
@Composable
fun ZoomIndicator(
    zoomRatio: Float,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = Color.Black.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "%.1fx".format(zoomRatio),
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
```

### Auto-Hide Logic
```kotlin
@Composable
fun CameraScreen(/* ... */) {
    var showZoomIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.zoomRatio) {
        showZoomIndicator = true
        delay(1500)
        showZoomIndicator = false
    }

    // Use showZoomIndicator for ZoomIndicator visibility
}
```

### Project Structure Notes

- Zoom gesture: Integrated into `CameraPreview.kt`
- Zoom indicator: `feature/camera/components/ZoomIndicator.kt`
- Integration: Update `CameraViewModel.kt` and `CameraScreen.kt`

### References

- [Source: docs/architecture.md#CameraX] - CameraX camera control APIs
- [Source: docs/prd.md#FR7] - Users can zoom the camera view (pinch gesture)
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.4] - Full story definition

### Testing Requirements

- Test zoom gesture increases/decreases zoom ratio
- Test zoom is clamped at min/max bounds
- Test zoom indicator appears and auto-hides
- Manual test zoom smoothness on physical device

### Dependencies

- **Prerequisites:** Story 4.1 (camera preview must be active)
- **Parallel with:** Stories 4.2, 4.3, 4.5, 4.6, 4.8, 4.9, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Added zoom state properties to CameraUiState (zoomRatio, minZoomRatio, maxZoomRatio, showZoomIndicator). Added zoom management methods to CameraViewModel (setZoomBounds, setZoomRatio, hideZoomIndicator). Added 7 unit tests for zoom state management.
- Task 2: Updated CameraPreview to query cameraInfo.zoomState after camera binding. Created ZoomBounds data class to pass min/max zoom ratios. Added callbacks for zoom bounds and CameraControl availability. Wired up CameraScreen to store zoom bounds in ViewModel.
- Task 3 & 4: Implemented pinch-to-zoom gesture using Compose's detectTransformGestures. Wrapped CameraPreview in a Box with pointerInput modifier. Gesture scale factor multiplies current zoom ratio, clamped to bounds. CameraControl.setZoomRatio() called immediately on gesture change for responsive feedback.

### File List

**Modified Files:**
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt` - Added zoom state properties
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt` - Added zoom management methods
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt` - Wired up zoom bounds callback
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/components/CameraPreview.kt` - Added zoom bounds extraction, ZoomBounds data class, CameraControl callback
- `feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt` - Added zoom state tests

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** None
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.5, Story 4.6, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera preview for zoom gesture handling
