# Story 1.11: Core UI Components - RulebookHeaderBar

Status: Ready for Review

## Linear Issue

- **ID:** RULE-118
- **URL:** https://linear.app/project-rulebook/issue/RULE-118/story-111-core-ui-components-rulebookheaderbar

## Story

As a developer,
I want the RulebookHeaderBar component implemented,
So that screen headers have consistent styling.

## Acceptance Criteria

1. **Given** the theme exists
   **When** RulebookHeaderBar is implemented
   **Then** it displays:
   - Title text in brutalist style (24sp Black)
   - Thick bottom border (3dp)
   - Optional back navigation icon
   - Optional action buttons

2. **And** it accepts:
   - `title: String`
   - `onBackClick: (() -> Unit)?`
   - `actions: @Composable RowScope.() -> Unit`

3. **And** it respects status bar insets (edge-to-edge)

## Tasks / Subtasks

- [x] Task 1: Create RulebookHeaderBar composable (AC: #1, #2)
  - [x] Define composable function signature
  - [x] Accept title, onBackClick, actions parameters
  - [x] Use TopAppBar as base
  - [x] Apply brutalist title typography (24sp Black)
  - [x] Add thick bottom border (3dp)
- [x] Task 2: Implement back navigation (AC: #1, #2)
  - [x] Show back icon when onBackClick is provided
  - [x] Use standard Android back arrow
  - [x] Apply click handling
- [x] Task 3: Implement action buttons slot (AC: #1, #2)
  - [x] Add actions slot for trailing buttons
  - [x] Use RowScope for proper layout
- [x] Task 4: Handle edge-to-edge insets (AC: #3)
  - [x] Apply WindowInsets for status bar padding
  - [x] Ensure content doesn't overlap status bar
- [x] Task 5: Create Preview composables
  - [x] Preview with title only
  - [x] Preview with back button
  - [x] Preview with action buttons
  - [x] Preview light/dark modes

## Dev Notes

### Implementation Pattern

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulebookHeaderBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = RulebookTypography.brutalistTitle
                )
            },
            navigationIcon = {
                if (onBackClick != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.statusBars
            )
        )
        // Bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RulebookSpacing.borderWidth)
                .background(Color.Black)
        )
    }
}
```

### UX Specifications

| Property | Value |
|----------|-------|
| Title typography | 24sp Black weight |
| Bottom border | 3dp black |
| Back icon | Standard Android arrow |
| Status bar | Edge-to-edge with insets |

### Usage Examples

```kotlin
// Simple header
RulebookHeaderBar(title = "Library")

// With back navigation
RulebookHeaderBar(
    title = "Rules",
    onBackClick = { navController.navigateUp() }
)

// With actions
RulebookHeaderBar(
    title = "Game Rules",
    onBackClick = { navController.navigateUp() },
    actions = {
        IconButton(onClick = { /* share */ }) {
            Icon(Icons.Default.Share, "Share")
        }
    }
)
```

### Project Structure Notes

- Use TopAppBar as base with custom styling
- Back icon is standard Android arrow (AutoMirrored)
- WindowInsets for status bar padding

### References

- [Source: docs/ux-design-specification.md#Component Strategy]
- [Source: docs/architecture.md#Platform Integration] - Edge-to-edge

## Dev Agent Record

### Context Reference
- Loaded design system tokens from Story 1.7 (RulebookTypography, RulebookSpacing, RulebookTheme)

### Agent Model Used
- claude-opus-4-5-20251101

### Debug Log References

### Completion Notes List
- Tasks 1-4: Implemented RulebookHeaderBar composable with all core functionality:
  - Function signature: `RulebookHeaderBar(title: String, modifier: Modifier, onBackClick: (() -> Unit)?, actions: @Composable RowScope.() -> Unit)`
  - Title uses `RulebookTheme.typography.brutalistTitle` (24sp Black weight)
  - Bottom border uses `RulebookTheme.spacing.borderWidth` (3dp)
  - Back navigation shows `Icons.AutoMirrored.Filled.ArrowBack` when `onBackClick` is provided
  - Actions slot supports trailing IconButtons via RowScope
  - Status bar insets handled via `Modifier.windowInsetsPadding(WindowInsets.statusBars)`
- Unit tests created and passing
- Task 5: Added 6 preview composables covering all combinations:
  - Title only (light/dark)
  - With back button (light/dark)
  - With action buttons (light/dark)

### File List
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookHeaderBar.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/component/RulebookHeaderBarTest.kt (new)

## Dependencies

- **Depends On:** Story 1.7
- **Blocks:** None
- **Can Parallel With:** Story 1.8

### Dependency Rationale
- Story 1.7: RulebookHeaderBar requires theme foundation for styling (typography, colors, spacing)
