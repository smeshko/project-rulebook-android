# Epic Technical Context

## Epic 1: Foundation & Design System

**User Value:** Enables all subsequent features (no direct user value)

**PRD Coverage:** Infrastructure for all FRs

**Architecture Context:**
- Multi-module Gradle project with version catalogs
- Koin 4.x dependency injection setup
- Room database schema creation
- DataStore preferences initialization
- Retrofit + OkHttp network client
- TelemetryDeck analytics integration
- Compose Navigation shell

**UX Context:**
- RulebookTheme with Material 3 base
- Brutalist design tokens (colors, typography, spacing)
- Custom modifiers (brutalistShadow, brutalistBorder)
- Core components (RulebookButton, RulebookCard, RulebookHeaderBar)

**Dependencies:** None (first epic)

---

## Epic 2: App Shell & Navigation

**User Value:** User can launch app, see home screen, navigate between tabs

**PRD Coverage:** FR27 (library grid), FR33 (empty state), FR50 (edge-to-edge)

**Architecture Context:**
- MainActivity with edge-to-edge setup
- RulebookNavHost with Compose Navigation
- Bottom NavigationBar (Library, Settings tabs)
- FloatingActionButton for camera access
- WindowInsets handling

**UX Context:**
- Tab navigation with instant switching
- FAB always visible on main screens
- Empty library state with encouraging CTA
- Brutalist header bars per screen

**Dependencies:** Epic 1 (design system, navigation)

---

## Epic 3: Onboarding Experience

**User Value:** New users understand the app and receive 3 free credits

**PRD Coverage:** FR1-4, FR34 (credit initialization)

**Architecture Context:**
- `feature/onboarding` module
- DataStore for onboarding completion flag
- DataStore for credit balance persistence
- OnboardingViewModel with MVI pattern

**UX Context:**
- 2-screen horizontal pager
- Bold brutalist illustrations/messaging
- Skip button on each screen
- CTA to complete and receive credits

**Dependencies:** Epic 1 (design system), Epic 2 (navigation integration)

---

## Epic 4: Photo Capture Flow

**User Value:** User can take or select photos of game boxes

**PRD Coverage:** FR5-11, FR51-52

**Architecture Context:**
- `feature/camera` module
- CameraX 1.4.x integration
- ImageCapture use case
- Runtime permission handling
- Image compression before upload
- Coil for image loading

**UX Context:**
- Full-screen camera preview
- Flash toggle, pinch-to-zoom, tap-to-focus
- Gallery picker as alternative
- Credit balance display overlay
- Haptic feedback on capture

**Dependencies:** Epic 1 (design system), Epic 3 (credits exist)

---

## Epic 5: Game Recognition & Rules Generation

**User Value:** User's photo becomes playable rules in <60 seconds

**PRD Coverage:** FR12-18, FR26

**Architecture Context:**
- `feature/rules` module (generation flow)
- `core/network` Retrofit API client
- AI recognition endpoint integration
- Rules generation endpoint
- Room database for rules storage
- Result<T> error handling pattern

**UX Context:**
- 5-phase progress indicator with messaging
- Confidence badge display
- Confirmation dialog for <80% confidence
- Manual entry fallback field
- Cancel option throughout

**Dependencies:** Epic 4 (photo capture provides image)

---

## Epic 6: Rules Display & Reference

**User Value:** User can read, understand, and share rules

**PRD Coverage:** FR19-25, FR32

**Architecture Context:**
- `feature/rules` module (display)
- Room database queries
- Share intent integration
- Offline-first data access

**UX Context:**
- Game header with metadata badges
- CollapsibleSection components (4 sections)
- Color-coded headers (orange/blue/yellow/purple)
- Setup checklist with toggle persistence
- Share button with system share sheet

**Dependencies:** Epic 5 (rules data exists)

---

## Epic 7: Library Management

**User Value:** User can browse, sort, and manage their game collection

**PRD Coverage:** FR27-31

**Architecture Context:**
- `feature/library` module
- Room queries with sort options
- GameRepository interface
- Delete with cascade

**UX Context:**
- 2-column grid with GameCard components
- Sort dropdown (Recent, A-Z, Date Added)
- Long-press for delete option
- Confirmation dialog for delete
- Pull-to-refresh (if applicable)

**Dependencies:** Epic 5/6 (games exist in library)

---

## Epic 8: Credit System & Purchases

**User Value:** User can purchase credits when they run out

**PRD Coverage:** FR34-41

**Architecture Context:**
- `feature/purchase` module
- `core/billing` module
- Play Billing Library 7.x
- BillingClient lifecycle management
- ProductDetails querying
- Purchase verification flow

**UX Context:**
- Paywall bottom sheet
- ProductCard components (1, 3, 10 credits)
- "Most Popular" highlighting
- Purchase in-progress states
- Success/error feedback
- Restore purchases button

**Dependencies:** Epic 1 (billing module), Epic 4 (credit gate on camera)

---

## Epic 9: Settings & Platform Polish

**User Value:** User can customize the app and enjoy native Android experience

**PRD Coverage:** FR42-49

**Architecture Context:**
- `feature/settings` module
- DataStore for theme preference
- DataStore for haptic preference
- App Shortcuts API
- Predictive Back gesture handling

**UX Context:**
- Grouped settings sections
- Theme toggle (Light/Dark/System)
- Haptic feedback toggle
- Support links (external URLs)
- App version display
- Clear data with confirmation dialog
- Long-press launcher shortcut

**Dependencies:** Epic 2 (settings screen exists)

---
