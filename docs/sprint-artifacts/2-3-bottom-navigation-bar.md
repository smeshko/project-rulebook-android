# Story 2.3: Bottom Navigation Bar

Status: in-progress

## Story

As a user,
I want a bottom navigation bar with Library and Settings tabs,
So that I can quickly switch between main sections.

## Acceptance Criteria

1. **Given** the app is on a main screen (Library or Settings)
   **When** the bottom navigation is displayed
   **Then** two tabs are visible: "Library" and "Settings"

2. **And** each tab has an icon and label

3. **And** the selected tab is visually highlighted

4. **And** tapping a tab navigates to that screen instantly

5. **And** state is preserved when switching tabs

6. **And** the navigation bar has brutalist styling:
   - Thick top border (3dp)
   - Surface background color
   - Selected indicator with accent color

## Tasks / Subtasks

- [x] Task 1: Create RulebookBottomBar composable (AC: #1, #2, #6)
  - [x] Create composable in app/navigation package
  - [x] Use Material 3 NavigationBar as base
  - [x] Add Library and Settings NavigationBarItem
  - [x] Apply brutalist styling with thick border
- [x] Task 2: Implement tab icons and labels (AC: #2)
  - [x] Add grid icon for Library
  - [x] Add gear/settings icon for Settings
  - [x] Configure icon and label display
- [x] Task 3: Apply selection highlighting (AC: #3)
  - [x] Use accent color for selected indicator
  - [x] Ensure unselected tabs are visually distinct
  - [x] Apply brutalist styling to selection state
- [x] Task 4: Wire navigation to tabs (AC: #4)
  - [x] Accept currentRoute parameter
  - [x] Accept onNavigate callback
  - [x] Navigate on tab click
- [x] Task 5: Preserve tab state (AC: #5)
  - [x] Use saveState in navigation
  - [x] Use restoreState when returning to tab
  - [x] Test state preservation across tab switches
- [x] Task 6: Create BottomBarDestination sealed class
  - [x] Define destinations shown in bottom bar
  - [x] Include route, icon, and label for each
  - [x] Make easily extensible for future tabs

## Dev Notes

### Architecture Context

- **Module:** app
- **Package:** com.rulebook.app.navigation
- **Key Files:** RulebookBottomBar.kt, BottomBarDestination.kt

### Implementation Pattern

```kotlin
// BottomBarDestination.kt
sealed class BottomBarDestination(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
) {
    object Library : BottomBarDestination(
        route = Route.Library.route,
        icon = Icons.Outlined.GridView,
        selectedIcon = Icons.Filled.GridView,
        label = "Library"
    )

    object Settings : BottomBarDestination(
        route = Route.Settings.route,
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        label = "Settings"
    )

    companion object {
        val items = listOf(Library, Settings)
    }
}
```

```kotlin
// RulebookBottomBar.kt
@Composable
fun RulebookBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .drawBehind {
                // Thick top border (brutalist style)
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 3.dp.toPx()
                )
            },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        BottomBarDestination.items.forEach { destination ->
            val selected = currentRoute == destination.route

            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination.route) },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.icon,
                        contentDescription = destination.label
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        style = RulebookTypography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RulebookColors.pink,
                    selectedTextColor = RulebookColors.pink,
                    indicatorColor = RulebookColors.pink.copy(alpha = 0.2f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
```

### State Preservation Navigation

```kotlin
// In NavHost or navigation handling
fun navigateToBottomBarDestination(navController: NavController, route: String) {
    navController.navigate(route) {
        // Pop up to start destination to avoid building up back stack
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        // Avoid multiple copies of same destination
        launchSingleTop = true
        // Restore state when navigating back
        restoreState = true
    }
}
```

### Brutalist Styling

| Property | Value |
|----------|-------|
| Top border width | 3dp |
| Border color | Black |
| Background | Surface color |
| Selected indicator | Pink with 20% alpha |
| Selected icon/text | Pink |
| Tonal elevation | 0dp (flat) |

### Icons

| Tab | Unselected | Selected |
|-----|------------|----------|
| Library | GridView (Outlined) | GridView (Filled) |
| Settings | Settings (Outlined) | Settings (Filled) |

### Integration Point

The bottom bar will be integrated into Scaffold in Story 2.7. For now, create it as a standalone composable that can be composed into the Scaffold's `bottomBar` slot.

### References

- [Source: docs/architecture.md#Composable Structure]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.3]
- [Source: docs/ux-design-specification.md#Navigation Patterns]

## Dev Agent Record

### Context Reference
- Navigation routes from Story 2.2
- RulebookTheme, RulebookColors from Epic 1
- Brutalist styling patterns from Epic 1 design system

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Initial Java 25 compatibility issue resolved by using Android Studio JBR
- Tests for BottomBarDestination.items moved to instrumented tests due to Android framework dependency for icon initialization

### Completion Notes List
- Created BottomBarDestination sealed class with Library and Settings data objects
- Implemented RulebookBottomBar composable with brutalist styling (thick 3dp top border, flat appearance)
- Added selected/unselected icon variants (filled/outlined) for visual feedback
- Implemented NavigationBarItemDefaults.colors for pink accent highlighting
- Added navigateToBottomBarDestination extension for state preservation navigation
- Added comprehensive unit tests for BottomBarDestination and navigation extensions
- Added compose material icons extended dependency

### File List
- app/build.gradle.kts (modified - added material-icons-extended dependency)
- app/src/main/kotlin/com/rulebook/navigation/BottomBarDestination.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/RulebookBottomBar.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/NavControllerExtensions.kt (modified - added navigateToBottomBarDestination)
- app/src/test/kotlin/com/rulebook/navigation/BottomBarDestinationTest.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/RulebookBottomBarTest.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/NavControllerExtensionsTest.kt (modified - added bottom bar tests)

## Dependencies

- **Depends On:** Story 2.2 (Navigation Host & Route Definitions)
- **Blocks:** Story 2.4, Story 2.6
- **Can Parallel With:** Story 2.8

### Dependency Rationale
- Story 2.2: Bottom bar needs Route definitions for navigation
- Story 2.4: FAB positioning is relative to bottom nav
- Story 2.6: Settings screen navigated via bottom nav
- Story 2.8: Can develop in parallel (different navigation concern)
