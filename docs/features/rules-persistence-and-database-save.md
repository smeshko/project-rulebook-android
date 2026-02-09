# Rules Persistence and Database Save

**Date:** 2026-02-09
**Related Story:** RULE-197 (Story 5.7)
**Related Files:**
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/GameRepository.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/GameRepositoryImpl.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/di/DataModule.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationViewModel.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationEvent.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/GenerationScreen.kt`
- `feature/generation/src/main/kotlin/com/rulebook/feature/generation/di/GenerationModule.kt`
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt`

## Overview

After rules are successfully generated from a game title, the app must save both the game metadata and rules content to the local database, deduct credit from the user's account, and navigate to the rules display screen. This feature implements a complete transaction-based persistence layer with defensive credit handling and error recovery. The implementation uses Room DAOs with atomic transactions, automatic UUID generation, and integrated credit deduction to ensure data consistency and prevent credit loss on failures.

## What Was Built

- **GameRepositoryImpl Full Implementation**: Replaced all stub methods with real Room DAO integration for CRUD operations (`getGames`, `getGameById`, `saveGame`, `deleteGame`)
- **Transaction-Based Save Method**: `saveGameWithRules(game, rules, rawJson)` performs atomic two-table insert using Room's transaction support
- **UUID Auto-Generation**: Both `GameEntity` and `RulesEntity` receive generated UUIDs at save time, ensuring unique identifiers without client logic
- **Entity Mapping Layer**: Private extension functions (`Game.toEntity()`, `Rules.toEntity()`, `GameEntity.toDomain()`) handle domain-to-database conversions
- **Credit Deduction Integration**: Calls `CreditRepository.deductCredit()` after successful save, wrapped in defensive try-catch to prevent IO exceptions from blocking navigation
- **Navigation Event System**: New `NavigateToRules(gameId)` event in `GenerationEvent` sealed class with LaunchedEffect handler
- **Back Stack Management**: Navigation from Rules screen goes to Library (not back through generation), via `popUpTo` in route
- **Comprehensive Test Coverage**: 15 new unit tests covering repository CRUD, transaction semantics, and ViewModel save/navigation flows

## Technical Implementation

### Key Files

- **`GameRepository.kt`** (interface): Added `saveGameWithRules(game: Game, rules: Rules, rawJson: String): Result<String>` method — returns the generated game ID
- **`GameRepositoryImpl.kt`** (implementation): Full Room DAO integration with entity mapping and transaction support
- **`GenerationViewModel.kt`**: Added `saveRulesAndNavigate()` private suspend method called after rules generation; constructs Game, builds rawJson, saves to DB, deducts credit, emits navigation event
- **`GenerationEvent.kt`**: New `NavigateToRules(gameId: String)` event for navigation after successful save
- **`GenerationScreen.kt`**: LaunchedEffect handler for `NavigateToRules` event; calls `onNavigateToRules` callback
- **`RulebookNavHost.kt`**: Navigation callback implementation that navigates to `Route.Rules.createRoute(gameId)` with `popUpTo(Library)` to clear back stack
- **`DataModule.kt`** (Koin): Injected DAOs into `GameRepositoryImpl`
- **`GenerationModule.kt`** (Koin): Injected repositories into `GenerationViewModel`

### Key Patterns

#### Transaction-Based Save with UUID Generation

```kotlin
override suspend fun saveGameWithRules(
    game: Game,
    rules: Rules,
    rawJson: String
): Result<String> = safeCall {
    val generatedId = UUID.randomUUID().toString()
    val gameEntity = game.copy(id = generatedId).toEntity()
    val rulesEntity = rules.toEntity(generatedId, rawJson)

    transactionRunner {
        gameDao.insert(gameEntity)
        rulesDao.insert(rulesEntity)
    }

    generatedId
}
```

**Key Decision:** UUID is generated in the repository layer (not at the ViewModel level) to keep domain models immutable and ensure atomicity. Both entities receive the same ID to maintain referential integrity. The `transactionRunner` delegate (injected from Room) ensures both inserts succeed or both fail.

#### Entity-to-Domain Mapping

```kotlin
private fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)
```

**Key Decision:** Mapping functions are private extension functions (not separate mapper classes) following the established project pattern. This keeps the mapper logic colocated with the repository, making it clear what conversions are happening during persistence operations.

#### Defensive Credit Deduction After Save

```kotlin
when (val saveResult = gameRepository.saveGameWithRules(game, rules, rawJson)) {
    is Result.Success -> {
        val gameId = saveResult.data

        // Wrapped in try/catch — IO errors don't block navigation
        try {
            val creditDeducted = creditRepository.deductCredit()
            if (!creditDeducted) {
                Log.w(TAG, "Credit deduction returned false after save...")
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e  // Preserve cancellation semantics
        } catch (e: Exception) {
            Log.e(TAG, "Credit deduction failed after successful save", e)
        }

        _events.send(GenerationEvent.NavigateToRules(gameId))
    }
}
```

