---
title: Feature ViewModel Template
description: Template for creating feature ViewModels with StateFlow
author: Ivo
date: 2026-01-25
---

# Feature ViewModel Template

## When to Use

- Creating state management for a new feature screen
- Need reactive UI state with StateFlow
- Want MVI-style unidirectional data flow
- Require coroutine-based async operations

## Quick Reference

| Aspect | Value |
|--------|-------|
| Location | `feature/{name}/src/main/kotlin/.../Feature{Name}ViewModel.kt` |
| Pattern | MVI with StateFlow |
| Naming | `{Feature}ViewModel.kt` |

## Code Template

```kotlin
package com.rulebook.feature.{featurename}

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rulebook.core.common.Result
import com.rulebook.core.data.repository.{Feature}Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the {Feature} screen.
 *
 * Manages {description of what it manages} and handles loading/refresh operations.
 *
 * @param {dependency}Repository Repository for accessing {data type} data.
 */
class {Feature}ViewModel(
    private val {dependency}Repository: {Feature}Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow({Feature}UiState())
    val uiState: StateFlow<{Feature}UiState> = _uiState.asStateFlow()

    init {
        load{Data}()
    }

    /**
     * Refreshes the data.
     * Called when the user performs a pull-to-refresh gesture.
     */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            load{Data}Internal()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    /**
     * Handles user action.
     * Called when {describe when this is called}.
     */
    fun on{Action}() {
        viewModelScope.launch {
            // Handle action
        }
    }

    private fun load{Data}() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            load{Data}Internal()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun load{Data}Internal() {
        when (val result = {dependency}Repository.get{Data}()) {
            is Result.Success -> _uiState.update {
                it.copy({data} = result.data, error = null)
            }
            is Result.Error -> _uiState.update {
                it.copy(error = result.message)
            }
        }
    }
}
```

## Existing Patterns

Reference implementations in the codebase:
- [LibraryViewModel.kt](../../feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryViewModel.kt)
- [SettingsViewModel.kt](../../feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsViewModel.kt)

## Key Patterns

### State Updates
Always use `update` with `copy()` for immutable state updates:
```kotlin
_uiState.update { it.copy(isLoading = true) }
```

### Error Handling
Use the `Result` sealed class from `core/common`:
```kotlin
when (val result = repository.getData()) {
    is Result.Success -> // handle success
    is Result.Error -> // handle error
}
```

### Coroutine Scope
Always use `viewModelScope` for coroutines - it auto-cancels on ViewModel clear:
```kotlin
viewModelScope.launch {
    // async work
}
```

## Integrations

1. **Create UiState:** See [uistate-template.md](uistate-template.md)
2. **Register in Koin:** Add to feature's DI module:
   ```kotlin
   viewModel { {Feature}ViewModel(get()) }
   ```
3. **Inject in Screen:** Use `koinViewModel()` in composable

## Checklist

- [ ] Created `{Feature}ViewModel.kt` extending `ViewModel`
- [ ] Private `MutableStateFlow`, public `StateFlow` exposure
- [ ] Constructor injection for repositories
- [ ] `init` block loads initial data
- [ ] `refresh()` method for pull-to-refresh
- [ ] All async work in `viewModelScope`
- [ ] `Result` pattern for error handling
- [ ] KDoc comments on class and public methods

## References

- [uistate-template.md](uistate-template.md) - UiState pattern
- [repository-template.md](repository-template.md) - Repository pattern
- [koin-module-template.md](koin-module-template.md) - DI registration
