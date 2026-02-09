# Rules Generation API Integration

**Date:** 2026-02-09
**Related Story:** RULE-196 (Story 5.6)
**Related Files:**
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ScanRepository.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ScanRepositoryImpl.kt`
- `core/network/src/main/kotlin/com/rulebook/core/network/mapper/RulesMapper.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationViewModel.kt`

## Overview

Rules generation transforms an identified game title into structured, playable rules through the Rulebook API. Once a game is identified (auto-proceed or manual entry), the system calls the rules generation endpoint to retrieve game rules organized into four sections: Overview, Setup, First Round, and Advanced. This feature completes the game identification-to-rules pipeline and enables the app to provide users with immediately playable game instructions.

## What Was Built

- **Rules Generation Repository Method**: `ScanRepository.generateRules(gameTitle: String): Result<Rules>` provides a consistent API for calling the rules generation endpoint with error handling
- **Intelligent Response Mapper**: `RulesMapper.kt` with title-based and positional fallback matching converts flat API response lists to the four-section domain model
- **ViewModel Integration**: Three entry points (auto-proceed, confirm, manual entry) wired to call rules generation with proper state management and phase advancement
- **Analytics Tracking**: Success and failure events (`scan_generation_complete`, `scan_failed`) with custom properties for monitoring generation performance
- **Optional Thumbnail Support**: `GenerateRequest` updated to accept optional thumbnail URL from scan results, preparing for enhanced API capabilities
- **Comprehensive Test Coverage**: 10 new tests covering success/error flows, analytics tracking, and mapper edge cases

## Technical Implementation

### Key Files

- **`ScanRepository.kt`** (interface): Added `generateRules()` method signature following the established `analyzeImage()` pattern
- **`ScanRepositoryImpl.kt`** (implementation): Implements `generateRules()` with `safeCall` wrapper, error mapping through `NetworkErrorMapper`, and `Result<T>` return type
- **`RulesMapper.kt`** (NEW): Core intelligence for transforming API responses - matches section titles intelligently or falls back to positional indexing
- **`GenerationViewModel.kt`**: Wired three entry points to call `generateRules()`, update state with parsed rules, advance pipeline to `SAVING_RULES` phase
- **`AnalyticsManager.kt`**: Added convenience methods `trackScanGenerationComplete()` and `trackScanFailed()` for generation events

### Key Patterns

#### Section Mapping Strategy
The API returns a flat list of rule sections (`rules_sections: List<RulesSection>`) but the domain model `Rules` has four named fields. The mapper uses intelligent matching:

1. **Title-Based Matching** (primary): Matches section titles case-insensitively against expected names ("Overview"/"summary" → `overview`, "Setup" → `setup`, "First Round"/"initial turns" → `firstRound`, "Advanced"/"detailed rules" → `advanced`)
2. **Positional Fallback** (secondary): If title matching yields incomplete results, uses positional mapping (index 0 → overview, 1 → setup, 2 → firstRound, 3 → advanced)
3. **Default Handling**: Missing sections receive empty `RuleSection` objects to maintain API contract

This approach handles real-world API variations while remaining deterministic for testing.

#### Three-Path Integration
Rules generation is triggered from three locations in the pipeline:

```kotlin
// Path 1: Auto-proceed with high confidence
if (confidence >= AUTO_PROCEED_THRESHOLD) {
    generateRules(gameTitle, thumbnailUrl)  // line 107
}

// Path 2: User confirms identified game
onConfirmGame() {
    generateRules(gameTitle, thumbnailUrl)  // line 206-208
}

// Path 3: User manually enters game name
onManualGameNameSubmitted(manualGameName) {
    generateRules(manualGameName, thumbnailUrl)  // line 244-246
}
```

All three paths use the same `generateRules()` private method, ensuring consistent behavior and error handling.

#### Repository Pattern with Result Wrapper
Following the `analyzeImage()` pattern established in Story 5.3:

```kotlin
override suspend fun generateRules(gameTitle: String): Result<Rules> {
    return safeCall {
        val response = api.generateRules(GenerateRequest(gameTitle = gameTitle))
        response.toDomain()  // Uses RulesMapper extension function
    }
}
```

The `safeCall` utility wraps the suspend function and converts exceptions to `Result.Error`, which is then mapped through `NetworkErrorMapper` for user-friendly error messages.

#### Analytics with Duration Tracking
Success and failure tracking includes contextual properties:

```kotlin
// Success: Captures game name and generation duration
trackScanGenerationComplete(gameName = gameTitle, durationMs = duration)

