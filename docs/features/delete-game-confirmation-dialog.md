# Delete Game with Confirmation Dialog

**Date:** 2026-02-11
**Story:** RULE-219 - Story 7.4: Delete Game with Confirmation
**Related Files:**
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryEvent.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryViewModel.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryUiState.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/components/DeleteConfirmationDialog.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryScreen.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/components/GameCard.kt`

## Overview

Implemented comprehensive delete functionality for games in the Library screen. This story introduces three critical patterns: (1) a one-time event system using `Channel` for emitting snackbar notifications, (2) long-press gesture detection for triggering delete, and (3) brutalist-styled confirmation dialogs. The implementation demonstrates optimistic UI patterns with Room's cascading deletes and establishes reusable patterns for future destructive operations (Story 7.5 context menu).

## What Was Built

- **LibraryEvent Sealed Class** - One-time event pattern for snackbar messages, following GenerationEvent model
- **Long-Press Gesture Support** - `combinedClickable` modifier on GameCard for long-press delete trigger
- **DeleteConfirmationDialog Component** - Brutalist Material 3 AlertDialog for confirming destructive operations
- **Delete Logic in ViewModel** - Three new functions: `requestDelete()`, `cancelDelete()`, `confirmDelete()`
- **Snackbar Integration** - First use of `SnackbarHost` in Library screen with event-driven feedback
- **Comprehensive Unit Tests** - 5 new test cases covering all delete scenarios including error handling

## Technical Implementation

### Key Files

- **`LibraryEvent.kt`** (NEW) - Sealed class with `ShowSnackbar` event; uses Channel pattern matching GenerationEvent
- **`DeleteConfirmationDialog.kt`** (NEW) - Composable component with brutalist styling (3dp border, 8dp shadow, zero corners)
- **`LibraryViewModel.kt`** - Added `_events` Channel field, `requestDelete()`, `cancelDelete()`, `confirmDelete()` methods; manages delete state and triggers repository operations
- **`LibraryUiState.kt`** - Added `deleteConfirmation: Game? = null` property for dialog visibility
- **`LibraryScreen.kt`** - Added `SnackbarHostState`, `LaunchedEffect` for event collection, dialog rendering, and snackbar display
- **`GameCard.kt`** - Added `onLongClick` parameter with `Modifier.combinedClickable()`

### Key Patterns

#### 1. **Channel-Based One-Time Events Pattern**
The library feature now emits one-time events using a buffered Channel, matching the GenerationViewModel pattern:

```kotlin
// In ViewModel
private val _events = Channel<LibraryEvent>(Channel.BUFFERED)
val events: Flow<LibraryEvent> = _events.receiveAsFlow()

// In composable
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is LibraryEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
            }
        }
    }
}
```

**Why important:** One-time events should not be stored in StateFlow (they'd repeat on recomposition). Channel + `receiveAsFlow()` ensures each event is consumed exactly once, ideal for notifications and confirmations.

#### 2. **Gesture Detection: Long-Press for Delete**
GameCard uses `combinedClickable` to support both tap and long-press:

```kotlin
@OptIn(ExperimentalFoundationApi::class)
Modifier.combinedClickable(
    onClick = onClick,
    onLongClick = onLongClick
)
```

**Why important:** `combinedClickable` is the Compose-native way to handle multiple click behaviors. Tap navigates to rules, long-press triggers delete. This establishes the pattern for Story 7.5 (long-press context menu).

#### 3. **Brutalist Dialog Styling**
DeleteConfirmationDialog demonstrates Rulebook's brutalist design applied to Material 3 components:

```kotlin
AlertDialog(
    shape = RectangleShape,  // 0dp corners
    containerColor = MaterialTheme.colorScheme.surface,
    modifier = Modifier
        .brutalistShadow(offset = RulebookTheme.spacing.shadowOffsetMedium)  // 8dp offset
        .brutalistBorder()  // 3dp solid black border
)
```

**Why important:** Material 3 components are flexible. Rulebook enforces brand consistency through custom modifiers while leveraging Material for accessibility and standard behavior.

#### 4. **Optimistic UI with Reactive Rollback**
Delete implementation clears dialog immediately before calling repository:

```kotlin
fun confirmDelete() {
    viewModelScope.launch {
        val game = _uiState.value.deleteConfirmation ?: return@launch

        // Optimistic: clear dialog immediately
        _uiState.update { it.copy(deleteConfirmation = null) }

        // Then delete
        when (gameRepository.deleteGame(game.id)) {
            is Result.Success -> _events.send(LibraryEvent.ShowSnackbar("Game deleted"))
            is Result.Error -> _events.send(LibraryEvent.ShowSnackbar("Failed to delete game"))
        }
    }
}
```

The game disappears from the list automatically because `gameRepository.getGamesSorted()` returns a `Flow<List<Game>>` that re-emits after the Room delete. No manual list filtering needed.

**Why important:** Room's reactive Flows eliminate manual state management for optimistic UI. The list naturally updates when the database changes, providing seamless UX with automatic rollback if needed.

#### 5. **Snackbar Via Event Flow**
Library screen establishes snackbar pattern using SnackbarHostState:

```kotlin
// In Scaffold
Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) })