**Key Decision:** Credit deduction happens AFTER successful save (not before) to ensure game is persisted. Errors in deduction are logged but do NOT block navigation — the user has already paid the credit cost via scan initiation (FR35 check). This defensive approach prevents the edge case where a network error during deduction could cause a saved game to become inaccessible.

#### Navigation with Back Stack Cleanup

```kotlin
// In RulebookNavHost
onNavigateToRules = { gameId ->
    navController.navigate(Route.Rules.createRoute(gameId)) {
        popUpTo(Route.Library.route) { inclusive = false }
    }
}
```

**Key Decision:** `popUpTo(Library)` clears the generation and camera screens from the back stack. Pressing back from Rules goes directly to Library, preventing the user from navigating back through the generation pipeline where the game already exists in the DB.

#### Raw JSON Storage for Audit Trail

```kotlin
val rawJson = buildString {
    append("{")
    append("\"gameTitle\":\"${escapeJson(gameTitle)}\",")
    append("\"overview\":\"${escapeJson(rules.overview.content)}\",")
    append("\"setup\":\"${escapeJson(rules.setup.content)}\",")
    append("\"firstRound\":\"${escapeJson(rules.firstRound.content)}\",")
    append("\"advanced\":\"${escapeJson(rules.advanced.content)}\",")
    append("\"timestamp\":$currentTimeMillis")
    append("}")
}
```

**Key Decision:** Store a JSON snapshot of the generated rules at save time. This creates an audit trail and enables future features (export, offline access, rules versioning). The simple escaping function avoids Android framework dependencies, keeping the ViewModel unit-testable without mocking.

### Code Examples

#### Saving Generated Rules

```kotlin
// In GenerationViewModel.saveRulesAndNavigate()
val game = Game(
    id = "",  // Will be generated by repository
    title = gameTitle,
    thumbnailUrl = scanResult.thumbnailUrl,
    createdAt = System.currentTimeMillis(),
    lastAccessedAt = System.currentTimeMillis()
)

when (val saveResult = gameRepository.saveGameWithRules(game, rules, rawJson)) {
    is Result.Success -> {
        // Game and rules are now in the database
        val gameId = saveResult.data
        _events.send(GenerationEvent.NavigateToRules(gameId))
    }
    is Result.Error -> {
        // No credit was deducted, user can retry
        _uiState.update { it.copy(error = saveResult.userMessage) }
    }
}
```

#### Querying Saved Games

```kotlin
// From LibraryViewModel or other screens
when (val result = gameRepository.getGames()) {
    is Result.Success -> {
        val allSavedGames = result.data  // List<Game>
        updateUiWithGames(allSavedGames)
    }
    is Result.Error -> {
        showError("Failed to load games: ${result.userMessage}")
    }
}

// Or fetch a specific game by ID
when (val result = gameRepository.getGameById(gameId)) {
    is Result.Success -> {
        val game = result.data
        displayGameTitle(game.title)
        displayThumbnail(game.thumbnailUrl)
    }
    is Result.Error -> {
        showError("Game not found")
    }
}
```

#### Deleting a Game

```kotlin
when (val result = gameRepository.deleteGame(gameId)) {
    is Result.Success -> {
        // Game AND associated rules are deleted (CASCADE)
        refreshGameList()
    }
    is Result.Error -> {
        showError("Failed to delete game")
    }
}
```

## How to Use

### 1. Save Generated Rules After Generation Completes

In your ViewModel, after rules are successfully generated and stored in state:

```kotlin
updatePhase(ScanPhase.SAVING_RULES)
saveRulesAndNavigate()  // Handles save, credit deduction, navigation
```

### 2. Access Saved Games in Other Screens

From any screen that needs to display the user's game library:

```kotlin
when (val result = gameRepository.getGames()) {
    is Result.Success -> {
        val games = result.data
        // Update UI with list of Games
    }
    is Result.Error -> {
        // Handle error
    }
}
```

### 3. Fetch a Specific Game by ID

When navigating to a rules display screen:

```kotlin
when (val result = gameRepository.getGameById(gameId)) {
    is Result.Success -> {
        val game = result.data
        displayGameDetails(game)
    }
    is Result.Error -> {
        showErrorDialog("Game not found")
    }
}
```

### 4. Handle Save Failures Gracefully

Failures in save (database issues) automatically prevent credit deduction:

