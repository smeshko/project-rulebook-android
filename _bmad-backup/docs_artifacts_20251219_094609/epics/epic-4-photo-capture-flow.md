# Epic 4: Photo Capture Flow

**Goal:** Implement the camera experience for capturing game box photos. After this epic, users can open the camera, control flash/zoom/focus, select from gallery, and see their credit balance.

---

## Story 4.1: Camera Screen with CameraX Preview

As a user,
I want to see a live camera preview when I tap the scan button,
So that I can frame my game box photo.

**Acceptance Criteria:**

**Given** the user taps the camera FAB (FR5)
**When** the camera screen opens
**Then** a live camera preview fills the screen
**And** the preview starts within 1 second (NFR3)
**And** the preview uses the rear camera by default
**And** the screen is full-screen (no bottom nav)

**Technical Notes:**
- CameraX 1.4.x Preview use case (Architecture section)
- PreviewView bound to lifecycle
- CameraScreen in `feature/camera` module
- Handle camera lifecycle properly (pause/resume)

**Prerequisites:** Epic 2 (navigation), Epic 1 (design system)

---

## Story 4.2: Photo Capture Button with Haptic Feedback

As a user,
I want to tap a button to capture my photo,
So that I can submit my game box image for analysis.

**Acceptance Criteria:**

**Given** the camera preview is active
**When** the user taps the capture button
**Then** a photo is captured from the current preview
**And** haptic feedback confirms the capture (medium click)
**And** the captured image is passed to the next step (processing)
**And** the capture button is prominent and easy to tap (48dp+)

**And** the capture button has brutalist styling (circle with border)

**Technical Notes:**
- CameraX ImageCapture use case
- Save to temporary file or in-memory
- VibrationEffect for haptic (respects system setting)
- Disable button during capture to prevent double-tap

**Prerequisites:** Story 4.1

---

## Story 4.3: Flash/Torch Control

As a user,
I want to control the camera flash,
So that I can photograph game boxes in low-light conditions.

**Acceptance Criteria:**

**Given** the camera preview is active (FR6)
**When** the user taps the flash toggle
**Then** the flash mode cycles: Off → On → Auto → Off
**And** the current mode is indicated by icon state
**And** torch is activated immediately in "On" mode (not just on capture)

**And** the toggle is positioned in the camera UI (top area)
**And** devices without flash hide this control

**Technical Notes:**
- CameraX `ImageCapture.flashMode` and `Camera.cameraControl.enableTorch()`
- Check `CameraInfo.hasFlashUnit()` before showing
- Icon states: flash_off, flash_on, flash_auto

**Prerequisites:** Story 4.1

---

## Story 4.4: Pinch-to-Zoom Gesture

As a user,
I want to zoom the camera with a pinch gesture,
So that I can focus on the game box from a distance.

**Acceptance Criteria:**

**Given** the camera preview is active (FR7)
**When** the user performs a pinch gesture
**Then** the camera zooms in (spread) or out (pinch)
**And** zoom is smooth and responsive
**And** zoom level is bounded (1x to max supported)
**And** optional: zoom level indicator appears briefly

**Technical Notes:**
- CameraX `Camera.cameraControl.setZoomRatio()`
- Use `ScaleGestureDetector` or Compose gesture detection
- Animate zoom changes for smoothness

**Prerequisites:** Story 4.1

---

## Story 4.5: Tap-to-Focus

As a user,
I want to tap the preview to focus on a specific area,
So that I get a sharp image of the game box.

**Acceptance Criteria:**

**Given** the camera preview is active (FR8)
**When** the user taps on the preview
**Then** the camera focuses on that point
**And** a focus indicator appears briefly at the tap location
**And** auto-focus continues after manual focus

**Technical Notes:**
- CameraX `Camera.cameraControl.startFocusAndMetering()`
- Create `MeteringPoint` from tap coordinates
- Show animated focus ring (fade in/out)

**Prerequisites:** Story 4.1

---

## Story 4.6: Gallery Picker Alternative

As a user,
I want to select a photo from my gallery,
So that I can use an existing image of a game box.

**Acceptance Criteria:**

**Given** the camera screen is displayed (FR9)
**When** the user taps the gallery button
**Then** the system photo picker opens
**And** selecting a photo returns it for processing
**And** canceling returns to camera preview

**And** the gallery button shows a thumbnail of the last photo (optional)
**And** the button is positioned near the capture button

**Technical Notes:**
- Use AndroidX Activity Result API with `PickVisualMedia`
- `ActivityResultContracts.PickVisualMedia()`
- Handle result in CameraViewModel

**Prerequisites:** Story 4.1

---

## Story 4.7: Image Compression Before Upload

As a developer,
I want images compressed before upload,
So that API calls are fast and bandwidth-efficient.

