# Story 1.9: Core UI Components - RulebookButton

Status: ready-for-dev

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

- [ ] Task 1: Create ButtonVariant enum (AC: #1)
  - [ ] Create sealed class or enum for Primary, Secondary, Destructive
- [ ] Task 2: Create RulebookButton composable (AC: #1, #2, #3, #4, #5)
  - [ ] Define composable function signature
  - [ ] Accept text, onClick, enabled, modifier parameters
  - [ ] Accept variant parameter (default Primary)
  - [ ] Apply variant-specific colors
  - [ ] Apply brutalist border and shadow
  - [ ] Apply Black weight typography
  - [ ] Handle disabled state with reduced opacity
  - [ ] Use Material ripple for touch feedback
- [ ] Task 3: Implement Primary variant (AC: #1)
  - [ ] Pink fill color (#E91E63 light, #F06292 dark)
  - [ ] Black border
  - [ ] Offset shadow
- [ ] Task 4: Implement Secondary variant (AC: #1)
  - [ ] No fill (transparent background)
  - [ ] Black border only
  - [ ] No shadow
- [ ] Task 5: Implement Destructive variant (AC: #1)
  - [ ] Red fill color (#E74C3C light, #EC7063 dark)
  - [ ] Black border
  - [ ] Offset shadow
- [ ] Task 6: Create Preview composables
  - [ ] Preview all variants
  - [ ] Preview enabled/disabled states
  - [ ] Preview light/dark modes

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

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
