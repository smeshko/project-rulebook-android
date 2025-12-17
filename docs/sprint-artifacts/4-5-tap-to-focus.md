# Story 4.5: Tap-to-Focus

Status: ready-for-dev

## Story

As a user,
I want to tap the preview to focus on a specific area,
so that I get a sharp image of the game box.

## Acceptance Criteria

1. **Given** the camera preview is active (FR8)
   **When** the user taps on the preview
   **Then** the camera focuses on that point

2. **Given** the user taps to focus
   **When** focus begins
   **Then** a focus indicator appears briefly at the tap location

3. **Given** manual focus has been set
   **When** focus completes
   **Then** auto-focus continues after manual focus (not locked)

## Tasks / Subtasks

- [ ] Task 1: Add Focus State to CameraUiState (AC: #1, #2)
  - [ ] Add `focusPoint: Offset?` to CameraUiState (null when no manual focus)
  - [ ] Add `showFocusIndicator: Boolean` to CameraUiState
  - [ ] Create `onTapToFocus(offset: Offset)` action in ViewModel

- [ ] Task 2: Implement Tap Detection on Preview (AC: #1)
  - [ ] Add `pointerInput` modifier with `detectTapGestures`
  - [ ] Convert tap coordinates to normalized (0-1) values
  - [ ] Pass tap offset to ViewModel

- [ ] Task 3: Create MeteringPoint from Tap (AC: #1)
  - [ ] Use `MeteringPointFactory` from PreviewView
  - [ ] Create `MeteringPoint` at tap coordinates
  - [ ] Build `FocusMeteringAction` with the point

- [ ] Task 4: Execute Focus on Camera (AC: #1, #3)
  - [ ] Use `camera.cameraControl.startFocusAndMetering(action)`
  - [ ] Set `FocusMeteringAction.FLAG_AF` for autofocus
  - [ ] Set reasonable timeout (auto-cancel after 5 seconds)
  - [ ] Handle focus result callback

- [ ] Task 5: Create FocusIndicator Composable (AC: #2)
  - [ ] Create `FocusIndicator.kt` in `feature/camera/components/`
  - [ ] Design focus ring animation (scale + fade)
  - [ ] Position at tap location
  - [ ] Animate in on tap, fade out after focus

- [ ] Task 6: Implement Focus Indicator Animation (AC: #2)
  - [ ] Scale animation: start larger, animate to target size
  - [ ] Fade animation: appear, hold, fade out
  - [ ] Total animation duration ~1 second
  - [ ] Remove indicator after animation completes

## Dev Notes

### Tap-to-Focus Implementation
```kotlin
@Composable
fun FocusablePreview(
    previewView: PreviewView,
    onTap: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onTap(offset)
                }
            }
    )
}
```

### MeteringPoint Creation
```kotlin
fun focusAtPoint(
    previewView: PreviewView,
    x: Float,
    y: Float,
    camera: Camera
) {
    val meteringPointFactory = previewView.meteringPointFactory
    val meteringPoint = meteringPointFactory.createPoint(x, y)

    val focusAction = FocusMeteringAction.Builder(meteringPoint, FocusMeteringAction.FLAG_AF)
        .setAutoCancelDuration(5, TimeUnit.SECONDS)
        .build()

    camera.cameraControl.startFocusAndMetering(focusAction)
        .addListener({
            // Focus completed - auto-focus will resume
        }, ContextCompat.getMainExecutor(context))
}
```

### Focus Indicator Composable
```kotlin
@Composable
fun FocusIndicator(
    position: Offset,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 1.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    if (alpha > 0f) {
        Canvas(
            modifier = modifier
                .offset { IntOffset(position.x.toInt() - 32, position.y.toInt() - 32) }
                .size(64.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        ) {
            // Focus ring
            drawCircle(
                color = Color.White,
                radius = size.minDimension / 2,
                style = Stroke(width = 2.dp.toPx())
            )
            // Corner brackets (optional, for visual interest)
        }
    }
}
```

### Focus Indicator Auto-Hide
```kotlin
@Composable
fun CameraScreen(/* ... */) {
    var focusPosition by remember { mutableStateOf<Offset?>(null) }
    var showFocusIndicator by remember { mutableStateOf(false) }

    LaunchedEffect(focusPosition) {
        if (focusPosition != null) {
            showFocusIndicator = true
            delay(1000)
            showFocusIndicator = false
        }
    }

    // Use focusPosition and showFocusIndicator for FocusIndicator
}
```

### Alternative: Bracket-Style Focus Indicator
```kotlin
@Composable
fun FocusBrackets(position: Offset, visible: Boolean) {
    // Four corner brackets forming a square
    // More distinctive than a simple circle
}
```

### Project Structure Notes

- Focus indicator: `feature/camera/components/FocusIndicator.kt`
- Tap handling: Integrated into `CameraPreview.kt`
- Integration: Update `CameraViewModel.kt` and `CameraScreen.kt`

### References

- [Source: docs/architecture.md#CameraX] - CameraX focus and metering APIs
- [Source: docs/prd.md#FR8] - Users can tap to focus the camera on a specific area
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.5] - Full story definition

### Testing Requirements

- Test tap triggers focus at correct location
- Test focus indicator appears at tap point
- Test focus indicator fades out after animation
- Manual test focus quality on physical device

### Dependencies

- **Prerequisites:** Story 4.1 (camera preview must be active)
- **Parallel with:** Stories 4.2, 4.3, 4.4, 4.6, 4.8, 4.9, 4.10

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
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.6, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera preview for tap-to-focus functionality
