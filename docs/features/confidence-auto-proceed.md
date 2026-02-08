# Confidence Display & Auto-Proceed Logic

**Date:** 2026-02-07
**Related Files:** `feature/generation/`, `core/designsystem/src/main/kotlin/.../component/ConfidenceBadge.kt`, `core/analytics/src/main/kotlin/.../AnalyticsManager.kt`

## Overview

After image analysis returns a `ScanResult` with a confidence level, the generation pipeline branches: high confidence (>= 80%) auto-proceeds to rules generation, while low confidence (< 80%) displays a confirmation screen asking the user to verify or reject the identified game. This avoids unnecessary friction for confident identifications while giving users control over uncertain ones.

## What Was Built

- `ConfidenceBadge` design system component — color-coded badge displaying confidence as a percentage
- `ConfirmationContent` composable — full-screen confirmation UI with game title, confidence badge, and confirm/reject buttons
- Confidence threshold logic in `GenerationViewModel` — evaluates `ScanResult.confidence` against a configurable threshold
- New `GenerationEvent` variant: `AutoProceeding` (note: `NavigateToManualEntry` was removed in Story 5.5 — manual entry is now in-screen state)
- New `GenerationUiState` fields: `showConfirmation` and `gameTitleDisplay`
- Analytics convenience methods: `trackScanAnalysisComplete`, `trackScanConfirmed`, `trackScanManualEntry`

## Technical Implementation

### Key Files

- `core/designsystem/src/main/kotlin/.../component/ConfidenceBadge.kt`: Reusable color-coded badge (green >80%, yellow 50-80%, red <50%) with brutalist styling
- `feature/generation/src/main/kotlin/.../components/ConfirmationContent.kt`: Confirmation screen with game title, ConfidenceBadge, and two action buttons
- `feature/generation/src/main/kotlin/.../GenerationViewModel.kt`: Confidence evaluation logic, `onConfirmGame()`, `onRejectGame()` handlers
- `feature/generation/src/main/kotlin/.../GenerationUiState.kt`: Added `showConfirmation` and `gameTitleDisplay` fields
- `feature/generation/src/main/kotlin/.../GenerationEvent.kt`: Added `AutoProceeding` event type (note: `NavigateToManualEntry` was removed in Story 5.5)
- `feature/generation/src/main/kotlin/.../GenerationScreen.kt`: Conditional rendering — `ConfirmationContent` vs progress indicator
- `core/analytics/src/main/kotlin/.../AnalyticsManager.kt`: Three new convenience methods for confidence analytics

### Key Patterns

- **Threshold-Based Flow Branching**: The ViewModel compares `scanResult.confidence >= autoProceedThreshold` to decide whether to auto-advance or show confirmation. The threshold is a constructor parameter (`DEFAULT_AUTO_PROCEED_THRESHOLD = 0.80f`) to support future remote config without code changes.

- **Confirmation State in IDENTIFYING_GAME Phase**: When showing confirmation, the pipeline stays at `ScanPhase.IDENTIFYING_GAME` rather than advancing. This is deliberate — the progress indicator should reflect actual pipeline state, and the user hasn't confirmed yet. Only after `onConfirmGame()` does it advance to `GENERATING_RULES`.

- **ConfidenceBadge Color Thresholds**: The badge uses three color tiers: green (>0.80), yellow (>=0.50), red (<0.50). Input is clamped via `coerceIn(0f, 1f)` for defensive handling. The color function is extracted as `internal fun confidenceBadgeColor()` for unit testing.

- **Analytics Event Pattern**: Three scan confidence events follow a consistent pattern with confidence as a string property. `scan_analysis_complete` includes `auto_proceed` boolean. `scan_confirmed` and `scan_manual_entry` fire on user action.

### Code Examples

**Evaluating confidence in the pipeline:**

```kotlin
val confidence = scanResult.confidence
if (confidence >= autoProceedThreshold) {
    trackScanAnalysisComplete(confidence, autoProceeded = true)
    _uiState.update { it.copy(gameTitleDisplay = scanResult.gameTitle) }
    _events.send(GenerationEvent.AutoProceeding(scanResult.gameTitle))
    updatePhase(ScanPhase.GENERATING_RULES)
} else {
    trackScanAnalysisComplete(confidence, autoProceeded = false)
    _uiState.update { it.copy(showConfirmation = true) }
}
```

**Conditional rendering in the screen:**

```kotlin
if (uiState.showConfirmation && uiState.scanResult != null) {
    ConfirmationContent(
        gameTitle = uiState.scanResult.gameTitle,
        confidence = uiState.scanResult.confidence,
        onConfirm = onConfirmGame,
        onReject = onRejectGame
    )
} else {
    // Show progress phase indicator
}
```

## How to Use

1. The pipeline automatically evaluates confidence after `scanRepository.analyzeImage()` returns
2. If confidence >= 80%: auto-proceeds, briefly shows game name in header title via `gameTitleDisplay`
3. If confidence < 80%: renders `ConfirmationContent` instead of progress indicator
4. "Yes, continue" calls `viewModel.onConfirmGame()` — clears confirmation, advances to `GENERATING_RULES`
5. "No, enter manually" calls `viewModel.onRejectGame()` — sets `showManualEntry = true` to show manual game name entry (see `docs/features/manual-game-entry.md`)
6. To use `ConfidenceBadge` elsewhere: `ConfidenceBadge(confidence = 0.75f)` — accepts 0.0-1.0 float

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `autoProceedThreshold` | Float | 0.80f | Confidence level at or above which the system auto-proceeds without confirmation |
| Badge green threshold | Float | >0.80 | Confidence above which badge is green (hardcoded in ConfidenceBadge) |
| Badge yellow range | Float | 0.50-0.80 | Confidence range for yellow badge (hardcoded in ConfidenceBadge) |
| Badge red threshold | Float | <0.50 | Confidence below which badge is red (hardcoded in ConfidenceBadge) |

## Notes

- The `autoProceedThreshold` is injected via constructor parameter to support future remote config (e.g., Firebase Remote Config)
- `NavigateToManualEntry` event was removed in Story 5.5 — manual entry is now handled via in-screen state (`showManualEntry`)
- `AutoProceeding` event does not trigger navigation; it's consumed by the UI for brief game name display
- The `ConfidenceBadge` component is in the design system module and can be reused for any score/quality display
- Boundary case: exactly 0.80 confidence auto-proceeds (uses `>=` comparison)
- `coerceIn(0f, 1f)` in ConfidenceBadge protects against out-of-range API responses
