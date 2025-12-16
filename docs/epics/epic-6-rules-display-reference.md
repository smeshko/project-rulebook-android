# Epic 6: Rules Display & Reference

**Goal:** Implement the rules viewing experience with collapsible sections. After this epic, users can read generated rules with progressive disclosure, setup checklists, and sharing.

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

**Technical Notes:**
- RulesScreen in `feature/rules` module
- RulesViewModel loads from Room by gameId
- LazyColumn for efficient scrolling

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

**Technical Notes:**
- CollapsibleSection composable
- Orange color: #FF6B35 (light) / #FF8C5F (dark)
- Default expanded state for Overview only

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

**Technical Notes:**
- Blue color: #3498DB (light) / #5DADE2 (dark)
- Checklist state in RulesViewModel (local, not persisted to DB)
- AnimatedVisibility for expand/collapse

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

**Technical Notes:**
- Yellow color: #FFD23F (light) / #FFE066 (dark)
- Same CollapsibleSection pattern

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

**Technical Notes:**
- Purple color: #7209B7 (light) / #9D4EDD (dark)
- Support for nested content if needed

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

**Technical Notes:**
- AnimatedVisibility with expandVertically
- Animate shadow offset (4dp → 8dp on expand)
- Rotation animation for chevron (0° → 180°)

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

**Technical Notes:**
- Android Intent.ACTION_SEND
- Format rules as plain text for sharing
- Include "Shared from Rulebook" footer

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

**Technical Notes:**
- Room database is source of truth
- No network calls for saved rules
- Coil disk cache for thumbnails

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

**Technical Notes:**
- GameRepository.updateLastAccessed(gameId)
- Room UPDATE query
- Fire-and-forget coroutine

**Prerequisites:** Story 6.1, Epic 1 (Room)

---

**Epic 6 Complete: Rules Display & Reference**

**Stories Created:** 9
**FR Coverage:** FR19-25, FR32
**Architecture Sections Referenced:** feature/rules, core/database
**UX Patterns Incorporated:** Collapsible sections, color coding, checklists, share sheet

---
