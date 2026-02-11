# Epic 6: Rules Display & Reference

**Goal:** Implement the rules viewing experience with collapsible sections. After this epic, users can read generated rules with progressive disclosure, setup checklists, and sharing.

**FRs covered:** FR19-25, FR32

---

## Story 6.1: Rules Screen Layout

As a user,
I want to view my game's rules in a clear layout,
So that I can quickly understand how to play.

**Acceptance Criteria:**

**Given** the user navigates to a game's rules (FR19)
**When** the rules screen is displayed
**Then** it shows:
- Header with game title and back button
- Game thumbnail (if available)
- Metadata badges (player count, time, complexity - if available)
- Four collapsible sections

**And** the screen scrolls vertically
**And** brutalist styling is applied throughout

**Architecture requirements:**
- Located in `feature/rules` module — `RulesScreen.kt`, `RulesViewModel.kt`, `RulesUiState.kt`
- `RulesViewModel` loads game + rules from `core/data` `GameRepository.getGameWithRules(gameId): Result<Pair<Game, Rules>>`
- `RulesUiState` data class: `game: Game?, rules: Rules?, expandedSections: Set<SectionType>, checklistState: Map<Int, Boolean>, isLoading: Boolean, error: String?`
- Screen receives `gameId: String` as navigation argument from `Route.Rules("rules/{gameId}")`
- Data loaded from Room database — fully offline capable, no network calls (FR32)
- ViewModel scoped to navigation destination — disposed on back navigation

