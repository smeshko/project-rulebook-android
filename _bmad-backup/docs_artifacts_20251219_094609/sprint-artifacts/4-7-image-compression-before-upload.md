# Story 4.7: Image Compression Before Upload

Status: ready-for-dev

## Story

As a developer,
I want images compressed before upload,
so that API calls are fast and bandwidth-efficient.

## Acceptance Criteria

1. **Given** a photo is captured or selected (FR10)
   **When** processing the image
   **Then** the image is compressed to <1MB

2. **Given** an image is being compressed
   **When** compression runs
   **Then** compression completes in <2 seconds (NFR6)

3. **Given** an image is compressed
   **When** checking quality
   **Then** quality is sufficient for AI recognition

4. **Given** an image has EXIF orientation data
   **When** processing the image
   **Then** EXIF orientation is preserved/corrected

## Tasks / Subtasks

- [ ] Task 1: Create ImageProcessor Utility Class (AC: #1, #2)
  - [ ] Create `ImageProcessor.kt` in `core/common/`
  - [ ] Define interface for image compression
  - [ ] Implement on background thread (Dispatchers.IO)
  - [ ] Return compressed file path or ByteArray

- [ ] Task 2: Implement Bitmap Compression (AC: #1, #3)
  - [ ] Load image to Bitmap from URI
  - [ ] Calculate target dimensions (max 1920px longest edge)
  - [ ] Resize if necessary using createScaledBitmap
  - [ ] Compress to JPEG with 80% quality

- [ ] Task 3: Handle EXIF Orientation (AC: #4)
  - [ ] Read EXIF data from original image
  - [ ] Detect rotation from `ExifInterface`
  - [ ] Apply rotation matrix to Bitmap if needed
  - [ ] Ensure output image is correctly oriented

- [ ] Task 4: Validate File Size (AC: #1)
  - [ ] Check compressed file size
  - [ ] If still >1MB, reduce quality incrementally (75%, 70%, etc.)
  - [ ] Log final file size for debugging
  - [ ] Return error if unable to meet target

- [ ] Task 5: Create Compression Result Class (AC: #1, #2, #4)
  - [ ] Create `CompressionResult` sealed class
  - [ ] Include success case with file path and size
  - [ ] Include error case with reason
  - [ ] Include metadata (original size, compressed size, time)

- [ ] Task 6: Integrate with Camera Flow (AC: #1)
  - [ ] Call ImageProcessor after capture/gallery selection
  - [ ] Show progress indicator during compression
  - [ ] Pass compressed image to processing/upload

- [ ] Task 7: Add Performance Logging (AC: #2)
  - [ ] Log compression start/end times
  - [ ] Track compression ratio
  - [ ] Analytics event for slow compressions (>2s)

## Dev Notes

### Image Processor Interface
```kotlin
interface ImageProcessor {
    suspend fun compress(
        uri: Uri,
        maxSizeBytes: Long = 1_000_000L, // 1MB
        maxDimension: Int = 1920,
        quality: Int = 80
    ): Result<CompressedImage>
}

data class CompressedImage(
    val file: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val width: Int,
    val height: Int,
    val compressionTimeMs: Long
)
```

### Bitmap Loading and Resizing
```kotlin
suspend fun loadAndResizeBitmap(
    context: Context,
    uri: Uri,
    maxDimension: Int
): Bitmap = withContext(Dispatchers.IO) {
    // First, decode bounds only
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    }

    // Calculate sample size
    val sampleSize = calculateSampleSize(
        options.outWidth,
        options.outHeight,
        maxDimension
    )

    // Decode with sample size
    val bitmap = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
    }.let { opts ->
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
    } ?: throw IllegalStateException("Failed to decode image")

    // Scale to exact dimensions if needed
    val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
    if (scale < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    } else {
        bitmap
    }
}
```

### EXIF Orientation Handling
```kotlin
fun correctOrientation(bitmap: Bitmap, uri: Uri, context: Context): Bitmap {
    val exif = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it)
    } ?: return bitmap

    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
    }

    return if (!matrix.isIdentity) {
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}
```

### Compression Implementation
```kotlin
suspend fun compressToFile(
    bitmap: Bitmap,
    outputDir: File,
    quality: Int = 80
): File = withContext(Dispatchers.IO) {
    val outputFile = File(outputDir, "compressed_${System.currentTimeMillis()}.jpg")
    FileOutputStream(outputFile).use { fos ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
    }
    outputFile
}
```

### Adaptive Quality Compression
```kotlin
suspend fun compressToMaxSize(
    bitmap: Bitmap,
    outputDir: File,
    maxSizeBytes: Long
): File = withContext(Dispatchers.IO) {
    var quality = 80
    var outputFile: File

    do {
        outputFile = compressToFile(bitmap, outputDir, quality)
        if (outputFile.length() <= maxSizeBytes) break
        quality -= 5
    } while (quality >= 50)

    if (outputFile.length() > maxSizeBytes) {
        // Still too large, resize further
        // ...
    }

    outputFile
}
```

### Project Structure Notes

- Image processor: `core/common/ImageProcessor.kt`
- Image processor impl: `core/common/ImageProcessorImpl.kt`
- Compression result: `core/common/CompressionResult.kt`
- DI: Register in `core/common/CommonModule.kt`

### References

- [Source: docs/architecture.md#Image Loading] - Coil for loading, Bitmap for processing
- [Source: docs/prd.md#FR10] - System compresses and optimizes images before upload
- [Source: docs/prd.md#NFR6] - Image compression completes in <2 seconds
- [Source: docs/epics/epic-4-photo-capture-flow.md#Story 4.7] - Full story definition

### Testing Requirements

- Unit test compression reduces file size
- Unit test EXIF orientation handling
- Unit test compression completes in <2 seconds (benchmark)
- Test various image sizes and orientations

### Dependencies

- **Prerequisites:** Story 4.2 (capture), Story 4.6 (gallery selection)
- **Depends on:** 4.2 (captured image), 4.6 (gallery image)
- **No parallel execution** - must wait for Wave 2

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List

### File List

## Epic Dependencies

- **Depends On:** Story 4.2, Story 4.6
- **Blocks:** None
- **Can Parallel With:** None (Wave 3 - requires Wave 2 completion)

### Dependency Rationale
- Story 4.2: Requires captured images from photo capture functionality
- Story 4.6: Requires selected images from gallery picker functionality
