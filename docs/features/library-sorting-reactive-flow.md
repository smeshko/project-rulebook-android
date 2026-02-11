# Library Sorting with Reactive Flow State Management

**Date:** 2026-02-11
**Related Files:**
- `core/data/repository/GameRepository.kt`
- `core/data/repository/GameRepositoryImpl.kt`
- `feature/library/LibraryViewModel.kt`
- `core/datastore/RulebookPreferences.kt`

## Overview

This feature implements reactive library sorting with automatic re-sorting when the sort order changes, and persistence of user preferences across sessions. Games automatically re-sort when the sort order preference changes without requiring explicit data reloading, using Kotlin Flows and `flatMapLatest` to switch between different Room query sources.

## What Was Built

- **Repository Flow API**: `getGamesSorted(sortOrder: SortOrder): Flow<List<Game>>` that returns a reactive Flow of sorted games based on the specified sort order
- **Reactive ViewModel State**: `LibraryViewModel` with reactive sort order management using `flatMapLatest` to automatically switch between different Room queries
- **DataStore Persistence**: Sort preference stored in `RulebookPreferences` using DataStore with `SortPreferencesSource` interface
- **Three Sort Orders**: RECENT (by last access), ALPHABETICAL (by title), DATE_ADDED (by creation date)
- **Automatic UI Updates**: Sort order changes trigger reactive re-sort and UI updates without explicit refresh

## Technical Implementation

### Key Files

- **`GameRepository.kt`**: Defines `getGamesSorted(sortOrder: SortOrder): Flow<List<Game>>` interface method
- **`GameRepositoryImpl.kt`**: Implements reactive sorting using Room DAOs with `when` expression to select appropriate query
- **`LibraryViewModel.kt`**: Uses `flatMapLatest` to switch between sorted Flows when sort order changes, combines with `combine` for UI state
- **`RulebookPreferences.kt`**: Provides `sortOrder: Flow<SortOrder>` and `setSortOrder(order)` for DataStore persistence
- **`LibraryUiState.kt`**: Includes `sortOrder: SortOrder` property for tracking current sort in UI

### Key Patterns

#### 1. **Reactive Flow Switching with flatMapLatest**

The core pattern uses `flatMapLatest` to automatically switch between different Room query Flows when the sort order preference changes:

```kotlin
_sortOrder
    .flatMapLatest { sortOrder ->
        gameRepository.getGamesSorted(sortOrder)
    }
    .collect { games ->
        // Update UI with newly sorted games
    }
```

This pattern ensures:
- Only one active Room query subscription at a time
- Automatic cancellation of previous query when sort order changes
- Seamless transition between sort orders with reactive updates

#### 2. **Repository Pattern with Multiple Sort Options**

Repository method returns appropriate sorted Flow based on enum parameter:

```kotlin
override fun getGamesSorted(sortOrder: SortOrder): Flow<List<Game>> {
    val entitiesFlow = when (sortOrder) {
        SortOrder.RECENT -> gameDao.getAllSortedByLastAccessedDesc()
        SortOrder.ALPHABETICAL -> gameDao.getAllSortedByTitleAsc()
        SortOrder.DATE_ADDED -> gameDao.getAllSortedByCreatedAtDesc()
    }
    return entitiesFlow.map { entities -> entities.map { it.toDomain() } }
}
```

This pattern:
- Centralizes sort order logic in repository
- Allows DAO methods to use native Room `@Query` sorting
- Provides clean separation of concerns

#### 3. **DataStore-Driven Preference Persistence**

Sort order loaded from DataStore and changes update it automatically:

```kotlin
// Initialize from preferences
sortPreferencesSource.sortOrder.collect { order ->
    _sortOrder.value = order
}

// Change sort order
fun changeSortOrder(order: SortOrder) {
    viewModelScope.launch {
        sortPreferencesSource.setSortOrder(order)
        // _sortOrder updates via Flow collection
    }
}
```

This pattern:
- Persists sort preference across app sessions
- Uses reactive Flow for preference changes
- Automatically triggers UI updates when preference changes

### Code Examples

#### Complete ViewModel Setup

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val gameRepository: GameRepository,
    private val sortPreferencesSource: SortPreferencesSource
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder.RECENT)
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        // Load initial sort order from preferences
        viewModelScope.launch {
            sortPreferencesSource.sortOrder.collect { order ->
                _sortOrder.value = order
            }
        }

        // Set up reactive sorting with flatMapLatest
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            _sortOrder
                .flatMapLatest { sortOrder ->
                    gameRepository.getGamesSorted(sortOrder)
                        .map { games -> games to sortOrder }
                        .catch { e ->
                            _uiState.update {
                                it.copy(error = e.message)
                            }
                        }
                }
                .collect { (games, sortOrder) ->
                    _uiState.update {
                        it.copy(
                            games = games,
                            sortOrder = sortOrder,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun changeSortOrder(order: SortOrder) {
        viewModelScope.launch {
            sortPreferencesSource.setSortOrder(order)
        }
    }
}
```

#### Repository Implementation

```kotlin
override fun getGamesSorted(sortOrder: SortOrder): Flow<List<Game>> {
    val entitiesFlow = when (sortOrder) {
        SortOrder.RECENT -> gameDao.getAllSortedByLastAccessedDesc()
        SortOrder.ALPHABETICAL -> gameDao.getAllSortedByTitleAsc()
        SortOrder.DATE_ADDED -> gameDao.getAllSortedByCreatedAtDesc()
    }
    return entitiesFlow.map { entities -> entities.map { it.toDomain() } }
}
```

## How to Use

### 1. **Inject Dependencies**

ViewModel requires `GameRepository` and `SortPreferencesSource`:

```kotlin
val libraryModule = module {
    viewModel { LibraryViewModel(get(), get()) }
}
```

### 2. **Display Current Sort Order**

Access sort order from UI state:

```kotlin
val sortOrder = viewModel.uiState.value.sortOrder
Text("Sorted: ${sortOrder.name}")
```

### 3. **Change Sort Order**

Call `changeSortOrder()` with new sort order:

```kotlin
viewModel.changeSortOrder(SortOrder.ALPHABETICAL)
```

### 4. **Display Games**

Games list updates automatically when sort order changes:

```kotlin
val games = viewModel.uiState.collect { state ->
    GameList(state.games)
}
```

### 5. **Handle Refresh**

Refresh gesture updates isRefreshing state:

```kotlin
viewModel.refresh()
```

## Configuration

### Sort Order Enum

Available sort orders in `SortOrder` enum:

| Order | Description | Sort Field | Direction |
|-------|-------------|-----------|-----------|
| RECENT | Last accessed games first | lastAccessedAt | Descending |
| ALPHABETICAL | Games A-Z by title | title | Ascending |
| DATE_ADDED | Newest added games first | createdAt | Descending |

### DataStore Keys

Sort order persisted in DataStore under `Keys.SORT_ORDER` with String values matching enum names.

## Notes

- The default sort order is `SortOrder.RECENT` (defined in `LibraryUiState`)
- Room queries must include `@Query` with `ORDER BY` clause for each sort variant
- `flatMapLatest` automatically cancels previous query and subscribes to new one
- Sort preference survives app process death via DataStore persistence
- Error handling uses `.catch()` to capture Flow errors and update UI error state
- The `SortPreferencesSource` interface abstracts DataStore for testability (use fake implementations in tests)

