# Story 1.10: Core UI Components - RulebookCard

Status: ready-for-dev

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

- [ ] Task 1: Create RulebookCard composable (AC: #1, #2, #3)
  - [ ] Define composable function signature
  - [ ] Accept modifier, onClick, content parameters
  - [ ] Apply surface background from theme
  - [ ] Apply brutalist border (3dp)
  - [ ] Apply brutalist shadow (4dp offset)
  - [ ] Set corner radius to 0dp
  - [ ] Add 16dp internal padding
- [ ] Task 2: Handle clickable state (AC: #2, #3)
  - [ ] Make card clickable when onClick is provided
  - [ ] Apply ripple effect for clickable cards
  - [ ] Keep non-clickable for cards without onClick
- [ ] Task 3: Create elevated variant
  - [ ] Create variant with larger shadow offset (8dp)
  - [ ] Use for emphasized cards or expanded states
- [ ] Task 4: Create Preview composables
  - [ ] Preview non-clickable card
  - [ ] Preview clickable card
  - [ ] Preview elevated card
  - [ ] Preview light/dark modes

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

### File List
