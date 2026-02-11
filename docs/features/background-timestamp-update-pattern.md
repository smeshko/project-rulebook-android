---
title: Background Timestamp Update Pattern
description: Fire-and-forget coroutine pattern for non-blocking background operations in ViewModels
---

# Background Timestamp Update Pattern

**Date:** 2026-02-11
**Related Files:**
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesViewModel.kt`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/GameRepository.kt`
- `core/database/src/main/kotlin/com/rulebook/core/database/GameDao.kt`

## Overview

This pattern implements non-blocking background updates in ViewModels using `viewModelScope.launch` as a fire-and-forget coroutine. The timestamp update runs independently from the main loading operation, ensuring UI responsiveness while tracking user interactions in the database.

## What Was Built

- **Background Timestamp Update**: Updates game's `lastAccessedAt` when rules load successfully
- **Fire-and-Forget Pattern**: Launches independent coroutine that doesn't block UI or affect loading state
- **Defensive Error Handling**: Updates only execute on successful data load, preventing database operations during error states
- **Silent Failure**: Timestamp update failures don't affect UI state or propagate errors to the user
- **DAO Query**: Targeted Room `@Query` with raw SQL UPDATE for efficiency

## Technical Implementation

### Key Files

- **`RulesViewModel.kt`** (lines 109-112): Launches fire-and-forget coroutine after successful rules load
- **`GameRepository.kt`** (interface): Defines `suspend fun updateLastAccessed(gameId: String): Result<Unit>`
- **`GameRepositoryImpl.kt`**: Implements via `safeCall` wrapper and DAO query
- **`GameDao.kt`**: Room `@Query("UPDATE saved_games SET last_accessed_at = :timestamp WHERE id = :id")`

### Key Patterns

#### Pattern 1: Fire-and-Forget for Non-Critical Operations

**When to use:**
- Operations that must complete but don't block the main flow
- Non-critical updates (logging, analytics, timestamps)
- Operations where failure shouldn't affect user experience

**Implementation:**
```kotlin
viewModelScope.launch {
    repository.updateLastAccessed(gameId)
}
```

