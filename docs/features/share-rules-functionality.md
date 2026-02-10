# Share Rules Functionality

**Date:** 2026-02-10
**Related Files:**
- feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesShareFormatter.kt
- feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesScreen.kt
- feature/rules/src/main/kotlin/com/rulebook/feature/rules/RulesViewModel.kt

## Overview

Users can now share game rules with others via the Android system share sheet. The rules are formatted as plain text including the game title, all four rule sections (Overview, Setup, First Round, Advanced), items, win conditions, and a "Shared from Rulebook" footer attribution.

## What Was Built

- **RulesShareFormatter**: Pure utility object that formats `Game` + `Rules` domain models into shareable plain text
- **Share Action in ViewModel**: `shareRules(onShareText: (String) -> Unit)` method that formats current data only when loaded
- **Share Button in Header**: IconButton with Share icon in RulesScreenContent header bar, visible only when data is successfully loaded
- **Intent Integration**: Uses Android `Intent.ACTION_SEND` with `Intent.createChooser()` for consistent share sheet UX
- **Offline Support**: Share works without network — no API calls, uses locally stored data

## Technical Implementation

### Key Files

- `RulesShareFormatter.kt`: Pure Kotlin utility object with `formatForSharing(game: Rules): String` method
  - Formats game title as markdown header (`# Title`)
  - Formats each section with title, content, items list, and win condition
  - Detects if items are already numbered or need bullet markers (`•`)
  - Falls back to default section titles ("Overview", "Setup", "First Round", "Advanced Rules") if section title is blank
  - Appends "Shared from Rulebook" footer

- `RulesViewModel.kt`: Added `shareRules()` method
  - Only produces text when `uiState.value.isSuccess` is true (both game and rules loaded)
  - Delegates formatting to `RulesShareFormatter`
  - Invokes callback with formatted text

- `RulesScreen.kt`: Share button integration in composable
  - Uses `LocalContext.current` to get Android Context
  - Creates Intent with `Intent.ACTION_SEND`, `type = "text/plain"`
  - Sets Intent extras: `EXTRA_SUBJECT` (game title), `EXTRA_TEXT` (formatted rules)
  - Wraps with `Intent.createChooser()` for system share sheet
  - Wraps in try-catch for `ActivityNotFoundException` (defensive — some devices may have no sharing apps)

### Key Patterns

- **Pure Formatter Function**: Text formatting is a stateless utility function with no Android dependencies — trivially testable and reusable for other shareable content
- **State-Aware Button Visibility**: Share button only visible when `uiState.isSuccess` — prevents users from attempting to share incomplete or error-state data
- **Intent Launching Pattern**: Demonstrates proper use of `LocalContext.current` in Compose to launch Android Intents without coupling ViewModel to Android framework
- **Defensive Error Handling**: Catches `ActivityNotFoundException` silently — handles edge case where user's device has no sharing apps installed
- **Plain Text Format**: Ensures maximum compatibility across all share targets (SMS, email, messaging apps, notes) and works offline

### Code Examples

**Formatting Rules for Sharing:**
```kotlin
// ViewModel method
viewModel.shareRules { formattedText ->
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, gameTitle)
        putExtra(Intent.EXTRA_TEXT, formattedText)
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

// Expected output format:
// # Game Title
//
// ## Overview
// Content here...
//
// • Item 1
// • Item 2
//
// 🏆 Win Condition: Description
//
// ## Setup
// ...
//
// Shared from Rulebook
```

**Detecting Item Formatting:**
```kotlin
// Smart detection: if item already starts with number, preserve as-is
val formattedItem = if (item.trimStart().matches(NUMBERED_ITEM_REGEX)) {
    item  // Already numbered, use as-is
} else {
    "• $item"  // Add bullet marker
}
```

## How to Use

1. **User opens Rules screen** for a game
2. **User taps Share icon** in header bar (only visible when rules are loaded)
3. **System calls `viewModel.shareRules()`** with a callback
4. **ViewModel formats the text** using `RulesShareFormatter.formatForSharing()`
5. **Composable creates and launches** Android share Intent with formatted text
6. **System share sheet appears** with list of available sharing apps
7. **User selects destination** (Email, Messages, Slack, etc.)
8. **Rules are shared** as plain text in the user's chosen app

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Section titles | String | "Overview", "Setup", "First Round", "Advanced Rules" | Default titles used when section title is blank |
| Item marker | String | "• " | Bullet marker used for non-numbered items |
| Win condition prefix | String | "🏆 Win Condition: " | Prefix for win condition display |
| Footer text | String | "Shared from Rulebook" | Attribution text appended to shared content |

## Notes

- **Offline:** Share functionality requires no network connection — uses locally stored rules data
- **Compatibility:** Works with all Android share targets because plain text is universally supported
- **Error Handling:** Gracefully handles edge case where device has no sharing apps via `ActivityNotFoundException` catch
- **Item Formatting:** Smart detection of numbered vs bulleted items prevents double-numbering when rules already contain numbers
- **Accessibility:** Plain text format is highly accessible — screen readers work well, copy-paste friendly
- **Future Extensions:** This pattern could be extended to share other content (game metadata, credits, etc.) by creating additional formatter methods
