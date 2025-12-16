# Story 2.5: Library Screen Shell with Empty State

Status: ready-for-dev

## Story

As a user,
I want to see an encouraging empty state when I have no saved games,
So that I understand how to get started.

## Acceptance Criteria

1. **Given** the user has no saved games (FR33)
   **When** the Library screen is displayed
   **Then** the empty state shows:
   - Illustration or icon (game-related)
   - Headline: "No games yet"
   - Subtext: "Scan your first game to get started"
   - Optional CTA button pointing to camera

2. **And** the header bar shows "Library" with brutalist styling

3. **And** the screen supports pull-to-refresh (prepares for future)

## Tasks / Subtasks

- [ ] Task 1: Create LibraryScreen composable (AC: #1, #2)
  - [ ] Create feature/library module structure
  - [ ] Create LibraryScreen.kt
  - [ ] Add RulebookHeaderBar with "Library" title
  - [ ] Implement empty state layout
- [ ] Task 2: Create LibraryViewModel (AC: #1)
  - [ ] Create LibraryViewModel.kt
  - [ ] Create LibraryUiState data class
  - [ ] Track empty state vs games list state
  - [ ] Expose StateFlow for UI observation
- [ ] Task 3: Implement empty state content (AC: #1)
  - [ ] Add game-related icon/illustration
  - [ ] Add "No games yet" headline
  - [ ] Add "Scan your first game to get started" subtext
  - [ ] Add optional CTA button using RulebookButton
  - [ ] Use RulebookCard styling for empty state container
- [ ] Task 4: Add pull-to-refresh support (AC: #3)
  - [ ] Wrap content in PullToRefreshBox
  - [ ] Wire refresh action to ViewModel
  - [ ] Handle refresh state
- [ ] Task 5: Create Koin module for library feature
  - [ ] Create LibraryModule.kt
  - [ ] Register LibraryViewModel
  - [ ] Register in app module
- [ ] Task 6: Wire navigation callbacks
  - [ ] Add onNavigateToCamera callback
  - [ ] Add onNavigateToRules callback (for future)

## Dev Notes

### Architecture Context

- **Module:** feature/library
- **Package:** com.rulebook.feature.library
- **Key Files:** LibraryScreen.kt, LibraryViewModel.kt, LibraryUiState.kt, LibraryModule.kt

### Module Structure

```
feature/library/
├── src/main/kotlin/com/rulebook/feature/library/
│   ├── LibraryScreen.kt
│   ├── LibraryViewModel.kt
│   ├── LibraryUiState.kt
│   ├── components/
│   │   └── LibraryEmptyState.kt
│   ├── navigation/
│   │   └── LibraryNavigation.kt
│   └── di/
│       └── LibraryModule.kt
└── build.gradle.kts
```

### Implementation Pattern

```kotlin
// LibraryUiState.kt
data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean get() = games.isEmpty() && !isLoading
}

// LibraryViewModel.kt
class LibraryViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadGames()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            loadGames()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    private fun loadGames() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = gameRepository.getGames()) {
                is Result.Success -> _uiState.update {
                    it.copy(games = result.data, isLoading = false, error = null)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
```

```kotlin
// LibraryScreen.kt
@Composable
fun LibraryScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToRules: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        RulebookHeaderBar(title = "Library")

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isEmpty -> LibraryEmptyState(onScanClick = onNavigateToCamera)
                else -> LibraryContent(
                    games = uiState.games,
                    onGameClick = onNavigateToRules
                )
            }
        }
    }
}
```

```kotlin
// LibraryEmptyState.kt
@Composable
fun LibraryEmptyState(
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RulebookCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = RulebookTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No games yet",
                style = RulebookTypography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Scan your first game to get started",
                style = RulebookTypography.bodyMedium,
                color = RulebookTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            RulebookButton(
                text = "Scan a Game",
                onClick = onScanClick,
                variant = ButtonVariant.Primary
            )
        }
    }
}
```

### Empty State Design

| Element | Specification |
|---------|---------------|
| Icon | SportsEsports (Outlined), 64dp |
| Headline | "No games yet", headlineMedium |
| Subtext | "Scan your first game...", bodyMedium, 60% alpha |
| CTA | RulebookButton Primary "Scan a Game" |
| Container | RulebookCard with brutalist styling |
| Padding | 24dp internal, 16dp from edges |

### Dependencies Required

```kotlin
// feature/library/build.gradle.kts
dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.lifecycle.viewmodel.compose)
}
```

### References

- [Source: docs/architecture.md#Feature Module Structure]
- [Source: docs/architecture.md#State Management]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.5]
- [Source: docs/ux-design-specification.md#Empty States]

## Dev Agent Record

### Context Reference
- RulebookHeaderBar from Story 1.11
- RulebookCard from Story 1.10
- RulebookButton from Story 1.9
- MVI pattern from architecture

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 2.4 (FAB), Epic 1 (RulebookHeaderBar)
- **Blocks:** Story 2.7
- **Can Parallel With:** Story 2.6

### Dependency Rationale
- Story 2.4: Library screen CTA can alternatively use FAB for camera
- Epic 1: Requires RulebookHeaderBar, RulebookCard, RulebookButton components
- Story 2.7: Scaffold integration requires Library screen to exist
- Story 2.6: Can develop in parallel (different screen)
