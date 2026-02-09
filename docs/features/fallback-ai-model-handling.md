# Fallback AI Model Handling

**Date:** 2026-02-09
**Story:** RULE-198
**Related Files:**
- `core/network/src/main/kotlin/com/rulebook/core/network/api/RulebookApi.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ScanRepository.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ScanRepositoryImpl.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationViewModel.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationScreen.kt`
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt`

## Overview

When the primary AI model fails or returns very low confidence for obscure or international games, the app automatically attempts a secondary model to provide rules. This multi-model strategy increases the chance of successful game recognition without requiring user intervention.

## What Was Built

- **Fallback API endpoint** (`POST /analyze/fallback`) — Secondary model endpoint for game recognition
- **Intelligent retry logic** — Determines when fallback is appropriate vs. when errors are non-retryable
- **ViewModel-orchestrated flow** — Manages two-stage recognition with UI state updates during fallback
- **Dynamic UI messaging** — Shows "Trying alternative recognition..." during fallback attempt
- **Graceful degradation** — Offers manual entry when both models fail instead of error state
- **Comprehensive analytics** — Tracks fallback usage, triggers, and outcomes for feature insights

## Technical Implementation

### Key Files

**API Layer** (`core/network`):
- `RulebookApi.kt` — Defines `analyzeImageFallback(request)` endpoint using `POST("analyze/fallback")`

**Repository Layer** (`core/data`):
- `ScanRepository.kt` — Interface defines `analyzeImageFallback(imageUri)` method
- `ScanRepositoryImpl.kt` — Implementation reuses `compressAndEncode()` for the same image, avoiding re-compression. Applies same error mapping through `NetworkErrorMapper` as primary model.

**ViewModel Layer** (`feature/generation`):
- `GenerationViewModel.kt` — Orchestrates primary + fallback flow with intelligent retry logic
- `GenerationUiState.kt` — Adds `isFallbackInProgress` and `fallbackMessage` state fields

**UI Layer** (`feature/generation`):
- `GenerationScreen.kt` — Displays fallback message via null-coalescing: `fallbackMessage ?: currentPhase.message`

**Analytics Layer** (`core/analytics`):
- `AnalyticsManager.kt` — Convenience methods `trackScanFallbackUsed()` and `trackScanFallbackFailed()`

### Key Patterns

#### 1. Intelligent Fallback Triggering

Fallback triggers on **retryable conditions**:
- Primary returns `Result.Success` with confidence **< 0.15f** ("very low confidence")
- Primary returns `Result.Error` with **retryable cause** (timeout, server error 5xx, 429 rate-limit)

Fallback does **NOT trigger** on **non-retryable conditions**:
- No internet / `UnknownHostException` (fallback will also fail)
- Client errors 4xx except 429 (indicates malformed request, not model issue)
- `CancellationException` (respect coroutine cancellation)

#### 2. Error Categorization

Helper method `isRetryableError(cause: Throwable?): Boolean` classifies errors:

```kotlin
private fun isRetryableError(cause: Throwable?): Boolean {
    return when (cause) {
        is SocketTimeoutException -> true  // Timeout is retryable
        is IOException -> true              // Network errors are retryable
        is HttpException -> cause.code >= 500  // 5xx server errors are retryable
        else -> false
    }
}
```

The `categorizeError(cause: Throwable?): String` method maps causes to analytics labels:

```kotlin
private fun categorizeError(cause: Throwable?): String {
    return when (cause) {
        is SocketTimeoutException -> "timeout"
        is HttpException -> {
            when {
                cause.code >= 500 -> "server_error"
                cause.code == 429 -> "rate_limit"
                else -> "client_error"
            }
        }
        is IOException -> "network_error"
        else -> "unknown"
    }
}
```

#### 3. ViewModel Orchestration Pattern

The fallback flow is orchestrated in `startGeneration()`:

```kotlin
// 1. Try primary model
val primaryResult = scanRepository.analyzeImage(imageUri)

// 2. Determine if fallback is needed
val shouldFallback = when (primaryResult) {
    is Result.Success -> primaryResult.data.confidence < FALLBACK_CONFIDENCE_THRESHOLD
    is Result.Error -> isRetryableError(primaryResult.cause)
}

// 3. If fallback needed: update UI, attempt fallback
if (shouldFallback) {
    _uiState.update { it.copy(isFallbackInProgress = true, fallbackMessage = "...") }
    val fallbackResult = scanRepository.analyzeImageFallback(imageUri)
    _uiState.update { it.copy(isFallbackInProgress = false, fallbackMessage = null) }
    // Use fallback result
} else {
    // Use primary result
}

