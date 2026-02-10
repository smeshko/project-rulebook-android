# Advanced Rules Section Pattern

**Feature:** Story 6.5 - Advanced Rules Section
**Module:** `feature/rules`
**Status:** Implemented
**Story:** RULE-210

---

## Overview

The Advanced Rules section is the fourth collapsible section in the Rules screen, designed to display detailed rules and edge cases for experienced players. It uses the shared `CollapsibleRuleSection` component with purple accent color and default bullet point rendering.

## Implementation

### Component Usage

The Advanced section reuses the existing `CollapsibleRuleSection` component:

```kotlin
CollapsibleRuleSection(
    title = rules.advanced.title.ifBlank { "Advanced Rules" },
    content = rules.advanced.content,
    items = rules.advanced.items,
    accentColor = RulebookTheme.colors.purple,
    isExpanded = expandedSections["advanced"] ?: false,
    onToggle = {
        expandedSections["advanced"] = !(expandedSections["advanced"] ?: false)
    }
)
```

**Location:** `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesScreen.kt:266-278`

### Key Characteristics

1. **Purple Accent Color**
   - Light theme: `#7209B7`
   - Dark theme: `#9D4EDD`
   - Applied via `RulebookTheme.colors.purple`

2. **Default Collapsed State**
   - Section starts collapsed (`expandedSections["advanced"] = false`)
   - User must expand to view content

3. **Rendering Mode: Bullets (Default)**
   - Uses default bullet point rendering (not checklist, not numbered steps)
   - Priority chain: checklist → numbered steps → **bullets**
   - Advanced section intentionally does NOT pass `checkedItems` or `useNumberedSteps`

4. **Graceful Empty Handling**
   - Supports `items = null` (content-only scenario)
   - Supports `items = emptyList()` (no bullets displayed)
   - Always displays content text paragraph

### Data Flow

```
API → RulesMapper (network) → Rules domain model → RulesEntity (database) → Rules domain model → RulesViewModel → RulesScreen
```

**Network Mapping:**
- Recognizes "advanced", "advanced rules", "detailed rules", "edge cases" section titles
- Maps to `Rules.advanced: RuleSection`

**Domain Model:**
```kotlin
data class RuleSection(
    val title: String,
    val content: String,
    val items: List<String>?,
    val winCondition: String?
)
```

**Files:**
- Network mapper: `core/network/src/main/kotlin/.../RulesMapper.kt:42-46`
- Database mapper: `core/database/src/main/kotlin/.../RulesMapper.kt:65`
- Domain model: `core/model/src/main/kotlin/.../RuleSection.kt`

---

## Design Rationale

### Why Default Bullets?

The Advanced section uses **default bullet rendering** (not interactive checklist or numbered steps) because:

1. **Reference Material** - Advanced rules are looked up during play, not executed sequentially
2. **No Progress Tracking** - Unlike setup steps, edge cases aren't "completed"
3. **Non-Sequential** - Rules may be consulted in any order based on game state
4. **Consistency** - Overview section also uses bullets for non-sequential items

### Why Collapsed by Default?

All sections except Overview start collapsed to:
- Reduce cognitive load on first view
- Focus user on essential rules first
- Allow users to drill down into details as needed

---

## Testing

### Unit Tests

**File:** `feature/rules/src/test/kotlin/com/rulebook/feature/rules/RulesViewModelTest.kt`

```kotlin
// Test: Advanced section loads with content and items
@Test
fun `advanced section loads with content and items correctly`() = runTest { ... }

// Test: Handles null items (content-only scenario)
@Test
fun `advanced section handles null items gracefully`() = runTest { ... }

// Test: Handles empty items list
@Test
fun `advanced section handles empty items list gracefully`() = runTest { ... }
```

### Preview Composables

**File:** `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/CollapsibleRuleSection.kt`

```kotlin
// Preview: Advanced section expanded with long content and 5 items (light)
@Preview(name = "Advanced Section - Expanded with Items - Light")
private fun CollapsibleRuleSectionAdvancedExpandedLightPreview() { ... }

// Preview: Advanced section expanded with items (dark theme validation)
@Preview(name = "Advanced Section - Expanded with Items - Dark")
private fun CollapsibleRuleSectionAdvancedExpandedDarkPreview() { ... }

// Preview: Advanced section with content only (null items)
@Preview(name = "Advanced Section - Content Only - Light")
private fun CollapsibleRuleSectionAdvancedContentOnlyLightPreview() { ... }
```

