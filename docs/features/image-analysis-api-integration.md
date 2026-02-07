# Image Analysis API Integration

**Date:** 2026-02-07
**Related Files:** `core/data/src/main/kotlin/.../repository/ScanRepository.kt`, `core/data/src/main/kotlin/.../repository/ScanRepositoryImpl.kt`, `core/data/src/main/kotlin/.../util/NetworkErrorMapper.kt`, `feature/generation/src/main/kotlin/.../GenerationViewModel.kt`

## Overview

The image analysis integration captures a board game photo, compresses and encodes it as base64, sends it to the `POST /analyze` backend endpoint, and maps the response to a domain `ScanResult`. It uses a two-pass bitmap decode for memory-efficient downsampling, offloads IO to `Dispatchers.IO`, and maps all network errors to user-friendly messages via `NetworkErrorMapper`.

## What Was Built

- `ScanRepository` interface and `ScanRepositoryImpl` with image compression, base64 encoding, and API call orchestration
- `NetworkErrorMapper` utility mapping `SocketTimeoutException`, `UnknownHostException`, `HttpException`, and unknown errors to user-friendly messages
- `GenerationViewModel.startGeneration()` pipeline executing the PROCESSING_IMAGE and ANALYZING_IMAGE phases
- `GenerationUiState.scanResult` field for downstream consumption by Stories 5.4+
- Koin DI wiring in `DataModule` and `GenerationModule`

## Technical Implementation

### Key Files

- `core/data/src/main/kotlin/.../repository/ScanRepository.kt`: Interface with `suspend fun analyzeImage(imageUri: String): Result<ScanResult>`
- `core/data/src/main/kotlin/.../repository/ScanRepositoryImpl.kt`: Implementation with image compression pipeline and error mapping
- `core/data/src/main/kotlin/.../util/NetworkErrorMapper.kt`: Maps exception types to user-facing error strings
- `core/network/src/main/kotlin/.../model/AnalyzeRequest.kt`: DTO with `image_data` (base64) and `image_format` fields
- `core/network/src/main/kotlin/.../model/AnalyzeResponse.kt`: DTO with `game_title`, `confidence`, and optional `thumbnail_url`
- `core/network/src/main/kotlin/.../mapper/ScanResultMapper.kt`: Extension function `AnalyzeResponse.toDomain()` converting to domain model

### Key Patterns

- **Two-Pass Bitmap Decode**: The first pass reads only image dimensions (`inJustDecodeBounds = true`), calculates an optimal `inSampleSize` to cap at 1920px max dimension, then the second pass decodes the downsampled bitmap. This prevents OOM on large camera photos while maintaining quality.

- **IO Dispatcher Offloading**: All bitmap operations (decode, compress, encode) run inside `withContext(Dispatchers.IO)` to avoid blocking the main thread. The API call itself suspends via Retrofit's coroutine adapter.

- **Error Message Mapping**: `NetworkErrorMapper.mapToUserMessage()` converts raw exceptions to user-facing strings. The mapping is applied in the repository layer (not the ViewModel) so all consumers get consistent error messages. When adding new API calls, use this same pattern.

- **safeCall + Error Remapping**: The repository wraps the entire operation in `safeCall { }` which catches exceptions and returns `Result.Error`. Then a `.let { }` block remaps the error message using `NetworkErrorMapper`, preserving the original `cause` for logging.

### Code Examples

**Calling the image analysis API from a repository:**

```kotlin
override suspend fun analyzeImage(imageUri: String): Result<ScanResult> =
    safeCall {
        val base64 = withContext(Dispatchers.IO) { compressAndEncode(imageUri) }
        val request = AnalyzeRequest(imageData = base64)
        val response = api.analyzeImage(request)
        response.toDomain()
    }.let { result ->
        when (result) {
            is Result.Success -> result
            is Result.Error -> Result.Error(
                message = NetworkErrorMapper.mapToUserMessage(result.cause),
                cause = result.cause,
            )
        }
    }
```

**Handling the result in a ViewModel:**

```kotlin
val result = scanRepository.analyzeImage(imageUri)
when (result) {
    is Result.Success -> {
        _uiState.update { it.copy(scanResult = result.data) }
        updatePhase(ScanPhase.IDENTIFYING_GAME)
    }
    is Result.Error -> {
        _uiState.update { it.copy(error = result.message) }
        _events.send(GenerationEvent.Error(result.message))
    }
}
```

## How to Use

1. Inject `ScanRepository` into your ViewModel via Koin: `val scanRepository: ScanRepository = get()`
2. Call `scanRepository.analyzeImage(imageUri)` with a content URI string
3. Handle `Result.Success` to get `ScanResult(gameTitle, confidence, thumbnailUrl?)`
4. Handle `Result.Error` to get a user-friendly error message string
5. The `NetworkErrorMapper` is applied automatically - no need to map errors manually

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `MAX_IMAGE_DIMENSION` | Int | 1920 | Maximum bitmap dimension before downsampling |
| JPEG quality | Int | 80 | Compression quality (0-100) passed to `bitmap.compress()` |
| `image_format` | String | "jpeg" | Format hint sent in `AnalyzeRequest` |
| OkHttp read timeout | Int | 30s | Configured in `NetworkModule` - triggers `SocketTimeoutException` |

## Notes

- `ScanRepositoryImpl` requires Android framework classes (`BitmapFactory`, `ContentResolver`, `Base64`) so unit testing requires Robolectric or instrumented tests
- `bitmap.recycle()` is called after compression to free native memory immediately
- `CancellationException` is always rethrown (via `safeCall`) to preserve structured concurrency
- The compression result is checked before encoding - if `bitmap.compress()` returns false, an `IllegalStateException` is thrown
- `Base64.NO_WRAP` is used to avoid line breaks in the encoded string, which could break JSON serialization
- `NetworkErrorMapper` is an `object` (singleton) for stateless, thread-safe access
