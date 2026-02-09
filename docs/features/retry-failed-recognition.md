# Feature: Retry Failed Recognition (Story 5.9)

**Status:** ✅ Implemented
**Story ID:** RULE-199
**Dependencies:** Stories 5.2 (Image Analysis), 5.5 (Manual Entry), 5.8 (Fallback AI)
**Related Files:**
- `GenerationUiState.kt` - Added `showError` state field
- `GenerationViewModel.kt` - Error handling and retry methods
- `GenerationEvent.kt` - Added `RetryFromCamera` event
- `ErrorContent.kt` - Error screen composable (NEW)
- `GenerationScreen.kt` - Rendering priority and event handling

---

## Overview

When image recognition or rules generation fails, users now see an in-screen error message with two options:
- **"Try Again"** - Returns to camera to take a new photo
- **"Enter Manually"** - Shows manual game name entry UI

This replaces the previous behavior where errors simply navigated back to the camera screen without any explanation or alternative options.

---

## User Experience Flow

### Non-Retryable Error Path
1. User takes photo → Recognition starts
2. **Non-retryable error occurs** (e.g., no internet connection)
3. Error screen displays with:
   - Header: "Oops!"
   - Friendly error message (mapped from technical error)
   - "Try Again" button (primary)
   - "Enter Manually" button (secondary)
4. User chooses:
   - **Try Again** → Navigates to camera (can take new photo or select different image)
   - **Enter Manually** → Shows manual game name entry screen

### Downstream Error Path
1. Recognition succeeds → Rules generation fails
2. Error screen displays with same options
3. User can retry entire flow or bypass recognition with manual entry

---

## Error Types Covered

### Primary Recognition Errors (Non-Retryable)
These skip fallback and immediately show the error screen:
- **No Internet** (`UnknownHostException`) - Fallback would also fail
- **Client Errors** (4xx except 429) - Request problem, not model problem

**User-friendly message:** "No internet connection. Please check your network."

### Rules Generation Errors
- AI service errors during rules generation
- Server errors (5xx)
- Timeout during generation

**User-friendly message:** From `NetworkErrorMapper` (e.g., "Server error. Please try again later.")

### Save Errors
- Database write failures
- Validation errors (missing rules/title)

**User-friendly message:** "Failed to save rules. Please try again."

### Generic Errors
- Unexpected exceptions in the generation pipeline

**User-friendly message:** "Something went wrong. Please try again."

---

## Architecture Details

### Rendering Priority Chain
Error screen has the highest rendering priority in `GenerationScreenContent`:

```
if (showError) → ErrorContent
else if (showManualEntry) → ManualEntryContent
else if (showConfirmation) → ConfirmationContent
else → Progress Indicator (default)
```

This ensures error messages are never hidden by other UI states.

### State Management (MVI Pattern)

**UI State Changes:**
```kotlin
// Before (navigates back):
_events.send(GenerationEvent.Error(message))

// After (shows error screen):
_uiState.update { it.copy(error = message, showError = true) }
```

**Event Flow:**
- `onRetry()` → clears error state → emits `RetryFromCamera` → navigates to camera
- `onErrorManualEntry()` → clears error state → sets `showManualEntry = true`

### Credit Safety
No credit is consumed on error because credits are only deducted in `saveRulesAndNavigate()` after a successful save. All error paths occur before credit deduction, so this guarantee is maintained automatically.

---

## Analytics Events

Two new events track user choices from the error screen:

### `scan_retry_from_error`
Fired when user taps "Try Again"
```kotlin
properties = mapOf("error_type" to errorType)
```

### `scan_manual_entry_from_error`
Fired when user taps "Enter Manually"
```kotlin
properties = mapOf("error_type" to errorType)
```

These events help track which errors lead to retries vs. manual entry, informing UX improvements.

---

## Error Message Mapping

All technical errors are mapped to user-friendly messages via `NetworkErrorMapper`:

| Technical Error | User-Friendly Message |
|----------------|----------------------|
| `UnknownHostException` | "No internet connection. Please check your network." |
| `SocketTimeoutException` | "The request took too long. Please try again." |
| HTTP 5xx | "Server error. Please try again later." |
| HTTP 4xx | "Request failed. Please check your input." |
| Generic `Exception` | "Something went wrong. Please try again." |

The ViewModel ensures **no raw exception text** reaches the UI.

---

## Relationship to Story 5.8 (Fallback AI)

Story 5.8 handles **retryable errors** by attempting a fallback AI model. Story 5.9 handles **non-retryable errors** and downstream failures:

