# Story 1.13: Result Wrapper & Error Handling

Status: done

## Linear Issue

- **ID:** RULE-120
- **URL:** https://linear.app/project-rulebook/issue/RULE-120/story-113-result-wrapper-and-error-handling

## Story

As a developer,
I want the Result wrapper pattern implemented,
So that errors are handled consistently across the app.

## Acceptance Criteria

1. **Given** the `core/common` module
   **When** Result wrapper is implemented
   **Then** sealed class exists:

   ```kotlin
   sealed class Result<out T> {
       data class Success<T>(val data: T) : Result<T>()
       data class Error(
           val message: String,
           val cause: Throwable? = null
       ) : Result<Nothing>()
   }
   ```

2. **And** extension functions exist:
   - `Result.map()` - Transform success data
   - `Result.onSuccess()` - Execute on success
   - `Result.onError()` - Execute on error
   - `Result.getOrNull()` - Get data or null

3. **And** all repository operations return `Result<T>`

## Tasks / Subtasks

- [x] Task 1: Create Result sealed class (AC: #1)
  - [x] Create sealed class Result<out T>
  - [x] Create Success data class with data: T
  - [x] Create Error data class with message and cause
- [x] Task 2: Create map() extension (AC: #2)
  - [x] Transform Success data while preserving Error
  - [x] Return new Result<R>
- [x] Task 3: Create onSuccess() extension (AC: #2)
  - [x] Execute action only on Success
  - [x] Return original Result for chaining
- [x] Task 4: Create onError() extension (AC: #2)
  - [x] Execute action only on Error
  - [x] Return original Result for chaining
- [x] Task 5: Create getOrNull() extension (AC: #2)
  - [x] Return data if Success
  - [x] Return null if Error
- [x] Task 6: Create additional utility extensions
  - [x] Create getOrDefault(default: T)
  - [x] Create getOrElse(onError: (Error) -> T)
  - [x] Create fold(onSuccess, onError)
- [x] Task 7: Create runCatching wrapper
  - [x] Create suspend fun safeCall<T>(block) -> Result<T>
  - [x] Catch exceptions and wrap in Result.Error
  - [x] Return Success on successful execution

## Dev Notes

### Implementation Pattern

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>()
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Result.Error) -> Unit): Result<T> {
    if (this is Result.Error) action(this)
    return this
}

fun <T> Result<T>.getOrNull(): T? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

fun <T> Result<T>.getOrDefault(default: T): T =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    } ?: default

inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onError: (Result.Error) -> R
): R = when (this) {
    is Result.Success -> onSuccess(data)
    is Result.Error -> onError(this)
}

suspend fun <T> safeCall(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(
            message = e.localizedMessage ?: "Unknown error",
            cause = e
        )
    }
```

### Usage Examples

```kotlin
// Repository operation
class GameRepository(private val dao: GameDao) {
    suspend fun getGame(id: String): Result<Game> = safeCall {
        dao.getById(id)?.toDomain()
            ?: throw NotFoundException("Game not found")
    }
}

// ViewModel usage
viewModelScope.launch {
    repository.getGame(gameId)
        .onSuccess { game ->
            _uiState.update { it.copy(game = game) }
        }
        .onError { error ->
            _uiState.update { it.copy(errorMessage = error.message) }
        }
}

// Chained operations
repository.getGame(gameId)
    .map { game -> game.title }
    .getOrDefault("Unknown")
```

### Error Message Guidelines

- User-friendly error messages
- Technical details in cause Throwable
- Consistent message patterns across app

| Error Type | Message Pattern |
|------------|-----------------|
| Network | "Unable to connect. Please check your connection." |
| Not Found | "Game not found." |
| API Error | "Something went wrong. Please try again." |
| Parse Error | "Unable to process response." |

### Project Structure Notes

- Result is in core/common
- All repository operations return Result<T>
- safeCall wrapper for try-catch boilerplate

### References

- [Source: docs/architecture.md#Error Handling]
- [Source: docs/architecture.md#State Management]
- [Source: docs/prd.md#NFR10] - Graceful degradation on network failure

## Dev Agent Record

### Context Reference
- Architecture: docs/architecture.md (Error Handling pattern, Result<T> specification)

### Agent Model Used
- Claude Opus 4.5

### Debug Log References

### Completion Notes List
- **Task 1 (2025-12-15):** Created Result sealed class in core/common module. Removed pre-existing Loading state to match architecture spec. Result now has only Success<T> and Error subtypes as required. Added 6 unit tests covering: Success data holding, Error message/cause handling, null cause support, and sealed class exhaustiveness.
- **Task 2 (2025-12-15):** Created map() extension function. Transforms Success data while preserving Error. Added 4 unit tests for transformation, error preservation, type changes, and cause preservation.
- **Task 3 (2025-12-15):** Created onSuccess() extension function. Executes action only on Success and returns original Result for chaining. Added 4 unit tests.
- **Task 4 (2025-12-15):** Created onError() extension function. Executes action only on Error and returns original Result for chaining. Added 5 unit tests including cause handling.
- **Task 5 (2025-12-15):** Created getOrNull() extension function. Returns data on Success, null on Error. Added 3 unit tests including nullable type handling.
- **Task 6 (2025-12-15):** Created getOrDefault(), getOrElse(), and fold() utility extensions. Added 7 unit tests covering all utility functions.
- **Task 7 (2025-12-15):** Created safeCall() suspend function wrapper. Catches exceptions and wraps in Result.Error with message and cause. Added 4 unit tests including suspend function support.

### File List
- core/common/src/main/kotlin/com/rulebook/core/common/Result.kt (modified)
- core/common/src/test/kotlin/com/rulebook/core/common/ResultTest.kt (created)
- core/common/build.gradle.kts (modified - added test dependencies)

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.4, Story 1.5, Story 1.6, Story 1.7, Story 1.12

### Dependency Rationale
- Story 1.1: Result wrapper requires common module to exist
