# Game Grid Display - Library Screen

**Date:** 2026-02-11
**Story:** RULE-216 - Story 7.1: Game Card Grid Display
**Related Files:**
- `feature/library/src/main/kotlin/com/rulebook/feature/library/LibraryScreen.kt`
- `feature/library/src/main/kotlin/com/rulebook/feature/library/components/GameCard.kt`
- `core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookTypography.kt`

## Overview

Implemented a 2-column game grid display for the Library screen using `LazyVerticalGrid`. This story introduces the `GameCard` composable component, a reusable pattern for displaying individual games with brutalist card styling, thumbnail images (with fallback placeholders), and game titles. The implementation extends the design system with a new `cardTitle` typography token and demonstrates best practices for integrating Coil 3 image loading with error handling.

## What Was Built

- **GameCard Component** - Reusable brutalist card composable for displaying individual games
- **LazyVerticalGrid Layout** - 2-column fixed grid with 16dp spacing and reactive scrolling
- **Typography Extension** - Added `cardTitle` token (17sp, SemiBold) to design system
- **Image Handling** - Coil 3 AsyncImage integration with placeholder and error states for game thumbnails
- **Grid Preview Composables** - Light/dark theme previews demonstrating multiple games with mixed thumbnail states

## Technical Implementation

### Key Files

- **`LibraryScreen.kt`** - Main screen composable; replaced `LibraryContent` placeholder with `LazyVerticalGrid` implementation (141 line addition)
- **`GameCard.kt`** (NEW) - Screen-specific composable component for individual game cards; uses `RulebookCard` base component with 200dp min height and AsyncImage cover area
- **`RulebookTypography.kt`** - Design system file; added `cardTitle` `TextStyle` constant and extended `RulebookExtendedTypography` data class (23 line addition)

### Key Patterns

#### 1. **LazyVerticalGrid with Stable Keys Pattern**
The grid uses stable key function for efficient recomposition:
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    modifier = Modifier
        .fillMaxWidth()
        .background(RulebookTheme.colors.surfaceSecondary),
    contentPadding = PaddingValues(RulebookTheme.spacing.md),
    horizontalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.md),
    verticalArrangement = Arrangement.spacedBy(RulebookTheme.spacing.md)
) {
    items(games, key = { it.id }) { game ->
        GameCard(game = game, onClick = { onGameClick(game.id) })
    }
}
```
**Why important:** The `key = { it.id }` ensures stable recomposition when games are reordered or filtered. Without stable keys, list animations and state preservation break.

#### 2. **Coil 3 AsyncImage with Fallback Strategy**
Handle both null thumbnails and loading/error states:
```kotlin
AsyncImage(
    model = game.thumbnailUrl,
    contentDescription = "${game.title} thumbnail",
    modifier = Modifier
        .fillMaxWidth()
        .height(120.dp),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(com.rulebook.core.designsystem.R.drawable.ic_game_placeholder),
    error = painterResource(com.rulebook.core.designsystem.R.drawable.ic_game_placeholder)
)
```
When `thumbnailUrl` is null, show colored background (`surfaceTertiary`) with placeholder icon.

#### 3. **Brutalist Card Component Reuse**
`GameCard` uses `RulebookCard` base component which provides:
- 4dp solid black border (via `brutalistBorder()`)
- 4dp shadow offset (via `shadowOffset`)
- 0dp corner radius (brutalist style)
- Ripple effect on tap via `onClick` parameter

#### 4. **Typography Token Integration Pattern**
New `cardTitle` token demonstrates how to extend design system:
- Added `TextStyle` constant in `RulebookTypography.kt`
- Extended `RulebookExtendedTypography` data class with `cardTitle` field
- Accessed via `RulebookTheme.typography.cardTitle` throughout the codebase
- Provides consistency for all game card titles

### Code Examples

#### GameCard Component Usage
```kotlin
GameCard(
    game = game,
    onClick = { onGameClick(game.id) },
    modifier = Modifier.fillMaxWidth()
)
```

#### Adding New Typography Token
When other features need new text styles:
1. Define `TextStyle` constant in `RulebookTypography.kt`
2. Add field to `RulebookExtendedTypography` data class
3. Initialize in `RulebookExtendedTypographyInstance`
4. Access via `RulebookTheme.typography.[tokenName]`

## How to Use

### Displaying a Game Grid

1. **Pass games from ViewModel:**
```kotlin
val games = viewModel.uiState.games
```

2. **Render grid in screen composable:**
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(2),
    // ... spacing and arrangement config
) {
    items(games, key = { it.id }) { game ->
        GameCard(game = game, onClick = { onGameClick(game.id) })
    }
}
```