// Collect events
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is LibraryEvent.ShowSnackbar -> {
                snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
}
```

**Why important:** SnackbarHostState auto-queues messages when called rapidly. Events ensure snackbars display without storage in StateFlow. This pattern will extend to all one-time notifications (copy success, validation errors, etc.).

### Code Examples

**Long-Press Delete Trigger:**
```kotlin
GameCard(
    game = game,
    onClick = { onNavigateToRules(game.id) },
    onLongClick = { onRequestDelete(game) }  // Long-press handler
)
```

**Delete Confirmation Request:**
```kotlin
fun requestDelete(game: Game) {
    _uiState.update { it.copy(deleteConfirmation = game) }
}
```

**Handling Delete with Event Emission:**
```kotlin
fun confirmDelete() {
    viewModelScope.launch {
        val game = _uiState.value.deleteConfirmation ?: return@launch
        _uiState.update { it.copy(deleteConfirmation = null) }

        when (gameRepository.deleteGame(game.id)) {
            is Result.Success -> _events.send(LibraryEvent.ShowSnackbar("Game deleted"))
            is Result.Error -> _events.send(LibraryEvent.ShowSnackbar("Failed to delete game"))
        }
    }
}
```

## How to Use

### Triggering Delete

1. User long-presses a game card in the library grid
2. ViewModel's `requestDelete(game)` is called, setting `deleteConfirmation` state
3. DeleteConfirmationDialog appears with "Delete [GameName]?" title

### Confirming Deletion

1. User taps the "Delete" button
2. ViewModel's `confirmDelete()` is called
3. Dialog closes immediately (optimistic UI)
4. Game is deleted from repository (Room CASCADE handles rule deletion)
5. Library grid updates reactively
6. Snackbar shows "Game deleted"

### Handling Cancellation

1. User taps "Cancel" or outside dialog
2. ViewModel's `cancelDelete()` is called
3. Dialog closes without deleting
4. Game remains in library

## Configuration

### Event Types

| Event | Message | Trigger | Duration |
|-------|---------|---------|----------|
| ShowSnackbar | "Game deleted" | Delete succeeds | Short (~4s) |
| ShowSnackbar | "Failed to delete game" | Delete fails | Short (~4s) |

### Dialog Styling

| Property | Value | Reference |
|----------|-------|-----------|
| Title Style | `brutalistTitle` (24sp, Black 900) | RulebookTheme.typography |
| Body Style | `body` (17sp, Regular) | RulebookTheme.typography |
| Border Width | 3dp solid black | brutalistBorder() modifier |
| Shadow Offset | 8dp | RulebookTheme.spacing.shadowOffsetMedium |
| Corner Radius | 0dp (RectangleShape) | Brutalist spec |
| Button Variant | Destructive (red #E74C3C) | RulebookButton |

## Notes

### Pattern Reuse

- **Long-Press Pattern:** Extends to Story 7.5 (context menu) and any future gesture-based features
- **Event Channel Pattern:** Foundation for all one-time notifications (copy success, validation errors, network status)
- **Dialog Pattern:** Reusable for any confirmations (logout, reset, bulk delete)
- **Snackbar Pattern:** Extends to all features needing transient feedback

### Database Behavior

- Room CASCADE foreign key on `RulesEntity.gameId` automatically deletes associated rules
- Delete is idempotent — deleting non-existent game returns success (per repository implementation)
- No manual cleanup required; reactive Flow handles list updates

### Known Limitations

- Snackbar duration is `SnackbarDuration.Short` (~4s), not exact 2s as specified in UX (Material 3 limitation)
- Long-press may conflict with text selection on some devices (not applicable for this UI)
- Snackbar only shows one message at a time (auto-queues if rapid deletes occur)

### Future Considerations

- Story 7.5 will extend long-press with context menu instead of direct delete
- Additional event types will be added to LibraryEvent (e.g., `ConfirmationRequired`, `NavigationEvent`)
- Snackbar pattern may migrate to app-level Scaffold for consistency across all screens
