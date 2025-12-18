# Story 4.6: Gallery Picker Alternative

Status: Ready for Review

## Story

As a user,
I want to select a photo from my gallery,
so that I can use an existing image of a game box.

## Acceptance Criteria

1. **Given** the camera screen is displayed (FR9)
   **When** the user taps the gallery button
   **Then** the system photo picker opens

2. **Given** the photo picker is open
   **When** the user selects a photo
   **Then** selecting a photo returns it for processing

3. **Given** the photo picker is open
   **When** the user cancels
   **Then** canceling returns to camera preview

4. **Given** the gallery button is displayed
   **When** viewing the button (optional enhancement)
   **Then** the gallery button shows a thumbnail of the last photo

5. **Given** the camera screen layout
   **When** viewing the controls
   **Then** the gallery button is positioned near the capture button

## Tasks / Subtasks

- [x] Task 1: Add Gallery Thumbnail State to CameraUiState (AC: #4)
  - [x] Add `lastGalleryThumbnailUri: String?` to CameraUiState
  - [x] Create `setLastGalleryThumbnail(uri: String?)` action in ViewModel

- [x] Task 2: Implement Photo Picker Contract (AC: #1, #2, #3)
  - [x] Use `ActivityResultContracts.PickVisualMedia()` for modern picker
  - [x] Configure for image media type only
  - [x] Create `rememberLauncherForActivityResult` in composable
  - [x] Handle selected URI result via callback

- [x] Task 3: Create GalleryButton Composable (AC: #4, #5)
  - [x] Create `GalleryButton.kt` in `feature/camera/components/`
  - [x] Apply brutalist styling with border
  - [x] Position to the left of capture button
  - [x] Ensure adequate touch target (56dp)

- [x] Task 4: Implement Gallery Thumbnail (AC: #4 - Optional)
  - [x] Load last image from MediaStore via getLastPhotoThumbnailUri()
  - [x] Display as button background using Coil AsyncImage
  - [x] Fall back to gallery icon if no images
  - [x] Use Coil for efficient thumbnail loading

- [x] Task 5: Handle Gallery Result (AC: #2)
  - [x] Receive URI from photo picker
  - [x] Pass URI directly to parent via `onGalleryImageSelected` callback
  - [x] Trigger navigation to processing (same as capture via callback)

- [x] Task 6: Handle Cancellation (AC: #3)
  - [x] Detect null/empty result from picker
  - [x] Return to camera preview with no state change
  - [x] No error message needed for cancellation

- [x] Task 7: Wire Gallery to Processing Flow (AC: #2)
  - [x] Same processing path as captured photos via `onGalleryImageSelected` callback
  - [x] Wire callback in RulebookNavHost and CameraNavigation
  - [ ] Ensure compression applied to gallery images too (handled in processing module)
  - [x] Navigate to processing screen with selected image (via callback)

- [x] Task 8: Gallery Available Without Camera (Code Review Fix)
  - [x] Show gallery button even when camera permission denied
  - [x] Show gallery button in rationale and denied screens
  - [x] Allows users to use gallery as true alternative to camera

## Dev Notes

### Photo Picker Implementation (AndroidX)
```kotlin
@Composable
fun CameraScreen(
    viewModel: CameraViewModel = koinViewModel(),
    onImageSelected: (Uri) -> Unit
) {
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onGalleryImageSelected(uri)
            onImageSelected(uri)
        }
        // null = user cancelled, no action needed
    }

    // In your UI
    GalleryButton(
        onClick = {
            pickMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}
```

### Gallery Button Composable
```kotlin
@Composable
fun GalleryButton(
    onClick: () -> Unit,
    thumbnailUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .border(
                width = 3.dp,
                color = RulebookTheme.colors.onBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUri != null) {
            AsyncImage(
                model = thumbnailUri,
                contentDescription = "Last photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = "Open gallery",
                tint = RulebookTheme.colors.onBackground
            )
        }
    }
}
```

### Getting Last Photo Thumbnail
```kotlin
suspend fun getLastPhotoThumbnail(context: Context): Uri? {
    return withContext(Dispatchers.IO) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
            } else null
        }
    }
}
```

### Camera Controls Layout
```kotlin
@Composable
fun CameraControls(
    onCapture: () -> Unit,
    onGalleryClick: () -> Unit,
    isCapturing: Boolean,
    lastPhotoUri: Uri?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GalleryButton(
            onClick = onGalleryClick,
            thumbnailUri = lastPhotoUri
        )

        CaptureButton(
            onClick = onCapture,
            enabled = !isCapturing
        )

        // Placeholder for symmetry (or close button)
        Spacer(modifier = Modifier.size(56.dp))
    }
}
```

### Project Structure Notes

- Gallery button: `feature/camera/components/GalleryButton.kt`
- Camera controls: `feature/camera/components/CameraControls.kt`
- Integration: Update `CameraViewModel.kt` and `CameraScreen.kt`

### References

- [Source: docs/architecture.md#Image Loading] - Coil for image loading
- [Source: docs/prd.md#FR9] - Users can select existing photos from device gallery
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.6] - Full story definition
- AndroidX Documentation: `ActivityResultContracts.PickVisualMedia`

### Testing Requirements

- Test photo picker launches on button tap
- Test selected photo URI is received correctly
- Test cancellation returns to camera without error
- Test thumbnail loading (optional feature)

### Dependencies

- **Prerequisites:** Story 4.1 (camera screen must exist)
- **Blocks:** Story 4.7 (gallery images also need compression)
- **Parallel with:** Stories 4.2, 4.3, 4.4, 4.5, 4.8, 4.9, 4.10

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

- Task 1: Added `lastGalleryThumbnailUri` to CameraUiState with corresponding `setLastGalleryThumbnail` method.
- Tasks 2-3, 5-7: Implemented GalleryButton composable with brutalist styling, photo picker using ActivityResultContracts.PickVisualMedia(), integrated into CameraScreen with proper callback handling. Gallery button positioned to the left of capture button.
- Task 4: Added optional thumbnail feature - loads last photo from MediaStore and displays it on the gallery button using Coil. Falls back to gallery icon when no images available.
- Code Review Cycle 1: Removed unused `selectedGalleryImageUri` state - the callback mechanism to parent handles gallery selection directly without intermediate state.
- Code Review Cycle 2: Wired `onGalleryImageSelected` callback in navigation layer. Added gallery button to permission denied/rationale screens so users can access gallery even without camera permission.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/navigation/CameraNavigation.kt (modified - added callback params)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/components/GalleryButton.kt (new)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/util/GalleryThumbnail.kt (new)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)
- feature/camera/src/androidTest/kotlin/com/rulebook/feature/camera/components/GalleryButtonTest.kt (new)
- feature/camera/build.gradle.kts (modified - added coil-compose dependency)
- app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt (modified - wired callbacks)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** Story 4.7
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera screen context for gallery button placement