**UX/Component specifications:**
- Presentation: Full-screen (no bottom nav, no FAB) — per migration screens doc: "Rules Display is Sheet Modal" but Android adaptation uses full-screen with back navigation
- Header: `RulebookHeaderBar` with game title in `brutalist.title` (24sp, Black 900), back button using `RulebookIconButton.back()` with `brutalist.orange` (#FF6B35/#FF8C5F) accent
- Thumbnail: `AsyncImage` (Coil) with brutalist border (3dp black), 120dp height, `ContentScale.Crop`
  - Placeholder: `surface.tertiary` (#F5E6D3/#3A3A3C) with game controller icon in `content.tertiary`
- Metadata badges row: `Row` with `spacing.sm` (8dp) horizontal arrangement using `RulebookBadge` composables:
  - Player count: `brutalist.orange` background
  - Duration: `brutalist.blue` background
  - Difficulty: `brutalist.green` background
  - Badge spec: 8dp horizontal / 4dp vertical padding, 2dp black border, 11sp bold uppercase text
- Content: `LazyColumn` with `spacing.md` (16dp) content padding and `spacing.sm` (8dp) item spacing
- Background: `surface.secondary` (#FFF9F0/#2C2C2E)
- Screen margin: `spacing.md` (16dp)

**Technical notes:**
- RulesScreen in `feature/rules` module
- RulesViewModel loads from Room by gameId via `GameRepository`
- LazyColumn for efficient scrolling with `key` parameter per section
- Navigation: `navController.navigate("rules/$gameId")`

**Prerequisites:** Epic 5 (rules data exists), Epic 1 (design system)

---

## Story 6.2: Overview Section - Game Summary

As a user,
I want to read a quick overview of the game,
So that I understand the basic concept before setup.

**Acceptance Criteria:**

**Given** the rules screen is displayed (FR19)
**When** viewing the Overview section
**Then** it shows:
- Section header: "Overview" with orange accent color
- Game summary paragraph
- Win condition clearly stated
- Section is expanded by default

**And** text is readable and well-formatted

**Architecture requirements:**
- Uses `RulebookCollapsibleSection` composable from `core/designsystem`
- Section state managed in `RulesViewModel` via `expandedSections: Set<SectionType>`
- Overview section uses `SectionType.GameOverview` enum value
- Content rendered from `rules.overview.content` string — may contain markdown-like formatting

**UX/Component specifications:**
- Section component: `RulebookCollapsibleSection` with `accentColor = BrutalistColors.orange`
  - Orange accent: `brutalist.orange` light=#FF6B35, dark=#FF8C5F (from design-system-android.json)
  - Header: `brutalist.sectionTitle` (16sp, Black 900) with orange left accent bar (4dp width)
  - Expanded: border 3dp black, shadow offset 8dp (elevated state from shadows.offsets.medium)
  - Collapsed: border 3dp black, shadow offset 4dp (from shadows.offsets.xsmall)
- Default state: **expanded** — Overview is the only section expanded on load (UX §Experience Principles: "Progressive Clarity")
- Summary text: `body.body` (17sp, Regular), `content.primary` color
- Win condition: Highlighted with `body.bodyEmphasized` (17sp, Medium) and prefixed with bold "Win Condition:" label
- Content padding: `spacing.md` (16dp) inside section
- Section spacing: `spacing.sm` (8dp) between sections in list

**Technical notes:**
- CollapsibleSection composable with `SectionType.GameOverview`
- Orange color: #FF6B35 (light) / #FF8C5F (dark) from design tokens
- Default expanded state for Overview only — initialized in `RulesUiState(expandedSections = setOf(SectionType.GameOverview))`

**Prerequisites:** Story 6.1

---

## Story 6.3: Setup Section with Checklist

As a user,
I want to follow setup steps with a checklist,
So that I can track my progress while preparing the game.

**Acceptance Criteria:**

**Given** the rules screen is displayed (FR20, FR21)
**When** viewing the Setup section
**Then** it shows:
- Section header: "Setup" with blue accent color
- Numbered setup steps
- Checkbox next to each step
- Tapping checkbox toggles completion state

**And** checkbox state persists during session
**And** visual feedback on toggle (strikethrough or checkmark)
**And** section is collapsed by default

**Architecture requirements:**
- Uses `SectionType.SetupInstructions` enum
- Checklist state tracked in `RulesViewModel`: `checklistState: Map<Int, Boolean>` (step index → checked)
- Checklist is session-only — NOT persisted to Room database (resets on screen exit)
- Toggle dispatched as intent: `RulesIntent.ToggleChecklistItem(index: Int)`
- Setup items sourced from `rules.setup.items: List<String>?`

**UX/Component specifications:**
- Section: `RulebookCollapsibleSection` with `accentColor = BrutalistColors.blue`
  - Blue accent: `brutalist.blue` light=#3498DB, dark=#5DADE2
- Checklist items layout:
  - Each item: `Row` with checkbox + step text
  - Checkbox: `RulebookCheckbox` or Material 3 `Checkbox` with brutalist styling — green (#2ECC71) when checked, 3dp black border
  - Step number: `RulebookBadge` with `brutalist.blue` background, showing "1", "2", etc.
  - Step text: `body.body` (17sp, Regular)
  - Checked state: text gets `TextDecoration.LineThrough` and `content.tertiary` (#00000066/#FFFFFF66) color
- Haptic feedback: Light click on checkbox toggle (UX §Haptic Patterns: "Toggle → Light click")
- Item spacing: `spacing.sm` (8dp) between checklist items
- Touch target: 48dp minimum for checkbox (Accessibility §Touch Targets)

**Technical notes:**
- Blue color: #3498DB (light) / #5DADE2 (dark) from design tokens
- Checklist state in RulesViewModel (local, not persisted to DB)
- `AnimatedVisibility` with `expandVertically` for expand/collapse animation
- Checkbox green: `brutalist.green` (#2ECC71/#58D68D) when checked

**Prerequisites:** Story 6.1

---

## Story 6.4: First Round Section

As a user,
I want to read a guide for the first round,
So that I can start playing without reading all the rules.

**Acceptance Criteria:**

**Given** the rules screen is displayed (FR22)
**When** viewing the First Round section
**Then** it shows:
- Section header: "First Round" with yellow accent color
- Step-by-step guide for initial gameplay
- Turn structure explanation

**And** section is collapsed by default
**And** content is concise and actionable

**Architecture requirements:**
- Uses `SectionType.FirstRoundWalkthrough` enum
- Content from `rules.firstRound.content` and optional `rules.firstRound.items`
- Same `RulebookCollapsibleSection` pattern as other sections

**UX/Component specifications:**
- Section: `RulebookCollapsibleSection` with `accentColor = BrutalistColors.yellow`
  - Yellow accent: `brutalist.yellow` light=#FFD23F, dark=#FFE066
  - Note: Yellow text on light backgrounds needs dark text color for contrast — use `Color.Black` for header text when yellow accent
- Content: Numbered steps using `body.body` (17sp, Regular)
- Turn structure may include sub-headers using `body.bodyEmphasized` (17sp, Medium)
- Collapsed by default — user must tap to expand (UX §Experience Principles: "Progressive Clarity")

**Technical notes:**
- Yellow color: #FFD23F (light) / #FFE066 (dark) from design tokens
- Same `RulebookCollapsibleSection` pattern
- Yellow section header text should be black for readability

**Prerequisites:** Story 6.1

---

## Story 6.5: Advanced Rules Section

As a user,
I want to access detailed rules for edge cases,
So that I can look up specific situations during play.

**Acceptance Criteria:**

**Given** the rules screen is displayed (FR23)
**When** viewing the Advanced section
**Then** it shows:
- Section header: "Advanced Rules" with purple accent color
- Detailed rules and edge cases
- May include sub-sections or bullet points

**And** section is collapsed by default
**And** this section can be longer than others

**Architecture requirements:**
- Uses `SectionType.AdvancedRules` enum
- Content from `rules.advanced.content` — may be significantly longer than other sections
- Support for nested content structure (bullet points, sub-headers)

**UX/Component specifications:**
- Section: `RulebookCollapsibleSection` with `accentColor = BrutalistColors.purple`
  - Purple accent: `brutalist.purple` light=#7209B7, dark=#9D4EDD
- Content supports longer text with:
  - Bullet points: `•` prefix with `spacing.sm` (8dp) indent
  - Sub-headers: `body.bodyEmphasized` (17sp, Medium)
  - Body text: `body.body` (17sp, Regular)
- Section may require significant scroll — LazyColumn handles this naturally
- Collapsed by default — this is the "Deep Dive" content for experienced players

**Technical notes:**
- Purple color: #7209B7 (light) / #9D4EDD (dark) from design tokens
- Support for nested content formatting if rules text contains markdown
- Content may need `SelectionContainer` for text selection during gameplay reference

**Prerequisites:** Story 6.1

---

## Story 6.6: Section Expand/Collapse Animation

As a user,
I want sections to expand and collapse smoothly,
So that the interface feels polished and responsive.

**Acceptance Criteria:**

**Given** a collapsible section exists (FR24)
**When** the user taps the section header
**Then** the section toggles expand/collapse state
**And** content animates in/out smoothly
**And** shadow depth increases when expanded
**And** chevron icon rotates to indicate state

**And** multiple sections can be expanded simultaneously

**Architecture requirements:**
- Expand/collapse state managed centrally in `RulesViewModel` via `expandedSections: Set<SectionType>`
- Toggle dispatched as: `RulesIntent.ToggleSection(sectionType: SectionType)`
- Multiple sections can be open simultaneously — `Set` allows any combination
- Animation handled entirely in `RulebookCollapsibleSection` composable (no ViewModel involvement for animation)

**UX/Component specifications:**
- Expand animation: `AnimatedVisibility` with `expandVertically(animationSpec = tween(300, easing = EaseInOut))`
  - Duration: 300ms from `animation.duration.medium` in design-system-android.json
  - Easing: `EaseInOut` from `animation.easing.standard`
- Collapse animation: `shrinkVertically(animationSpec = tween(300, easing = EaseInOut))`
- Shadow depth transition:
  - Collapsed: `brutalistShadow(offset = 4.dp)` (shadows.offsets.xsmall)
  - Expanded: `brutalistShadow(offset = 8.dp)` (shadows.offsets.medium)
  - Animated via `animateDpAsState(targetValue = if (expanded) 8.dp else 4.dp, tween(300))`
- Chevron rotation:
  - Collapsed: 0° (pointing right/down)
  - Expanded: 180° (pointing up)
  - Animated via `animateFloatAsState(targetValue = if (expanded) 180f else 0f, tween(300))`
  - Icon: `Icons.Default.ExpandMore` rotated
- Header tap area: Full width, 48dp minimum height (Accessibility §Touch Targets)
- Haptic feedback: Light click on header tap (UX §Haptic Patterns: "Button press → Light click")

**Technical notes:**
- `AnimatedVisibility` with `expandVertically` for content reveal
- Animate shadow offset: 4dp → 8dp on expand (shadows.offsets.xsmall → medium)
- Rotation animation for chevron: 0° → 180° with `tween(300)`
- Press animation on header: `pressedScale = 0.98f` per `RulebookButton` press animation pattern

**Prerequisites:** Story 6.1

---

## Story 6.7: Share Rules Functionality

As a user,
I want to share game rules with others,
So that everyone at game night can reference them.

**Acceptance Criteria:**

**Given** the rules screen is displayed (FR25)
**When** the user taps the share button
**Then** the system share sheet opens
**And** shared content includes:
- Game title
- Formatted rules text (or link, future)
- App attribution

**And** share button is in the header bar
**And** share works even when offline

**Architecture requirements:**
- Share action in `RulesViewModel` — formats rules into shareable plain text string
- Uses Android `Intent.ACTION_SEND` with `text/plain` MIME type
- No network calls — shares text content from local Room data
- Share button as `trailingContent` in `RulebookHeaderBar`
- Share intent launched from screen composable via `LocalContext.current`

**UX/Component specifications:**
- Share button: `RulebookIconButton.share()` in header bar trailing position
  - Icon: `Icons.Default.Share`, size medium (44dp)
  - Color: `brutalist.pink` (#E91E63/#F06292) per component library presets
- Shared text format:
  ```
  🎲 [Game Title] - Rules

  📋 Overview
  [overview content]

  🔧 Setup
  [setup steps]

  🎯 First Round
  [first round content]

  📚 Advanced Rules
  [advanced content]

  — Shared from Rulebook
  ```
- Haptic feedback: Light click on share button tap

**Technical notes:**
- Android `Intent.ACTION_SEND` with `Intent.EXTRA_TEXT`
- Format rules sections into readable plain text
- Include "Shared from Rulebook" footer as app attribution
- Offline-capable — reads from local database

**Prerequisites:** Story 6.1

---

## Story 6.8: Offline Rules Access

As a user,
I want to view rules even without internet,
So that I can reference them in basements, cabins, and cafes.

**Acceptance Criteria:**

**Given** rules are saved in the database (FR32)
**When** the device is offline
**Then** all saved rules are fully accessible
**And** no network indicator or degraded state
**And** images may use placeholder if not cached

**Architecture requirements:**
- Room database is the single source of truth for saved rules — no network layer involved
- `RulesViewModel` loads exclusively from `GameRepository` which reads from Room
- Coil image loading uses disk cache for thumbnails — `ImageRequest.Builder.diskCachePolicy(CachePolicy.ENABLED)`
- If thumbnail not cached: show `surface.tertiary` placeholder with icon — no error state
- ZERO network calls from Rules screen — fully offline architecture (NFR8)

**UX/Component specifications:**
- No offline indicator needed — the experience is identical online and offline (UX §Experience Principles: "Offline Resilience")
- Thumbnail fallback: `surface.tertiary` (#F5E6D3/#3A3A3C) colored box with centered game controller `Icon` in `content.tertiary`
- All text content renders from Room — no loading states for text
- If user somehow reaches rules screen with invalid gameId: centered error message "Rules not found" with back button

**Technical notes:**
- Room database is source of truth — `GameDao.getById()` + `RulesDao.getByGameId()`
- No network calls for saved rules
- Coil disk cache for thumbnails with `CachePolicy.ENABLED`
- Placeholder composable for missing thumbnails

**Prerequisites:** Story 6.1, Epic 1 (Room)

---

## Story 6.9: Update Last Accessed Timestamp

As a developer,
I want to track when games are accessed,
So that "Recent" sort in library works correctly.

**Acceptance Criteria:**

**Given** a user views a game's rules
**When** the rules screen is displayed
**Then** the game's `lastAccessedAt` timestamp is updated
**And** update is performed in background
**And** library sort reflects the access

**Architecture requirements:**
- Timestamp update via `GameRepository.updateLastAccessed(gameId: String)` in `core/data`
- Room DAO: `@Query("UPDATE saved_games SET last_accessed_at = :timestamp WHERE id = :gameId")`
- Called from `RulesViewModel.init{}` block — fire-and-forget coroutine
- Uses `viewModelScope.launch(Dispatchers.IO)` — no UI feedback needed
- Timestamp value: `System.currentTimeMillis()` stored as `Long` (Architecture §Database Conventions)

**Technical notes:**
- `GameRepository.updateLastAccessed(gameId)` calls Room DAO
- Room `@Query` UPDATE operation
- Fire-and-forget in `viewModelScope.launch` — errors silently logged, not shown to user
- Library `Flow<List<Game>>` from Room automatically reflects the update when user returns

**Prerequisites:** Story 6.1, Epic 1 (Room)

---

**Epic 6 Complete: Rules Display & Reference**

**Stories Created:** 9
**FR Coverage:** FR19-25, FR32
**Architecture Sections Referenced:** feature/rules, core/data, core/database, core/designsystem, core/model
**UX Patterns Incorporated:** Collapsible sections, color coding, checklists, share sheet, offline-first, progressive disclosure
**Design Tokens Referenced:** brutalist.orange, brutalist.blue, brutalist.yellow, brutalist.purple, brutalist.green, brutalist.pink, shadows.offsets.xsmall/medium, animation.duration.medium, typography.brutalist.sectionTitle
