# Epic 7: Library Management

**Goal:** Implement full library functionality with grid display, sorting, and deletion. After this epic, users can browse, sort, and manage their saved game collection.

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

**Technical Notes:**
- LazyVerticalGrid with 2 columns
- GameCard composable
- Coil AsyncImage for thumbnails
- 16dp grid spacing

**Prerequisites:** Epic 5/6 (games exist), Epic 1 (GameCard)

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

**Technical Notes:**
- DropdownMenu with brutalist styling
- LibraryViewModel manages sort state
- Room queries with ORDER BY
- Persist sort preference in DataStore

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

**Technical Notes:**
- Navigate with argument: `navController.navigate("rules/${game.id}")`
- Game ID passed as navigation argument
- Rules screen loads from database

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

**Technical Notes:**
- AlertDialog with brutalist styling
- GameRepository.deleteGame(gameId) with cascade
- Optimistic UI update with rollback on error

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

**Technical Notes:**
- DropdownMenu anchored to card
- Long-press detector with haptic
- Same actions as other entry points

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

**Technical Notes:**
- `pullRefresh` modifier from Material 3
- Currently just reloads local data
- Future: trigger cloud sync

**Prerequisites:** Story 7.1

---

**Epic 7 Complete: Library Management**

**Stories Created:** 6
**FR Coverage:** FR27-31
**Architecture Sections Referenced:** feature/library, core/database
**UX Patterns Incorporated:** Card grid, sort dropdown, long-press menu, confirmation dialogs

---
