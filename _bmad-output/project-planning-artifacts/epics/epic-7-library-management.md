# Epic 7: Library Management

**Goal:** Implement full library functionality with grid display, sorting, and deletion. After this epic, users can browse, sort, and manage their saved game collection.

**FRs covered:** FR27-33

---

## Story 7.1: Game Card Grid Display

As a user,
I want to see my saved games in a visual grid,
So that I can quickly find the game I want.

**Acceptance Criteria:**

**Given** the user has saved games (FR27)
**When** the Library screen is displayed
**Then** games appear in a 2-column grid
**And** each game shows:
- Thumbnail image (or placeholder)
- Game title
- Brutalist card styling (border, shadow)

**And** grid scrolls vertically if many games
**And** cards are tappable to view rules (FR29)

**Architecture requirements:**
- Located in `feature/library` module — `LibraryScreen.kt`, `LibraryViewModel.kt`, `LibraryUiState.kt`
- `LibraryViewModel` observes `GameRepository.getAllGames(sortOrder: SortOrder): Flow<List<Game>>` from `core/data`
- `LibraryUiState`: `games: List<Game>, sortOrder: SortOrder, isLoading: Boolean, error: String?, selectedGameId: String?`
- Data flows reactively from Room via `Flow` — grid auto-updates when games are added/deleted
- `SortOrder` enum in `core/model`: `RECENT` (lastAccessedAt DESC), `ALPHABETICAL` (title ASC), `DATE_ADDED` (createdAt DESC)
- Room queries with `ORDER BY` clause per sort option — sorting done at database level, not in ViewModel
- Feature module depends on: `core/data`, `core/model`, `core/designsystem`, `core/common`

**UX/Component specifications:**
- Grid: `LazyVerticalGrid` with `GridCells.Fixed(2)`, spacing: `spacing.md` (16dp) both horizontal and vertical
- Screen margin: `spacing.md` (16dp) — grid content padding
- Game card: `RulebookCard` composable from `core/designsystem`:
  - Min height: 200dp
  - Border: 4dp solid black (`spacing.brutalist.thickBorderWidth`)
  - Shadow: 6dp offset (`shadows.offsets.subtle`)
  - Corner radius: 0dp (brutalist)
  - Internal padding: 12dp
  - Cover area: `AsyncImage` (Coil) with `ContentScale.Crop`, filling top portion
    - Placeholder: `CoverPattern` from component library — `DiagonalStripe`, `ColorDot`, or `Plain` with `surface.tertiary` background
  - Title area: Game title in `heading.cardTitle` (17sp, SemiBold), max 2 lines with `TextOverflow.Ellipsis`
  - Metadata row (optional): `RulebookBadge` components for player count/duration if available
