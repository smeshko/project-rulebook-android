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

**Epic 2 Complete: App Shell & Navigation**

**Stories Created:** 8
**FR Coverage:** FR27 (library view), FR33 (empty state), FR49 (predictive back), FR50 (edge-to-edge)
**Architecture Sections Referenced:** app module, feature/library, feature/settings, Compose Navigation
**UX Patterns Incorporated:** Bottom nav + FAB, empty states, edge-to-edge, predictive back

---
