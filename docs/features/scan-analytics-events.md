# Scan Analytics Events

**Date:** 2026-02-09
**Related Files:**
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationViewModel.kt`
- `core/analytics/src/test/kotlin/com/rulebook/core/analytics/AnalyticsManagerTest.kt`

## Overview

This document describes the complete scan analytics event system that tracks user behavior through the scan, image analysis, and rules generation pipeline. The implementation provides a comprehensive funnel analysis capability, allowing product teams to understand conversion rates, failure points, and user recovery actions across the game recognition flow.

## What Was Built

The scan analytics event system consists of 13 convenience methods on the `AnalyticsManager` interface, providing type-safe event tracking across six stages of the scan flow:

1. **Scan Initiation** - `trackScanStarted()`
2. **Image Analysis** - `trackScanAnalysisComplete()`, `trackScanConfirmed()`, `trackScanManualEntry()`
3. **Game Identification** - `trackScanManualNameSubmitted()`
4. **Rules Generation** - `trackScanGenerationComplete()`, `trackScanFailed()`
5. **Error Handling** - `trackScanCreditCheck()`, `trackScanFallbackUsed()`, `trackScanFallbackFailed()`, `trackScanRetryFromError()`, `trackScanManualEntryFromError()`
6. **User Cancellation** - `trackScanCancelled()`

## Technical Implementation

### Key Files

- **`AnalyticsManager.kt`**: Interface defining 13 scan convenience methods with default implementations
  - Methods delegate to the core `trackEvent()` method via Kotlin interface defaults
  - Eliminates repeated property mapping logic in ViewModels
  - Provides type-safe method signatures with proper parameter names

- **`GenerationViewModel.kt`**: Consumes scan analytics events
  - Injects `AnalyticsManager` via Koin DI
  - Calls convenience methods at each stage of the scan flow
  - Passes context-specific properties (confidence, error_type, duration, etc.)

- **`AnalyticsManagerTest.kt`**: Unit tests for all scan convenience methods
  - Tests verify event name and property mapping for each method
  - Uses `FakeAnalyticsManager` to assert events were fired correctly
  - Tests edge cases (null confidence, zero credits, etc.)

### Key Patterns

**Interface Default Methods for Convenience**
- Implemented in the `AnalyticsManager` interface as default methods
- Each method maps parameters to event properties and calls `trackEvent()`
- This pattern was established in Story 1.6 (onboarding analytics) and replicated for scan events
- Avoids code duplication across implementations (`TelemetryDeckAnalyticsManager`, `FakeAnalyticsManager`)

**Example:**
```kotlin
fun trackScanFailed(errorType: String) {
    trackEvent(
        "scan_failed",
        mapOf("error_type" to errorType)
    )
}
```

**Property Mapping Consistency**
- Event properties use snake_case naming (e.g., `confidence`, `error_type`, `duration_ms`)
- Numeric properties are converted to strings (TelemetryDeck requirement)
- Optional properties (like `primary_confidence` in fallback tracking) are only included if present

**Null Safety for Optional Properties**
- Methods handle nullable parameters gracefully
- Example: `trackScanFallbackUsed()` accepts `primaryConfidence: Float?` and only includes the property if non-null

```kotlin
fun trackScanFallbackUsed(primaryErrorType: String, primaryConfidence: Float?, fallbackResult: String) {
    val properties = mutableMapOf(
        "primary_error_type" to primaryErrorType,
        "fallback_result" to fallbackResult
    )
    if (primaryConfidence != null) {
        properties["primary_confidence"] = primaryConfidence.toString()
    }
    trackEvent("scan_fallback_used", properties)
}
```

### Code Examples

**Tracking scan initiation:**
```kotlin
// In GenerationViewModel.onPhotoSelected()
analyticsManager.trackScanStarted()
```

**Tracking confidence-based decision:**
```kotlin
// In GenerationViewModel when analysis completes
analyticsManager.trackScanAnalysisComplete(
    confidence = scanResult.confidence,
    autoProceeded = shouldAutoProceed(scanResult.confidence)
)
```

**Tracking error recovery:**
```kotlin
// In GenerationViewModel when primary model fails
analyticsManager.trackScanFallbackUsed(
    primaryErrorType = "timeout",
    primaryConfidence = null,
    fallbackResult = "success"
)

