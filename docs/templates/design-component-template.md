---
title: Design System Component Template
description: Template for creating brutalist-styled reusable Compose components
author: Ivo
date: 2026-01-25
---

# Design System Component Template

## When to Use

- Creating a new reusable UI component
- Need to follow the brutalist design system
- Want variant support for different styles
- Require comprehensive preview coverage

## Quick Reference

| Aspect | Value |
|--------|-------|
| Location | `core/designsystem/src/main/kotlin/.../component/Rulebook{Component}.kt` |
| Pattern | Composable with Variant enum |
| Naming | `Rulebook{Component}.kt` |

## Code Template

```kotlin
package com.rulebook.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rulebook.core.designsystem.modifier.brutalistBorder
import com.rulebook.core.designsystem.modifier.brutalistShadow
import com.rulebook.core.designsystem.theme.RulebookTheme

/**
 * {Component} variant enum defining the visual styles.
 *
 * | Type        | Usage                 | Style                        |
 * |-------------|-----------------------|------------------------------|
 * | Primary     | Main usage            | Filled, border, shadow       |
 * | Secondary   | Alternative           | Outlined, no fill            |
 */
enum class {Component}Variant {
    /** Primary style with fill, border, and shadow. */
    Primary,

    /** Secondary style with outline only. */
    Secondary
}

/**
 * Rulebook{Component} - Brutalist-styled {component} component.
 *
 * A {component} component that follows the Rulebook brutalist design system,
 * featuring bold typography, sharp corners, and offset shadows.
 *
 * @param {mainParam} The {description}.
 * @param modifier Modifier to be applied to the {component}.
 * @param variant The visual style variant. Defaults to [{Component}Variant.Primary].
 * @param enabled Whether the {component} is enabled. Disabled state shows reduced opacity.
 * @param onClick Optional callback invoked when clicked. If null, {component} is not clickable.
 */
@Composable
fun Rulebook{Component}(
    {mainParam}: String,
    modifier: Modifier = Modifier,
    variant: {Component}Variant = {Component}Variant.Primary,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val colors = RulebookTheme.colors
    val spacing = RulebookTheme.spacing

    // Determine colors based on variant
    val backgroundColor = when (variant) {
        {Component}Variant.Primary -> colors.surface
        {Component}Variant.Secondary -> Color.Transparent
    }

    val contentColor = when (variant) {
        {Component}Variant.Primary -> colors.contentPrimary
        {Component}Variant.Secondary -> colors.contentSecondary
    }

    // Shadow only for Primary
    val hasShadow = variant == {Component}Variant.Primary

    // Apply disabled alpha
    val disabledAlpha = if (enabled) 1f else 0.5f

    // Apply brutalist modifiers
    val componentModifier = modifier
        .alpha(disabledAlpha)
        .heightIn(min = 48.dp) // Min touch target if clickable
        .then(if (hasShadow) Modifier.brutalistShadow() else Modifier)
        .brutalistBorder()

    Surface(
        modifier = componentModifier,
        shape = RectangleShape,
        color = backgroundColor,
        contentColor = contentColor,
        onClick = onClick ?: {}
    ) {
        // Component content
        Text(
            text = {mainParam},
            style = RulebookTheme.typography.body
        )
    }
}

// =============================================================================
// PREVIEWS
// =============================================================================

@Preview(showBackground = true, name = "Primary - Light")
@Composable
private fun Rulebook{Component}PrimaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        Rulebook{Component}(
            {mainParam} = "Example"
        )
    }
}

@Preview(showBackground = true, name = "Primary - Dark")
@Composable
private fun Rulebook{Component}PrimaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        Rulebook{Component}(
            {mainParam} = "Example"
        )
    }
}

@Preview(showBackground = true, name = "Secondary - Light")
@Composable
private fun Rulebook{Component}SecondaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        Rulebook{Component}(
            {mainParam} = "Example",
            variant = {Component}Variant.Secondary
        )
    }
}

@Preview(showBackground = true, name = "Secondary - Dark")
@Composable
private fun Rulebook{Component}SecondaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        Rulebook{Component}(
            {mainParam} = "Example",
            variant = {Component}Variant.Secondary
        )
    }
}

@Preview(showBackground = true, name = "Disabled Primary - Light")
@Composable
private fun Rulebook{Component}DisabledPrimaryLightPreview() {
    RulebookTheme(darkTheme = false) {
        Rulebook{Component}(
            {mainParam} = "Disabled",
            enabled = false
        )
    }
}

@Preview(showBackground = true, name = "Disabled Primary - Dark")
@Composable
private fun Rulebook{Component}DisabledPrimaryDarkPreview() {
    RulebookTheme(darkTheme = true) {
        Rulebook{Component}(
            {mainParam} = "Disabled",
            enabled = false
        )
    }
}
```

## Existing Patterns

Reference implementations in the codebase:
- [RulebookButton.kt](../../core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookButton.kt)
- [RulebookCard.kt](../../core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookCard.kt)
- [RulebookHeaderBar.kt](../../core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/component/RulebookHeaderBar.kt)

## Key Patterns

### Brutalist Modifiers
Apply the design system modifiers:
```kotlin
Modifier
    .brutalistShadow()  // Offset shadow (Primary only)
    .brutalistBorder()  // Bold black border
```

### Design Tokens
Always use theme tokens, not hardcoded values:
```kotlin
val colors = RulebookTheme.colors
val spacing = RulebookTheme.spacing
val typography = RulebookTheme.typography

// Use tokens
backgroundColor = colors.surface
padding = spacing.md
textStyle = typography.body
```

### Disabled State
Apply alpha to entire component for consistent disabled appearance:
```kotlin
val disabledAlpha = if (enabled) 1f else 0.5f
Modifier.alpha(disabledAlpha)
```

### Preview Coverage
Create previews for all combinations:
- Each variant (Primary, Secondary, etc.)
- Light and Dark themes
- Enabled and Disabled states

### Sharp Corners
Brutalist design uses `RectangleShape`:
```kotlin
shape = RectangleShape  // No rounded corners
```

## Integrations

1. **Add to designsystem module:** Create in `component/` folder
2. **Export if needed:** Add to module's public API
3. **Use in features:** Import from `com.rulebook.core.designsystem.component`

## Checklist

- [ ] Created `Rulebook{Component}.kt` in designsystem/component/
- [ ] Defined `{Component}Variant` enum if multiple styles needed
- [ ] Used `RulebookTheme.colors`, `spacing`, `typography`
- [ ] Applied `brutalistBorder()` modifier
- [ ] Applied `brutalistShadow()` for primary variants
- [ ] Used `RectangleShape` for sharp corners
- [ ] Handled `enabled` state with alpha modifier
- [ ] Created Preview for each variant (light/dark)
- [ ] Created Preview for disabled states
- [ ] KDoc comments on component and variant enum

## References

- [Architecture - Design System](../architecture/overview.md#design-system)
- [RulebookTheme](../../core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/theme/RulebookTheme.kt)
