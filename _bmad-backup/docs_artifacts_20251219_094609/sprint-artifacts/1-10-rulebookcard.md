# Story 1.10: Core UI Components - RulebookCard

Status: done

## Linear Issue

- **ID:** RULE-117
- **URL:** https://linear.app/project-rulebook/issue/RULE-117/story-110-core-ui-components-rulebookcard

## Story

As a developer,
I want the RulebookCard component implemented,
So that cards have consistent brutalist styling.

## Acceptance Criteria

1. **Given** the theme and modifiers exist
   **When** RulebookCard is implemented
   **Then** it applies:
   - Surface background color
   - 3dp black border
   - 4dp offset shadow
   - 0dp corner radius
   - 16dp internal padding

2. **And** it accepts:
   - `modifier: Modifier`
   - `onClick: (() -> Unit)?` (optional click handling)
   - `content: @Composable () -> Unit`

3. **And** clickable cards show ripple effect

## Tasks / Subtasks

- [x] Task 1: Create RulebookCard composable (AC: #1, #2, #3)
  - [x] Define composable function signature
  - [x] Accept modifier, onClick, content parameters
  - [x] Apply surface background from theme
  - [x] Apply brutalist border (3dp)
  - [x] Apply brutalist shadow (4dp offset)
  - [x] Set corner radius to 0dp
  - [x] Add 16dp internal padding
- [x] Task 2: Handle clickable state (AC: #2, #3)
  - [x] Make card clickable when onClick is provided
  - [x] Apply ripple effect for clickable cards
  - [x] Keep non-clickable for cards without onClick
- [x] Task 3: Create elevated variant
  - [x] Create variant with larger shadow offset (8dp)
  - [x] Use for emphasized cards or expanded states
- [x] Task 4: Create Preview composables
  - [x] Preview non-clickable card
  - [x] Preview clickable card
  - [x] Preview elevated card
  - [x] Preview light/dark modes

## Dev Notes

### Implementation Pattern

```kotlin
@Composable
fun RulebookCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shadowOffset: Dp = RulebookSpacing.shadowOffset,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .brutalistShadow(offset = shadowOffset)
        .brutalistBorder()
        .background(MaterialTheme.colorScheme.surface)
        .then(
            if (onClick != null) {
                Modifier.clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple()
                )
            } else Modifier
        )
        .padding(RulebookSpacing.md)

    Box(modifier = cardModifier) {
        content()
    }
}
```

### UX Specifications

| Property | Value |
|----------|-------|
| Background | Surface color from theme |
| Border width | 3dp |
| Shadow offset | 4dp (normal), 8dp (elevated) |
| Corner radius | 0dp |
| Internal padding | 16dp |

### Usage Examples

```kotlin
// Non-clickable card
RulebookCard {
    Text("Card content")
}

// Clickable card
RulebookCard(onClick = { /* handle click */ }) {
    Text("Tappable card")
}

// Elevated card
RulebookCard(shadowOffset = 8.dp) {
    Text("Emphasized card")
}
```

### Component Relationships

- RulebookCard is base for:
  - GameCard (library grid items)
  - ProductCard (paywall items)
  - CollapsibleSection (when expanded)

### References

- [Source: docs/ux-design-specification.md#Component Strategy]
- [Source: docs/ux-design-specification.md#Spacing & Layout Foundation]

## Dev Agent Record

### Context Reference

### Agent Model Used

### Debug Log References

### Completion Notes List

- Tasks 1-3: Implemented RulebookCard composable with brutalist styling (surface background, 3dp border, 4dp shadow, 0dp corners, 16dp padding). Added optional onClick with ripple effect. Created ElevatedRulebookCard variant with 8dp shadow offset.
- Task 4: Added 6 preview composables for non-clickable, clickable, and elevated cards in both light and dark themes.

### File List

- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookCard.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/component/RulebookCardTest.kt (new)

## Dependencies

- **Depends On:** Story 1.8
- **Blocks:** None
- **Can Parallel With:** Story 1.9

### Dependency Rationale
- Story 1.8: RulebookCard requires brutalist modifiers (brutalistShadow, brutalistBorder)
