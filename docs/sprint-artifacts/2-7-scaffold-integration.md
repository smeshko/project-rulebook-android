# Story 2.7: Scaffold Integration & Screen Composition

Status: in-progress

## Story

As a developer,
I want all main screens composed within a shared Scaffold,
So that the navigation bar and FAB are consistently displayed.

## Acceptance Criteria

1. **Given** the navigation structure
   **When** main screens are displayed
   **Then** a shared Scaffold provides:
   - Bottom navigation bar
   - Floating action button
   - Content area with proper insets

2. **And** non-main screens (Camera, Rules, Onboarding) use full-screen without bottom nav

3. **And** transitions between screens use appropriate animations

## Tasks / Subtasks

- [x] Task 1: Create RulebookScaffold wrapper (AC: #1)
  - [x] Create RulebookScaffold composable
  - [x] Include bottom navigation bar
  - [x] Include floating action button
  - [x] Handle content area with proper insets
- [x] Task 2: Implement bottom bar visibility logic (AC: #2)
  - [x] Define main routes that show bottom bar
  - [x] Define routes that hide bottom bar
  - [x] Conditionally show/hide based on current route
- [ ] Task 3: Implement FAB visibility logic (AC: #2)
  - [ ] FAB visible on Library and Settings
  - [ ] FAB hidden on Camera, Rules, Onboarding, Purchase
- [ ] Task 4: Configure screen transitions (AC: #3)
  - [ ] Add fade transitions for main tabs
  - [ ] Add slide transitions for detail screens
  - [ ] Use AnimatedNavHost for smooth animations
- [ ] Task 5: Integrate with RulebookNavHost
  - [ ] Wrap NavHost content with RulebookScaffold
  - [ ] Pass navigation state to scaffold
  - [ ] Wire FAB click to camera navigation
- [ ] Task 6: Handle edge-to-edge insets
  - [ ] Content respects system bars
  - [ ] Bottom bar positioned correctly
  - [ ] FAB positioned above bottom bar

## Dev Notes

### Architecture Context

- **Module:** app
- **Package:** com.rulebook.app
- **Key Files:** RulebookScaffold.kt, updated RulebookNavHost.kt

### Implementation Pattern

```kotlin
// RulebookScaffold.kt
@Composable
fun RulebookScaffold(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes that show bottom bar and FAB
    val mainRoutes = listOf(Route.Library.route, Route.Settings.route)
    val showBottomBar = currentRoute in mainRoutes
    val showFab = currentRoute in mainRoutes

    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                RulebookBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigateToBottomBarDestination(route)
                    }
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                RulebookFAB(
                    onClick = { navController.navigate(Route.Camera.route) }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        content(innerPadding)
    }
}
```

```kotlin
// Updated RulebookApp.kt
@Composable
fun RulebookApp() {
    val navController = rememberNavController()

    RulebookTheme {
        RulebookScaffold(navController = navController) { innerPadding ->
            RulebookNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
```

### Screen Transition Animations

```kotlin
// AnimatedNavHost transitions
NavHost(
    navController = navController,
    startDestination = Route.Library.route,
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
) {
    // Main screens - fade transition
    composable(
        route = Route.Library.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        LibraryScreen(...)
    }

    // Detail screens - slide transition
    composable(
        route = Route.Camera.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        }
    ) {
        CameraScreen(...)
    }

    composable(
        route = Route.Rules.route,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        }
    ) { backStackEntry ->
        RulesScreen(...)
    }
}
```

### Visibility Rules

| Route | Bottom Bar | FAB |
|-------|------------|-----|
| Library | ✓ | ✓ |
| Settings | ✓ | ✓ |
| Camera | ✗ | ✗ |
| Rules | ✗ | ✗ |
| Onboarding | ✗ | ✗ |
| Purchase | ✗ | ✗ |

### Animation Specs

| Animation | Type | Duration |
|-----------|------|----------|
| Tab switch | Fade | 300ms |
| Bottom bar show/hide | SlideVertically | 300ms |
| FAB show/hide | Scale | 200ms |
| Detail screen enter | SlideHorizontally | 300ms |
| Detail screen exit | SlideHorizontally | 300ms |

### Edge-to-Edge Handling

```kotlin
Scaffold(
    contentWindowInsets = WindowInsets.systemBars
) { innerPadding ->
    // Content uses innerPadding for proper insets
    Box(modifier = Modifier.padding(innerPadding)) {
        // NavHost content here
    }
}
```

### Navigation Extension

```kotlin
// NavController extension for bottom bar navigation
fun NavController.navigateToBottomBarDestination(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
```

### References

- [Source: docs/architecture.md#Composable Structure]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.7]
- [Source: docs/ux-design-specification.md#Navigation Patterns]

## Dev Agent Record

### Context Reference
- RulebookBottomBar from Story 2.3
- RulebookFAB from Story 2.4
- LibraryScreen from Story 2.5
- SettingsScreen from Story 2.6
- Navigation routes from Story 2.2

### Agent Model Used
claude-opus-4-5-20251101

### Debug Log References

### Completion Notes List
- Task 1: Created RulebookScaffold composable with bottom bar, FAB, and proper WindowInsets handling. Added shouldShowBottomBar() and shouldShowFab() helper functions with comprehensive unit tests.
- Task 2: Added AnimatedVisibility with slideInVertically/slideOutVertically animations for smooth bottom bar show/hide transitions.

### File List
- app/src/main/kotlin/com/rulebook/navigation/RulebookScaffold.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/RulebookScaffoldTest.kt (new)

## Dependencies

- **Depends On:** Stories 2.3, 2.4, 2.5, 2.6
- **Blocks:** None (final integration story)
- **Can Parallel With:** Story 2.8

### Dependency Rationale
- Story 2.3: Requires RulebookBottomBar component
- Story 2.4: Requires RulebookFAB component
- Story 2.5: Requires LibraryScreen to exist
- Story 2.6: Requires SettingsScreen to exist
- Story 2.8: Can develop in parallel (handles different navigation concern)
