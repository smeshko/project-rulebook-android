# Game Context Menu with Long-Press

**Date:** 2026-02-12
**Story:** RULE-220 - Story 7.5: Long-Press Context Menu
**Related Files:**
- `feature/library/src/main/kotlin/com/rulebook/feature/library/components/GameCardContextMenu.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryUiState.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryViewModel.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryScreen.kt`
- `core/common/src/main/kotlin/com/rulebook/core/common/HapticUtils.kt`

## Overview

Implemented long-press context menu for game cards in the Library screen. This feature replaces the previous direct delete behavior on long-press with a contextual menu offering multiple actions: "View Rules" and "Delete". The implementation demonstrates brutalist-styled dropdown menus, long-press gesture integration with haptic feedback, and nullable state management for UI elements. This pattern extends the state management approach established in Story 7.4 (delete confirmation dialog) and provides reusable patterns for context menus throughout the application.

## What Was Built

- **GameCardContextMenu Composable** - Brutalist `DropdownMenu` with "View Rules" and "Delete" actions
- **Context Menu State Management** - `contextMenuGame: Game?` in UiState following delete confirmation pattern
- **ViewModel Methods** - `showContextMenu(game: Game)` and `dismissContextMenu()` for state control
- **Long-Press Haptic Integration** - `performLongPressHaptic()` utility providing medium-intensity feedback
- **Gesture Integration** - Changed GameCard long-press from direct delete to context menu trigger
- **Comprehensive Unit Tests** - 6 test cases covering context menu state lifecycle and menu dismissal

## Technical Implementation

### Key Files

- **`GameCardContextMenu.kt`** (NEW) - Composable dropdown menu with brutalist styling (2dp border, 4dp shadow, zero corners)
- **`LibraryUiState.kt`** - Added `contextMenuGame: Game? = null` field for menu visibility state
- **`LibraryViewModel.kt`** - Added `showContextMenu(game: Game)` and `dismissContextMenu()` methods
- **`LibraryScreen.kt`** - Integrated context menu into game card layout, changed long-press behavior, wired callbacks
- **`HapticUtils.kt`** - Added `performLongPressHaptic(view: View)` using `HapticFeedbackConstants.LONG_PRESS`

### Key Patterns

#### 1. **Context Menu State Management Pattern**
Follows the same nullable state pattern as the delete confirmation dialog (Story 7.4):

```kotlin
// In UiState
data class LibraryUiState(
    val deleteConfirmation: Game? = null,    // From Story 7.4
    val contextMenuGame: Game? = null         // New - same pattern
)

// In ViewModel
fun showContextMenu(game: Game) {
    _uiState.update { it.copy(contextMenuGame = game) }
}

fun dismissContextMenu() {
    _uiState.update { it.copy(contextMenuGame = null) }
}
```

**Why this pattern:** Null = menu hidden, non-null Game = menu visible and anchored to that card. Reusable for any UI element needing temporary state (menus, tooltips, popovers).

#### 2. **Brutalist Styled Dropdown Menu**
Uses Material 3 `DropdownMenu` with brutalist design system modifiers:

```kotlin
DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismiss,
    modifier = modifier
        .background(color = RulebookTheme.colors.surfacePrimary, shape = RectangleShape)
        .brutalistShadow(offset = RulebookTheme.spacing.shadowOffset)
        .brutalistBorder(width = RulebookTheme.spacing.borderWidth)
)
```

**Key details:**
- Background: `surfacePrimary` (respects light/dark theme)
- Border: 2dp solid black via `brutalistBorder()`
- Shadow: 4dp offset via `brutalistShadow()`
- Shape: `RectangleShape` (0dp corners) for brutalist aesthetic
- Items: 8dp vertical, 16dp horizontal padding via `DropdownMenuItem` defaults

#### 3. **Long-Press Gesture with Haptic Feedback**
Changed GameCard's long-press behavior and integrated haptic feedback:

```kotlin
// In LibraryScreen composable
Box {
    GameCard(
        game = game,
        onClick = { onNavigateToRules(game.id) },
        onLongClick = {
            val view = LocalView.current
            view.performHapticFeedback(HapticFeedbackType.LongPress)
            onShowContextMenu(game)
        }
    )
    GameCardContextMenu(
        expanded = contextMenuGame?.id == game.id,
        onDismiss = { onDismissContextMenu() },
        onViewRules = { onNavigateToRules(game.id) },
        onDelete = { onRequestDelete(game) }
    )
}
```

**Haptic feedback timing:** Fires immediately in `onLongClick` before state update for snappy user feedback.

#### 4. **Menu Dismissal Pattern**
Menu dismisses in two ways:
- **Outside tap** - `DropdownMenu.onDismissRequest` called automatically by Material 3
- **Item selection** - Each menu item calls its action then `onDismiss()` to clear menu

```kotlin
DropdownMenuItem(
    onClick = {
        onViewRules()      // Execute action first
        onDismiss()         // Then dismiss menu
    }
)
```

### Code Examples

