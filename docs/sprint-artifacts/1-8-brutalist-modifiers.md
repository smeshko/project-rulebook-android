# Story 1.8: Brutalist Modifier Extensions

Status: Ready for Review

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

- [x] Task 1: Create Modifier.brutalistShadow() (AC: #1, #3)
  - [x] Create extension function on Modifier
  - [x] Use drawBehind for custom shadow rendering
  - [x] Draw solid black rectangle offset by specified amount
  - [x] Make offset configurable (default 4dp)
  - [x] Make color configurable (default black)
  - [x] Ensure works with any composable
- [x] Task 2: Create Modifier.brutalistBorder() (AC: #2, #3)
  - [x] Create extension function on Modifier
  - [x] Apply BorderStroke with configurable width
  - [x] Default width = 3dp from RulebookSpacing
  - [x] Use RectangleShape for sharp corners
  - [x] Make width and color configurable
- [x] Task 3: Create combined modifier (AC: #3)
  - [x] Create Modifier.brutalistCard() convenience modifier
  - [x] Combine shadow + border in one call
  - [x] Use sensible defaults for card styling

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
claude-opus-4-5-20251101

### Debug Log References

### Completion Notes List
- Task 1: Implemented Modifier.brutalistShadow() extension function using drawBehind to draw a solid black rectangle offset from the composable. Default offset uses BrutalistShadowOffset (4dp), configurable via parameters. Unit tests added and passing.
- Task 2: Implemented Modifier.brutalistBorder() extension function using Compose border() with RectangleShape for sharp corners. Default width uses BrutalistBorderWidth (3dp), configurable via parameters. Unit tests added and passing.
- Task 3: Implemented Modifier.brutalistCard() convenience modifier that combines brutalistShadow() and brutalistBorder() in one call with all parameters configurable. Unit tests added and passing.

### File List
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/modifier/BrutalistModifiers.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/modifier/BrutalistModifiersTest.kt (new)

## Dependencies

- **Depends On:** Story 1.7
- **Blocks:** Story 1.9, Story 1.10
- **Can Parallel With:** Story 1.11

### Dependency Rationale
- Story 1.7: Brutalist modifiers use RulebookSpacing values (shadowOffset, borderWidth)
- Story 1.9: RulebookButton uses brutalistShadow() and brutalistBorder() modifiers
- Story 1.10: RulebookCard uses brutalistShadow() and brutalistBorder() modifiers