- Card tap: Ripple effect, navigates to Rules screen
- Scroll: Standard `LazyVerticalGrid` overscroll effect
- Empty state: Handled in Story 2.5 — shown when `games.isEmpty()`
- Background: `surface.secondary` (#FFF9F0/#2C2C2E)

**Technical notes:**
- `LazyVerticalGrid` with `GridCells.Fixed(2)` columns
- `GameCard` composable per component library spec
- Coil `AsyncImage` for thumbnails with disk caching
- 16dp grid spacing per design system
- Card key: `game.id` for stable recomposition

**Prerequisites:** Epic 5/6 (games exist), Epic 1 (design system, GameCard)

---

## Story 7.2: Sort Options Dropdown

As a user,
I want to sort my library by different criteria,
So that I can organize my collection my way.

**Acceptance Criteria:**

**Given** the library has games (FR28)
**When** the user taps the sort button
**Then** a dropdown shows options:
- Recent (last accessed) - default
- Alphabetical (A-Z)
- Date Added (newest first)

**And** selecting an option re-sorts immediately
**And** current sort is indicated
**And** sort preference persists across sessions

**Architecture requirements:**
- Sort state in `LibraryViewModel` — reads initial value from `RulebookPreferences.sortOrder: Flow<SortOrder>` in `core/datastore`
- **Note:** `sortOrder` preference must be added to DataStore schema (not in original Story 1.4 — added here): `stringPreferencesKey("sort_order")` with default `SortOrder.RECENT`
- Sort change: `LibraryIntent.ChangeSortOrder(order: SortOrder)` → updates DataStore + reloads Room query
- Room queries per sort: `GameDao.getAllSortedByRecent()`, `GameDao.getAllSortedAlphabetically()`, `GameDao.getAllSortedByDate()`
- DataStore persistence: `RulebookPreferences.setSortOrder(order: SortOrder)` — suspend function
- Sort change triggers Flow switch via `flatMapLatest` — seamless reactive update

**UX/Component specifications:**
- Sort button: `RulebookPicker` composable from `core/designsystem` positioned in header bar `trailingContent`
  - Height: 56dp, padding: 16dp horizontal
  - Background: `surface.primary`, border: 2dp solid black, shadow: 4dp offset
  - Chevron icon on right indicates dropdown
  - Selected value displayed in `body.body` (17sp, Regular)
- Dropdown menu: Appears below picker with same brutalist border styling
  - Each option: `body.body` text, checkmark icon on selected item
  - Options: "Recent", "A-Z", "Date Added"
  - Menu dismisses on selection or outside tap
- Re-sort animation: `LazyVerticalGrid` automatically animates items via `animateItem()` modifier
- Sort indicator in header: Shows current sort as subtitle text in `detail.caption` (12sp, Regular), `content.secondary`

**Technical notes:**
- `RulebookPicker` or `DropdownMenu` with brutalist styling
- `LibraryViewModel` manages sort state with `MutableStateFlow<SortOrder>`
- Room queries with `ORDER BY` per sort option
- Persist sort preference in DataStore for cross-session persistence
- `flatMapLatest` to switch between Room query Flows when sort changes

**Prerequisites:** Story 7.1, Epic 1 (DataStore)

---

## Story 7.3: Navigate to Rules from Library

As a user,
I want to tap a game card to view its rules,
So that I can reference saved games quickly.

**Acceptance Criteria:**

**Given** the library shows game cards (FR29)
**When** the user taps a game card
**Then** navigation goes to Rules screen with that gameId
**And** transition animation is smooth
**And** back navigation returns to library

**Architecture requirements:**
- Navigation via callback: `LibraryScreen(onGameClick: (gameId: String) -> Unit)`
- NavHost wires: `onGameClick = { id -> navController.navigate("rules/$id") }`
- Feature modules don't hold NavController references — navigation callbacks only (Architecture §Module Dependencies)
- Back navigation: Standard back stack pop — `NavController.popBackStack()` returns to Library

**UX/Component specifications:**
- Card tap: Ripple effect from Material 3 (preserved per UX §Design System: "Preserve Android Conventions")
- Transition: Slide in from right, 300ms (`animation.duration.medium`)
- Back transition: Slide out to right, 300ms
- Predictive back: Shows library screen preview during back gesture (Epic 2, Story 2.8)
- No loading indicator between tap and rules display — Room reads are instant

**Technical notes:**
- Navigate with argument: `navController.navigate("rules/${game.id}")`
- Game ID passed as navigation argument to Rules screen
- Rules screen loads from database — appears instantly (no network)
- Slide transition via `AnimatedNavHost` configuration

**Prerequisites:** Story 7.1, Epic 6 (Rules screen)

---

## Story 7.4: Delete Game with Confirmation

As a user,
I want to delete games from my library,
So that I can remove games I no longer need.

**Acceptance Criteria:**

**Given** the user wants to delete a game (FR30)
**When** the user long-presses a game card (or taps delete option)
**Then** a confirmation dialog appears (FR31)
**And** dialog shows: "Delete [Game Name]?"
**And** "Delete" button (destructive) and "Cancel" button

**When** user confirms deletion
**Then** game and rules are removed from database
**And** library updates immediately
**And** toast confirms: "Game deleted"

**Architecture requirements:**
- Delete operation: `GameRepository.deleteGame(gameId: String): Result<Unit>` in `core/data`
- Room cascading delete: `GameEntity` deletion cascades to `RulesEntity` via foreign key `onDelete = CASCADE`
- Optimistic UI: Remove from list immediately, rollback if delete fails (unlikely with Room)
- Toast/snackbar shown via one-time event: `LibraryEvent.ShowSnackbar("Game deleted")`
- Confirmation dialog state: `LibraryUiState(deleteConfirmation: Game?)` — null = hidden

**UX/Component specifications:**
- Confirmation dialog: `AlertDialog` with brutalist styling:
  - Title: `brutalist.title` (24sp, Black 900) — "Delete [Game Name]?"
  - Body: `body.body` (17sp, Regular) — "This will permanently remove this game and its rules."
  - Background: `surface.primary`, border: 3dp solid black, shadow: 8dp offset
  - Corner radius: 0dp (brutalist)
- "Delete" button: `RulebookButton` destructive variant (red fill #E74C3C, 3dp border, 4dp shadow)
- "Cancel" button: `RulebookButton` secondary variant (outlined)
- Buttons side-by-side with "Cancel" left, "Delete" right
- Toast: Material 3 `Snackbar` with brief message "Game deleted" — 2s auto-dismiss (UX §Feedback Patterns: "Toast notifications, 2s auto-dismiss")
- Haptic feedback: Medium click on delete confirmation (UX §Haptic Patterns)

**Technical notes:**
- `AlertDialog` with brutalist styling
- `GameRepository.deleteGame(gameId)` with CASCADE delete in Room
- Optimistic UI update — `LibraryUiState.games` filtered immediately
- Snackbar via `SnackbarHostState.showSnackbar()` in Scaffold

**Prerequisites:** Story 7.1

---

## Story 7.5: Long-Press Context Menu

As a user,
I want to long-press a game for quick actions,
So that I can access options without extra navigation.

**Acceptance Criteria:**

**Given** the library shows game cards
**When** the user long-presses a card
**Then** a context menu appears with:
- "View Rules" - navigates to rules
- "Delete" - triggers delete confirmation

**And** haptic feedback on long-press
**And** menu dismisses on outside tap

**Architecture requirements:**
- Context menu state in `LibraryViewModel`: `LibraryUiState(contextMenuGame: Game?)` — null = hidden
- Long-press detected via `Modifier.combinedClickable(onLongClick = { ... })`
- Menu actions dispatch: `LibraryIntent.ViewRules(gameId)` or `LibraryIntent.RequestDelete(game)`

**UX/Component specifications:**
- Long-press detection: `Modifier.combinedClickable(onLongClick = { ... })` with `onLongClick` callback
- Haptic feedback on long-press: Medium click VibrationEffect (UX §Haptic Patterns: "Button press → Light click" — stronger for long-press)
- Context menu: `DropdownMenu` anchored to card position:
  - Background: `surface.primary`, border: 2dp solid black, shadow: 4dp offset
  - Corner radius: 0dp (brutalist)
  - Menu items: `DropdownMenuItem` with brutalist text styling
    - "View Rules" — icon: `Icons.Default.MenuBook`, text: `body.body` (17sp, Regular)
    - "Delete" — icon: `Icons.Default.Delete`, text color: `brutalist.red` (#E74C3C/#EC7063)
  - Item padding: `spacing.sm` (8dp) vertical, `spacing.md` (16dp) horizontal
- Menu dismisses: Outside tap or item selection
- Card visual feedback during long-press: scale to 0.98 briefly (from `pressAnimation` utility)

**Technical notes:**
- `DropdownMenu` anchored to card via `Modifier.combinedClickable`
- Long-press detector with VibrationEffect
- Same actions as other entry points (view rules, delete)
- Menu dismissed by setting `contextMenuGame = null`

**Prerequisites:** Story 7.1

---

## Story 7.6: Pull-to-Refresh (Future Proofing)

As a developer,
I want pull-to-refresh implemented,
So that future cloud sync features have a refresh mechanism.

**Acceptance Criteria:**

**Given** the library screen is displayed
**When** the user pulls down on the grid
**Then** a refresh indicator appears
**And** library reloads from database (currently no-op for sync)
**And** indicator dismisses after reload

**Architecture requirements:**
- Pull-to-refresh using `PullToRefreshBox` from Material 3
- Refresh action: `LibraryIntent.Refresh` → `LibraryViewModel` re-collects from Room Flow
- Currently a local-only reload — future: trigger cloud sync API call
- Refresh state: `LibraryUiState(isRefreshing: Boolean)`

**UX/Component specifications:**
- Pull indicator: Material 3 `PullToRefreshDefaults.Indicator` — uses standard Android pull-to-refresh behavior
- Indicator color: `brutalist.pink` (#E91E63/#F06292) for brand consistency
- Refresh animation: Standard circular progress
- Dismiss: After data reload completes (near-instant for Room)
- No custom brutalist styling on refresh indicator — preserve platform convention (UX §Design System: "Preserve Android Conventions")

**Technical notes:**
- `PullToRefreshBox` modifier from Material 3
- Currently just re-collects local Room data — effectively a no-op reload
- Future: cloud sync trigger point
- `isRefreshing` state drives indicator visibility

**Prerequisites:** Story 7.1

---

**Epic 7 Complete: Library Management**

**Stories Created:** 6
**FR Coverage:** FR27-33
**Architecture Sections Referenced:** feature/library, core/data, core/database, core/datastore, core/designsystem, core/model
**UX Patterns Incorporated:** Card grid, sort dropdown, long-press context menu, confirmation dialogs, pull-to-refresh, haptic feedback
**Design Tokens Referenced:** brutalist.pink, brutalist.red, surface.primary, surface.secondary, surface.tertiary, shadows.offsets.subtle, spacing.md, spacing.sm, typography.heading.cardTitle, typography.brutalist.title, animation.duration.medium
