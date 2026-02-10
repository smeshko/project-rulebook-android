# Setup Section Interactive Checklist Pattern

**Date:** 2026-02-10
**Related Files:**
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/SetupChecklistItem.kt`
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesViewModel.kt`
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesUiState.kt`
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/CollapsibleRuleSection.kt`
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesScreen.kt`

## Overview

This document describes the interactive checklist pattern implemented in the Setup section of the Rules screen. Users can mark setup steps as complete using Material 3 Checkboxes, with visual feedback including strikethrough text and reduced opacity. The checklist state is managed in the ViewModel as session-local state (not persisted to database), persisting during section collapse/expand cycles but resetting on navigation away from the Rules screen.

This pattern is designed to be reusable for other rule sections (First Round, Advanced) and demonstrates best practices for session-local user interaction state in Jetpack Compose.

## What Was Built

- **SetupChecklistItem Composable**: A checkbox-based list item component for displaying numbered setup steps
- **Checklist State Management**: Session-local Set<Int> tracking of checked item indices in RulesViewModel
- **Backward-Compatible Extension**: CollapsibleRuleSection extended with optional checklist rendering mode
- **Visual Feedback System**: Strikethrough text, reduced opacity, and custom checkbox colors for checked/unchecked states
- **Composable Hierarchy Integration**: Checklist state wired through RulesScreen → RulesScreenContent → RulesSuccessState → CollapsibleRuleSection

## Technical Implementation

### Key Files

- **SetupChecklistItem.kt**: The core checklist item composable. Renders a Material 3 Checkbox paired with step number and text. Uses RulebookTheme colors for consistency.

- **RulesViewModel.kt**: Contains `toggleSetupItem(index: Int)` function that adds/removes indices from the `setupCheckedItems` Set. Uses the `_uiState.update { }` pattern for immutable state updates.

- **RulesUiState.kt**: Data class with `setupCheckedItems: Set<Int> = emptySet()` field. Tracks which setup steps are checked during the current session.

- **CollapsibleRuleSection.kt**: Extended with optional `checkedItems: Set<Int>?` and `onItemToggle: ((Int) -> Unit)?` parameters. Conditionally renders items as SetupChecklistItem when checklist mode is enabled, or as bullet points when disabled (backward compatible).

- **RulesScreen.kt**: Wired checklist parameters through the composable hierarchy: passes `viewModel::toggleSetupItem` and `uiState.setupCheckedItems` to RulesSuccessState, which passes them only to the Setup section's CollapsibleRuleSection. Other sections (Overview, First Round, Advanced) remain unchanged.

### Key Patterns

- **Reusable Checklist Component**: SetupChecklistItem encapsulates the visual appearance and interaction of a single checklist item. Accepts `stepNumber`, `text`, `isChecked`, and `onToggle` callback. Can be used in any section requiring a checkbox-based list.

- **Session-Local State in ViewModel**: Using Set<Int> in StateFlow for tracking indices of checked items. Not persisted to Room database; resets when user navigates away from Rules screen. This pattern is suitable for temporary user interaction state that aids navigation/completion tracking.

- **Backward-Compatible Component Extension**: CollapsibleRuleSection now supports two rendering modes via optional parameters. When `checkedItems` is null (the default), items render as bullet points (existing behavior). When `checkedItems` is provided, items render as SetupChecklistItem (new behavior). This allows existing sections to continue working unchanged.

- **Material 3 Checkbox with Custom Colors**: Using `CheckboxDefaults.colors()` to override checkbox appearance. The implementation uses `RulebookTheme.colors.blue` for the checked state (matching section accent) and `RulebookTheme.colors.green` for the checkmark (success indicator). This pattern can be applied to other form elements requiring themed checkboxes.

- **Text Decoration with Reduced Alpha**: When an item is checked, the text receives `TextDecoration.LineThrough` styling and `alpha(0.6f)` modifier. This combination provides clear visual feedback without removing the text (useful for reference). The pattern is first usage of LineThrough in the codebase and demonstrates how to layer text effects.

- **State Management via Immutable Updates**: Using `_uiState.update { currentState -> ... currentState.copy(...) }` pattern for thread-safe state mutations. The toggleSetupItem function creates a new Set using Kotlin's set arithmetic (`+ index` to add, `- index` to remove).

### Code Examples

**Using SetupChecklistItem in a custom list:**
```kotlin
SetupChecklistItem(
    stepNumber = 1,
    text = "Shuffle the terrain hexes",
    isChecked = false,
    onToggle = { viewModel.toggleSetupItem(0) },
    modifier = Modifier.padding(RulebookTheme.spacing.xs)
)
```

**Toggling state in ViewModel:**
```kotlin
fun toggleSetupItem(index: Int) {
    _uiState.update { currentState ->
        val newCheckedItems = if (index in currentState.setupCheckedItems) {
            currentState.setupCheckedItems - index
        } else {
            currentState.setupCheckedItems + index
        }
        currentState.copy(setupCheckedItems = newCheckedItems)
    }
}
```

**Using CollapsibleRuleSection with checklist mode:**
```kotlin
CollapsibleRuleSection(
    title = "Setup",
    content = "Prepare the game...",
    items = rules.setup,
    accentColor = RulebookTheme.colors.blue,
    isExpanded = expandedSections["setup"] ?: false,
    onToggle = { expandedSections["setup"] = !(expandedSections["setup"] ?: false) },
    checkedItems = setupCheckedItems,
    onItemToggle = onSetupItemToggle
)
```

## How to Use

### For End Users
1. Navigate to the Rules screen for any game
2. Locate the "Setup" section (collapsed by default)
3. Click the section header to expand it
4. View the numbered setup steps
5. Tap the checkbox next to each step to mark it as complete
6. Checked items display with strikethrough text and reduced opacity for visual feedback
7. Collapse and re-expand the section—checklist state persists during the session
8. Navigate away from the Rules screen—checklist state resets when you return

### For Developers (Extending to Other Sections)

To add checklist functionality to another section (e.g., "First Round"):

1. Create a new ViewModel state field for that section:
   ```kotlin
   val firstRoundCheckedItems: Set<Int> = emptySet()
   ```

2. Create a toggle function:
   ```kotlin
   fun toggleFirstRoundItem(index: Int) { /* same pattern as toggleSetupItem */ }
   ```

3. Pass state and callback through the composable hierarchy to that section's CollapsibleRuleSection:
   ```kotlin
   CollapsibleRuleSection(
       title = "First Round",
       // ...
       checkedItems = firstRoundCheckedItems,
       onItemToggle = onFirstRoundItemToggle
   )
   ```

4. No changes to CollapsibleRuleSection needed—it already supports the pattern.

## Configuration

No configuration options or environment variables required. All behavior is hardcoded in the implementation:

| Setting | Value | Notes |
|---------|-------|-------|
| Checked color | `RulebookTheme.colors.blue` | Matches Setup section accent (#3498DB light, #5DADE2 dark) |
| Checkmark color | `RulebookTheme.colors.green` | Success indicator (#2ECC71 light, #58D68D dark) |
| Checked text opacity | 0.6f | Reduces visual prominence of completed items |
| Strikethrough text | `TextDecoration.LineThrough` | Applied when checked |
| Step numbering | 1-indexed | "1. ", "2. ", etc. |
| State persistence | Session-local | Resets on navigation away from Rules screen |

## Notes

### Why Session-Local and Not Persisted?

The checklist state is intentionally not persisted to the database (Room). This is a deliberate product decision based on these considerations:

- **Checkboxes are guidance tools**: They help users track their progress *during a single session* while they're actively setting up the game
- **Not part of game state**: The completed setup steps don't define the game; they're just markers for the current player
- **Reduced database overhead**: Avoids creating a new table or modifying the Game model for ephemeral state
- **Clean UX**: Users see a "fresh" checklist each time they view rules, encouraging them to re-read key steps

If future requirements change (e.g., "persist setup completion across sessions"), the pattern can be extended by:
1. Adding `setupCheckedItems: Set<Int>` to the Game model
2. Saving to database in the repository layer
3. Loading from database in `loadRules()`

### Backward Compatibility

The implementation maintains 100% backward compatibility with existing sections:

- **Overview section**: Uses bullet points (no `checkedItems` parameter passed)
- **First Round section**: Uses bullet points (no `checkedItems` parameter passed)
- **Advanced section**: Uses bullet points (no `checkedItems` parameter passed)

The CollapsibleRuleSection component checks `if (checkedItems != null && onItemToggle != null)` before rendering checklist mode. When both are null (the default), it falls back to bullet points. This allows the new functionality to coexist with existing sections without modification.

### First Usage of TextDecoration.LineThrough

This is the first usage of `TextDecoration.LineThrough` in the Rulebook codebase. The implementation pairs strikethrough with reduced opacity (0.6f alpha) to provide redundant visual signals. This ensures accessibility for color-blind users who might not perceive the strikethrough as clearly.

### Testing Patterns

Unit tests for the ViewModel pattern:
```kotlin
@Test
fun toggleSetupItem_adds_index_when_not_checked() {
    viewModel.toggleSetupItem(0)
    assertTrue(viewModel.uiState.value.setupCheckedItems.contains(0))
}

@Test
fun toggleSetupItem_removes_index_when_already_checked() {
    // Set initial state with index 0 checked
    viewModel.toggleSetupItem(0)
    viewModel.toggleSetupItem(0)
    assertFalse(viewModel.uiState.value.setupCheckedItems.contains(0))
}
```

### Performance Considerations

- **Recomposition efficiency**: The SetupChecklistItem uses immutable state (all parameters are data, no mutable remember state). Recomposition only occurs when `isChecked` changes.
- **Set operations**: Using `Set<Int>` for indices is more efficient than `List<Boolean>` because Set operations are O(1) lookup vs O(n) for lists.
- **AnimatedVisibility**: The section collapse/expand uses `AnimatedVisibility` which optimizes recomposition—content inside the animation is not rendered when collapsed.

### Future Enhancement Opportunities

1. **Animated Checkbox**: Replace static Checkbox with animated Material 3 transition when toggling
2. **Multi-Section Checklist**: Extend setup progress indicator to show completion percentage across all setup steps
3. **Haptic Feedback**: Add haptic feedback (vibration) when checkbox is toggled
4. **Persistence Option**: Add feature flag to make checklist state optional (persist vs. session-local)
5. **Keyboard Accessibility**: Ensure tab navigation works correctly through checklist items

