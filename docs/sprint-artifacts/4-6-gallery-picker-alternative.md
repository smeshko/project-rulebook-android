# Story 4.6: Gallery Picker Alternative

Status: ready-for-dev

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

- [x] Task 1: Add Gallery State to CameraUiState (AC: #1, #2)
  - [x] Add `selectedGalleryImageUri: String?` to CameraUiState
  - [ ] Add `lastGalleryThumbnail: Bitmap?` to CameraUiState (optional - deferred to Task 4)
  - [x] Create `onGalleryImageSelected(uri: String)` action in ViewModel
  - [x] Create `clearSelectedGalleryImage()` action in ViewModel

- [ ] Task 2: Implement Photo Picker Contract (AC: #1, #2, #3)
  - [ ] Use `ActivityResultContracts.PickVisualMedia()` for modern picker
  - [ ] Configure for image media type only
  - [ ] Create `rememberLauncherForActivityResult` in composable
  - [ ] Handle selected URI result

- [ ] Task 3: Create GalleryButton Composable (AC: #4, #5)
  - [ ] Create `GalleryButton.kt` in `feature/camera/components/`
  - [ ] Apply brutalist styling with border
  - [ ] Position to the left of capture button
  - [ ] Ensure adequate touch target (48dp+)

- [ ] Task 4: Implement Gallery Thumbnail (AC: #4 - Optional)
  - [ ] Load last image from MediaStore
  - [ ] Display as button background/icon
  - [ ] Fall back to gallery icon if no images
  - [ ] Use Coil for efficient thumbnail loading

- [ ] Task 5: Handle Gallery Result (AC: #2)
  - [ ] Receive URI from photo picker
  - [ ] Validate URI is accessible
  - [ ] Update state with selected image
  - [ ] Trigger navigation to processing (same as capture)

- [ ] Task 6: Handle Cancellation (AC: #3)
  - [ ] Detect null/empty result from picker
  - [ ] Return to camera preview with no state change
  - [ ] No error message needed for cancellation

- [ ] Task 7: Wire Gallery to Processing Flow (AC: #2)
  - [ ] Same processing path as captured photos
  - [ ] Ensure compression applied to gallery images too
  - [ ] Navigate to processing screen with selected image

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

- Task 1: Added `selectedGalleryImageUri` to CameraUiState and corresponding ViewModel methods for gallery image selection. Tests pass.

### File List

- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraUiState.kt (modified)
- feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt (modified)
- feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt (modified)

## Epic Dependencies

- **Depends On:** Story 4.1
- **Blocks:** Story 4.7
- **Can Parallel With:** Story 4.2, Story 4.3, Story 4.4, Story 4.5, Story 4.8, Story 4.9, Story 4.10

### Dependency Rationale
- Story 4.1: Requires camera screen context for gallery button placement
