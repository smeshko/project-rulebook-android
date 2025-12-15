# Story 1.9: Core UI Components - RulebookButton

Status: in-progress

## Linear Issue

- **ID:** RULE-116
- **URL:** https://linear.app/project-rulebook/issue/RULE-116/story-19-core-ui-components-rulebookbutton

## Story

As a developer,
I want the RulebookButton component implemented,
So that all buttons have consistent brutalist styling.

## Acceptance Criteria

1. **Given** the theme and modifiers exist
   **When** RulebookButton is implemented
   **Then** it supports variants:
   - `Primary` - Filled pink (#E91E63), border, offset shadow
   - `Secondary` - Outlined, no fill, border only
   - `Destructive` - Filled red (#E74C3C), border, offset shadow

2. **And** it accepts:
   - `text: String`
   - `onClick: () -> Unit`
   - `enabled: Boolean`
   - `modifier: Modifier`

3. **And** disabled state shows reduced opacity

4. **And** touch feedback uses ripple effect

5. **And** text uses Black weight (900) typography

## Tasks / Subtasks

- [x] Task 1: Create ButtonVariant enum (AC: #1)
  - [x] Create sealed class or enum for Primary, Secondary, Destructive
- [x] Task 2: Create RulebookButton composable (AC: #1, #2, #3, #4, #5)
  - [x] Define composable function signature
  - [x] Accept text, onClick, enabled, modifier parameters
  - [x] Accept variant parameter (default Primary)
  - [x] Apply variant-specific colors
  - [x] Apply brutalist border and shadow
  - [x] Apply Black weight typography
  - [x] Handle disabled state with reduced opacity
  - [x] Use Material ripple for touch feedback
- [x] Task 3: Implement Primary variant (AC: #1)
  - [x] Pink fill color (#E91E63 light, #F06292 dark)
  - [x] Black border
  - [x] Offset shadow
- [x] Task 4: Implement Secondary variant (AC: #1)
  - [x] No fill (transparent background)
  - [x] Black border only
  - [x] No shadow
- [x] Task 5: Implement Destructive variant (AC: #1)
  - [x] Red fill color (#E74C3C light, #EC7063 dark)
  - [x] Black border
  - [x] Offset shadow
- [x] Task 6: Create Preview composables
  - [x] Preview all variants
  - [x] Preview enabled/disabled states
  - [x] Preview light/dark modes

## Dev Notes

### Implementation Pattern

```kotlin
enum class ButtonVariant {
    Primary, Secondary, Destructive
}

@Composable
fun RulebookButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true
) {
    val backgroundColor = when (variant) {
        ButtonVariant.Primary -> RulebookColors.pink
        ButtonVariant.Secondary -> Color.Transparent
        ButtonVariant.Destructive -> RulebookColors.red
    }

    val hasShadow = variant != ButtonVariant.Secondary

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .then(if (hasShadow) Modifier.brutalistShadow() else Modifier)
            .brutalistBorder(),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.Black,
            disabledContainerColor = backgroundColor.copy(alpha = 0.5f),
            disabledContentColor = Color.Black.copy(alpha = 0.5f)
        ),
        shape = RectangleShape
    ) {
        Text(
            text = text,
            style = RulebookTypography.buttonText // Black weight
        )
    }
}
```

### UX Specifications

| Property | Value |
|----------|-------|
| Min touch target | 48dp |
| Horizontal padding | 20dp |
| Font weight | Black (900) |
| Corner radius | 0dp |
| Border width | 3dp |
| Shadow offset | 4dp |

### Button Hierarchy

| Type | Usage | Style |
|------|-------|-------|
| Primary | Main CTA | Filled pink, border, shadow |
| Secondary | Cancel, alternatives | Outlined, no fill |
| Destructive | Delete, clear | Filled red, border, shadow |

**Rule:** Maximum one primary button per screen/modal.

### References

- [Source: docs/ux-design-specification.md#Button Hierarchy]
- [Source: docs/ux-design-specification.md#Component Strategy]
- [Source: docs/ux-design-specification.md#UX Consistency Patterns]

## Dev Agent Record

### Context Reference
- RulebookTheme, RulebookColors, RulebookTypography for theme tokens
- BrutalistModifiers (brutalistShadow, brutalistBorder) from Story 1.8
- RulebookHeaderBar pattern for component structure

### Agent Model Used
- Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- All tests pass: RulebookButtonTest (6 unit tests)

### Completion Notes List
- Implemented ButtonVariant enum with Primary, Secondary, Destructive variants
- Created RulebookButton composable with full API (text, onClick, modifier, variant, enabled)
- Primary: Pink fill (RulebookTheme.colors.pink), black border, offset shadow
- Secondary: Transparent background, black border only, no shadow
- Destructive: Red fill (RulebookTheme.colors.red), black border, offset shadow
- Disabled state uses 50% opacity on container and content colors
- Material Button provides ripple touch feedback by default
- Text uses brutalistButtonText typography (14sp, Black weight)
- Min touch target 48dp, horizontal padding 20dp, RectangleShape (0dp corners)
- 10 preview composables covering all variants, enabled/disabled, light/dark modes

### File List
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookButton.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/component/RulebookButtonTest.kt (new)
- docs/sprint-artifacts/1-9-rulebookbutton.md (modified)

## Dependencies

- **Depends On:** Story 1.8
- **Blocks:** None
- **Can Parallel With:** Story 1.10

### Dependency Rationale
- Story 1.8: RulebookButton requires brutalist modifiers (brutalistShadow, brutalistBorder)