// If fallback also fails
analyticsManager.trackScanFallbackFailed(
    primaryErrorType = "timeout",
    fallbackErrorType = "network_error"
)
```

**Tracking user cancellation:**
```kotlin
// In GenerationViewModel.cancel()
analyticsManager.trackScanCancelled(
    phase = generateProgressPhase(generationProgress),
    progress = generationProgress
)
```

## How to Use

### Adding a New Scan Analytics Method

1. **Define the method** in `AnalyticsManager.kt`:
   ```kotlin
   fun trackScanNewAction(param1: String, param2: Int) {
       trackEvent(
           "scan_new_action",
           mapOf(
               "param_1" to param1,
               "param_2" to param2.toString()
           )
       )
   }
   ```

2. **Add unit test** in `AnalyticsManagerTest.kt`:
   ```kotlin
   @Test
   fun `trackScanNewAction fires scan_new_action with correct properties`() {
       analyticsManager.trackScanNewAction(param1 = "test", param2 = 42)

       assertEquals(1, analyticsManager.trackedEvents.size)
       assertEquals("scan_new_action", analyticsManager.trackedEvents[0].name)
       assertEquals("test", analyticsManager.trackedEvents[0].properties["param_1"])
       assertEquals("42", analyticsManager.trackedEvents[0].properties["param_2"])
   }
   ```

3. **Call from ViewModel**:
   ```kotlin
   analyticsManager.trackScanNewAction("value", 42)
   ```

### Tracking the Scan Funnel

The complete funnel typically looks like:

1. User taps camera → `trackScanStarted()`
2. System analyzes image → `trackScanAnalysisComplete(confidence, autoProceeded)`
3. User confirms or rejects → `trackScanConfirmed()` or `trackScanManualEntry()`
4. Rules generate → `trackScanGenerationComplete(gameName, durationMs)`
5. Success, or if error → `trackScanFailed(errorType)`
6. If fallback used → `trackScanFallbackUsed(...)` or `trackScanFallbackFailed(...)`
7. If user retries → `trackScanRetryFromError(errorType)`

This produces a complete funnel trace in the analytics dashboard.

## Configuration

No configuration required. Events are automatically fired when the corresponding methods are called in `GenerationViewModel`. All event names and properties are hardcoded in the interface methods.

**Event Names:**
- `scan_started`
- `scan_analysis_complete`
- `scan_confirmed`
- `scan_manual_entry`
- `scan_manual_name_submitted`
- `scan_generation_complete`
- `scan_failed`
- `scan_credit_check`
- `scan_fallback_used`
- `scan_fallback_failed`
- `scan_retry_from_error`
- `scan_manual_entry_from_error`
- `scan_cancelled`

## Notes

### Cross-Platform Event Naming

Android event names should align with iOS where applicable. The following mapping was identified:

| Android Event | iOS Event | Status |
|--------------|-----------|--------|
| `scan_started` | `scan_started` | ✅ Matched |
| `scan_analysis_complete` | (not documented in iOS) | Android-specific |
| `scan_confirmed` | (not documented in iOS) | Android-specific |
| `scan_manual_entry` | (not documented in iOS) | Android-specific |
| `scan_generation_complete` | `scan_completed` | ❌ **MISMATCH** - Consider renaming in future alignment story |
| `scan_failed` | `scan_failed` | ✅ Matched |
| `scan_cancelled` | (not documented in iOS) | Android-specific |

A future story should align the `scan_generation_complete` → `scan_completed` naming across platforms for consistency in dashboards.

### Testing Pattern

The scan analytics methods use the same testing pattern as onboarding analytics:
1. Create a `FakeAnalyticsManager` instance
2. Call the tracked method
3. Assert `trackedEvents` contains the expected event name and properties
4. Check property values with `assertEquals()`

Note: There are two `FakeAnalyticsManager` classes:
- **Core fake** at `core/analytics/src/test/kotlin/.../FakeAnalyticsManager.kt` - Reusable across modules, records all tracked events
- **ViewModel-specific fake** in `GenerationViewModelTest.kt` - Adds `setThrowOnTrackEvent()` for error scenario testing

### Null Safety Considerations

Some methods accept nullable parameters (e.g., `primaryConfidence: Float?` in `trackScanFallbackUsed()`). This is intentional:
- When the primary model returns an error (not a confidence result), `primaryConfidence` is null
- The method omits the `primary_confidence` property from the event if null
- This allows analytics dashboards to filter/segment by presence of this property

### Future Extensions

Potential future analytics additions:
- `trackScanScreenImpression()` - Track when scan result screen becomes visible
- `trackScanDebugInfo()` - For beta/debug builds only, capture detailed diagnostic data
- `trackScanMetrics()` - Batch metrics like average confidence per device model
- Structured error categorization beyond `error_type` string