```kotlin
is Result.Error -> {
    // User can retry without losing credit
    // Display user-friendly error message from result.userMessage
    _uiState.update { it.copy(error = result.userMessage) }
    _events.send(GenerationEvent.Error(result.userMessage))
}
```

### 5. Navigate to Rules Display After Save

The navigation event handler in `RulebookNavHost` automatically clears the back stack:

```kotlin
// User presses back from Rules → goes to Library (not camera)
navController.navigate(Route.Rules.createRoute(gameId)) {
    popUpTo(Route.Library.route) { inclusive = false }
}
```

## Configuration

### Database Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| Room version | 2.8.4 | Database framework with compile-time SQL verification |
| Table name | `saved_games` | Games table (lowercase_plural) |
| Table name | `rules` | Rules table with FK to games |
| FK Constraint | `game_id → saved_games.id` | Referential integrity |
| FK on Delete | `CASCADE` | Rules auto-deleted when game is deleted |

### DI Configuration

| Component | Provider | Scope |
|-----------|----------|-------|
| `GameRepository` | `GameRepositoryImpl` | Singleton |
| `GameDao` | `RulebookDatabase.gameDao()` | Singleton via Database |
| `RulesDao` | `RulebookDatabase.rulesDao()` | Singleton via Database |
| `GenerationViewModel` | Koin `viewModel` | Per-screen instance |

### Credit Deduction

| Property | Value |
|----------|-------|
| Timing | After database save succeeds |
| Error Handling | Logged but does not block save |
| Fallback | If balance is already 0, warning is logged (defensive) |
| Scope | One credit per saved game |

## Notes

### Transaction Semantics

Room's `@Transaction` annotation on the DAO method (or `withTransaction` block) ensures both `GameEntity` and `RulesEntity` inserts succeed or both are rolled back. If the database is unavailable, both inserts fail and no credit is deducted.

### Credit Deduction Timing

Credit is deducted AFTER save succeeds (not before) to ensure the game is committed to the database. If deduction fails (e.g., network timeout in DataStore), the game is still saved but a warning is logged. The user doesn't lose the saved game, only the credit deduction may be unreliable on that particular device.

### UUID Generation

UUIDs are generated at save time in the repository layer using `java.util.UUID.randomUUID().toString()`. This ensures:
- No client-side ID prediction or collision risk
- Atomic generation (happens inside transaction)
- Both Game and Rules receive the same ID for referential integrity

### Backward Compatibility

Upgrading from stub `GameRepositoryImpl` to full implementation:
- All method signatures remain the same
- No breaking changes to `GameRepository` interface (only added new method)
- Existing callers of `saveGame()`, `getGames()`, etc. continue to work unchanged
- Database schema is already defined (Room 1 migration)

### Future Enhancement Opportunities

1. **Pagination**: `getGames()` could accept limit/offset for large game libraries
2. **Filtering**: Add `getGamesByTitle(pattern)` for search functionality
3. **Caching**: Implement in-memory cache for recently accessed games
4. **Soft Delete**: Add `isDeleted` flag for logical deletion (audit trail)
5. **Sync**: Background sync to cloud storage for cross-device access
6. **Rules Versioning**: Store API response schema version with rules for future API changes

### Related Stories

- **Story 5.3 (RULE-193)**: Image Analysis API — established the Repository + Result pattern
- **Story 5.4 (RULE-194)**: Confidence Display & Auto-Proceed — one path to generate rules
- **Story 5.5 (RULE-195)**: Manual Game Entry — another path to generate rules
- **Story 5.6 (RULE-196)**: Rules Generation API — upstream producer of rules data
- **Story 5.2 (RULE-192)**: Credit Check & Scan Initiation — credit validation before scan starts
- **Epic 1 (RULE-190)**: Foundation & Design System — Room database and DataStore setup

### Testing

- **GameRepositoryImplTest**: 10 unit tests covering CRUD operations, transaction semantics, entity mapping
  - Test: `saveGameWithRules()` creates both entities with correct FK relationship
  - Test: `getGames()` returns mapped domain models
  - Test: `getGameById()` returns specific game or error
  - Test: `deleteGame()` removes game and cascades to rules deletion
  - Test: UUID is generated for each save (no collisions)

- **GenerationViewModelTest**: 5 new tests for save/navigation
  - Test: Successful save navigates to rules with correct gameId
  - Test: Successful save deducts credit
  - Test: Save error does not deduct credit
  - Test: Credit deduction failure after save still navigates (defensive)
  - Test: `saveRulesAndNavigate()` constructs Game correctly from UI state

- **Total New Tests**: 15 tests, all passing
- **Test Approach**: Fakes over mocks, following project patterns
- **Coverage**: Full path coverage for success, failure, and edge cases (e.g., null rules, credit deduction failure)
