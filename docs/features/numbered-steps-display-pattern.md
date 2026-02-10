# Numbered Steps Display Pattern

**Date:** 2026-02-10
**Related Files:**
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/NumberedStepItem.kt`
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/CollapsibleRuleSection.kt`

## Overview

The numbered steps display pattern provides a reusable way to present sequential, ordered content in collapsible rule sections. It's specifically designed for step-by-step guides like the First Round section, but the pattern can be applied to any sequential content that needs clear ordering and visual hierarchy.

## What Was Built

- **NumberedStepItem** - A new composable component that displays a single step with a yellow accent circle badge containing the step number, followed by the step text
- **useNumberedSteps parameter** - An optional parameter added to CollapsibleRuleSection that switches rendering mode from bullets to numbered steps
- **Numbered steps rendering mode** - A third rendering mode alongside existing bullet and checklist modes in CollapsibleRuleSection

## Technical Implementation

### Key Files

- **`NumberedStepItem.kt`** - The core component that renders a single numbered step. Displays:
  - A 24dp circular badge with semi-transparent yellow background (#FFD23F/#FFE066)
  - Solid yellow step number text inside the badge
  - Step text using `bodyMedium` typography
  - Full-width layout with horizontal centering

- **`CollapsibleRuleSection.kt`** - Extended with:
  - New `useNumberedSteps: Boolean = false` parameter
  - Conditional rendering logic that prioritizes: checklist mode → numbered steps mode → bullet mode
  - Two new preview composables demonstrating numbered steps in light and dark themes

### Key Patterns

**Pattern: Optional Rendering Modes in Shared Components**

The `CollapsibleRuleSection` uses optional parameters to control rendering behavior without creating separate components:

```kotlin
// Priority order of rendering modes:
if (checkedItems != null && onItemToggle != null) {
    // Checklist mode - interactive checkboxes
} else if (useNumberedSteps) {
    // Numbered steps mode - sequential ordering
} else {
    // Bullet mode - simple list (default, backward compatible)
}
```

This pattern allows the same component to serve multiple use cases while maintaining:
- **Backward compatibility** - Existing callers work without changes (useNumberedSteps defaults to false)
- **Clear precedence** - Checklist mode takes priority, then numbered steps, then bullets
- **Separation of concerns** - Item-specific rendering is delegated to child components (SetupChecklistItem, NumberedStepItem)

**Pattern: Accent Color with Semi-Transparent Background**

The numbered step badge uses the yellow accent color with controlled transparency:

```kotlin
Box(
    modifier = Modifier
        .size(24.dp)
        .background(
            color = RulebookTheme.colors.yellow.copy(alpha = 0.2f),
            shape = CircleShape
        ),
    contentAlignment = Alignment.Center
) {
    Text(
        text = stepNumber.toString(),
        style = MaterialTheme.typography.labelMedium,
        color = RulebookTheme.colors.yellow  // Solid color for text
    )
}
```

This creates visual hierarchy: the subtle background provides context, the solid-colored number stands out for readability.

### Code Examples

**Basic Usage in CollapsibleRuleSection:**

```kotlin
CollapsibleRuleSection(
    title = "First Round",
    content = "Follow these steps for your first round:",
    items = listOf(
        "Each player rolls both dice. The player with the highest total goes first.",
        "On your turn, roll the dice and collect resources based on the number rolled.",
        "You may trade resources with other players or the bank."
    ),
    accentColor = RulebookTheme.colors.yellow,
    isExpanded = true,
    onToggle = {},
    useNumberedSteps = true  // Enable numbered steps rendering
)
```

**Standalone NumberedStepItem:**

```kotlin
NumberedStepItem(
    stepNumber = 1,
    text = "Each player rolls both dice. The player with the highest total goes first.",
    modifier = Modifier.padding(vertical = RulebookTheme.spacing.xs)
)
```

## How to Use

### Step 1: Identify Sequential Content

Use numbered steps when you have:
- Step-by-step instructions or guides
- Ordered procedures that users follow sequentially
- First-time user onboarding or tutorial content
- Numbered rules or gameplay phases

### Step 2: Decide on Section Type

- If content needs **checkboxes for user tracking** → Use `checkedItems` parameter with `CollapsibleRuleSection`
- If content is **sequential and read-only** → Use `useNumberedSteps = true`
- If content is **just a list with no ordering significance** → Use default bullets

### Step 3: Implement in CollapsibleRuleSection

```kotlin
CollapsibleRuleSection(
    title = "[Section Title]",
    content = "[Introductory text]",
    items = listOf(
        "Step 1 description",
        "Step 2 description",
        "Step 3 description"
    ),
    accentColor = RulebookTheme.colors.[colorChoice],  // Match section theme
    isExpanded = expandedSections["[sectionKey]"] ?: false,
    onToggle = {
        expandedSections["[sectionKey]"] = !expandedSections["[sectionKey]"]
    },
    useNumberedSteps = true  // Enable numbered steps mode
)
```

### Step 4: Use Correct Accent Colors

Match the accent color to the section semantics:
- **Yellow** (#FFD23F/#FFE066) - First Round, introductory/sequential content
- **Blue** - Setup, foundational/prerequisite steps
- **Orange** - Overview, primary information
- **Purple** - Advanced rules, complex procedures

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `useNumberedSteps` | Boolean | `false` | Enables numbered steps rendering mode instead of bullets |
| `items` | List<String>? | `null` | List of step descriptions; if null or empty, no items are rendered |
| `content` | String | Required | The introductory text displayed above the steps |
| `accentColor` | Color | Required | The badge and left-accent color |

## Notes

- **Backward Compatible**: Existing code using `CollapsibleRuleSection` without `useNumberedSteps` continues to work unchanged
- **Mode Priority**: If `checkedItems` is provided, checklist mode takes priority over `useNumberedSteps`. Use one mode at a time
- **Theme Support**: Numbered step badges automatically adapt to light/dark theme through `RulebookTheme.colors.yellow`
- **Spacing**: Items are automatically spaced with `RulebookTheme.spacing.xs` vertical padding
- **Typography**: Step numbers use `labelMedium`, step text uses `bodyMedium` for consistency with other list items
- **Preview Testing**: All components include comprehensive preview composables demonstrating light/dark themes

### Future Extensibility

The pattern can be extended to other sections or features by:

1. Creating a new child component (e.g., `OrderedListItem`, `ProgressStepItem`)
2. Adding an optional parameter to the parent component
3. Adding conditional rendering logic
4. Adding preview composables for visual validation

This approach has been proven with:
- `WinConditionCallout` for Overview section (Story 6.2)
- `SetupChecklistItem` for Setup section (Story 6.3)
- `NumberedStepItem` for First Round section (Story 6.4)
