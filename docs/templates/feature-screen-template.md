---
title: Feature Screen Template
description: Template for creating feature screen composables
author: Ivo
date: 2026-01-25
---

# Feature Screen Template

## When to Use

- Creating a new screen for a feature module
- Need ViewModel integration with state collection
- Want proper separation between stateful and stateless composables
- Require comprehensive preview coverage

## Quick Reference

| Aspect | Value |
|--------|-------|
| Location | `feature/{name}/src/main/kotlin/.../Feature{Name}Screen.kt` |
| Pattern | Screen + Content separation |
| Naming | `{Feature}Screen.kt` |

## Directory Structure

```
feature/{name}/
├── {Name}Screen.kt          # This template
├── {Name}ViewModel.kt       # See viewmodel-template.md
├── {Name}UiState.kt         # See uistate-template.md
├── components/              # Screen-specific composables
├── navigation/              # Navigation registration
└── di/                      # Koin module
```

## Code Template

```kotlin
package com.rulebook.feature.{featurename}

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rulebook.core.designsystem.component.RulebookHeaderBar
import com.rulebook.core.designsystem.theme.RulebookTheme
import org.koin.androidx.compose.koinViewModel

/**
 * {Feature} screen displaying {description}.
 *
 * @param onNavigateTo{Destination} Callback invoked when navigating to {destination}.
 * @param viewModel The ViewModel managing {feature} state.
 * @param modifier Modifier to be applied to the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {Feature}Screen(
    onNavigateTo{Destination}: () -> Unit,
    viewModel: {Feature}ViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    {Feature}ScreenContent(
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onNavigateTo{Destination} = onNavigateTo{Destination},
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun {Feature}ScreenContent(
    uiState: {Feature}UiState,
    onRefresh: () -> Unit,
    onNavigateTo{Destination}: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        RulebookHeaderBar(title = "{Feature}")

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.error != null -> {Feature}ErrorState(
                    message = uiState.error,
                    onRetry = onRefresh
                )
                uiState.isEmpty -> {Feature}EmptyState(
                    onAction = onNavigateTo{Destination}
                )
                else -> {Feature}Content(
                    // Pass content-specific parameters
                )
            }
        }
    }
}

/**
 * Content displayed when data is available.
 */
@Composable
private fun {Feature}Content(
    modifier: Modifier = Modifier
) {
    // TODO: Implement content
}

/**
 * Empty state displayed when no data exists.
 */
@Composable
private fun {Feature}EmptyState(
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement empty state
}

/**
 * Error state displayed when loading fails.
 */
@Composable
private fun {Feature}ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement error state with retry button
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
private fun {Feature}ScreenEmptyLightPreview() {
    RulebookTheme(darkTheme = false) {
        {Feature}ScreenContent(
            uiState = {Feature}UiState(),
            onRefresh = {},
            onNavigateTo{Destination} = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Dark")
@Composable
private fun {Feature}ScreenEmptyDarkPreview() {
    RulebookTheme(darkTheme = true) {
        {Feature}ScreenContent(
            uiState = {Feature}UiState(),
            onRefresh = {},
            onNavigateTo{Destination} = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun {Feature}ScreenLoadingPreview() {
    RulebookTheme(darkTheme = false) {
        {Feature}ScreenContent(
            uiState = {Feature}UiState(isLoading = true),
            onRefresh = {},
            onNavigateTo{Destination} = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Light")
@Composable
private fun {Feature}ScreenErrorLightPreview() {
    RulebookTheme(darkTheme = false) {
        {Feature}ScreenContent(
            uiState = {Feature}UiState(error = "Failed to load. Please try again."),
            onRefresh = {},
            onNavigateTo{Destination} = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Dark")
@Composable
private fun {Feature}ScreenErrorDarkPreview() {
    RulebookTheme(darkTheme = true) {
        {Feature}ScreenContent(
            uiState = {Feature}UiState(error = "Failed to load. Please try again."),
            onRefresh = {},
            onNavigateTo{Destination} = {}
        )
    }
}
```

## Existing Patterns

Reference implementations in the codebase:
- [LibraryScreen.kt](../../feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryScreen.kt)
- [SettingsScreen.kt](../../feature/settings/src/main/kotlin/com/rulebook/feature/settings/SettingsScreen.kt)
- [CameraScreen.kt](../../feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraScreen.kt)

## Integrations

1. **Create companion files:** UiState and ViewModel (see related templates)
2. **Register in Koin:** Add to feature's `di/{Feature}Module.kt`
3. **Add navigation:** Register in `app/navigation/RulebookNavHost.kt`
4. **Add to settings.gradle.kts:** If new module

## Checklist

- [ ] Created `{Feature}Screen.kt` with ViewModel injection
- [ ] Separated public Screen from internal Content composable
- [ ] Implemented Empty, Error, and Content states
- [ ] Added navigation callbacks as parameters
- [ ] Created Preview composables for all states (light/dark)
- [ ] Used `collectAsStateWithLifecycle()` for state collection

## References

- [viewmodel-template.md](viewmodel-template.md) - ViewModel pattern
- [uistate-template.md](uistate-template.md) - UiState pattern
- [koin-module-template.md](koin-module-template.md) - DI registration
