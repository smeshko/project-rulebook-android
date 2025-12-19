# Epic 2: App Shell & Navigation

**Goal:** Create the main app structure with bottom navigation, FAB, and basic screens. After this epic, users can launch the app, see the library (empty state), navigate to settings, and tap the camera FAB.

---

## Story 2.1: MainActivity with Edge-to-Edge Display

As a user,
I want the app to display content edge-to-edge,
So that the experience feels modern and immersive.

**Acceptance Criteria:**

**Given** the app is launched
**When** MainActivity starts
**Then** content extends under the status bar and navigation bar (FR50)
**And** the status bar is transparent with appropriate icon colors (light/dark)
**And** the navigation bar is transparent or matches the bottom nav color
**And** content is properly inset to avoid overlap with system UI

**Technical Notes:**
- Use `enableEdgeToEdge()` from AndroidX Activity
- Apply `WindowInsets` padding to content
- Status bar icons adapt to theme (light on dark, dark on light)

**Prerequisites:** Epic 1 (theme, design system)

---

## Story 2.2: Navigation Host & Route Definitions

As a developer,
I want Compose Navigation configured with route definitions,
So that screens can be navigated to consistently.

**Acceptance Criteria:**

**Given** the app module
**When** navigation is configured
**Then** `NavigationDestination.kt` defines sealed class routes:
```kotlin
sealed class Route(val route: String) {
    object Library : Route("library")
    object Settings : Route("settings")
    object Camera : Route("camera")
    object Rules : Route("rules/{gameId}")
    object Onboarding : Route("onboarding")
    object Purchase : Route("purchase")
}
```

**And** `RulebookNavHost.kt` configures NavHost with all destinations
**And** navigation arguments are type-safe
**And** deep links are configured for future use

**Technical Notes:**
- Compose Navigation 2.8.x (Architecture section)
- NavController passed down to screens via parameter
- Use `composable()` with route patterns

**Prerequisites:** Story 2.1

---

## Story 2.3: Bottom Navigation Bar

As a user,
I want a bottom navigation bar with Library and Settings tabs,
So that I can quickly switch between main sections.

**Acceptance Criteria:**

**Given** the app is on a main screen (Library or Settings)
**When** the bottom navigation is displayed
**Then** two tabs are visible: "Library" and "Settings"
**And** each tab has an icon and label
**And** the selected tab is visually highlighted
**And** tapping a tab navigates to that screen instantly
**And** state is preserved when switching tabs

**And** the navigation bar has brutalist styling:
- Thick top border (3dp)
- Surface background color
- Selected indicator with accent color

**Technical Notes:**
- Use Material 3 NavigationBar with custom styling
- Icons: Library (grid), Settings (gear)
- Navigation state preserved via `rememberSaveable`

**Prerequisites:** Story 2.2

---

## Story 2.4: Floating Action Button for Camera

As a user,
I want a prominent camera button always visible,
So that I can quickly scan a game from any main screen.

**Acceptance Criteria:**

**Given** the user is on Library or Settings screen
**When** the FAB is displayed
**Then** it appears in the standard FAB position (bottom-right, above nav bar)
**And** it shows a camera icon
**And** it has brutalist styling (border, shadow, pink fill)
**And** tapping it navigates to the camera screen

**And** the FAB respects edge-to-edge (proper insets)
**And** the FAB has ripple feedback on press

**Technical Notes:**
- Use Material 3 FloatingActionButton with custom styling
- Position using Scaffold's floatingActionButton slot
- FAB hidden on non-main screens (camera, rules, onboarding)

**Prerequisites:** Story 2.3

---

## Story 2.5: Library Screen Shell with Empty State

As a user,
I want to see an encouraging empty state when I have no saved games,
So that I understand how to get started.

**Acceptance Criteria:**

**Given** the user has no saved games (FR33)
**When** the Library screen is displayed
**Then** the empty state shows:
- Illustration or icon (game-related)
- Headline: "No games yet"
- Subtext: "Scan your first game to get started"
- Optional CTA button pointing to camera

**And** the header bar shows "Library" with brutalist styling
**And** the screen supports pull-to-refresh (prepares for future)

