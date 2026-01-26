# Credit-Gated Navigation Pattern

**Date:** 2026-01-26
**Related Files:** `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`, `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraEvent.kt`

## Overview

This pattern controls navigation flow based on credit availability. When a user attempts a premium action (like scanning), the system checks their credit balance and either proceeds with the action or redirects to the paywall. Credits are NOT deducted at check time - only when the action completes successfully.

## What Was Built

- Credit check integration in CameraViewModel before proceeding to image analysis
- Event-based navigation using Kotlin Channel for one-time navigation events
- Analytics tracking for credit check outcomes (`scan_credit_check` event)
- Consistent handling for both camera capture and gallery selection flows

## Technical Implementation

### Key Files

- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`: Contains `checkCreditsAndProceed()` method and event channel
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraEvent.kt`: Sealed class defining navigation events
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt`: Analytics tracking interface with `trackScanCreditCheck()` method

### Key Patterns

- **Channel-based Navigation Events**: Using `Channel<CameraEvent>` with `BUFFERED` capacity ensures events aren't lost if emitted before the collector is ready. Events are consumed exactly once via `receiveAsFlow()`.

- **Deferred Credit Deduction**: Credits are checked but NOT deducted during the check. Deduction only happens when the scan completes successfully (Story 5.7). This prevents charging users for failed operations.

- **Safe Default on Error**: If credit check fails for any reason, the system defaults to showing the paywall rather than proceeding. This prevents unauthorized access to premium features.

### Code Examples

**Emitting navigation events from ViewModel:**

```kotlin
private val _events = Channel<CameraEvent>(Channel.BUFFERED)
val events: Flow<CameraEvent> = _events.receiveAsFlow()

fun checkCreditsAndProceed(imageUri: String) {
    viewModelScope.launch {
        val hasCredits = creditRepository.hasCredits()
        if (hasCredits) {
            _events.send(CameraEvent.ProceedToAnalysis(imageUri))
        } else {
            _events.send(CameraEvent.NavigateToPaywall)
        }
    }
}
```

**Collecting events in Composable:**

```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is CameraEvent.ProceedToAnalysis -> onPhotoCaptured(event.imageUri)
            is CameraEvent.NavigateToPaywall -> onNavigateToPaywall()
        }
    }
}
```

## How to Use

1. Add a sealed class for your screen's events (e.g., `CameraEvent`)
2. Create a `Channel<YourEvent>` in the ViewModel with `BUFFERED` capacity
3. Expose as `Flow` via `receiveAsFlow()`
4. Call `checkCreditsAndProceed()` (or similar) when the user initiates a premium action
5. Collect events in a `LaunchedEffect` in your Composable
6. Handle navigation based on the received event

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| N/A | - | - | No configuration options - behavior is hardcoded for consistency |

## Notes

- The credit check happens AFTER the photo is captured/selected, not before. This provides a better UX (user can see their photo) and the check is nearly instant.
- Analytics tracks both successful checks (`has_credits=true`) and failed checks (`has_credits=false`) with the current balance.
- The `CancellationException` is explicitly rethrown to respect coroutine cancellation - this is critical for proper resource cleanup when the ViewModel is cleared.
- For testing, use `FakeAnalyticsManager` which tracks all events for verification.
