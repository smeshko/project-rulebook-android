# Story 1.11: Core UI Components - RulebookHeaderBar

Status: ready-for-dev

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

- [ ] Task 1: Create RulebookHeaderBar composable (AC: #1, #2)
  - [ ] Define composable function signature
  - [ ] Accept title, onBackClick, actions parameters
  - [ ] Use TopAppBar as base
  - [ ] Apply brutalist title typography (24sp Black)
  - [ ] Add thick bottom border (3dp)
- [ ] Task 2: Implement back navigation (AC: #1, #2)
  - [ ] Show back icon when onBackClick is provided
  - [ ] Use standard Android back arrow
  - [ ] Apply click handling
- [ ] Task 3: Implement action buttons slot (AC: #1, #2)
  - [ ] Add actions slot for trailing buttons
  - [ ] Use RowScope for proper layout
- [ ] Task 4: Handle edge-to-edge insets (AC: #3)
  - [ ] Apply WindowInsets for status bar padding
  - [ ] Ensure content doesn't overlap status bar
- [ ] Task 5: Create Preview composables
  - [ ] Preview with title only
  - [ ] Preview with back button
  - [ ] Preview with action buttons
  - [ ] Preview light/dark modes

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

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