// Failure: Categorizes error type for monitoring
trackScanFailed(errorType = categorizeError(exception))
```

Error categorization maps exceptions to analytics-friendly types: "timeout", "no_internet", "server_error", "request_failed", "unknown".

### Code Examples

#### Using the Generation Flow

```kotlin
// In GenerationViewModel
private suspend fun generateRules(gameTitle: String, thumbnailUrl: String?) {
    val startTime = System.currentTimeMillis()

    when (val result = scanRepository.generateRules(gameTitle)) {
        is Result.Success -> {
            _uiState.update { it.copy(rules = result.data) }
            _phase.value = ScanPhase.SAVING_RULES
            analyticsManager.trackScanGenerationComplete(
                gameName = gameTitle,
                durationMs = System.currentTimeMillis() - startTime
            )
        }
        is Result.Error -> {
            val userMessage = result.userMessage
            _uiState.update { it.copy(errorMessage = userMessage) }
            _events.send(GenerationEvent.Error(userMessage))
            analyticsManager.trackScanFailed(
                errorType = categorizeError(result.throwable)
            )
        }
    }
}
```

#### Handling the Parsed Rules in State

```kotlin
// The rules are now available in GenerationUiState
data class GenerationUiState(
    val rules: Rules? = null,  // Contains overview, setup, firstRound, advanced
    // ... other state fields
)

// Display rules sections in UI
rules?.overview?.let { section ->
    Text(section.title)
    Text(section.content)
}
```

## How to Use

### 1. Call Rules Generation from Game Identification

After a game is identified (via auto-proceed, manual entry, or confirmation), call `generateRules()` with the game title:

```kotlin
when {
    confidence >= AUTO_PROCEED_THRESHOLD -> generateRules(gameTitle, thumbnailUrl)
    // User confirms game -> generateRules(gameTitle, thumbnailUrl)
    // User enters name -> generateRules(manualGameName, thumbnailUrl)
}
```

### 2. Access Generated Rules from State

```kotlin
val currentState = uiState.value
currentState.rules?.let { rules ->
    // Display rules sections
    displayOverview(rules.overview)
    displaySetup(rules.setup)
    displayFirstRound(rules.firstRound)
    displayAdvanced(rules.advanced)
}
```

### 3. Handle Generation Errors

Errors are automatically mapped to user-friendly messages via `NetworkErrorMapper`:

```kotlin
is Result.Error -> {
    showErrorDialog(result.userMessage)  // "Check your internet connection" etc.
    logAnalytics("scan_failed", errorType = categorizeError(result.throwable))
}
```

### 4. Track Generation Analytics

```kotlin
// Success tracking
analyticsManager.trackScanGenerationComplete(
    gameName = "Catan",
    durationMs = 2500
)

// Failure tracking
analyticsManager.trackScanFailed(errorType = "timeout")
```

## Configuration

### API Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| `generateRules` endpoint | `/generate` (Retrofit) | Rulebook API rules generation endpoint |
| Call timeout | 60s | Overall timeout for the API call |
| Generation target | <45s | Target duration for generation (leaves buffer within timeout) |

### Error Categories

| Error Type | When It Occurs | User Message |
|------------|----------------|--------------|
| `timeout` | Call exceeds 60s | "Generation took too long, please try again" |
| `no_internet` | Network unreachable | "Check your internet connection" |
| `server_error` | HTTP 5xx response | "Server error, please try again later" |
| `request_failed` | HTTP 4xx response | "Unable to generate rules for this game" |
| `unknown` | Other exceptions | "An error occurred during generation" |

### Rules Domain Model

```kotlin
data class Rules(
    val overview: RuleSection,      // Game summary, win condition
    val setup: RuleSection,         // Step-by-step setup instructions
    val firstRound: RuleSection,    // How to play initial turns
    val advanced: RuleSection       // Detailed rules, edge cases
)

data class RuleSection(
    val title: String,
    val content: String
)
```

## Notes

### Section Mapping Flexibility
The mapper prioritizes title-based matching but falls back to positional mapping if titles don't match expected patterns. This handles real-world API variations:
- If API returns sections with different titles, positional mapping ensures reasonable defaults
- If API adds extra sections, they're ignored (only 4 are captured)
- If sections are missing, empty defaults are provided to maintain API contract

### Thumbnail URL Preparation
The `thumbnailUrl` field is added to `GenerateRequest` in preparation for potential API enhancement to use game images. Currently, `ScanRepository.generateRules()` only accepts `gameTitle`, but the ViewModel's `generateRules()` method captures `thumbnailUrl` for future use.

### Future Enhancement Opportunities
1. **Streaming Responses**: API may support streaming rules content (chunked responses) - mapper can be extended to handle incremental section delivery
2. **Rules Caching**: Generated rules could be cached by game title to avoid re-generation
3. **Thumbnail Integration**: When API supports `thumbnail_url` parameter, mapper can extract image data from response
4. **Localization**: Section titles could be matched in multiple languages

### Related Stories
- **Story 5.3 (RULE-193)**: Image Analysis API - established the `ScanRepository` pattern this story extends
- **Story 5.4 (RULE-194)**: Confidence Display & Auto-Proceed - one of three entry points for rules generation
- **Story 5.5 (RULE-195)**: Manual Game Entry - another entry point for rules generation
- **Story 5.7 (RULE-197)**: Rules Persistence - downstream consumer of the generated rules

### Testing
- 10 new unit tests cover success/error flows, analytics, and edge cases
- Mapper tests (6 tests) validate title matching, positional fallback, and missing section handling
- ViewModel tests verify rules storage in state, phase advancement, and event emission
- All 119 tests in generation module pass