**Technical Notes:**
- LibraryScreen in `feature/library` module
- LibraryViewModel with UiState pattern
- Empty state uses RulebookCard styling

**Prerequisites:** Story 2.4, Epic 1 (RulebookHeaderBar)

---

## Story 2.6: Settings Screen Shell

As a user,
I want to access the settings screen,
So that I can view app options (full functionality in Epic 9).

**Acceptance Criteria:**

**Given** the user taps the Settings tab
**When** the Settings screen is displayed
**Then** the header bar shows "Settings" with brutalist styling
**And** placeholder sections are visible:
- Appearance (theme toggle placeholder)
- Feedback (haptics toggle placeholder)
- Support (links placeholder)
- About (version, legal placeholder)
- Data (clear data placeholder)

**And** sections are grouped with headers
**And** the screen scrolls if content exceeds viewport

**Technical Notes:**
- SettingsScreen in `feature/settings` module
- SettingsViewModel with UiState pattern
- Use LazyColumn for scrollable content
- Sections styled with brutalist borders

**Prerequisites:** Story 2.3, Epic 1 (design system)

---

## Story 2.7: Scaffold Integration & Screen Composition

As a developer,
I want all main screens composed within a shared Scaffold,
So that the navigation bar and FAB are consistently displayed.

**Acceptance Criteria:**

**Given** the navigation structure
**When** main screens are displayed
**Then** a shared Scaffold provides:
- Bottom navigation bar
- Floating action button
- Content area with proper insets

**And** non-main screens (Camera, Rules, Onboarding) use full-screen without bottom nav
**And** transitions between screens use appropriate animations

**Technical Notes:**
- Single Scaffold at RulebookNavHost level
- Conditional bottom bar visibility based on route
- Use `AnimatedNavHost` for transitions

**Prerequisites:** Stories 2.3, 2.4, 2.5, 2.6

---

## Story 2.8: Predictive Back Gesture Support

As a user,
I want the app to support Android's predictive back gesture,
So that navigation feels native and modern.

**Acceptance Criteria:**

**Given** the user performs a back gesture (FR49)
**When** on a detail screen (Rules, Camera)
**Then** a preview of the previous screen is shown during the gesture
**And** completing the gesture navigates back
**And** canceling the gesture returns to current screen

**And** the back gesture works from the left edge
**And** the animation follows system conventions

**Technical Notes:**
- Enable `android:enableOnBackInvokedCallback="true"` in manifest
- Use `BackHandler` composable for custom back handling
- Predictive back supported on Android 14+

**Prerequisites:** Story 2.2

---

## Epic 2: Dependency Flowchart

```
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 1: Start Immediately                                                    ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [2-1] MainActivity with Edge-to-Edge Display                                 ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 2: After 2-1                                                            ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [2-2] Navigation Host & Route Definitions                                    ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 3: After 2-2 (PARALLEL)                                                 ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [2-3] Bottom Navigation Bar          ║  [2-8] Predictive Back Gesture        ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
          │                                                    │
          ▼                                                    │
╔═════════════════════════════════════════════════════╗        │
║  WAVE 4: After 2-3 (PARALLEL)                       ║        │
╠═════════════════════════════════════════════════════╣        │
║                                                     ║        │
║  [2-4] Camera FAB   ║  [2-6] Settings Screen Shell  ║        │
║                                                     ║        │
╚═════════════════════════════════════════════════════╝        │
          │                        │                           │
          ▼                        │                           │
╔═════════════════════════════════╗│                           │
║  WAVE 5: After 2-4              ║│                           │
╠═════════════════════════════════╣│                           │
║                                 ║│                           │
║  [2-5] Library Empty State      ║│                           │
║                                 ║│                           │
╚═════════════════════════════════╝│                           │
          │                        │                           │
          └────────────┬───────────┘                           │
                       ▼                                       │
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 6: After 2-5, 2-6 (+ 2-8 complete)                                      ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [2-7] Scaffold Integration & Screen Composition                              ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝

LEGEND:
  ─────── Sequential dependency
  ║  ║    Parallel execution opportunity
  ──┬──   Dependencies merging
```

### Wave Summary

