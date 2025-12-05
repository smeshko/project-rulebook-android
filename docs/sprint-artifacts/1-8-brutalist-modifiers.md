# Story 1.8: Brutalist Modifier Extensions

Status: ready-for-dev

## Linear Issue

- **ID:** RULE-115
- **URL:** https://linear.app/project-rulebook/issue/RULE-115/story-18-brutalist-modifier-extensions

## Story

As a developer,
I want brutalist modifier extensions for shadows and borders,
So that components can easily apply the signature visual style.

## Acceptance Criteria

1. **Given** the design tokens are implemented
   **When** modifier extensions are created
   **Then** `Modifier.brutalistShadow()` applies:
   - Offset rectangle shadow (default 4dp offset)
   - Configurable offset and color
   - Works with any composable

2. **And** `Modifier.brutalistBorder()` applies:
   - Thick border (default 3dp)
   - Configurable width and color
   - Sharp corners (0dp)

3. **And** modifiers compose cleanly with other modifiers

## Tasks / Subtasks

- [ ] Task 1: Create Modifier.brutalistShadow() (AC: #1, #3)
  - [ ] Create extension function on Modifier
  - [ ] Use drawBehind for custom shadow rendering
  - [ ] Draw solid black rectangle offset by specified amount
  - [ ] Make offset configurable (default 4dp)
  - [ ] Make color configurable (default black)
  - [ ] Ensure works with any composable
- [ ] Task 2: Create Modifier.brutalistBorder() (AC: #2, #3)
  - [ ] Create extension function on Modifier
  - [ ] Apply BorderStroke with configurable width
  - [ ] Default width = 3dp from RulebookSpacing
  - [ ] Use RectangleShape for sharp corners
  - [ ] Make width and color configurable
- [ ] Task 3: Create combined modifier (AC: #3)
  - [ ] Create Modifier.brutalistCard() convenience modifier
  - [ ] Combine shadow + border in one call
  - [ ] Use sensible defaults for card styling

## Dev Notes

### Implementation Pattern

```kotlin
fun Modifier.brutalistShadow(
    offset: Dp = RulebookSpacing.shadowOffset,
    color: Color = Color.Black
): Modifier = this.drawBehind {
    drawRect(
        color = color,
        topLeft = Offset(offset.toPx(), offset.toPx()),
        size = size
    )
}

fun Modifier.brutalistBorder(
    width: Dp = RulebookSpacing.borderWidth,
    color: Color = Color.Black
): Modifier = this.border(
    width = width,
    color = color,
    shape = RectangleShape
)

// Convenience modifier combining both
fun Modifier.brutalistCard(
    shadowOffset: Dp = RulebookSpacing.shadowOffset,
    borderWidth: Dp = RulebookSpacing.borderWidth,
    shadowColor: Color = Color.Black,
    borderColor: Color = Color.Black
): Modifier = this
    .brutalistShadow(offset = shadowOffset, color = shadowColor)
    .brutalistBorder(width = borderWidth, color = borderColor)
```

### UX Design Specification

| Element | Value |
|---------|-------|
| Border width | 3dp standard, 4dp thick |
| Shadow offset | 4dp (normal) to 12dp (elevated) |
| Corner radius | 0dp (sharp corners) |

### Key Visual Principles

- Shadow is solid rectangle, not blur
- Shadow is offset (not centered)
- Border uses BorderStroke with RectangleShape
- Sharp corners everywhere (0dp radius)

### Project Structure Notes

- Place in core/designsystem/modifier/
- Export as public extension functions
- Used by all Rulebook custom components

### References

- [Source: docs/ux-design-specification.md#Spacing & Layout Foundation]
- [Source: docs/ux-design-specification.md#Design Direction: Brutalist]

## Dev Agent Record

### Context Reference

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
