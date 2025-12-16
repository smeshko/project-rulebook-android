# Epic 3: Onboarding Experience

**Goal:** Create the first-time user experience that explains the app and awards free credits. After this epic, new users see a 2-screen introduction, receive 3 free credits, and are directed to the library.

---

## Story 3.1: Onboarding Flow Detection

As a returning user,
I want to skip onboarding after completing it once,
So that I go directly to the library on subsequent launches.

**Acceptance Criteria:**

**Given** the app is launched
**When** checking onboarding status
**Then** if `hasCompletedOnboarding` is false, navigate to Onboarding (FR4)
**And** if `hasCompletedOnboarding` is true, navigate to Library

**And** the check happens before any UI is shown
**And** there is no flash of wrong screen during navigation

**Technical Notes:**
- Read from DataStore in RulebookApplication or MainActivity
- Use SplashScreen API to hold until decision is made
- Navigation happens in NavHost startDestination logic

**Prerequisites:** Epic 1 (DataStore), Epic 2 (Navigation)

---

## Story 3.2: Onboarding Screen 1 - Value Proposition

As a first-time user,
I want to understand what Rulebook does,
So that I know the app's core value before using it.

**Acceptance Criteria:**

**Given** the user is on onboarding screen 1 (FR1)
**When** the screen is displayed
**Then** it shows:
- Bold headline: "Scan any game box"
- Subtext explaining: "Point your camera at a board game and get the rules instantly"
- Illustration or graphic representing scanning
- "Next" button to proceed
- "Skip" text button to bypass onboarding (FR3)

**And** the screen uses brutalist styling (bold typography, accent colors)
**And** the screen is full-bleed (edge-to-edge)

**Technical Notes:**
- OnboardingScreen in `feature/onboarding` module
- HorizontalPager for swipe navigation between screens
- Page indicators showing current position

**Prerequisites:** Story 3.1, Epic 1 (design system)

---

## Story 3.3: Onboarding Screen 2 - Getting Started

As a first-time user,
I want to know how to start using the app,
So that I can scan my first game immediately.

**Acceptance Criteria:**

**Given** the user is on onboarding screen 2 (FR1)
**When** the screen is displayed
**Then** it shows:
- Bold headline: "3 free scans on us"
- Subtext explaining: "Start building your game library today"
- Illustration or graphic representing the gift/credits
- "Get Started" primary button to complete onboarding
- "Skip" text button still available (FR3)

**And** tapping "Get Started" completes onboarding and awards credits
**And** the screen uses brutalist styling

**Technical Notes:**
- Same HorizontalPager, second page
- "Get Started" triggers completion logic
- Swipe gesture also navigates between pages

**Prerequisites:** Story 3.2

---

## Story 3.4: Credit Award on Completion

As a first-time user,
I want to receive 3 free scan credits when I complete onboarding,
So that I can immediately try the app's core feature.

**Acceptance Criteria:**

**Given** the user completes onboarding (taps "Get Started" or "Skip") (FR2)
**When** onboarding completion is triggered
**Then** `creditBalance` is set to 3 in DataStore
**And** `hasCompletedOnboarding` is set to true (FR4)
**And** user is navigated to Library screen
**And** credits are only awarded once (idempotent)

**Technical Notes:**
- OnboardingViewModel handles completion
- DataStore transaction ensures both writes succeed
- If credits already > 0, don't overwrite (edge case)

**Prerequisites:** Story 3.3, Epic 1 (DataStore)

---

## Story 3.5: Skip Onboarding Functionality

As an impatient user,
I want to skip onboarding at any point,
So that I can start using the app immediately.

**Acceptance Criteria:**

**Given** the user is on any onboarding screen (FR3)
**When** the user taps "Skip"
**Then** onboarding is marked complete
**And** 3 free credits are awarded (same as completing)
**And** user is navigated to Library

**And** skip button is visible on both screens
**And** skip does not require confirmation

**Technical Notes:**
- Same completion logic as "Get Started"
- Skip button positioned consistently (top-right or bottom)
- No penalty for skipping

**Prerequisites:** Story 3.4

---

## Story 3.6: Onboarding Page Indicator

As a user,
I want to see my progress through onboarding,
So that I know how many screens remain.

**Acceptance Criteria:**

**Given** the user is viewing onboarding
**When** page indicators are displayed
**Then** dots show total pages (2) and current position
**And** current page dot is highlighted (filled/larger)
**And** inactive dots are subtle but visible

**And** indicators use brutalist styling (sharp dots, accent color)
**And** indicators are positioned at bottom of content area

**Technical Notes:**
- Custom indicator composable or HorizontalPagerIndicator
- Synced with HorizontalPager state
- Animated transitions between states

**Prerequisites:** Story 3.2

---

## Story 3.7: Onboarding Analytics Events

As a product owner,
I want to track onboarding completion and skip rates,
So that I can optimize the first-time experience.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** onboarding events occur
**Then** the following events are tracked:
- `onboarding_started` - When onboarding screen appears
- `onboarding_page_viewed` - With page number (1 or 2)
- `onboarding_completed` - When "Get Started" tapped
- `onboarding_skipped` - When "Skip" tapped, with page number

**And** events match iOS event names for cross-platform consistency

**Technical Notes:**
- AnalyticsManager from core/analytics
- Events fired from OnboardingViewModel
- Include page number as property where relevant

**Prerequisites:** Epic 1 (Analytics), Story 3.4

---

**Epic 3 Complete: Onboarding Experience**

**Stories Created:** 7
**FR Coverage:** FR1 (2-screen intro), FR2 (3 free credits), FR3 (skip), FR4 (remember completion), FR34 (credit tracking)
**Architecture Sections Referenced:** feature/onboarding, core/datastore, core/analytics
**UX Patterns Incorporated:** Horizontal pager, page indicators, bold CTAs, skip option

---