| Wave | Stories | Parallelism |
|------|---------|-------------|
| 1 | 2.1 | - |
| 2 | 2.2 | - |
| 3 | 2.3, 2.8 | 2 parallel |
| 4 | 2.4, 2.6 | 2 parallel |
| 5 | 2.5 | - |
| 6 | 2.7 | - |

### Critical Path

2.1 → 2.2 → 2.3 → 2.4 → 2.5 → 2.7

### Parallel Opportunities

- **Wave 3:** Story 2.8 (Predictive Back) can run in parallel with Story 2.3 (Bottom Nav)
- **Wave 4:** Story 2.6 (Settings) can run in parallel with Story 2.4 (FAB)

---

**Epic 2 Complete: App Shell & Navigation**

**Stories Created:** 8
**FR Coverage:** FR27 (library view), FR33 (empty state), FR49 (predictive back), FR50 (edge-to-edge)
**Architecture Sections Referenced:** app module, feature/library, feature/settings, Compose Navigation
**UX Patterns Incorporated:** Bottom nav + FAB, empty states, edge-to-edge, predictive back

---

## Manual Testing Checklist

### Prerequisites
- Android 14+ device or emulator (API 34+)
- Gesture navigation enabled (Settings > System > Gestures > System navigation)

---

### 1. Edge-to-Edge Display (Story 2.1)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 1.1 | Launch app | Content extends under status bar | ☐ |
| 1.2 | Check status bar | Transparent with appropriate icon colors | ☐ |
| 1.3 | Check navigation bar | Transparent or matches bottom nav color | ☐ |
| 1.4 | Verify content insets | Content doesn't overlap with system UI | ☐ |
| 1.5 | Toggle dark mode (system) | Status bar icons adapt (light/dark) | ☐ |

---

### 2. Bottom Navigation Bar (Story 2.3)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 2.1 | View bottom nav | Two tabs visible: "Library" and "Settings" | ☐ |
| 2.2 | Check tab icons | Each tab has icon + label | ☐ |
| 2.3 | Tap Library tab | Library screen shown, tab highlighted pink | ☐ |
| 2.4 | Tap Settings tab | Settings screen shown, tab highlighted pink | ☐ |
| 2.5 | Check brutalist styling | Thick top border (3dp), flat appearance | ☐ |
| 2.6 | Switch tabs multiple times | State preserved when returning to tab | ☐ |
| 2.7 | Check icon states | Outlined when unselected, filled when selected | ☐ |

---

### 3. Floating Action Button (Story 2.4)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 3.1 | View FAB on Library | Pink FAB visible bottom-right, above nav bar | ☐ |
| 3.2 | View FAB on Settings | Pink FAB visible bottom-right | ☐ |
| 3.3 | Check FAB styling | Pink fill, black border, camera icon | ☐ |
| 3.4 | Tap FAB | Navigates to Camera screen | ☐ |
| 3.5 | Check FAB press feedback | Ripple effect on tap | ☐ |
| 3.6 | Navigate to Camera | FAB disappears with scale animation | ☐ |
| 3.7 | Return to Library | FAB reappears with scale animation | ☐ |

---

### 4. Library Screen - Empty State (Story 2.5)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 4.1 | View Library (no games) | Empty state displayed | ☐ |
| 4.2 | Check empty state icon | Game controller icon visible | ☐ |
| 4.3 | Check headline | "No games yet" displayed | ☐ |
| 4.4 | Check subtext | "Scan your first game to get started" | ☐ |
| 4.5 | Check CTA button | "Scan a Game" button visible | ☐ |
| 4.6 | Tap CTA button | Navigates to Camera screen | ☐ |
| 4.7 | Check header bar | "Library" title with brutalist styling | ☐ |
| 4.8 | Pull down to refresh | Refresh indicator appears | ☐ |

---