**Basic Usage:**
```kotlin
// In a composable
var contextMenuGame by remember { mutableStateOf<Game?>(null) }

Box {
    GameCard(
        onLongClick = {
            val view = LocalView.current
            view.performHapticFeedback(HapticFeedbackType.LongPress)
            contextMenuGame = game
        }
    )
    GameCardContextMenu(
        expanded = contextMenuGame?.id == game.id,
        onDismiss = { contextMenuGame = null },
        onViewRules = { /* navigate */ },
        onDelete = { /* delete */ }
    )
}
```

**With ViewModel (Library implementation):**
```kotlin
// In LibraryScreen
LibraryScreenContent(
    contextMenuGame = uiState.contextMenuGame,
    onShowContextMenu = { viewModel.showContextMenu(it) },
    onDismissContextMenu = { viewModel.dismissContextMenu() }
)
```

## How to Use

### Adding a Context Menu to a Game Card

1. **Add state to UiState** - If not using ViewModel, use local `remember` state
2. **Create menu callbacks** - Define `onViewRules`, `onDelete` handlers
3. **Wrap card in Box** - Allows positioning menu alongside the card
4. **Call `onLongClick`** - Update state and fire haptic feedback
5. **Show `GameCardContextMenu`** - Expanded when `contextMenuGame?.id == currentCard.id`
6. **Wire menu actions** - Each item calls its action + dismisses menu

### Haptic Feedback Integration

Use `HapticUtils.performLongPressHaptic()` for consistent long-press feedback:

```kotlin
val view = LocalView.current
HapticUtils.performLongPressHaptic(view)
```

Or directly via Compose:
```kotlin
val haptic = LocalHapticFeedback.current
haptic.performHapticFeedback(HapticFeedbackType.LongPress)
```

### Testing Context Menu State

Use the ViewModel test pattern from LibraryViewModelTest:

```kotlin
@Test
fun `showContextMenu sets contextMenuGame in state`() = runTest(testDispatcher) {
    val game = Game("1", "Catan", null, 1000L, 2000L)
    val viewModel = LibraryViewModel(repository, preferences)

    viewModel.showContextMenu(game)
    advanceUntilIdle()

    assertEquals(game, viewModel.uiState.value.contextMenuGame)
}
```

## Configuration

### Theme Integration

The context menu respects the Rulebook design system:

| Property | Token | Default |
|----------|-------|---------|
| Background | `surfacePrimary` | Theme-aware surface color |
| Text Color | `contentPrimary` | Theme-aware content color |
| Border Width | `2dp` | Via `brutalistBorder()` |
| Shadow Offset | `4dp` | Via `brutalistShadow()` |
| Delete Text Color | `red` | `#E74C3C` (light) / `#EC7063` (dark) |
| Corner Radius | `0dp` | `RectangleShape` |

### Menu Positioning

`DropdownMenu` automatically positions relative to its parent `Box`. The `offset` parameter can adjust positioning:

```kotlin
DropdownMenu(
    offset = DpOffset(
        x = RulebookTheme.spacing.xs,  // Small horizontal offset
        y = RulebookTheme.spacing.xs   // Small vertical offset
    )
)
```

## Notes

### Design System Consistency
- Menu styling follows brutalist design principles: solid borders, sharp corners, no rounded edges
- Color scheme respects light/dark theme modes via `RulebookTheme.colors`
- Spacing tokens ensure alignment with card and overall layout

### Behavioral Changes
**This feature replaces Story 7.4 behavior:**
- **Before:** Long-press on card → Delete confirmation dialog
- **After:** Long-press on card → Context menu (with "View Rules" or "Delete" options)

The delete confirmation dialog is still triggered, but now through the context menu's "Delete" action rather than directly on long-press.

### Performance Considerations
- Context menu state is managed in ViewModel (reactive) rather than local Compose state
- Only one menu can be shown at a time (anchored to the card with matching ID)
- Menu dismisses on outside tap automatically via Material 3's `DropdownMenu.onDismissRequest`

### Testing Notes
- 6 unit tests validate context menu state lifecycle
- Tests focus on ViewModel state transitions, not Compose UI rendering
- No Compose UI tests added - Material 3 `DropdownMenu` behavior is well-tested upstream

### Future Considerations
- Additional menu items can be added to `GameCardContextMenu` following the same pattern
- Similar context menu pattern can be applied to other game list views (search results, favorites)
- Menu actions are decoupled via callbacks, allowing reuse in different contexts

### Haptic Feedback Details
- Uses `HapticFeedbackConstants.LONG_PRESS` for medium-intensity feedback
- Automatically respects system haptic settings (no manual Settings check needed)
- Feedback fires immediately on `onLongClick` for snappy UX

---

## Related Features

- **Story 7.4 (RULE-219):** Delete Game with Confirmation Dialog - Established the nullable state pattern and event-driven architecture
- **Story 7.1 (RULE-216):** Game Grid Display - GameCard component used here with enhanced long-press behavior
- **Story 7.3 (RULE-218):** Library Navigation - View Rules navigation callback used in context menu