- **Retryable error** (timeout, server error) → Story 5.8 triggers fallback → if both fail → manual entry
- **Non-retryable error** (no internet, client error) → Story 5.9 shows error screen immediately
- **Rules generation error** → Story 5.9 shows error screen (recognition succeeded, generation failed)

The two stories work together to provide comprehensive error handling coverage.

---

## Design System Compliance

### Component: `ErrorContent.kt`
Follows existing component patterns:
- Uses `RulebookHeaderBar` with title "Oops!"
- Uses `RulebookCard` for content container
- Uses `RulebookButton` with `ButtonVariant.Primary` (Try Again) and `ButtonVariant.Secondary` (Enter Manually)
- Follows brutalist design system (3dp borders, 0dp corners, offset shadows)
- Spacing from `RulebookTheme.spacing`
- Typography from `RulebookTheme.typography`

### Accessibility
- Adequate touch targets (48dp minimum via `RulebookButton`)
- Content descriptions on all interactive elements
- Semantic header structure

---

## Testing Coverage

### Unit Tests Added (GenerationViewModelTest.kt)
1. ✅ `non-retryable error shows error screen instead of navigating back`
2. ✅ `onRetry clears error state and emits RetryFromCamera event`
3. ✅ `onErrorManualEntry clears error state and shows manual entry`
4. ✅ `rules generation error shows error screen in Story 5_9`
5. ✅ `save error shows error screen in Story 5_9`
6. ✅ `generic exception shows error screen with friendly message`

### Updated Existing Tests
- `analyzeImage error shows error screen` - Updated from checking Error event to checking `showError` state
- `rules generation error shows error screen` - Renamed for clarity, updated assertions
- `save error shows error screen` - Updated from checking Error event to checking `showError` state

All tests pass ✅ (119+ tests in generation module)

---

## Files Modified

### Created
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/components/ErrorContent.kt`

### Modified
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationUiState.kt` - Added `showError: Boolean`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationViewModel.kt` - Error handling, `onRetry()`, `onErrorManualEntry()`, analytics
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationEvent.kt` - Added `RetryFromCamera` event
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationScreen.kt` - Rendering priority, event handler, callback wiring
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt` - Added retry/manual entry from error analytics
- `feature/generation/src/test/kotlin/com/rulebook/feature/generation/GenerationViewModelTest.kt` - Added/updated tests

---

## Implementation Notes

### Key Decision: In-Screen Error State
The error screen is implemented as an **in-screen UI state** (via `showError` boolean) rather than a separate navigation destination. This:
- Preserves the ViewModel state and generation pipeline context
- Matches the established pattern from `showManualEntry` and `showConfirmation`
- Allows the user to retry with the original image URI preserved

### "Try Again" Button Behavior
The button navigates back to the camera rather than re-executing the pipeline with the same image because:
- Re-executing with the same image URI would likely produce the same error
- User needs the option to take a new photo or select a different image
- Analytics tracking differentiates between retry-from-error vs. normal camera usage

### Error Message Storage
The existing `error: String?` field in `GenerationUiState` was retained alongside `showError: Boolean` because:
- `error` stores the user-friendly message text
- `showError` controls rendering priority
- This separation keeps state management clean and explicit

---

## Acceptance Criteria Validation

✅ **Given** recognition fails (network error, AI error)
✅ **When** error screen is displayed
✅ **Then** it shows:
- Friendly error message (not technical) ✓
- "Try Again" button (returns to camera) ✓
- "Enter Manually" button (go to manual entry) ✓

✅ **And** original photo can be retried or new photo taken ✓
✅ **And** no credit is consumed on failure ✓

---

## Future Enhancements

1. **Error-Specific Guidance**: Different error messages could show contextual help (e.g., "No internet" → show network troubleshooting tips)
2. **Retry with Same Image**: Add a third option "Retry Recognition" that re-executes the pipeline without returning to camera
3. **Error Telemetry**: Track which errors occur most frequently to prioritize reliability improvements
4. **Offline Mode**: Cache compressed images and queue recognition requests for when connectivity returns

---

**Last Updated:** 2026-02-09
**Author:** dev-story workflow
**Commits:**
- `df8be99` - feat(story-RULE-199): add error state to GenerationUiState
- `018c2e3` - feat(story-RULE-199): add ViewModel error handling and RetryFromCamera event
- `e4d1fdb` - feat(story-RULE-199): create ErrorContent composable
- `1476dd0` - feat(story-RULE-199): wire ErrorContent with rendering priority