### 5. Settings Screen Shell (Story 2.6)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 5.1 | Navigate to Settings | Settings screen displayed | ☐ |
| 5.2 | Check header | "Settings" title with brutalist styling | ☐ |
| 5.3 | Scroll content | Screen scrolls if content exceeds viewport | ☐ |
| **Appearance Section** |
| 5.4 | Find Appearance section | Section header "APPEARANCE" visible | ☐ |
| 5.5 | Check Dark Mode toggle | Toggle row with switch visible | ☐ |
| **Feedback Section** |
| 5.6 | Find Feedback section | Section header "FEEDBACK" visible | ☐ |
| 5.7 | Check Haptic toggle | Toggle row with switch visible | ☐ |
| **Support Section** |
| 5.8 | Find Support section | Section header "SUPPORT" visible | ☐ |
| 5.9 | Check links | "Contact Us", "Rate the App" rows visible | ☐ |
| **About Section** |
| 5.10 | Find About section | Section header "ABOUT" visible | ☐ |
| 5.11 | Check version | Version info displayed (e.g., "1.0.0") | ☐ |
| 5.12 | Check legal links | "Privacy Policy", "Terms of Service" visible | ☐ |
| **Data Section** |
| 5.13 | Find Data section | Section header "DATA" visible | ☐ |
| 5.14 | Check clear button | "Clear All Data" red button visible | ☐ |
| 5.15 | Check brutalist rows | All rows have 3dp black border | ☐ |

---

### 6. Screen Transitions (Story 2.7)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 6.1 | Switch Library ↔ Settings | Fade transition (300ms) | ☐ |
| 6.2 | Tap FAB → Camera | Slide in from right (300ms) | ☐ |
| 6.3 | Back from Camera | Slide out to right (300ms) | ☐ |
| 6.4 | Bottom bar on Camera | Hidden with slide down animation | ☐ |
| 6.5 | Return to Library | Bottom bar slides up | ☐ |

---

### 7. Navigation Routes (Story 2.2)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 7.1 | App start | Opens on Library screen | ☐ |
| 7.2 | Navigate to Camera | Camera placeholder screen shown | ☐ |
| 7.3 | Navigate to Settings | Settings screen shown | ☐ |
| 7.4 | Check placeholder screens | Onboarding, Purchase, Rules show placeholders | ☐ |

---

### 8. Predictive Back Gesture (Story 2.8)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 8.1 | Go to Camera screen | Camera screen displayed | ☐ |
| 8.2 | Start back gesture (left edge) | Preview of Library screen appears | ☐ |
| 8.3 | Complete back gesture | Navigates back to Library | ☐ |
| 8.4 | Start + cancel gesture | Returns to Camera screen | ☐ |
| 8.5 | Back from Settings tab | System handles (no navigation) | ☐ |
| 8.6 | Animation follows system | Smooth, native-feeling animation | ☐ |

---

### 9. Visibility Rules Matrix

Verify bottom bar and FAB visibility on each screen:

| Screen | Bottom Bar | FAB | Pass |
|--------|------------|-----|------|
| Library | ✓ Visible | ✓ Visible | ☐ |
| Settings | ✓ Visible | ✓ Visible | ☐ |
| Camera | ✗ Hidden | ✗ Hidden | ☐ |
| Rules (placeholder) | ✗ Hidden | ✗ Hidden | ☐ |
| Onboarding (placeholder) | ✗ Hidden | ✗ Hidden | ☐ |
| Purchase (placeholder) | ✗ Hidden | ✗ Hidden | ☐ |

---

### 10. Theme/Dark Mode (if system dark mode enabled)

| # | Test | Expected Result | Pass |
|---|------|-----------------|------|
| 10.1 | Enable system dark mode | App follows system theme | ☐ |
| 10.2 | Check colors adapt | Background, text colors change | ☐ |
| 10.3 | Check brutalist elements | Borders still visible in dark mode | ☐ |
| 10.4 | Status bar icons | Light icons on dark background | ☐ |

---

### Quick Smoke Test (5 minutes)

If short on time, test these critical paths:

1. ☐ App launches to Library with empty state
2. ☐ Bottom nav switches between Library and Settings
3. ☐ FAB visible on both main screens, tapping goes to Camera
4. ☐ FAB and bottom bar hidden on Camera screen
5. ☐ Predictive back gesture shows preview and navigates back
6. ☐ Settings screen scrolls and shows all 5 sections

---

**Tester:** _________________ **Date:** _________________ **Build:** _________________

---
