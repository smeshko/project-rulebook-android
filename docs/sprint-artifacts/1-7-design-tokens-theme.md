# Story 1.7: Design Tokens & Theme Foundation

Status: ready-for-dev

## Linear Issue

- **ID:** RULE-114
- **URL:** https://linear.app/project-rulebook/issue/RULE-114/story-17-design-tokens-and-theme-foundation

## Story

As a developer,
I want the Rulebook theme and design tokens implemented,
So that the brutalist design system is consistently applied.

## Acceptance Criteria

1. **Given** the `core/designsystem` module
   **When** design tokens are implemented
   **Then** `RulebookColors.kt` defines:
   - Light/Dark surface palette (Primary #FFFFFF/#1C1C1E, Secondary #FFF9F0/#2C2C2E)
   - Content palette with opacity variants
   - Accent palette (Orange #FF6B35, Blue #3498DB, Yellow #FFD23F, Purple #7209B7, Pink #E91E63, Green #2ECC71, Red #E74C3C)

2. **And** `RulebookTypography.kt` defines:
   - Display styles (34sp Bold, 28sp Bold, 22sp SemiBold)
   - Brutalist styles (24sp Black, 16sp Black, 14sp Black)
   - Body styles (17sp Regular, 16sp Regular)
   - Caption (12sp Regular)

3. **And** `RulebookSpacing.kt` defines:
   - xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp
   - Brutalist: borderWidth=3dp, shadowOffset=4dp

4. **And** `RulebookTheme.kt` wraps MaterialTheme with custom colors, typography, shapes (0dp corners)

## Tasks / Subtasks

- [ ] Task 1: Create RulebookColors.kt (AC: #1)
  - [ ] Define surface palette for light mode
  - [ ] Define surface palette for dark mode
  - [ ] Define content palette with opacity variants
  - [ ] Define accent color palette
  - [ ] Create lightColorScheme() function
  - [ ] Create darkColorScheme() function
- [ ] Task 2: Create RulebookTypography.kt (AC: #2)
  - [ ] Define display styles (Large Title, Title, Title 2)
  - [ ] Define brutalist styles (Title, Section Title, Button Text)
  - [ ] Define body styles (Body, Callout)
  - [ ] Define caption style
  - [ ] Create RulebookTypography object
- [ ] Task 3: Create RulebookSpacing.kt (AC: #3)
  - [ ] Define spacing scale (xs, sm, md, lg, xl)
  - [ ] Define brutalist specifications (borderWidth, shadowOffset)
  - [ ] Create RulebookSpacing object
- [ ] Task 4: Create RulebookShapes.kt (AC: #4)
  - [ ] Define shapes with 0dp corner radius
  - [ ] Create RulebookShapes object
- [ ] Task 5: Create RulebookTheme.kt (AC: #4)
  - [ ] Create RulebookTheme composable
  - [ ] Apply custom colors based on dark/light mode
  - [ ] Apply custom typography
  - [ ] Apply custom shapes
  - [ ] Wrap MaterialTheme with customizations

## Dev Notes

### Design System Architecture

**Three-layer approach:**
1. Material 3 Foundation - accessibility, Android conventions
2. Rulebook Theme - custom tokens from iOS design
3. Rulebook Components - brutalist aesthetic

### Color Tokens

#### Surface Palette
| Role | Light | Dark |
|------|-------|------|
| Primary | `#FFFFFF` | `#1C1C1E` |
| Secondary | `#FFF9F0` | `#2C2C2E` |
| Tertiary | `#F5E6D3` | `#3A3A3C` |

#### Content Palette
| Role | Light | Dark |
|------|-------|------|
| Primary | `#000000` | `#FFFFFF` |
| Secondary (70%) | `#000000B2` | `#FFFFFFB2` |
| Tertiary (40%) | `#00000066` | `#FFFFFF66` |

#### Accent Palette
| Color | Light | Dark | Usage |
|-------|-------|------|-------|
| Orange | `#FF6B35` | `#FF8C5F` | Overview, warnings |
| Blue | `#3498DB` | `#5DADE2` | Setup, info |
| Yellow | `#FFD23F` | `#FFE066` | First Round |
| Purple | `#7209B7` | `#9D4EDD` | Advanced Rules |
| Pink | `#E91E63` | `#F06292` | Actions, buttons |
| Green | `#2ECC71` | `#58D68D` | Success |
| Red | `#E74C3C` | `#EC7063` | Errors, destructive |

### Typography System

| Category | Style | Size | Weight |
|----------|-------|------|--------|
| Display | Large Title | 34sp | Bold |
| Display | Title | 28sp | Bold |
| Display | Title 2 | 22sp | SemiBold |
| Brutalist | Title | 24sp | Black (900) |
| Brutalist | Section Title | 16sp | Black |
| Brutalist | Button Text | 14sp | Black |
| Body | Body | 17sp | Regular |
| Body | Callout | 16sp | Regular |
| Detail | Caption | 12sp | Regular |

### Implementation Pattern

```kotlin
@Composable
fun RulebookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) rulebookDarkColors else rulebookLightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RulebookTypography,
        shapes = RulebookShapes,
        content = content
    )
}
```

### References

- [Source: docs/ux-design-specification.md#Color System]
- [Source: docs/ux-design-specification.md#Typography System]
- [Source: docs/ux-design-specification.md#Spacing & Layout Foundation]
- [Source: docs/ux-design-specification.md#Design System Foundation]
- [Source: docs/ios/design-system-android.json]

## Dev Agent Record

### Context Reference

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
