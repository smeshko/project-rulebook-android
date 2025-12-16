# Story 2.3: Bottom Navigation Bar

Status: ready-for-dev

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

- [ ] Task 1: Create RulebookBottomBar composable (AC: #1, #2, #6)
  - [ ] Create composable in app/navigation package
  - [ ] Use Material 3 NavigationBar as base
  - [ ] Add Library and Settings NavigationBarItem
  - [ ] Apply brutalist styling with thick border
- [ ] Task 2: Implement tab icons and labels (AC: #2)
  - [ ] Add grid icon for Library
  - [ ] Add gear/settings icon for Settings
  - [ ] Configure icon and label display
- [ ] Task 3: Apply selection highlighting (AC: #3)
  - [ ] Use accent color for selected indicator
  - [ ] Ensure unselected tabs are visually distinct
  - [ ] Apply brutalist styling to selection state
- [ ] Task 4: Wire navigation to tabs (AC: #4)
  - [ ] Accept currentRoute parameter
  - [ ] Accept onNavigate callback
  - [ ] Navigate on tab click
- [ ] Task 5: Preserve tab state (AC: #5)
  - [ ] Use saveState in navigation
  - [ ] Use restoreState when returning to tab
  - [ ] Test state preservation across tab switches
- [ ] Task 6: Create BottomBarDestination sealed class
  - [ ] Define destinations shown in bottom bar
  - [ ] Include route, icon, and label for each
  - [ ] Make easily extensible for future tabs

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
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 2.2 (Navigation Host & Route Definitions)
- **Blocks:** Story 2.4, Story 2.6
- **Can Parallel With:** Story 2.8

### Dependency Rationale
- Story 2.2: Bottom bar needs Route definitions for navigation
- Story 2.4: FAB positioning is relative to bottom nav
- Story 2.6: Settings screen navigated via bottom nav
- Story 2.8: Can develop in parallel (different navigation concern)
