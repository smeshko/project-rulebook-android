# Generation Progress Screen

**Date:** 2026-02-07
**Related Files:** `feature/generation/`, `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/ProgressPhaseIndicator.kt`, `app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt`

## Overview

The Generation screen displays real-time progress while a board game image is being analyzed. It tracks five sequential phases (Processing Image, Analyzing Image, Identifying Game, Generating Rules, Saving Rules), each with defined progress ranges. The screen uses MVI architecture with a reusable ProgressPhaseIndicator design system component.

## What Was Built

- `ScanPhase` enum defining 5 phases with display names, messages, and progress ranges (0-15%, 15-40%, 40-60%, 60-90%, 90-100%)
- `GenerationViewModel` with phase tracking, progress clamping, cancellation support, and analytics
- `GenerationScreen` composable with animated phase indicator, progress bar, and cancel button
- `ProgressPhaseIndicator` reusable design system component with pulse animations and color transitions
- `Generation` navigation route with URL-encoded `imageUri` argument
- Koin DI module for the generation feature

## Technical Implementation

### Key Files

- `feature/generation/src/main/kotlin/.../ScanPhase.kt`: Enum with 5 phases, each defining `startProgress` and `endProgress` float ranges
- `feature/generation/src/main/kotlin/.../GenerationViewModel.kt`: MVI ViewModel managing phase state, progress updates (clamped to phase range), and cancellation
- `feature/generation/src/main/kotlin/.../GenerationScreen.kt`: Stateful/stateless composable pair with event collection via LaunchedEffect
- `core/designsystem/src/main/kotlin/.../ProgressPhaseIndicator.kt`: Animated phase list with pulse effect on active phase, color transitions, and progress bar
- `app/src/main/kotlin/.../NavigationDestination.kt`: `Route.Generation` with `createRoute(imageUri)` factory using URL encoding

### Key Patterns

- **Phase-Based Progress Clamping**: Progress values are clamped to the current phase's `startProgress..endProgress` range using `coerceIn()`. This prevents the progress bar from jumping outside phase boundaries when the backend reports progress.

- **ScanPhase Enum with Progress Ranges**: Each phase defines its own progress range as floats (e.g., `PROCESSING_IMAGE` = 0.0-0.15). The `phaseNumber` property is computed from `ordinal + 1`. This design allows phases to be reordered or ranges adjusted without breaking the pipeline.

- **URL-Encoded Navigation Arguments**: The `imageUri` is URL-encoded when creating the route (`URLEncoder.encode` + replacing `+` with `%20`) because content URIs contain special characters that break navigation route parsing.

- **Placeholder Pipeline**: `startGeneration()` currently sets the initial phase but does not call the API. Stories 5.3-5.7 will wire the actual backend calls. The ViewModel is designed so `updatePhase()` and `updateProgress()` can be called from the pipeline without modification.

### Code Examples

**Updating phase and progress from a pipeline:**

```kotlin
// In GenerationViewModel (or future pipeline integration)
fun updatePhase(phase: ScanPhase) {
    _uiState.update {
        it.copy(currentPhase = phase, overallProgress = phase.startProgress)
    }
}

fun updateProgress(progress: Float) {
    val phase = _uiState.value.currentPhase
    _uiState.update {
        it.copy(overallProgress = progress.coerceIn(phase.startProgress, phase.endProgress))
    }
}
```

**Building phase items for the indicator:**

```kotlin
internal fun buildPhaseItems(currentPhase: ScanPhase): List<PhaseItem> {
    return ScanPhase.entries.map { phase ->
        PhaseItem(
            name = phase.displayName,
            status = when {
                phase.ordinal < currentPhase.ordinal -> PhaseStatus.COMPLETED
                phase.ordinal == currentPhase.ordinal -> PhaseStatus.ACTIVE
                else -> PhaseStatus.PENDING
            },
            phaseNumber = phase.phaseNumber
        )
    }
}
```

## How to Use

1. Navigate to the generation screen: `navController.navigate(Route.Generation.createRoute(imageUri))`
2. The ViewModel automatically initializes from the `imageUri` SavedStateHandle argument
3. Call `viewModel.updatePhase(ScanPhase.ANALYZING_IMAGE)` to advance phases
4. Call `viewModel.updateProgress(0.25f)` to update progress within the current phase
5. The user can cancel via the cancel button, which emits `GenerationEvent.Cancelled`
6. Collect events in the screen's `LaunchedEffect` to handle navigation back

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Phase ranges | Float pairs | Defined in ScanPhase enum | Each phase's start/end progress percentage |
| Animation duration | Int | 300ms | Progress bar and color transition duration |
| Pulse duration | Int | 800ms | Active phase pulse animation cycle |

## Notes

- The pipeline implementation is a placeholder - Stories 5.3-5.7 will wire actual API calls through `updatePhase()` and `updateProgress()`
- Progress is clamped per-phase to prevent visual jumps if the backend reports out-of-range values
- The `generationJob` is stored for cancellation support - calling `cancel()` cancels the coroutine and emits a `Cancelled` event
- `CancellationException` is rethrown in the pipeline to respect structured concurrency
- The `ProgressPhaseIndicator` is a design system component and can be reused for any multi-phase progress display
- URL encoding replaces `+` with `%20` because `URLEncoder.encode` uses `+` for spaces, but Android navigation routes expect `%20`
