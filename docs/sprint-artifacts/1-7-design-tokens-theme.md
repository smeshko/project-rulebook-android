# Story 1.7: Design Tokens & Theme Foundation

Status: Ready for Review

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

- [x] Task 1: Create RulebookColors.kt (AC: #1)
  - [x] Define surface palette for light mode
  - [x] Define surface palette for dark mode
  - [x] Define content palette with opacity variants
  - [x] Define accent color palette
  - [x] Create lightColorScheme() function
  - [x] Create darkColorScheme() function
- [x] Task 2: Create RulebookTypography.kt (AC: #2)
  - [x] Define display styles (Large Title, Title, Title 2)
  - [x] Define brutalist styles (Title, Section Title, Button Text)
  - [x] Define body styles (Body, Callout)
  - [x] Define caption style
  - [x] Create RulebookTypography object
- [x] Task 3: Create RulebookSpacing.kt (AC: #3)
  - [x] Define spacing scale (xs, sm, md, lg, xl)
  - [x] Define brutalist specifications (borderWidth, shadowOffset)
  - [x] Create RulebookSpacing object
- [x] Task 4: Create RulebookShapes.kt (AC: #4)
  - [x] Define shapes with 0dp corner radius
  - [x] Create RulebookShapes object
- [x] Task 5: Create RulebookTheme.kt (AC: #4)
  - [x] Create RulebookTheme composable
  - [x] Apply custom colors based on dark/light mode
  - [x] Apply custom typography
  - [x] Apply custom shapes
  - [x] Wrap MaterialTheme with customizations

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
- UX Design Specification: docs/ux-design-specification.md (Color System, Typography System, Spacing & Layout Foundation)
- iOS Design Tokens: docs/ios/design-system-android.json

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List
- **Task 1 (RulebookColors.kt):** Implemented complete color token system with surface palette (Primary/Secondary/Tertiary for light and dark modes), content palette with opacity variants (100%/70%/40%), and accent palette (Orange, Blue, Yellow, Purple, Pink, Green, Red). Created RulebookExtendedColors data class for Rulebook-specific semantic colors, LocalRulebookColors composition local, and Material 3 color scheme functions (rulebookLightColorScheme/rulebookDarkColorScheme). All 28 unit tests pass.
- **Task 2 (RulebookTypography.kt):** Implemented typography token system with Display styles (34sp/28sp/22sp), Brutalist styles using Black weight (24sp/16sp/14sp), Body styles (17sp/16sp), and Caption (12sp). Created RulebookExtendedTypography data class, LocalRulebookTypography composition local, and Material 3 Typography mapped to Rulebook styles. All 23 unit tests pass.
- **Task 3 (RulebookSpacing.kt):** Implemented spacing token system with scale (xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp), brutalist specifications (borderWidth 3dp/4dp, shadowOffset 4dp/8dp/12dp, cornerRadius 0dp), and layout specs (screenMargin, cardPadding, gridSpacing, tabBarHeight). Created RulebookSpacingValues data class and LocalRulebookSpacing composition local. All 22 unit tests pass.
- **Task 4 (RulebookShapes.kt):** Implemented shapes with 0dp corner radius for brutalist aesthetic. All shape definitions use RectangleShape. Created Material 3 Shapes with 0dp RoundedCornerShape and RulebookExtendedShapes data class. All 17 unit tests pass.
- **Task 5 (RulebookTheme.kt):** Replaced basic theme with complete three-layer design system implementation. RulebookTheme composable applies custom colorScheme, typography, and shapes to MaterialTheme. Provides CompositionLocals for extended tokens. Created RulebookTheme object for convenient access to colors, typography, spacing, and shapes. All 17 unit tests pass. Full regression test suite (107 tests) passes.

### File List
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookColors.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/theme/RulebookColorsTest.kt (new)
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookTypography.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/theme/RulebookTypographyTest.kt (new)
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookSpacing.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/theme/RulebookSpacingTest.kt (new)
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookShapes.kt (new)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/theme/RulebookShapesTest.kt (new)
- core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookTheme.kt (modified)
- core/designsystem/src/test/kotlin/com/rulebook/core/designsystem/theme/RulebookThemeTest.kt (new)
- core/designsystem/build.gradle.kts (modified - added test dependency)

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** Story 1.8, Story 1.11
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.4, Story 1.5, Story 1.6, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: Design tokens require designsystem module to exist
- Story 1.8: Brutalist modifiers need design tokens for default values (spacing, colors)
- Story 1.11: HeaderBar component needs theme foundation for styling
