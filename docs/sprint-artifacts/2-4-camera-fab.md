# Story 2.4: Floating Action Button for Camera

Status: ready-for-dev

## Story

As a user,
I want a prominent camera button always visible,
So that I can quickly scan a game from any main screen.

## Acceptance Criteria

1. **Given** the user is on Library or Settings screen
   **When** the FAB is displayed
   **Then** it appears in the standard FAB position (bottom-right, above nav bar)

2. **And** it shows a camera icon

3. **And** it has brutalist styling (border, shadow, pink fill)

4. **And** tapping it navigates to the camera screen

5. **And** the FAB respects edge-to-edge (proper insets)

6. **And** the FAB has ripple feedback on press

## Tasks / Subtasks

- [x] Task 1: Create RulebookFAB composable (AC: #2, #3, #6)
  - [x] Create composable in core/designsystem/component
  - [x] Use Material 3 FloatingActionButton as base
  - [x] Add camera icon
  - [x] Apply brutalist styling (border, shadow, pink fill)
  - [x] Ensure ripple feedback is present
- [x] Task 2: Apply brutalist styling (AC: #3)
  - [x] Pink fill color (#E91E63)
  - [x] Black border (3dp)
  - [x] Offset shadow (4dp)
  - [x] Use brutalistShadow and brutalistBorder modifiers
- [ ] Task 3: Configure FAB position (AC: #1, #5)
  - [ ] Standard FAB position (bottom-right)
  - [ ] Above navigation bar with proper spacing
  - [ ] Respect edge-to-edge insets
- [ ] Task 4: Wire navigation callback (AC: #4)
  - [ ] Accept onClick callback parameter
  - [ ] Navigate to camera screen when tapped
- [ ] Task 5: Create RulebookFAB preview composables
  - [ ] Preview in light mode
  - [ ] Preview in dark mode
  - [ ] Preview pressed state

## Dev Notes

### Architecture Context

- **Module:** core/designsystem (reusable component)
- **Package:** com.rulebook.core.designsystem.component
- **Key Files:** RulebookFAB.kt

### Implementation Pattern

```kotlin
// RulebookFAB.kt
@Composable
fun RulebookFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.CameraAlt,
    contentDescription: String = "Camera"
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .brutalistShadow()
            .brutalistBorder(),
        containerColor = RulebookTheme.colors.pink,
        contentColor = Color.Black,
        shape = RectangleShape,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp, // We use brutalist shadow instead
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp)
        )
    }
}
```

### Brutalist FAB Styling

| Property | Value |
|----------|-------|
| Fill color | Pink (#E91E63) |
| Border | 3dp black |
| Shadow | 4dp offset (brutalist) |
| Shape | RectangleShape (0dp corners) |
| Icon color | Black |
| Icon size | 24dp |
| Min size | 56dp (standard FAB) |

### Scaffold Integration

The FAB will be placed in Scaffold's `floatingActionButton` slot in Story 2.7:

```kotlin
Scaffold(
    bottomBar = { RulebookBottomBar(...) },
    floatingActionButton = {
        RulebookFAB(
            onClick = { navController.navigate(Route.Camera.route) }
        )
    },
    floatingActionButtonPosition = FabPosition.End
) { /* content */ }
```

### FAB Visibility Logic

The FAB should only be visible on main screens (Library, Settings). This visibility logic will be handled in Story 2.7:

```kotlin
val showFab = currentRoute in listOf(Route.Library.route, Route.Settings.route)

Scaffold(
    floatingActionButton = {
        if (showFab) {
            RulebookFAB(onClick = navigateToCamera)
        }
    }
)
```

### Edge-to-Edge Considerations

- Scaffold handles FAB positioning relative to navigation bar insets
- No additional inset handling needed in FAB component itself
- Position is handled by Scaffold's floatingActionButtonPosition

### References

- [Source: docs/architecture.md#Implementation Patterns & Consistency Rules]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.4]
- [Source: docs/ux-design-specification.md#Component Strategy]
- [Pattern: Epic 1 RulebookButton brutalist styling]

## Dev Agent Record

### Context Reference
- Brutalist modifiers from Story 1.8
- RulebookColors.pink from Story 1.7
- RulebookButton pattern for brutalist styling

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 2.3 (Bottom Navigation Bar)
- **Blocks:** Story 2.5
- **Can Parallel With:** Story 2.6

### Dependency Rationale
- Story 2.3: FAB positioning is relative to bottom nav bar
- Story 2.5: Library screen references FAB for camera CTA integration
- Story 2.6: Can develop in parallel (different component)