3. **Handle navigation callback:**
```kotlin
onGameClick = { gameId ->
    navController.navigate(Route.Rules.createRoute(gameId))
}
```

### Creating Similar Grids

When building other grid-based features:
1. Use `LazyVerticalGrid` with `GridCells.Fixed(n)` for fixed columns
2. Always include `key = { item.id }` for stable recomposition
3. Use `contentPadding` for screen margins (typically `spacing.md`)
4. Use `Arrangement.spacedBy()` for consistent spacing
5. Wrap content items in reusable card composables (like `GameCard`)

## Configuration

### Grid Layout Parameters

| Parameter | Value | Purpose |
|-----------|-------|---------|
| Columns | `Fixed(2)` | 2-column layout on all screen sizes |
| Horizontal Spacing | `16dp` (spacing.md) | Gap between card columns |
| Vertical Spacing | `16dp` (spacing.md) | Gap between card rows |
| Content Padding | `16dp` (spacing.md) | Screen margin on all sides |
| Background | `surfaceSecondary` | Cream (#FFF9F0) light / Dark (#2C2C2E) dark |

### GameCard Component Parameters

| Parameter | Type | Purpose |
|-----------|------|---------|
| game | `Game` | The game to display (id, title, thumbnailUrl required) |
| onClick | `() -> Unit` | Callback when card is tapped |
| modifier | `Modifier` | Optional modifier for styling (default: `Modifier`) |

### Typography Token

| Token | Size | Weight | Line Height | Usage |
|-------|------|--------|-------------|-------|
| cardTitle | 17sp | SemiBold | 22sp | Game card titles (max 2 lines with ellipsis) |

## Notes

### Design System Decisions

1. **Shadow Override Not Used**: The epic spec mentioned "6dp shadow offset" but the design system only provides 4dp (default), 8dp (medium), and 12dp (large) tokens. Decision: Use 4dp default for consistency with other card components in the app.

2. **Cover Pattern Not Implemented**: The spec mentions `CoverPattern` with pattern variants. This was deferred as it requires a new component and game metadata not yet available. Use simple solid background with placeholder icon instead.

3. **Metadata Badges Deferred**: The spec mentions optional `RulebookBadge` components for player count/duration. These were deferred as the badge component doesn't exist and game data doesn't include these fields yet. Can be added in future story when components and data are available.

### One-Shot vs Reactive Loading

The current `GameRepositoryImpl.getGames()` uses `.first()` on the Room Flow—this is a one-shot read. The epic spec mentions reactive flow that auto-updates when games are added/deleted. The reactive pattern was NOT implemented in this story because:

- Pull-to-refresh already covers manual refresh for users
- Reactive Flow with `flatMapLatest` is better implemented in Story 7.2 (Sort Options) when sort order changes need to trigger re-queries
- One-shot pattern is appropriate for initial load and refresh operations

### Future Enhancements

- Add `CoverPattern` component for more visually interesting placeholders
- Add `RulebookBadge` metadata (player count, duration) once component and game data available
- Consider `LazyVerticalStaggeredGrid` for varying card heights if game metadata expands
- Add animation transitions for grid item changes (add/remove/reorder)

### Testing Notes

- Unit tests verify ViewModel correctly loads multiple games with all fields present
- No Compose UI tests required (follows project pattern)
- Preview composables demonstrate light/dark themes with mixed thumbnail states
- All gradle builds pass: `:feature:library:build`, `:feature:library:test`, `:app:assembleDebug`