**Acceptance Criteria:**

**Given** a photo is captured or selected (FR10)
**When** processing the image
**Then** the image is compressed to <1MB
**And** compression completes in <2 seconds (NFR6)
**And** quality is sufficient for AI recognition
**And** EXIF orientation is preserved/corrected

**Technical Notes:**
- Use Android Bitmap compression (JPEG 80% quality)
- Resize to max 1920px on longest edge
- Handle rotation from EXIF data
- Process on background thread (Dispatchers.IO)

**Prerequisites:** Story 4.2

---

## Story 4.8: Credit Balance Display on Camera

As a user,
I want to see my remaining credits while in the camera,
So that I know if I can complete a scan.

**Acceptance Criteria:**

**Given** the camera screen is displayed (FR11)
**When** the credit balance is shown
**Then** it displays the current credit count (e.g., "3 credits")
**And** it's positioned unobtrusively (top corner)
**And** it updates reactively if credits change

**And** low credit warning (1 credit) uses warning color
**And** zero credits shows different state (handled in Epic 8)

**Technical Notes:**
- Observe `creditBalance` Flow from DataStore
- CreditsDisplay composable from design system
- Semi-transparent background for readability over preview

**Prerequisites:** Story 4.1, Epic 1 (DataStore)

---

## Story 4.9: Camera Permission Handling

As a user,
I want to be asked for camera permission only when I try to scan,
So that the app doesn't request unnecessary permissions at install.

**Acceptance Criteria:**

**Given** the user taps the camera FAB (FR51)
**When** camera permission is not granted
**Then** a permission rationale is shown explaining why camera is needed
**And** the system permission dialog appears
**And** if granted, camera opens immediately
**And** if denied, helpful message with settings link appears (FR52)

**And** permission is not requested at app launch or install

**Technical Notes:**
- Use `rememberPermissionState` from Accompanist or manual handling
- `Manifest.permission.CAMERA`
- Show rationale before system dialog (shouldShowRationale)
- Deep link to app settings: `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`

**Prerequisites:** Story 4.1

---

## Story 4.10: Camera Close/Back Navigation

As a user,
I want to close the camera and return to the library,
So that I can exit if I change my mind.

**Acceptance Criteria:**

**Given** the camera screen is displayed
**When** the user taps back or the close button
**Then** the camera is released properly
**And** user returns to the previous screen (Library)
**And** no resources leak (camera, memory)

**And** predictive back gesture shows library preview
**And** close button (X) is visible in top corner

**Technical Notes:**
- Release CameraProvider on screen exit
- Use `DisposableEffect` for cleanup
- BackHandler for custom back logic if needed

**Prerequisites:** Story 4.1, Epic 2 (predictive back)

---

## Epic 4: Dependency Flowchart

```
╔═══════════════════════════════════════════════════════════════════════════════════╗
║  WAVE 1: Start Immediately                                                         ║
╠═══════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  [4-1] Camera Screen with CameraX Preview                                          ║
║        └─ Foundation for all camera functionality                                  ║
║                                                                                    ║
╚═══════════════════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
╔═══════════════════════════════════════════════════════════════════════════════════╗
║  WAVE 2: After 4-1 (PARALLEL x8)                                                   ║
╠═══════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  [4-2] Photo Capture Button     ║  [4-3] Flash/Torch Control                       ║
║  [4-4] Pinch-to-Zoom Gesture    ║  [4-5] Tap-to-Focus                              ║
║  [4-6] Gallery Picker           ║  [4-8] Credit Balance Display                    ║
║  [4-9] Permission Handling      ║  [4-10] Close/Back Navigation                    ║
║                                                                                    ║
╚═══════════════════════════════════════════════════════════════════════════════════╝
                    │                                   │
                    └───────────┬───────────────────────┘
                                │
                                ▼
╔═══════════════════════════════════════════════════════════════════════════════════╗
║  WAVE 3: After 4-2 AND 4-6                                                         ║
╠═══════════════════════════════════════════════════════════════════════════════════╣
║                                                                                    ║
║  [4-7] Image Compression Before Upload                                             ║
║        └─ Requires captured images from 4-2 OR gallery images from 4-6             ║
║                                                                                    ║
╚═══════════════════════════════════════════════════════════════════════════════════╝

Legend:
  ═══ Wave boundary
  │ ▼ Sequential dependency
  ║   ║ Parallel stories (can be developed simultaneously)
```

---

**Epic 4 Complete: Photo Capture Flow**

**Stories Created:** 10
**FR Coverage:** FR5-11, FR51-52
**Architecture Sections Referenced:** feature/camera, CameraX, permissions
**UX Patterns Incorporated:** Full-screen camera, haptic feedback, gesture controls

---
