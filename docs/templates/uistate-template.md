---
title: UI State Template
description: Template for creating immutable UI state data classes
author: Ivo
date: 2026-01-25
---

# UI State Template

## When to Use

- Creating state representation for a feature screen
- Need immutable state with loading/error/content states
- Want computed properties for derived UI logic
- Require default values for all properties

## Quick Reference

| Aspect | Value |
|--------|-------|
| Location | `feature/{name}/src/main/kotlin/.../Feature{Name}UiState.kt` |
| Pattern | Immutable data class |
| Naming | `{Feature}UiState.kt` |

## Code Template

```kotlin
package com.rulebook.feature.{featurename}

import com.rulebook.core.model.{Model}

/**
 * UI state for the {Feature} screen.
 *
 * Tracks {description of what it tracks}.
 * When {condition}, the {state} is displayed.
 *
 * @param {data} The {description of data}.
 * @param isLoading Whether initial data loading is in progress.
 * @param isRefreshing Whether a pull-to-refresh is in progress.
 * @param error Optional error message if loading failed.
 */
data class {Feature}UiState(
    val {data}: List<{Model}> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    /**
     * Whether the empty state should be displayed.
     * True when there is no data and no error occurred.
     */
    val isEmpty: Boolean get() = {data}.isEmpty() && error == null

    /**
     * Whether content should be displayed.
     * True when data exists and no error.
     */
    val hasContent: Boolean get() = {data}.isNotEmpty() && error == null

    /**
     * The count of items for display.
     */
    val itemCount: Int get() = {data}.size
}
```

## Existing Patterns

Reference implementations in the codebase:
- [LibraryUiState.kt](../../feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryUiState.kt)
- [SettingsUiState.kt](../../feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsUiState.kt)

## Key Patterns

### Default Values
Always provide defaults so state can be instantiated empty:
```kotlin
data class FeatureUiState(
    val items: List<Item> = emptyList(),  // Empty list default
    val isLoading: Boolean = false,        // Boolean defaults
    val error: String? = null              // Nullable for optional
)
```

### Computed Properties
Use `get()` for derived state to avoid stale data:
```kotlin
val isEmpty: Boolean get() = items.isEmpty() && error == null
```

### Multiple States
A screen can be in multiple states simultaneously:
```kotlin
// Loading initial data
UiState(isLoading = true)

// Refreshing with existing data visible
UiState(items = existingItems, isRefreshing = true)

// Error with existing data still visible
UiState(items = existingItems, error = "Network error")
```

### State Transitions in ViewModel
```kotlin
// Start loading
_uiState.update { it.copy(isLoading = true) }

// Success
_uiState.update { it.copy(items = newItems, error = null) }

// Error
_uiState.update { it.copy(error = "Failed to load") }

// End loading
_uiState.update { it.copy(isLoading = false) }
```

## Integrations

1. **Import domain models:** From `core/model`
2. **Use in ViewModel:** As `MutableStateFlow<{Feature}UiState>`
3. **Collect in Screen:** With `collectAsStateWithLifecycle()`

## Checklist

- [ ] Created `{Feature}UiState.kt` as data class
- [ ] All properties have default values
- [ ] `isLoading` for initial load state
- [ ] `isRefreshing` for pull-to-refresh state
- [ ] `error: String?` for error messages
- [ ] Computed `isEmpty` property
- [ ] KDoc comments on class and properties

## References

- [viewmodel-template.md](viewmodel-template.md) - ViewModel pattern
- [feature-screen-template.md](feature-screen-template.md) - Screen pattern