---

## Usage Examples

### Standard Advanced Section (with items)

```kotlin
val rules = Rules(
    gameId = "game-1",
    // ...
    advanced = RuleSection(
        title = "Advanced Rules",
        content = "These advanced rules cover edge cases...",
        items = listOf(
            "If two players tie, the player who went first loses the tiebreaker",
            "Trading is not allowed during the first two rounds",
            "A player may skip their turn voluntarily"
        ),
        winCondition = null
    )
)
```

### Content-Only Advanced Section (no items)

```kotlin
val rules = Rules(
    gameId = "game-1",
    // ...
    advanced = RuleSection(
        title = "Advanced Rules",
        content = "For this game, all standard rules apply. There are no special edge cases.",
        items = null,
        winCondition = null
    )
)
```

---

## Extension Points

### Future Sub-Section Support

The Advanced section's longer content may eventually need sub-section support (bold sub-headers + body text). To implement:

1. Add `data class RuleSubSection(title: String, content: String, items: List<String>?)`
2. Add optional `subSections: List<RuleSubSection>?` parameter to `CollapsibleRuleSection`
3. Render sub-sections after items block with:
   - Bold sub-header using scaled-down `brutalistSectionTitle` or `titleSmall`
   - Body text and optional bullet items
   - Purple accent color for sub-headers
   - Vertical spacing between sub-sections

**Note:** Current implementation (Story 6.5) does NOT include sub-sections. They are future extensibility.

---

## Related Patterns

- **Win Condition Display Pattern** (`docs/features/win-condition-display-pattern.md`) - Shows how to extract and display win conditions; can be extended to Advanced section if needed
- **Setup Checklist Interactive Tracking** (`docs/features/setup-checklist-interactive-tracking.md`) - Pattern for extending CollapsibleRuleSection with interactive state
- **Numbered Steps Display Pattern** (`docs/features/numbered-steps-display-pattern.md`) - Pattern for rendering modes; Advanced uses default bullets

---

## Gotchas

1. **Default Title Fallback**
   - If API returns blank title, UI displays "Advanced Rules" (not "Advanced")
   - Implemented via `rules.advanced.title.ifBlank { "Advanced Rules" }`

2. **No Interactive State**
   - Unlike Setup section (checklist), Advanced section has no interactive state
   - ViewModel does NOT track "checked" advanced rules
   - Rendering is read-only bullets

3. **Color System**
   - Purple color is theme-aware: `#7209B7` (light) / `#9D4EDD` (dark)
   - Do NOT hardcode hex values; use `RulebookTheme.colors.purple`

4. **Expansion State Key**
   - Tracked via `expandedSections["advanced"]` map key (lowercase)
   - Key is string literal, not derived from section title

---

## Architecture Compliance

✅ **Module Structure:** All changes in `feature/rules` module
✅ **Shared Component:** Reuses `CollapsibleRuleSection` without breaking changes
✅ **Design System:** Uses `RulebookTheme.colors.purple` from design system
✅ **Preview Convention:** Provides light and dark theme previews
✅ **MVI Pattern:** Read-only rendering, no new ViewModel state needed

---

## Files Modified (Story 6.5)

| File | Change |
|------|--------|
| `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesScreen.kt` | Updated default title to "Advanced Rules"; added realistic preview items |
| `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/CollapsibleRuleSection.kt` | Added 3 Advanced section previews |
| `feature/rules/src/test/kotlin/com/rulebook/feature/rules/RulesViewModelTest.kt` | Added 4 unit tests covering advanced section scenarios |

---

## See Also

- **FR23** (PRD) - Users can view advanced rules and deep-dive content
- **Epic 6** - Rules Display & Reference (RULE-205)
- **Story 6.1** (RULE-206) - Rules Screen Layout (established CollapsibleRuleSection pattern)
- **Story 6.4** (RULE-209) - First Round Section (established numbered steps pattern)
