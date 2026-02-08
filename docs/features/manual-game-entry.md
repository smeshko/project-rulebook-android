# Manual Game Name Entry

**Date:** 2026-02-08
**Related Files:** `feature/generation/`, `core/designsystem/src/main/kotlin/.../component/RulebookTextField.kt`, `core/analytics/src/main/kotlin/.../AnalyticsManager.kt`

## Overview

When the user rejects the AI-identified game on the confirmation screen (or confidence is very low), a manual entry UI appears allowing them to type the game name themselves. The manual entry is rendered as an in-screen state within the existing `GenerationScreen`, not as a separate navigation destination. After submission, the same rules generation pipeline is triggered.

## What Was Built

- `RulebookTextField` design system component — brutalist-styled text input with 3dp border, offset shadow, and sharp corners
- `ManualEntryContent` composable — full-screen manual entry UI with text field, auto-focus keyboard, and "Generate Rules" submit button
- In-screen state management via `showManualEntry` and `manualGameName` fields in `GenerationUiState`
- `onManualGameNameChanged()` and `onManualGameNameSubmitted()` methods in `GenerationViewModel`
- Removed `GenerationEvent.NavigateToManualEntry` — replaced with in-screen state toggle
- `trackScanManualNameSubmitted` analytics convenience method

## Technical Implementation

### Key Files

- `core/designsystem/src/main/kotlin/.../component/RulebookTextField.kt`: Reusable brutalist text field wrapping `OutlinedTextField` with custom styling
- `feature/generation/src/main/kotlin/.../components/ManualEntryContent.kt`: Manual entry screen with `RulebookTextField`, auto-focus, word capitalization, IME Done action
- `feature/generation/src/main/kotlin/.../GenerationViewModel.kt`: `onManualGameNameChanged()`, `onManualGameNameSubmitted()`, modified `onRejectGame()` to set `showManualEntry = true`
- `feature/generation/src/main/kotlin/.../GenerationUiState.kt`: Added `showManualEntry` and `manualGameName` fields
- `feature/generation/src/main/kotlin/.../GenerationEvent.kt`: Removed `NavigateToManualEntry` event
- `feature/generation/src/main/kotlin/.../GenerationScreen.kt`: Three-way conditional rendering — manual entry > confirmation > progress
- `core/analytics/src/main/kotlin/.../AnalyticsManager.kt`: Added `trackScanManualNameSubmitted` convenience method

### Key Patterns

- **In-Screen State vs Navigation**: Manual entry is a state within `GenerationScreen` (`showManualEntry` boolean), not a separate navigation destination. This matches the `showConfirmation` pattern from Story 5.4 and keeps the generation pipeline state intact in the ViewModel.

- **Rendering Priority**: `showManualEntry` takes precedence over `showConfirmation`, which takes precedence over the progress indicator. This ensures the user always sees the correct UI after rejecting the game.

- **Input Validation**: The "Generate Rules" button is disabled when the text field is empty (after trimming whitespace). The `onManualGameNameSubmitted()` method also guards against blank input and trims whitespace before setting `gameTitleDisplay`.

- **Auto-Focus Keyboard**: `ManualEntryContent` uses `FocusRequester` + `LaunchedEffect(Unit)` to automatically request focus and show the keyboard when the manual entry UI appears.

### Code Examples

**Rejecting game and showing manual entry:**

```kotlin
fun onRejectGame() {
    val confidence = _uiState.value.scanResult?.confidence ?: return
    _uiState.update { it.copy(showConfirmation = false, showManualEntry = true) }
    trackScanManualEntry(confidence)
}
```

**Submitting manual game name:**

```kotlin
fun onManualGameNameSubmitted() {
    val name = _uiState.value.manualGameName.trim()
    if (name.isBlank()) return
    _uiState.update { it.copy(showManualEntry = false, gameTitleDisplay = name) }
    trackScanManualNameSubmitted(name)
    updatePhase(ScanPhase.GENERATING_RULES)
}
```

**Three-way conditional rendering:**

```kotlin
if (uiState.showManualEntry) {
    ManualEntryContent(...)
} else if (uiState.showConfirmation && uiState.scanResult != null) {
    ConfirmationContent(...)
} else {
    // Progress phase indicator
}
```

## How to Use

1. The user takes a photo and the AI analyzes it with low confidence (< 80%)
2. The confirmation screen appears with "No, enter manually" button
3. Tapping "No, enter manually" calls `viewModel.onRejectGame()` — sets `showManualEntry = true`
4. Manual entry UI appears with auto-focused text field and "Generate Rules" button
5. The user types a game name; "Generate Rules" is enabled when text is non-empty
6. Submitting calls `viewModel.onManualGameNameSubmitted()` — validates, sets `gameTitleDisplay`, advances to `GENERATING_RULES`
7. The keyboard IME "Done" action also triggers submission

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Keyboard capitalization | KeyboardCapitalization | Words | Capitalizes the first letter of each word |
| IME action | ImeAction | Done | Shows "Done" button on keyboard |
| Placeholder text | String | "Type game name..." | Hint text shown in empty field |

## Notes

- Credit deduction is NOT part of this story — credits are consumed on successful rules generation (Story 5.7)
- Autocomplete is explicitly deferred per acceptance criteria ("optional, future")
- `NavigateToManualEntry` event from Story 5.4 was removed — manual entry is now fully in-screen
- The `RulebookTextField` component is in `:core:designsystem` and can be reused for future text inputs (e.g., library search)
- Manual entry consumes a credit only if rules generation succeeds (same flow as AI-identified games)