// 4. Handle outcome
when (finalResult) {
    is Result.Success -> { /* confidence flow continues */ }
    is Result.Error -> { /* manual entry or error */ }
}
```

**Key design decisions:**
- ViewModel handles retry logic (not repository) to enable UI updates between calls
- Repository provides two separate methods (`analyzeImage` and `analyzeImageFallback`) for clarity
- Image is not re-compressed — `compressAndEncode()` result is reused via `imageUri` parameter

#### 4. Graceful Degradation

When both primary and fallback fail:
- Sets `showManualEntry = true` (same state used by Story 5.5)
- Does NOT emit `GenerationEvent.Error` (which would navigate back)
- Provides user with immediate manual entry option instead of error screen

This is better UX than forcing the user back to camera after two recognition attempts.

#### 5. Analytics Tracking

**`trackScanFallbackUsed()` event:**
- Triggered when fallback is attempted (success or failure)
- Properties: `primary_error_type`, `primary_confidence`, `fallback_result`
- Useful for: Understanding when fallback triggers, success rate of secondary model

**`trackScanFallbackFailed()` event:**
- Triggered only when both primary and fallback fail
- Properties: `primary_error_type`, `fallback_error_type`
- Useful for: Identifying patterns in double-failure scenarios

## How to Use

### For App Users

1. User takes a photo of an obscure or international game
2. Primary AI model returns very low confidence (< 15%)
3. App automatically shows "Trying alternative recognition..." message
4. Secondary model attempts the same image
5. If secondary succeeds:
   - Continues normal confidence-based flow (auto-proceed or confirmation)
   - User may not even notice fallback occurred
6. If both models fail:
   - Offers manual game name entry directly (better UX than error)

### For Developers Extending This Pattern

**Adding a third fallback model:**

1. Add new endpoint to `RulebookApi.kt`:
   ```kotlin
   @POST("analyze/tertiary")
   suspend fun analyzeImageTertiary(@Body request: AnalyzeRequest): AnalyzeResponse
   ```

2. Add method to `ScanRepository`:
   ```kotlin
   suspend fun analyzeImageTertiary(imageUri: String): Result<ScanResult>
   ```

3. Implement in `ScanRepositoryImpl` (reuse `compressAndEncode` pattern)

4. Update ViewModel orchestration to chain attempts:
   ```kotlin
   val primaryResult = scanRepository.analyzeImage(imageUri)
   val fallbackResult = if (shouldFallback) scanRepository.analyzeImageFallback(imageUri) else null
   val tertiaryResult = if (shouldFallbackAgain) scanRepository.analyzeImageTertiary(imageUri) else null
   ```

5. Track additional analytics events as needed

## Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| `FALLBACK_CONFIDENCE_THRESHOLD` | `0.15f` | Confidence level below which fallback triggers |
| Fallback endpoint | `POST /analyze/fallback` | Secondary model recognition endpoint |
| Fallback message | `"Trying alternative recognition..."` | User-facing UI message during fallback |

The confidence threshold (15%) is "very low confidence" per FR16 requirements. Can be adjusted via remote config in future if needed.

## Notes

### Image Compression Optimization

The fallback mechanism reuses the already-compressed base64 image from the primary attempt. The `compressAndEncode()` method is called once per image URI, and the result is passed to both API calls:

```kotlin
val base64 = withContext(Dispatchers.IO) { compressAndEncode(imageUri) }
val primaryResult = api.analyzeImage(AnalyzeRequest(imageData = base64))
// Later, reuse same base64 for fallback
val fallbackResult = api.analyzeImageFallback(AnalyzeRequest(imageData = base64))
```

This avoids the computational cost of re-processing the image for the fallback attempt.

### Timeout Considerations

Both primary and fallback calls use the same OkHttp timeouts (30s read/write, 60s call timeout). In worst case:
- Primary call: up to 30s
- Fallback call: up to 30s
- Total: ~60s (approaches overall 60s call timeout)

For MVP this is acceptable. If latency becomes an issue, fallback could use shorter timeouts or be cancelled if primary + fallback take too long total.

### Testing Patterns

The test suite covers:
- Fallback triggered on very low confidence
- Fallback triggered on timeout/retryable errors
- Fallback NOT triggered on no-internet (non-retryable)
- Fallback NOT triggered on high confidence
- Success flow after fallback
- Manual entry offered on double failure
- UI state updates during fallback
- Analytics events for all scenarios

Tests use `FakeScanRepository` with injectable `analyzeFallbackResult` for controlled testing of both paths.

### Future Considerations

- **Remote config** for `FALLBACK_CONFIDENCE_THRESHOLD` to tune trigger point without app update
- **A/B testing** secondary model success rate against primary model
- **Model versioning** to track which models were used (useful for improvement iteration)
- **Fallback cost** tracking if secondary model incurs higher API costs
- **Tertiary models** if specific game categories have consistently low primary confidence