**Why this pattern:**
- `viewModelScope` automatically cancels when ViewModel is cleared (no memory leaks)
- Launches on `Dispatchers.Main.immediate` by default
- Repository's suspend function internally switches to `Dispatchers.IO` for database access
- Error handling is optional (errors logged to logcat but don't crash the app)

#### Pattern 2: Defensive Conditional Updates

**When to use:**
- State-changing operations that should only proceed in specific states
- Operations that depend on prior success (guard clauses)
- Updates that should never happen during error states

**Implementation:**
```kotlin
is Result.Success -> {
    _uiState.update { /* update UI */ }

    // Only launch background update after BOTH game AND rules load successfully
    viewModelScope.launch {
        gameRepository.updateLastAccessed(gameId)
    }
}
is Result.Error -> {
    // Timestamp update is NOT called here
    _uiState.update { /* error state */ }
}
```

**Why this pattern:**
- Updates only execute when preconditions are met
- Prevents side effects during error handling
- Makes the logic self-documenting through structure

#### Pattern 3: Targeted SQL UPDATE for Single-Field Changes

**When to use:**
- Updating single fields without modifying the entity
- Efficient database operations on large datasets
- Avoiding unnecessary read-modify-write cycles

**Implementation:**
```kotlin
@Query("UPDATE saved_games SET last_accessed_at = :timestamp WHERE id = :id")
suspend fun updateLastAccessedAt(id: String, timestamp: Long)
```

**Why this pattern:**
- Single database operation (not read + write)
- More efficient than full entity update
- Room generates optimized SQL
- Useful for audit fields like timestamps

### Code Examples

#### Basic Usage

```kotlin
// In RulesViewModel when rules load successfully
is Result.Success -> {
    _uiState.update {
        it.copy(
            rules = rulesResult.data,
            isLoading = false,
            error = null
        )
    }

    // Fire-and-forget timestamp update
    viewModelScope.launch {
        gameRepository.updateLastAccessed(gameId)
    }
}
```

#### Repository Implementation

```kotlin
// In GameRepositoryImpl
override suspend fun updateLastAccessed(gameId: String): Result<Unit> {
    return safeCall {
        gameDao.updateLastAccessedAt(
            gameId,
            System.currentTimeMillis()
        )
    }
}
```

#### DAO Query

```kotlin
// In GameDao
@Query("UPDATE saved_games SET last_accessed_at = :timestamp WHERE id = :id")
suspend fun updateLastAccessedAt(id: String, timestamp: Long)
```

## How to Use

When you need to implement a background operation in your ViewModel that doesn't block the UI:

1. **Define the operation in Repository:**
   - Create a suspend function that returns `Result<Unit>`
   - Wrap database/network calls in `safeCall {}`

2. **Call from ViewModel after preconditions met:**
   - Launch in `viewModelScope.launch { }`
   - Do NOT assign return value or await the result
   - Do NOT catch exceptions (errors logged automatically)

3. **Avoid these mistakes:**
   - DON'T use `runBlocking` or blocking waits
   - DON'T call from initialization (use after preconditions)
   - DON'T handle the Result (let it fail silently)
   - DON'T use different scope (use `viewModelScope` for lifecycle management)

## Configuration

### Scope Options

| Scope | When to Use | Auto-Cancel | Thread |
|-------|-----------|-----------|--------|
| `viewModelScope.launch` | Most common, UI-adjacent | Yes (on clear) | Main |
| `lifecycleScope.launch` | Fragment-scoped operations | Yes (on destroy) | Main |
| `viewModelScope.launch(Dispatchers.IO)` | Heavy database ops | Yes (on clear) | IO thread |

For timestamp updates: `viewModelScope.launch` is sufficient because Room suspends to IO internally.

## Notes

### Why Silent Failure?

Timestamp updates are non-critical:
- User sees the content regardless of timestamp update success
- Sorting will reflect the last successful timestamp
- No need to notify user of this background operation

### Thread Safety

- Room suspend functions automatically switch to `Dispatchers.IO`
- `viewModelScope.launch` runs on `Dispatchers.Main.immediate`
- No explicit dispatcher switching needed
- CoroutineExceptionHandler catches unhandled exceptions and logs to logcat

### Performance Considerations

- Single SQL UPDATE operation is faster than full entity cycle
- `System.currentTimeMillis()` is called when update launches (not when it completes)
- Timestamp represents approximate access time, not exact moment
- Fire-and-forget pattern doesn't block the main UI thread

### Testing the Pattern

Use `FakeGameRepository` with call tracking to verify behavior:

```kotlin
@Test
fun `updates lastAccessedAt when rules load successfully`() = runTest {
    val repository = FakeGameRepository(
        gameResult = Result.Success(testGame),
        rulesResult = Result.Success(testRules)
    )
    val viewModel = RulesViewModel("game-1", repository)

    // Verify the call was made
    assertEquals(1, repository.updateLastAccessedCallCount)
    assertEquals("game-1", repository.lastUpdateLastAccessedGameId)
}

@Test
fun `timestamp update failure does not affect UI state`() = runTest {
    val repository = FakeGameRepository(
        gameResult = Result.Success(testGame),
        rulesResult = Result.Success(testRules),
        updateLastAccessedResult = Result.Error("Database error")
    )
    val viewModel = RulesViewModel("game-1", repository)

    // UI state is unaffected despite timestamp update error
    val state = viewModel.uiState.value
    assertTrue(state.isSuccess)
    assertNull(state.error)
}
```

### Future Extensions

This pattern can be extended for:
- Analytics tracking (fire-and-forget events)
- Cache invalidation (background refresh)
- Sync operations (background data upload)
- Cleanup operations (background resource management)

The key is ensuring the background operation never blocks the main flow or affects user-visible state.
