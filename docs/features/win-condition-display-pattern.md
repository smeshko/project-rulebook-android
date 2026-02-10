# Win Condition Display Pattern

**Date:** 2026-02-10
**Related Files:** Story 6.2 - Overview Section: Game Summary
**Story:** RULE-207

## Overview

Story 6.2 implements a complete win condition display pattern that extracts and renders win conditions from game rules with visual distinction. This pattern spans the entire architecture: intelligent parsing in the network layer, serialization in the database layer, and brutalist UI presentation through a new `WinConditionCallout` component.

## What Was Built

The win condition display pattern includes:

- **WinConditionCallout Component**: A brutalist-styled UI component for displaying win conditions with orange accent bar, trophy icon, and two-tier text hierarchy
- **Intelligent Win Condition Extraction**: Pattern-based parsing in the network mapper that identifies win condition phrases from unstructured API responses
- **Cross-Layer Data Flow**: Win condition field integrated into domain model, database serialization layer, and UI rendering
- **Backward Compatibility**: Optional field design ensures existing rules without win conditions continue to work seamlessly
- **Design System Integration**: Uses orange accent color (#FF6B35 light / #FF8C5F dark) consistent with Overview section branding

## Technical Implementation

### Key Files

- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/WinConditionCallout.kt`: New composable component that renders win conditions with orange accent styling and trophy icon
- `core/model/src/main/kotlin/com/rulebook/core/model/RuleSection.kt`: Domain model with new optional `winCondition: String? = null` field
- `core/network/src/main/kotlin/com/rulebook/core/network/mapper/RulesMapper.kt`: Network mapper containing intelligent win condition extraction heuristics
- `core/database/src/main/kotlin/com/rulebook/core/database/mapper/RulesMapper.kt`: Database mapper handling serialization of win condition field in JSON format
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/components/CollapsibleRuleSection.kt`: Updated to pass `winCondition` parameter to rendering
- `feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesScreen.kt`: Wired Overview section to pass win condition from domain model

### Key Patterns

- **Heuristic Content Parsing**: Network mapper scans overview section content for common win condition phrases ("win by", "wins", "win condition", "goal is to", "objective is") to intelligently extract structured data from unstructured API responses. Conservative approach: if unsure, keep full text in content and leave winCondition as null.

- **Optional Field for Backward Compatibility**: `winCondition: String? = null` in domain model and serialization preserves compatibility with existing cached rules. Database uses `ignoreUnknownKeys = true` to deserialize old data without the field.

- **Brutalist Component Design**: `WinConditionCallout` uses sharp corners, bold typography (brutalistSectionTitle), and subtle color (orange at ~10% opacity background) with accent bar. Pattern suitable for drawing attention without visual noise.

- **Cross-Layer Data Consistency**: Win condition flows through all layers (API → Network Mapper → Domain → Database → ViewModel → UI) as first-class concept, enabling future features like search, sharing, and analytics.

### Code Examples

**WinConditionCallout Component Usage:**
```kotlin
WinConditionCallout(
    winCondition = "Be the first player to collect 10 victory points.",
    modifier = Modifier.padding(vertical = 12.dp)
)
```

**Network Mapper Win Condition Extraction:**
```kotlin
// In RulesMapper.kt, the GenerateResponse.toDomain() extracts win condition:
private val winConditionPatterns = listOf(
    "win by", "wins", "win condition",
    "goal is to", "objective is", "victory"
)

// Scans overview content and splits into content + winCondition
val (content, winCondition) = extractWinCondition(overview.content)
```

**Database Round-Trip (Backward Compatible):**
```kotlin
// RuleSectionDto includes winCondition field
data class RuleSectionDto(
    val title: String,
    val content: String,
    val items: List<String>,
    val winCondition: String? = null
)

// Serialization with ignoreUnknownKeys=true handles old data gracefully
Json { ignoreUnknownKeys = true; encodeDefaults = true }
```

**Integration in Overview Section:**
```kotlin
CollapsibleRuleSection(
    title = "Overview",
    accentColor = RulebookTheme.colors.orange,
    content = rules.overview.content,
    items = rules.overview.items,
    winCondition = rules.overview.winCondition,  // Pass to component
    isExpanded = true
)
```

## How to Use

1. **Display Win Conditions**: Use `WinConditionCallout` component in any screen where you need to highlight win/goal conditions with visual distinction.

2. **Parse Unstructured Content**: When working with AI-generated game rules from the API, use the heuristic pattern matching approach from `RulesMapper.extractWinCondition()` to intelligently separate structured win conditions from descriptive text.

3. **Extend to Other Sections**: If future stories need to extract structured data from other rule sections (Setup, First Round, Advanced), follow the same pattern: create extraction heuristics in network mapper, add optional field to domain model, update database serialization, wire through UI.

4. **Preserve Backward Compatibility**: When adding new optional fields to rules data, always use nullable types with defaults, and ensure database serialization includes `ignoreUnknownKeys = true`.

## Configuration

| Option | Location | Purpose |
|--------|----------|---------|
| Orange Accent Color | `RulebookTheme.colors.orange` | Used for accent bar and icon in WinConditionCallout |
| Background Opacity | `WinConditionCallout.kt:48` | 0.1f (10%) for subtle orange-tinted background |
| Accent Bar Width | `WinConditionCallout.kt:55` | 4.dp for left border accent |
| Icon Size | `WinConditionCallout.kt:67` | 24.dp trophy icon |

## Notes

- **Heuristic Extraction Limitation**: Pattern matching may not catch all win condition variations. The conservative fallback (full text in content) ensures no information is lost.

- **Theme-Aware Rendering**: `WinConditionCallout` automatically uses dark mode orange variant (#FF8C5F) when `RulebookTheme(darkTheme = true)`. No manual theme handling required.

- **Optional by Design**: Only Overview section in Story 6.2 passes `winCondition`; other sections (Setup, First Round, Advanced) pass `null` by default. This allows gradual expansion if future stories extract conditions from other sections.

- **Compose Preview Coverage**: Component includes 4 previews (light/dark × short/long text) to validate both themes and text wrapping behavior.

- **Future Extension Opportunity**: If analytics tracking is added, win condition extraction patterns could inform user behavior analysis. Current implementation preserves data for this use case.
