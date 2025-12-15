# project-rulebook-android - Epic Breakdown

**Author:** Ivo
**Date:** 2025-12-03
**Project Level:** Mobile Application
**Target Scale:** Consumer Entertainment / Utility

---

## Overview

This document provides the complete epic and story breakdown for project-rulebook-android, decomposing the requirements from the [PRD](./prd.md) into implementable stories.

**Input Documents:**
- PRD: 52 functional requirements across 7 categories
- Architecture: Multi-module structure, MVI pattern, Kotlin/Compose stack
- UX Design: Brutalist design system, component library, user journeys

---

## Context Validation

### Prerequisite Check

| Document | Status | Content |
|----------|--------|---------|
| **PRD** | PRESENT | 52 FRs, 24 NFRs, 4 user journeys |
| **Architecture** | PRESENT | 16 modules, 10 technology decisions |
| **UX Design** | PRESENT | Brutalist design system, component strategy |

All prerequisites met.

---

## Functional Requirements Inventory

### Onboarding & First-Time Experience (4 FRs)

| FR | Description |
|----|-------------|
| FR1 | First-time users can view a 2-screen introduction explaining core app functionality |
| FR2 | First-time users receive 3 free scan credits upon completing onboarding |
| FR3 | Users can skip onboarding at any point |
| FR4 | System remembers onboarding completion and does not show it again |

### Photo Capture & Image Processing (7 FRs)

| FR | Description |
|----|-------------|
| FR5 | Users can capture photos using the device camera |
| FR6 | Users can control camera flash/torch during capture |
| FR7 | Users can zoom the camera view (pinch gesture) |
| FR8 | Users can tap to focus the camera on a specific area |
| FR9 | Users can select existing photos from device gallery |
| FR10 | System compresses and optimizes images before upload |
| FR11 | Users can see their current credit balance while in camera view |

### Game Recognition & AI Analysis (6 FRs)

| FR | Description |
|----|-------------|
| FR12 | System can identify board games from box photos using AI |
| FR13 | System displays confidence level for game identification |
| FR14 | Users can confirm or reject AI-suggested game identification |
| FR15 | Users can manually enter game name when AI confidence is low |
| FR16 | System uses fallback AI model for obscure/unrecognized games |
| FR17 | Users can retry failed recognition attempts |

### Rules Generation & Display (9 FRs)

| FR | Description |
|----|-------------|
| FR18 | System generates structured rules for identified games |
| FR19 | Users can view game overview with summary and win condition |
| FR20 | Users can view step-by-step setup instructions |
| FR21 | Users can mark setup steps as complete (checklist) |
| FR22 | Users can view first-round gameplay guide |
| FR23 | Users can view advanced rules and deep-dive content |
| FR24 | Users can expand/collapse individual rule sections |
| FR25 | Users can share generated rules via system share sheet |
| FR26 | System displays progress during rules generation phases |

### Game Library Management (7 FRs)

| FR | Description |
|----|-------------|
| FR27 | Users can view all saved games in a grid layout |
| FR28 | Users can sort library by recent, alphabetical, or date added |
| FR29 | Users can select a saved game to view its rules |
| FR30 | Users can delete saved games from library |
| FR31 | System prompts for confirmation before deleting games |
| FR32 | Users can access saved games and rules while offline |
| FR33 | System displays empty state when library has no games |

### Credit System & Monetization (8 FRs)

| FR | Description |
|----|-------------|
| FR34 | System tracks remaining scan credits |
| FR35 | System consumes one credit per successful scan |
| FR36 | Users can view available credit packs for purchase |
| FR37 | Users can purchase credit packs (1, 3, or 10 credits) |
| FR38 | System displays paywall when user has no credits |
| FR39 | Users can restore previous purchases |
| FR40 | System handles pending purchases (Ask-to-Buy) |
| FR41 | Users can see purchase confirmation and credit delivery status |

### User Preferences & Settings (6 FRs)

| FR | Description |
|----|-------------|
| FR42 | Users can switch between light, dark, and system themes |
| FR43 | Users can enable/disable haptic feedback |
| FR44 | Users can access support/bug reporting links |
| FR45 | Users can view app version and legal information |
| FR46 | Users can clear all app data (reset to fresh state) |
| FR47 | System prompts for confirmation before clearing data |

### Android Platform Integration (5 FRs)

| FR | Description |
|----|-------------|
| FR48 | Users can access "Scan Game" action via app shortcut (long-press icon) |
| FR49 | System supports predictive back gesture navigation |
| FR50 | App displays content edge-to-edge under system bars |
| FR51 | System requests camera permission at point of use (not install) |
| FR52 | Users can navigate to system settings to grant permissions |

---

**Total: 52 Functional Requirements**

---

## Epic Structure Plan

### Epic Design Strategy

**Guiding Principles:**
1. **User-Value First** - Each epic delivers something users can accomplish
2. **Architecture Alignment** - Epics respect the 16-module structure
3. **Incremental Delivery** - Each epic builds on previous, independently valuable
4. **Natural Dependencies** - Foundation → Core Features → Enhancement

**Module-to-Epic Mapping:**

| Architecture Module | Epic Coverage |
|---------------------|---------------|
| `app/` | Epic 1 (shell), Epic 2 (navigation) |
| `core/designsystem` | Epic 1 |
| `core/data`, `core/database`, `core/network` | Epic 1 |
| `core/model`, `core/common` | Epic 1 |
| `core/analytics` | Epic 1 |
| `core/datastore` | Epic 1, Epic 3 |
| `core/billing` | Epic 8 |
| `feature/onboarding` | Epic 3 |
| `feature/camera` | Epic 4 |
| `feature/rules` | Epic 5, Epic 6 |
| `feature/library` | Epic 2, Epic 7 |
| `feature/settings` | Epic 2, Epic 9 |
| `feature/purchase` | Epic 8 |

---

### Epic Overview

| Epic | Title | User Value | FRs Covered |
|------|-------|------------|-------------|
| **1** | Foundation & Design System | Technical enablement | Infrastructure |
| **2** | App Shell & Navigation | Launch and navigate app | FR27, FR33, FR50 |
| **3** | Onboarding Experience | Understand app, get free credits | FR1-4, FR34 |
| **4** | Photo Capture Flow | Take/select game box photos | FR5-11, FR51-52 |
| **5** | Game Recognition & Rules Generation | Photo becomes rules | FR12-18, FR26 |
| **6** | Rules Display & Reference | Read and reference rules | FR19-25, FR32 |
| **7** | Library Management | Manage game collection | FR27-31 |
| **8** | Credit System & Purchases | Buy and manage credits | FR34-41 |
| **9** | Settings & Platform Polish | Customize and polish app | FR42-49 |

---

## Epic Technical Context

### Epic 1: Foundation & Design System

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

### Epic 2: App Shell & Navigation

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

### Epic 3: Onboarding Experience

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

### Epic 4: Photo Capture Flow

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

### Epic 5: Game Recognition & Rules Generation

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

### Epic 6: Rules Display & Reference

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

### Epic 7: Library Management

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

### Epic 8: Credit System & Purchases

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

### Epic 9: Settings & Platform Polish

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

## Epic 1: Foundation & Design System

**Goal:** Establish the technical foundation and design system that enables all subsequent feature development. After this epic, the project has a working multi-module structure, core infrastructure, and brutalist component library.

---

### Story 1.1: Project Initialization & Multi-Module Structure

As a developer,
I want the Android project initialized with the correct multi-module structure,
So that feature development can proceed with proper separation of concerns.

**Acceptance Criteria:**

**Given** Android Studio with Empty Compose Activity template
**When** the project is initialized
**Then** the following module structure exists:
- `app/` - Application entry point
- `feature/library/`, `feature/camera/`, `feature/rules/`, `feature/settings/`, `feature/onboarding/`, `feature/purchase/`
- `core/designsystem/`, `core/data/`, `core/database/`, `core/network/`, `core/model/`, `core/common/`, `core/analytics/`, `core/billing/`, `core/datastore/`

**And** `settings.gradle.kts` includes all modules
**And** `gradle/libs.versions.toml` contains version catalog with:
- Kotlin 2.x
- Compose BOM (latest stable)
- Koin 4.x
- Room 2.6.x
- Retrofit 2.11.0
- OkHttp 4.12.0
- CameraX 1.4.x
- Play Billing 7.x
- Coil 3.x
- kotlinx.serialization 1.7.x

**And** root `build.gradle.kts` configures:
- minSdk = 34
- targetSdk = 35
- compileSdk = 35

**Technical Notes:**
- Follow Now in Android reference architecture (Architecture section)
- Use Kotlin DSL for all Gradle files
- Configure R8/ProGuard for release builds

**Prerequisites:** None

---

### Story 1.2: Dependency Injection Setup with Koin

As a developer,
I want Koin dependency injection configured across all modules,
So that dependencies are properly scoped and testable.

**Acceptance Criteria:**

**Given** the multi-module project structure
**When** Koin is configured
**Then** `RulebookApplication.kt` initializes Koin with all modules

**And** each feature module has a `di/` package with module definition:
```kotlin
val libraryModule = module {
    viewModel { LibraryViewModel(get()) }
}
```

**And** core modules expose their dependencies via Koin modules
**And** the app module aggregates all Koin modules

**Technical Notes:**
- Use Koin 4.x with Compose integration (Architecture section)
- ViewModels scoped to navigation destinations
- Repositories as singletons
- Use `koinViewModel()` in Composables

**Prerequisites:** Story 1.1

---

### Story 1.3: Room Database Schema & DAOs

As a developer,
I want the Room database configured with entity schemas,
So that game and rules data can be persisted offline.

**Acceptance Criteria:**

**Given** the `core/database` module
**When** Room is configured
**Then** `RulebookDatabase.kt` defines the database with entities:
- `GameEntity` (id, title, thumbnailUrl, createdAt, lastAccessedAt)
- `RulesEntity` (id, gameId, overview, setup, firstRound, advanced, rawJson)

**And** DAOs exist for each entity:
- `GameDao` with insert, update, delete, getAll, getById, getAllSorted
- `RulesDao` with insert, getByGameId, deleteByGameId

**And** table names use `snake_case` (e.g., `saved_games`)
**And** timestamps stored as `Long` (milliseconds)
**And** JSON data stored as `String` with TypeConverters

**Technical Notes:**
- Room 2.6.x with KSP for annotation processing (Architecture section)
- Use `@Transaction` for complex queries
- Primary keys named `id`
- Foreign key from rules to games with CASCADE delete

**Prerequisites:** Story 1.1

---

### Story 1.4: DataStore Preferences Setup

As a developer,
I want DataStore configured for user preferences,
So that settings and state persist across app sessions.

**Acceptance Criteria:**

**Given** the `core/datastore` module
**When** DataStore is configured
**Then** `RulebookPreferences.kt` provides access to:
- `hasCompletedOnboarding: Flow<Boolean>`
- `creditBalance: Flow<Int>`
- `themeMode: Flow<ThemeMode>` (LIGHT, DARK, SYSTEM)
- `hapticsEnabled: Flow<Boolean>`

**And** each preference has suspend functions to update
**And** default values are sensible (onboarding=false, credits=0, theme=SYSTEM, haptics=true)

**Technical Notes:**
- Use Preferences DataStore (not Proto) for simplicity (Architecture section)
- Expose as Flow for reactive updates
- Repository pattern wraps DataStore access

**Prerequisites:** Story 1.1

---

### Story 1.5: Network Client Configuration

As a developer,
I want Retrofit and OkHttp configured for API communication,
So that the app can communicate with the backend.

**Acceptance Criteria:**

**Given** the `core/network` module
**When** the network client is configured
**Then** `RulebookApiClient.kt` provides Retrofit instance with:
- Base URL configuration (from BuildConfig)
- kotlinx.serialization converter
- OkHttp client with logging interceptor (debug only)
- 30-second timeout default

**And** API interface defines endpoints (placeholder for now):
```kotlin
interface RulebookApi {
    @POST("analyze")
    suspend fun analyzeImage(@Body request: AnalyzeRequest): AnalyzeResponse

    @POST("generate")
    suspend fun generateRules(@Body request: GenerateRequest): GenerateResponse
}
```

**And** request/response models use `@Serializable` annotation
**And** `@SerialName` maps to snake_case API fields

**Technical Notes:**
- Retrofit 2.11.0 + OkHttp 4.12.0 (Architecture section)
- kotlinx.serialization for JSON (no reflection)
- Network security config enforces HTTPS

**Prerequisites:** Story 1.1

---

### Story 1.6: Analytics Integration with TelemetryDeck

As a developer,
I want TelemetryDeck analytics configured,
So that usage events can be tracked consistently with iOS.

**Acceptance Criteria:**

**Given** the `core/analytics` module
**When** TelemetryDeck is configured
**Then** `AnalyticsManager.kt` provides:
- `trackEvent(name: String, properties: Map<String, String>)`
- `trackScreenView(screenName: String)`

**And** TelemetryDeck SDK 6.0.1 is initialized in Application class
**And** App ID is configured via BuildConfig
**And** no PII is transmitted

**Technical Notes:**
- TelemetryDeck Kotlin SDK 6.0.1 (Architecture section)
- Same event names as iOS for cross-platform consistency
- Privacy-focused, GDPR compliant

**Prerequisites:** Story 1.1

---

### Story 1.7: Design Tokens & Theme Foundation

As a developer,
I want the Rulebook theme and design tokens implemented,
So that the brutalist design system is consistently applied.

**Acceptance Criteria:**

**Given** the `core/designsystem` module
**When** design tokens are implemented
**Then** `RulebookColors.kt` defines:
- Light/Dark surface palette (Primary #FFFFFF/#1C1C1E, Secondary #FFF9F0/#2C2C2E)
- Content palette with opacity variants
- Accent palette (Orange #FF6B35, Blue #3498DB, Yellow #FFD23F, Purple #7209B7, Pink #E91E63, Green #2ECC71, Red #E74C3C)

**And** `RulebookTypography.kt` defines:
- Display styles (34sp Bold, 28sp Bold, 22sp SemiBold)
- Brutalist styles (24sp Black, 16sp Black, 14sp Black)
- Body styles (17sp Regular, 16sp Regular)
- Caption (12sp Regular)

**And** `RulebookSpacing.kt` defines:
- xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp
- Brutalist: borderWidth=3dp, shadowOffset=4dp

**And** `RulebookTheme.kt` wraps MaterialTheme with custom colors, typography, shapes (0dp corners)

**Technical Notes:**
- Import values from `design-system-android.json` (UX Design section)
- Use Material 3 as foundation with Rulebook overrides
- Sharp corners (0dp radius) for brutalist aesthetic

**Prerequisites:** Story 1.1

---

### Story 1.8: Brutalist Modifier Extensions

As a developer,
I want brutalist modifier extensions for shadows and borders,
So that components can easily apply the signature visual style.

**Acceptance Criteria:**

**Given** the design tokens are implemented
**When** modifier extensions are created
**Then** `Modifier.brutalistShadow()` applies:
- Offset rectangle shadow (default 4dp offset)
- Configurable offset and color
- Works with any composable

**And** `Modifier.brutalistBorder()` applies:
- Thick border (default 3dp)
- Configurable width and color
- Sharp corners (0dp)

**And** modifiers compose cleanly with other modifiers

**Technical Notes:**
- Use `drawBehind` for custom shadow rendering (UX Design section)
- Shadow is solid rectangle, not blur
- Border uses `BorderStroke` with RectangleShape

**Prerequisites:** Story 1.7

---

### Story 1.9: Core UI Components - RulebookButton

As a developer,
I want the RulebookButton component implemented,
So that all buttons have consistent brutalist styling.

**Acceptance Criteria:**

**Given** the theme and modifiers exist
**When** RulebookButton is implemented
**Then** it supports variants:
- `Primary` - Filled pink (#E91E63), border, offset shadow
- `Secondary` - Outlined, no fill, border only
- `Destructive` - Filled red (#E74C3C), border, offset shadow

**And** it accepts:
- `text: String`
- `onClick: () -> Unit`
- `enabled: Boolean`
- `modifier: Modifier`

**And** disabled state shows reduced opacity
**And** touch feedback uses ripple effect
**And** text uses Black weight (900) typography

**Technical Notes:**
- Build on Material 3 Button with custom styling (UX Design section)
- 48dp minimum touch target
- 20dp horizontal padding

**Prerequisites:** Story 1.8

---

### Story 1.10: Core UI Components - RulebookCard

As a developer,
I want the RulebookCard component implemented,
So that cards have consistent brutalist styling.

**Acceptance Criteria:**

**Given** the theme and modifiers exist
**When** RulebookCard is implemented
**Then** it applies:
- Surface background color
- 3dp black border
- 4dp offset shadow
- 0dp corner radius
- 16dp internal padding

**And** it accepts:
- `modifier: Modifier`
- `onClick: (() -> Unit)?` (optional click handling)
- `content: @Composable () -> Unit`

**And** clickable cards show ripple effect

**Technical Notes:**
- Build on Material 3 Card with custom styling (UX Design section)
- Shadow offset configurable for elevation hierarchy

**Prerequisites:** Story 1.8

---

### Story 1.11: Core UI Components - RulebookHeaderBar

As a developer,
I want the RulebookHeaderBar component implemented,
So that screen headers have consistent styling.

**Acceptance Criteria:**

**Given** the theme exists
**When** RulebookHeaderBar is implemented
**Then** it displays:
- Title text in brutalist style (24sp Black)
- Thick bottom border (3dp)
- Optional back navigation icon
- Optional action buttons

**And** it accepts:
- `title: String`
- `onBackClick: (() -> Unit)?`
- `actions: @Composable RowScope.() -> Unit`

**And** it respects status bar insets (edge-to-edge)

**Technical Notes:**
- Use TopAppBar as base with custom styling
- Back icon is standard Android arrow
- WindowInsets for status bar padding

**Prerequisites:** Story 1.7

---

### Story 1.12: Domain Models

As a developer,
I want domain models defined in core/model,
So that data structures are shared across modules.

**Acceptance Criteria:**

**Given** the `core/model` module
**When** domain models are defined
**Then** the following data classes exist:

```kotlin
data class Game(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val createdAt: Long,
    val lastAccessedAt: Long
)

data class Rules(
    val gameId: String,
    val overview: RuleSection,
    val setup: RuleSection,
    val firstRound: RuleSection,
    val advanced: RuleSection
)

data class RuleSection(
    val title: String,
    val content: String,
    val items: List<String>? = null // For checklist items
)

data class ScanResult(
    val gameTitle: String,
    val confidence: Float,
    val thumbnailUrl: String?
)
```

**And** models are pure Kotlin (no Android dependencies)
**And** models are immutable data classes

**Technical Notes:**
- core/model has no dependencies (Architecture section)
- Used by both data layer and UI layer
- Mappers convert between Entity and Domain models

**Prerequisites:** Story 1.1

---

### Story 1.13: Result Wrapper & Error Handling

As a developer,
I want the Result wrapper pattern implemented,
So that errors are handled consistently across the app.

**Acceptance Criteria:**

**Given** the `core/common` module
**When** Result wrapper is implemented
**Then** sealed class exists:

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : Result<Nothing>()
}
```

**And** extension functions exist:
- `Result.map()` - Transform success data
- `Result.onSuccess()` - Execute on success
- `Result.onError()` - Execute on error
- `Result.getOrNull()` - Get data or null

**And** all repository operations return `Result<T>`

**Technical Notes:**
- User-friendly error messages in Error (Architecture section)
- Original exceptions preserved for logging
- Used by all repositories

**Prerequisites:** Story 1.1

---

**Epic 1 Complete: Foundation & Design System**

**Stories Created:** 13
**FR Coverage:** Infrastructure enabling all FRs
**Architecture Sections Referenced:** All core modules
**UX Patterns Incorporated:** Design tokens, brutalist modifiers, core components

---

## Epic 1: Dependency Flowchart

```
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 1: Start Immediately                                                    ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [1.1] Project Initialization & Multi-Module Structure                        ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 2: After 1.1 (PARALLEL x8)                                              ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [1.2] DI Setup (Koin)         ║  [1.3] Room Database       ║  [1.4] DataStore║
║  [1.5] Network Client          ║  [1.6] Analytics           ║  [1.7] Theme    ║
║  [1.12] Domain Models          ║  [1.13] Result Wrapper                       ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
                                        │
          ┌─────────────────────────────┴─────────────────────────────┐
          │                                                           │
          ▼                                                           ▼
╔═══════════════════════════════════════╗   ╔═════════════════════════════════════╗
║  WAVE 3a: After 1.7                   ║   ║  WAVE 3b: After 1.7                 ║
╠═══════════════════════════════════════╣   ╠═════════════════════════════════════╣
║                                       ║   ║                                     ║
║  [1.8] Brutalist Modifiers            ║   ║  [1.11] RulebookHeaderBar           ║
║                                       ║   ║                                     ║
╚═══════════════════════════════════════╝   ╚═════════════════════════════════════╝
                    │
                    ▼
╔═══════════════════════════════════════════════════════════════════════════════╗
║  WAVE 4: After 1.8 (PARALLEL x2)                                              ║
╠═══════════════════════════════════════════════════════════════════════════════╣
║                                                                               ║
║  [1.9] RulebookButton                  ║  [1.10] RulebookCard                  ║
║                                                                               ║
╚═══════════════════════════════════════════════════════════════════════════════╝
```

**Execution Summary:**
- **Wave 1:** 1 story (start immediately)
- **Wave 2:** 8 stories (can run in parallel after Wave 1)
- **Wave 3:** 2 stories (can run in parallel after Wave 2, specifically after 1.7)
- **Wave 4:** 2 stories (can run in parallel after Wave 3, specifically after 1.8)

**Critical Path:** 1.1 → 1.7 → 1.8 → 1.9/1.10

---

## Epic 2: App Shell & Navigation

**Goal:** Create the main app structure with bottom navigation, FAB, and basic screens. After this epic, users can launch the app, see the library (empty state), navigate to settings, and tap the camera FAB.

---

### Story 2.1: MainActivity with Edge-to-Edge Display

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

### Story 2.2: Navigation Host & Route Definitions

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

### Story 2.3: Bottom Navigation Bar

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

### Story 2.4: Floating Action Button for Camera

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

### Story 2.5: Library Screen Shell with Empty State

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

### Story 2.6: Settings Screen Shell

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

### Story 2.7: Scaffold Integration & Screen Composition

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

### Story 2.8: Predictive Back Gesture Support

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

## Epic 3: Onboarding Experience

**Goal:** Create the first-time user experience that explains the app and awards free credits. After this epic, new users see a 2-screen introduction, receive 3 free credits, and are directed to the library.

---

### Story 3.1: Onboarding Flow Detection

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

### Story 3.2: Onboarding Screen 1 - Value Proposition

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

### Story 3.3: Onboarding Screen 2 - Getting Started

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

### Story 3.4: Credit Award on Completion

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

### Story 3.5: Skip Onboarding Functionality

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

### Story 3.6: Onboarding Page Indicator

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

### Story 3.7: Onboarding Analytics Events

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

## Epic 4: Photo Capture Flow

**Goal:** Implement the camera experience for capturing game box photos. After this epic, users can open the camera, control flash/zoom/focus, select from gallery, and see their credit balance.

---

### Story 4.1: Camera Screen with CameraX Preview

As a user,
I want to see a live camera preview when I tap the scan button,
So that I can frame my game box photo.

**Acceptance Criteria:**

**Given** the user taps the camera FAB (FR5)
**When** the camera screen opens
**Then** a live camera preview fills the screen
**And** the preview starts within 1 second (NFR3)
**And** the preview uses the rear camera by default
**And** the screen is full-screen (no bottom nav)

**Technical Notes:**
- CameraX 1.4.x Preview use case (Architecture section)
- PreviewView bound to lifecycle
- CameraScreen in `feature/camera` module
- Handle camera lifecycle properly (pause/resume)

**Prerequisites:** Epic 2 (navigation), Epic 1 (design system)

---

### Story 4.2: Photo Capture Button with Haptic Feedback

As a user,
I want to tap a button to capture my photo,
So that I can submit my game box image for analysis.

**Acceptance Criteria:**

**Given** the camera preview is active
**When** the user taps the capture button
**Then** a photo is captured from the current preview
**And** haptic feedback confirms the capture (medium click)
**And** the captured image is passed to the next step (processing)
**And** the capture button is prominent and easy to tap (48dp+)

**And** the capture button has brutalist styling (circle with border)

**Technical Notes:**
- CameraX ImageCapture use case
- Save to temporary file or in-memory
- VibrationEffect for haptic (respects system setting)
- Disable button during capture to prevent double-tap

**Prerequisites:** Story 4.1

---

### Story 4.3: Flash/Torch Control

As a user,
I want to control the camera flash,
So that I can photograph game boxes in low-light conditions.

**Acceptance Criteria:**

**Given** the camera preview is active (FR6)
**When** the user taps the flash toggle
**Then** the flash mode cycles: Off → On → Auto → Off
**And** the current mode is indicated by icon state
**And** torch is activated immediately in "On" mode (not just on capture)

**And** the toggle is positioned in the camera UI (top area)
**And** devices without flash hide this control

**Technical Notes:**
- CameraX `ImageCapture.flashMode` and `Camera.cameraControl.enableTorch()`
- Check `CameraInfo.hasFlashUnit()` before showing
- Icon states: flash_off, flash_on, flash_auto

**Prerequisites:** Story 4.1

---

### Story 4.4: Pinch-to-Zoom Gesture

As a user,
I want to zoom the camera with a pinch gesture,
So that I can focus on the game box from a distance.

**Acceptance Criteria:**

**Given** the camera preview is active (FR7)
**When** the user performs a pinch gesture
**Then** the camera zooms in (spread) or out (pinch)
**And** zoom is smooth and responsive
**And** zoom level is bounded (1x to max supported)
**And** optional: zoom level indicator appears briefly

**Technical Notes:**
- CameraX `Camera.cameraControl.setZoomRatio()`
- Use `ScaleGestureDetector` or Compose gesture detection
- Animate zoom changes for smoothness

**Prerequisites:** Story 4.1

---

### Story 4.5: Tap-to-Focus

As a user,
I want to tap the preview to focus on a specific area,
So that I get a sharp image of the game box.

**Acceptance Criteria:**

**Given** the camera preview is active (FR8)
**When** the user taps on the preview
**Then** the camera focuses on that point
**And** a focus indicator appears briefly at the tap location
**And** auto-focus continues after manual focus

**Technical Notes:**
- CameraX `Camera.cameraControl.startFocusAndMetering()`
- Create `MeteringPoint` from tap coordinates
- Show animated focus ring (fade in/out)

**Prerequisites:** Story 4.1

---

### Story 4.6: Gallery Picker Alternative

As a user,
I want to select a photo from my gallery,
So that I can use an existing image of a game box.

**Acceptance Criteria:**

**Given** the camera screen is displayed (FR9)
**When** the user taps the gallery button
**Then** the system photo picker opens
**And** selecting a photo returns it for processing
**And** canceling returns to camera preview

**And** the gallery button shows a thumbnail of the last photo (optional)
**And** the button is positioned near the capture button

**Technical Notes:**
- Use AndroidX Activity Result API with `PickVisualMedia`
- `ActivityResultContracts.PickVisualMedia()`
- Handle result in CameraViewModel

**Prerequisites:** Story 4.1

---

### Story 4.7: Image Compression Before Upload

As a developer,
I want images compressed before upload,
So that API calls are fast and bandwidth-efficient.

**Acceptance Criteria:**

**Given** a photo is captured or selected (FR10)
**When** processing the image
**Then** the image is compressed to <1MB
**And** compression completes in <2 seconds (NFR6)
**And** quality is sufficient for AI recognition
**And** EXIF orientation is preserved/corrected

**Technical Notes:**
- Use Android Bitmap compression (JPEG 80% quality)
- Resize to max 1920px on longest edge
- Handle rotation from EXIF data
- Process on background thread (Dispatchers.IO)

**Prerequisites:** Story 4.2

---

### Story 4.8: Credit Balance Display on Camera

As a user,
I want to see my remaining credits while in the camera,
So that I know if I can complete a scan.

**Acceptance Criteria:**

**Given** the camera screen is displayed (FR11)
**When** the credit balance is shown
**Then** it displays the current credit count (e.g., "3 credits")
**And** it's positioned unobtrusively (top corner)
**And** it updates reactively if credits change

**And** low credit warning (1 credit) uses warning color
**And** zero credits shows different state (handled in Epic 8)

**Technical Notes:**
- Observe `creditBalance` Flow from DataStore
- CreditsDisplay composable from design system
- Semi-transparent background for readability over preview

**Prerequisites:** Story 4.1, Epic 1 (DataStore)

---

### Story 4.9: Camera Permission Handling

As a user,
I want to be asked for camera permission only when I try to scan,
So that the app doesn't request unnecessary permissions at install.

**Acceptance Criteria:**

**Given** the user taps the camera FAB (FR51)
**When** camera permission is not granted
**Then** a permission rationale is shown explaining why camera is needed
**And** the system permission dialog appears
**And** if granted, camera opens immediately
**And** if denied, helpful message with settings link appears (FR52)

**And** permission is not requested at app launch or install

**Technical Notes:**
- Use `rememberPermissionState` from Accompanist or manual handling
- `Manifest.permission.CAMERA`
- Show rationale before system dialog (shouldShowRationale)
- Deep link to app settings: `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`

**Prerequisites:** Story 4.1

---

### Story 4.10: Camera Close/Back Navigation

As a user,
I want to close the camera and return to the library,
So that I can exit if I change my mind.

**Acceptance Criteria:**

**Given** the camera screen is displayed
**When** the user taps back or the close button
**Then** the camera is released properly
**And** user returns to the previous screen (Library)
**And** no resources leak (camera, memory)

**And** predictive back gesture shows library preview
**And** close button (X) is visible in top corner

**Technical Notes:**
- Release CameraProvider on screen exit
- Use `DisposableEffect` for cleanup
- BackHandler for custom back logic if needed

**Prerequisites:** Story 4.1, Epic 2 (predictive back)

---

**Epic 4 Complete: Photo Capture Flow**

**Stories Created:** 10
**FR Coverage:** FR5-11, FR51-52
**Architecture Sections Referenced:** feature/camera, CameraX, permissions
**UX Patterns Incorporated:** Full-screen camera, haptic feedback, gesture controls

---

## Epic 5: Game Recognition & Rules Generation

**Goal:** Implement the AI recognition pipeline and rules generation. After this epic, users can capture a photo and receive generated rules with progress feedback and confidence handling.

---

### Story 5.1: Scan Flow Initiation & Credit Check

As a user,
I want the scan to check my credits before proceeding,
So that I don't waste time if I can't complete the scan.

**Acceptance Criteria:**

**Given** the user captures/selects a photo
**When** initiating the scan flow
**Then** credit balance is checked
**And** if credits > 0, proceed to image analysis
**And** if credits = 0, show paywall (FR38, handled in Epic 8)

**And** credit is NOT deducted until scan succeeds

**Technical Notes:**
- CameraViewModel checks credits before starting
- Navigate to paywall route if credits = 0
- Credit deduction happens only on successful rules generation

**Prerequisites:** Epic 4 (photo capture), Epic 1 (DataStore)

---

### Story 5.2: Progress Screen with Phase Indicator

As a user,
I want to see progress while my game is being analyzed,
So that I know the app is working and how long to wait.

**Acceptance Criteria:**

**Given** a scan is in progress (FR26)
**When** the progress screen is displayed
**Then** it shows 5 phases with current status:
1. Processing Image (0-15%)
2. Analyzing Image (15-40%)
3. Identifying Game (40-60%)
4. Generating Rules (60-90%)
5. Saving Rules (90-100%)

**And** current phase is highlighted with animation
**And** phase-specific message explains current action
**And** overall progress bar shows percentage
**And** cancel button allows aborting

**Technical Notes:**
- GenerationViewModel tracks phase state
- ProgressPhaseIndicator composable from design system
- Animated transitions between phases
- Cancel triggers coroutine cancellation

**Prerequisites:** Story 5.1, Epic 1 (design system)

---

### Story 5.3: Image Analysis API Integration

As a developer,
I want to call the image analysis API,
So that the game box can be identified.

**Acceptance Criteria:**

**Given** a compressed image is ready (FR12)
**When** calling the analyze endpoint
**Then** image is sent as multipart/form-data or base64
**And** response includes:
- Identified game title
- Confidence score (0.0-1.0)
- Thumbnail URL (optional)

**And** timeout is handled (30s)
**And** errors return Result.Error with user-friendly message

**Technical Notes:**
- Retrofit `@Multipart` or `@Body` with base64
- `RulebookApi.analyzeImage()` from core/network
- Map API response to `ScanResult` domain model

**Prerequisites:** Epic 1 (network client)

---

### Story 5.4: Confidence Display & Auto-Proceed Logic

As a user,
I want to see how confident the app is about the game identification,
So that I can verify it's correct before generating rules.

**Acceptance Criteria:**

**Given** the image analysis returns a result (FR13)
**When** confidence is >= 80%
**Then** auto-proceed to rules generation (no confirmation needed)
**And** briefly show identified game name

**When** confidence is < 80%
**Then** show confirmation screen with:
- Identified game name
- Confidence percentage badge
- "Is this your game?" prompt
- "Yes, continue" and "No, enter manually" buttons (FR14)

**Technical Notes:**
- ConfidenceBadge composable (color-coded: green >80%, yellow 50-80%, red <50%)
- Threshold configurable (start with 80%)
- Analytics event for confidence level

**Prerequisites:** Story 5.3

---

### Story 5.5: Manual Game Name Entry

As a user,
I want to enter the game name manually if the AI is wrong,
So that I can still get rules for obscure or misidentified games.

**Acceptance Criteria:**

**Given** the user rejects the AI suggestion or confidence is very low (FR15)
**When** manual entry is shown
**Then** a text field allows typing the game name
**And** "Generate Rules" button submits the manual name
**And** keyboard appears automatically
**And** entry supports autocomplete (optional, future)

**And** manual entry still consumes a credit

**Technical Notes:**
- TextField with brutalist styling
- CameraViewModel handles manual name submission
- Same rules generation flow after manual entry

**Prerequisites:** Story 5.4

---

### Story 5.6: Rules Generation API Integration

As a developer,
I want to call the rules generation API,
So that structured rules are created for the identified game.

**Acceptance Criteria:**

**Given** a game is identified (auto or manual) (FR18)
**When** calling the generate endpoint
**Then** request includes game name and optional thumbnail
**And** response includes structured rules:
- Overview (summary, win condition)
- Setup (step-by-step instructions)
- First Round (how to play initial turns)
- Advanced (detailed rules, edge cases)

**And** response is parsed into `Rules` domain model
**And** generation completes in <45s (allows 60s total with analysis)

**Technical Notes:**
- `RulebookApi.generateRules()` from core/network
- Parse JSON into RuleSection objects
- Handle streaming response if API supports it (future)

**Prerequisites:** Story 5.3

---

### Story 5.7: Save Rules to Database

As a user,
I want my generated rules saved automatically,
So that I can access them later without re-scanning.

**Acceptance Criteria:**

**Given** rules are successfully generated
**When** saving to database
**Then** a new `Game` record is created with:
- Generated UUID
- Game title
- Thumbnail URL
- Created timestamp
- Last accessed timestamp

**And** associated `Rules` record is created
**And** credit is deducted (FR35)
**And** user is navigated to Rules display screen

**Technical Notes:**
- GameRepository.saveGame() with transaction
- Room insert operations
- Update DataStore credit balance
- Navigate with gameId argument

**Prerequisites:** Story 5.6, Epic 1 (Room, DataStore)

---

### Story 5.8: Fallback AI Model Handling

As a user,
I want the app to try alternative AI models for obscure games,
So that I can get rules even for rare or international games.

**Acceptance Criteria:**

**Given** the primary AI model fails or returns very low confidence (FR16)
**When** fallback is triggered
**Then** secondary model is attempted
**And** user sees "Trying alternative recognition..." message
**And** if fallback succeeds, continue normal flow
**And** if fallback fails, offer manual entry

**Technical Notes:**
- Backend handles model fallback (may be transparent to client)
- Client may need to retry with different endpoint/flag
- Track fallback usage in analytics

**Prerequisites:** Story 5.3

---

### Story 5.9: Retry Failed Recognition

As a user,
I want to retry if recognition fails,
So that I can try a better photo without starting over.

**Acceptance Criteria:**

**Given** recognition fails (network error, AI error) (FR17)
**When** error screen is displayed
**Then** it shows:
- Friendly error message (not technical)
- "Try Again" button (returns to camera)
- "Enter Manually" button (go to manual entry)

**And** original photo can be retried or new photo taken
**And** no credit is consumed on failure

**Technical Notes:**
- Error state in GenerationViewModel
- Map specific errors to user-friendly messages
- Preserve compressed image for retry option

**Prerequisites:** Story 5.2

---

### Story 5.10: Scan Analytics Events

As a product owner,
I want to track scan funnel metrics,
So that I can understand conversion and failure points.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** scan events occur
**Then** the following events are tracked:
- `scan_started` - Photo captured/selected
- `scan_analysis_complete` - With confidence level
- `scan_confirmed` - User confirmed game (if prompted)
- `scan_manual_entry` - User entered name manually
- `scan_generation_complete` - Rules generated successfully
- `scan_failed` - With error type
- `scan_cancelled` - User cancelled

**Technical Notes:**
- Events fired from GenerationViewModel
- Include relevant properties (confidence, error_type, duration)
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 5 Complete: Game Recognition & Rules Generation**

**Stories Created:** 10
**FR Coverage:** FR12-18, FR26, FR35
**Architecture Sections Referenced:** feature/rules, core/network, core/database
**UX Patterns Incorporated:** Progress phases, confidence display, error recovery

---

## Epic 6: Rules Display & Reference

**Goal:** Implement the rules viewing experience with collapsible sections. After this epic, users can read generated rules with progressive disclosure, setup checklists, and sharing.

---

### Story 6.1: Rules Screen Layout

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

### Story 6.2: Overview Section - Game Summary

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

### Story 6.3: Setup Section with Checklist

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

### Story 6.4: First Round Section

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

### Story 6.5: Advanced Rules Section

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

### Story 6.6: Section Expand/Collapse Animation

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

### Story 6.7: Share Rules Functionality

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

### Story 6.8: Offline Rules Access

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

### Story 6.9: Update Last Accessed Timestamp

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

## Epic 7: Library Management

**Goal:** Implement full library functionality with grid display, sorting, and deletion. After this epic, users can browse, sort, and manage their saved game collection.

---

### Story 7.1: Game Card Grid Display

As a user,
I want to see my saved games in a visual grid,
So that I can quickly find the game I want.

**Acceptance Criteria:**

**Given** the user has saved games (FR27)
**When** the Library screen is displayed
**Then** games appear in a 2-column grid
**And** each game shows:
- Thumbnail image (or placeholder)
- Game title
- Brutalist card styling (border, shadow)

**And** grid scrolls vertically if many games
**And** cards are tappable to view rules (FR29)

**Technical Notes:**
- LazyVerticalGrid with 2 columns
- GameCard composable
- Coil AsyncImage for thumbnails
- 16dp grid spacing

**Prerequisites:** Epic 5/6 (games exist), Epic 1 (GameCard)

---

### Story 7.2: Sort Options Dropdown

As a user,
I want to sort my library by different criteria,
So that I can organize my collection my way.

**Acceptance Criteria:**

**Given** the library has games (FR28)
**When** the user taps the sort button
**Then** a dropdown shows options:
- Recent (last accessed) - default
- Alphabetical (A-Z)
- Date Added (newest first)

**And** selecting an option re-sorts immediately
**And** current sort is indicated
**And** sort preference persists across sessions

**Technical Notes:**
- DropdownMenu with brutalist styling
- LibraryViewModel manages sort state
- Room queries with ORDER BY
- Persist sort preference in DataStore

**Prerequisites:** Story 7.1, Epic 1 (DataStore)

---

### Story 7.3: Navigate to Rules from Library

As a user,
I want to tap a game card to view its rules,
So that I can reference saved games quickly.

**Acceptance Criteria:**

**Given** the library shows game cards (FR29)
**When** the user taps a game card
**Then** navigation goes to Rules screen with that gameId
**And** transition animation is smooth
**And** back navigation returns to library

**Technical Notes:**
- Navigate with argument: `navController.navigate("rules/${game.id}")`
- Game ID passed as navigation argument
- Rules screen loads from database

**Prerequisites:** Story 7.1, Epic 6 (Rules screen)

---

### Story 7.4: Delete Game with Confirmation

As a user,
I want to delete games from my library,
So that I can remove games I no longer need.

**Acceptance Criteria:**

**Given** the user wants to delete a game (FR30)
**When** the user long-presses a game card (or taps delete option)
**Then** a confirmation dialog appears (FR31)
**And** dialog shows: "Delete [Game Name]?"
**And** "Delete" button (destructive) and "Cancel" button

**When** user confirms deletion
**Then** game and rules are removed from database
**And** library updates immediately
**And** toast confirms: "Game deleted"

**Technical Notes:**
- AlertDialog with brutalist styling
- GameRepository.deleteGame(gameId) with cascade
- Optimistic UI update with rollback on error

**Prerequisites:** Story 7.1

---

### Story 7.5: Long-Press Context Menu

As a user,
I want to long-press a game for quick actions,
So that I can access options without extra navigation.

**Acceptance Criteria:**

**Given** the library shows game cards
**When** the user long-presses a card
**Then** a context menu appears with:
- "View Rules" - navigates to rules
- "Delete" - triggers delete confirmation

**And** haptic feedback on long-press
**And** menu dismisses on outside tap

**Technical Notes:**
- DropdownMenu anchored to card
- Long-press detector with haptic
- Same actions as other entry points

**Prerequisites:** Story 7.1

---

### Story 7.6: Pull-to-Refresh (Future Proofing)

As a developer,
I want pull-to-refresh implemented,
So that future cloud sync features have a refresh mechanism.

**Acceptance Criteria:**

**Given** the library screen is displayed
**When** the user pulls down on the grid
**Then** a refresh indicator appears
**And** library reloads from database (currently no-op for sync)
**And** indicator dismisses after reload

**Technical Notes:**
- `pullRefresh` modifier from Material 3
- Currently just reloads local data
- Future: trigger cloud sync

**Prerequisites:** Story 7.1

---

**Epic 7 Complete: Library Management**

**Stories Created:** 6
**FR Coverage:** FR27-31
**Architecture Sections Referenced:** feature/library, core/database
**UX Patterns Incorporated:** Card grid, sort dropdown, long-press menu, confirmation dialogs

---

## Epic 8: Credit System & Purchases

**Goal:** Implement the monetization flow with paywall and Google Play Billing. After this epic, users can purchase credit packs, restore purchases, and have credits gated appropriately.

---

### Story 8.1: Credit Balance Display Component

As a user,
I want to see my credit balance prominently,
So that I always know how many scans I have remaining.

**Acceptance Criteria:**

**Given** the user is in the app (FR34)
**When** viewing Library or Settings
**Then** credit balance is visible in the header or prominent location
**And** displays format: "X credits" or icon + number
**And** updates reactively when credits change

**Technical Notes:**
- CreditsDisplay composable
- Observe creditBalance Flow from DataStore
- Reusable across screens

**Prerequisites:** Epic 1 (DataStore, design system)

---

### Story 8.2: Credit Consumption on Successful Scan

As a user,
I want credits deducted only when a scan succeeds,
So that I don't lose credits on failed attempts.

**Acceptance Criteria:**

**Given** a scan completes successfully (FR35)
**When** rules are saved to database
**Then** credit balance is decremented by 1
**And** if balance reaches 0, future scans show paywall
**And** credit deduction is atomic with rules save

**Technical Notes:**
- Transaction in GameRepository
- DataStore update in same logical transaction
- Credit check before scan start (Story 5.1)

**Prerequisites:** Epic 5 (scan flow), Epic 1 (DataStore)

---

### Story 8.3: Paywall Screen Display

As a user,
I want to see purchase options when I run out of credits,
So that I can continue using the app.

**Acceptance Criteria:**

**Given** the user has 0 credits and tries to scan (FR38)
**When** the paywall is triggered
**Then** a bottom sheet or full screen shows:
- Header: "Get More Scans"
- Three product cards (1, 3, 10 credits)
- "Restore Purchases" link
- Close/dismiss option

**And** the paywall uses brutalist styling
**And** products show prices from Play Store

**Technical Notes:**
- PaywallScreen in `feature/purchase` module
- ModalBottomSheet or full-screen dialog
- BillingClient queries for product details

**Prerequisites:** Epic 1 (design system), Story 8.4

---

### Story 8.4: Google Play Billing Integration

As a developer,
I want Play Billing Library integrated,
So that users can make in-app purchases.

**Acceptance Criteria:**

**Given** the `core/billing` module
**When** BillingClient is configured
**Then** it connects to Play Store on app start
**And** queries ProductDetails for 3 SKUs:
- `credits_1` - 1 credit pack
- `credits_3` - 3 credit pack
- `credits_10` - 10 credit pack

**And** handles connection lifecycle (connect/disconnect)
**And** retries on transient failures

**Technical Notes:**
- Play Billing Library 7.x (Architecture section)
- BillingClient.newBuilder() with listener
- ProductType.INAPP for consumables
- BillingRepository wraps client

**Prerequisites:** Epic 1 (module structure)

---

### Story 8.5: Product Cards with Pricing

As a user,
I want to see credit pack options with clear pricing,
So that I can choose the best value.

**Acceptance Criteria:**

**Given** the paywall is displayed (FR36)
**When** product cards are shown
**Then** each card displays:
- Credit amount (1, 3, or 10)
- Price from Play Store (localized)
- "Most Popular" badge on 3-pack (optional)

**And** cards use brutalist styling (ProductCard component)
**And** cards are tappable to initiate purchase

**Technical Notes:**
- ProductCard composable
- Price from ProductDetails.oneTimePurchaseOfferDetails
- Highlight middle option for psychological anchoring

**Prerequisites:** Story 8.4, Story 8.3

---

### Story 8.6: Purchase Flow Execution

As a user,
I want to complete a purchase through Google Play,
So that I receive credits after payment.

**Acceptance Criteria:**

**Given** the user taps a product card (FR37)
**When** initiating purchase
**Then** Google Play purchase sheet appears
**And** user completes payment flow
**And** on success, credits are added to balance
**And** paywall dismisses automatically
**And** success feedback shown (toast or animation)

**Technical Notes:**
- BillingClient.launchBillingFlow()
- Handle PurchasesUpdatedListener callback
- Consume purchase immediately (consumable)
- Add credits to DataStore

**Prerequisites:** Story 8.5

---

### Story 8.7: Purchase Verification & Credit Delivery

As a developer,
I want purchases verified before delivering credits,
So that fraudulent purchases don't grant credits.

**Acceptance Criteria:**

**Given** a purchase completes (FR41)
**When** verifying the purchase
**Then** purchase state is PURCHASED (not PENDING)
**And** purchase is acknowledged/consumed via BillingClient
**And** credits are delivered based on SKU:
- `credits_1` → +1 credit
- `credits_3` → +3 credits
- `credits_10` → +10 credits

**And** verification failure shows error message

**Technical Notes:**
- Check Purchase.purchaseState
- BillingClient.consumeAsync() for consumables
- Map SKU to credit amount
- Log purchase events for analytics

**Prerequisites:** Story 8.6

---

### Story 8.8: Restore Purchases

As a user,
I want to restore my previous purchases,
So that I don't lose credits if I reinstall the app.

**Acceptance Criteria:**

**Given** the paywall shows "Restore Purchases" (FR39)
**When** the user taps restore
**Then** pending/unconsumed purchases are queried
**And** any found purchases are verified and consumed
**And** credits are delivered for valid purchases
**And** "No purchases to restore" shown if none found

**Technical Notes:**
- BillingClient.queryPurchasesAsync()
- Process any PURCHASED but not consumed
- Note: Consumables can only be restored if not yet consumed

**Prerequisites:** Story 8.4

---

### Story 8.9: Pending Purchase Handling (Ask-to-Buy)

As a user with family sharing,
I want pending purchases handled gracefully,
So that Ask-to-Buy works correctly.

**Acceptance Criteria:**

**Given** a purchase requires approval (FR40)
**When** purchase state is PENDING
**Then** user sees "Purchase pending approval" message
**And** app checks for resolution on next launch
**And** credits delivered when purchase is approved

**Technical Notes:**
- Check Purchase.purchaseState == PENDING
- Store pending purchase reference
- Check on app resume/launch
- Handle PENDING → PURCHASED transition

**Prerequisites:** Story 8.6

---

### Story 8.10: Purchase Analytics Events

As a product owner,
I want to track purchase funnel metrics,
So that I can optimize monetization.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** purchase events occur
**Then** the following events are tracked:
- `paywall_displayed` - Paywall shown
- `purchase_started` - User tapped product
- `purchase_completed` - Successful purchase with SKU
- `purchase_failed` - With error reason
- `purchase_restored` - Restore attempted with result

**Technical Notes:**
- Events fired from PurchaseViewModel
- Include SKU, price, and credit amount properties
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 8 Complete: Credit System & Purchases**

**Stories Created:** 10
**FR Coverage:** FR34-41
**Architecture Sections Referenced:** feature/purchase, core/billing, core/datastore
**UX Patterns Incorporated:** Paywall bottom sheet, product cards, purchase flow

---

## Epic 9: Settings & Platform Polish

**Goal:** Complete the settings functionality and add Android platform enhancements. After this epic, users can customize theme/haptics, access support, and enjoy native Android features.

---

### Story 9.1: Theme Selection (Light/Dark/System)

As a user,
I want to choose my preferred app theme,
So that the app matches my preference or system setting.

**Acceptance Criteria:**

**Given** the user is in Settings (FR42)
**When** viewing Appearance section
**Then** theme options are shown:
- Light
- Dark
- System (default)

**And** selecting an option applies immediately
**And** preference persists across sessions
**And** "System" follows device dark mode setting

**Technical Notes:**
- Segmented control or radio group
- DataStore `themeMode` preference
- RulebookTheme reads preference
- Use `isSystemInDarkTheme()` for System mode

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (DataStore, Theme)

---

### Story 9.2: Haptic Feedback Toggle

As a user,
I want to enable or disable haptic feedback,
So that I can control the app's vibration behavior.

**Acceptance Criteria:**

**Given** the user is in Settings (FR43)
**When** viewing Feedback section
**Then** a toggle shows "Haptic Feedback" with current state
**And** toggling updates immediately
**And** preference persists across sessions
**And** all haptics in app respect this setting

**Technical Notes:**
- Switch composable with brutalist styling
- DataStore `hapticsEnabled` preference
- Check preference before any VibrationEffect

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (DataStore)

---

### Story 9.3: Support Links Section

As a user,
I want to access support resources,
So that I can get help or report issues.

**Acceptance Criteria:**

**Given** the user is in Settings (FR44)
**When** viewing Support section
**Then** links are shown:
- "Contact Support" → Opens email composer
- "Report a Bug" → Opens email or form
- "Rate on Play Store" → Opens Play Store listing

**And** links open in external apps/browser
**And** email includes app version in subject

**Technical Notes:**
- Intent.ACTION_SENDTO for email
- Intent.ACTION_VIEW for Play Store
- Include version: "Rulebook v${BuildConfig.VERSION_NAME}"

**Prerequisites:** Epic 2 (Settings shell)

---

### Story 9.4: About Section (Version & Legal)

As a user,
I want to see app version and legal information,
So that I can verify my app version and access policies.

**Acceptance Criteria:**

**Given** the user is in Settings (FR45)
**When** viewing About section
**Then** it shows:
- App version: "Version X.Y.Z (build N)"
- "Privacy Policy" link → Opens URL
- "Terms of Service" link → Opens URL

**And** version is dynamically read from BuildConfig

**Technical Notes:**
- `BuildConfig.VERSION_NAME` and `VERSION_CODE`
- Intent.ACTION_VIEW for policy URLs
- WebView or external browser

**Prerequisites:** Epic 2 (Settings shell)

---

### Story 9.5: Clear App Data with Confirmation

As a user,
I want to reset the app to a fresh state,
So that I can start over if needed.

**Acceptance Criteria:**

**Given** the user is in Settings (FR46)
**When** tapping "Clear All Data"
**Then** a confirmation dialog appears (FR47)
**And** dialog warns: "This will delete all saved games and reset settings"
**And** "Clear Data" (destructive) and "Cancel" buttons

**When** user confirms
**Then** all Room data is deleted
**And** DataStore preferences are reset (except credits? TBD)
**And** App returns to onboarding or library
**And** success toast confirms

**Technical Notes:**
- RulebookDatabase.clearAllTables()
- DataStore clear or reset to defaults
- Decide on credit handling (preserve or reset)
- Navigate to appropriate screen

**Prerequisites:** Epic 2 (Settings shell), Epic 1 (Room, DataStore)

---

### Story 9.6: App Shortcuts for Quick Scan

As a user,
I want to long-press the app icon for quick actions,
So that I can start scanning faster.

**Acceptance Criteria:**

**Given** the app is installed (FR48)
**When** the user long-presses the launcher icon
**Then** a shortcut menu appears with:
- "Scan Game" → Opens camera directly

**And** shortcut works even if app is not running
**And** shortcut icon matches app branding

**Technical Notes:**
- Static shortcut in `res/xml/shortcuts.xml`
- ShortcutManager for dynamic shortcuts (optional)
- Deep link to camera route
- Handle credit check on shortcut launch

**Prerequisites:** Epic 4 (Camera screen)

---

### Story 9.7: Predictive Back Gesture Refinement

As a user,
I want back gestures to feel native and predictable,
So that navigation matches my Android expectations.

**Acceptance Criteria:**

**Given** predictive back is enabled (FR49)
**When** on any screen with back navigation
**Then** back gesture preview shows previous screen
**And** all screens properly handle back:
- Camera → Library
- Rules → Library
- Onboarding → Exit app (or nothing)
- Paywall → Previous screen

**And** no screens trap the user

**Technical Notes:**
- Verify BackHandler usage across all screens
- Test gesture on Android 14+ devices
- Ensure proper NavController back stack

**Prerequisites:** Epic 2 (Story 2.8)

---

### Story 9.8: Edge-to-Edge Polish

As a user,
I want the app to look polished edge-to-edge,
So that the interface feels modern and immersive.

**Acceptance Criteria:**

**Given** edge-to-edge is enabled (FR50)
**When** viewing any screen
**Then** content extends properly under system bars
**And** no content is obscured by status/nav bars
**And** status bar icons contrast properly on all screens
**And** navigation bar blends with bottom nav

**Technical Notes:**
- Review all screens for WindowInsets handling
- Ensure header bars have status bar padding
- Ensure bottom nav has nav bar padding
- Test on devices with different bar heights

**Prerequisites:** Epic 2 (Story 2.1)

---

### Story 9.9: Settings Analytics Events

As a product owner,
I want to track settings changes,
So that I can understand user preferences.

**Acceptance Criteria:**

**Given** TelemetryDeck is configured
**When** settings events occur
**Then** the following events are tracked:
- `settings_theme_changed` - With new theme value
- `settings_haptics_changed` - With enabled/disabled
- `settings_data_cleared` - User cleared data
- `settings_support_tapped` - Which link tapped

**Technical Notes:**
- Events fired from SettingsViewModel
- Include relevant property values
- Match iOS event names

**Prerequisites:** Epic 1 (Analytics)

---

**Epic 9 Complete: Settings & Platform Polish**

**Stories Created:** 9
**FR Coverage:** FR42-50
**Architecture Sections Referenced:** feature/settings, core/datastore, App Shortcuts
**UX Patterns Incorporated:** Theme switching, toggles, confirmation dialogs, deep links

---

## Final Validation

### Epic Summary

| Epic | Title | Stories | Primary FRs |
|------|-------|---------|-------------|
| 1 | Foundation & Design System | 13 | Infrastructure |
| 2 | App Shell & Navigation | 8 | FR27, FR33, FR49, FR50 |
| 3 | Onboarding Experience | 7 | FR1-4, FR34 |
| 4 | Photo Capture Flow | 10 | FR5-11, FR51-52 |
| 5 | Game Recognition & Rules Generation | 10 | FR12-18, FR26, FR35 |
| 6 | Rules Display & Reference | 9 | FR19-25, FR32 |
| 7 | Library Management | 6 | FR27-31 |
| 8 | Credit System & Purchases | 10 | FR34-41 |
| 9 | Settings & Platform Polish | 9 | FR42-50 |
| **Total** | | **82 stories** | **52 FRs** |

---

## FR Coverage Matrix

### Onboarding (FR1-4)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR1 | 2-screen introduction | Epic 3 | 3.2, 3.3 |
| FR2 | 3 free credits on completion | Epic 3 | 3.4 |
| FR3 | Skip onboarding | Epic 3 | 3.5 |
| FR4 | Remember completion | Epic 3 | 3.1, 3.4 |

### Photo Capture (FR5-11)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR5 | Capture photos with camera | Epic 4 | 4.1, 4.2 |
| FR6 | Control flash/torch | Epic 4 | 4.3 |
| FR7 | Zoom camera (pinch) | Epic 4 | 4.4 |
| FR8 | Tap to focus | Epic 4 | 4.5 |
| FR9 | Select from gallery | Epic 4 | 4.6 |
| FR10 | Compress images | Epic 4 | 4.7 |
| FR11 | Show credit balance | Epic 4 | 4.8 |

### Game Recognition (FR12-17)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR12 | AI game identification | Epic 5 | 5.3 |
| FR13 | Display confidence | Epic 5 | 5.4 |
| FR14 | Confirm/reject identification | Epic 5 | 5.4 |
| FR15 | Manual name entry | Epic 5 | 5.5 |
| FR16 | Fallback AI model | Epic 5 | 5.8 |
| FR17 | Retry failed recognition | Epic 5 | 5.9 |

### Rules Generation & Display (FR18-26)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR18 | Generate structured rules | Epic 5 | 5.6 |
| FR19 | View game overview | Epic 6 | 6.1, 6.2 |
| FR20 | View setup instructions | Epic 6 | 6.3 |
| FR21 | Setup checklist | Epic 6 | 6.3 |
| FR22 | View first-round guide | Epic 6 | 6.4 |
| FR23 | View advanced rules | Epic 6 | 6.5 |
| FR24 | Expand/collapse sections | Epic 6 | 6.6 |
| FR25 | Share rules | Epic 6 | 6.7 |
| FR26 | Progress during generation | Epic 5 | 5.2 |

### Library Management (FR27-33)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR27 | Grid layout view | Epic 2, 7 | 2.5, 7.1 |
| FR28 | Sort options | Epic 7 | 7.2 |
| FR29 | Select game to view | Epic 7 | 7.3 |
| FR30 | Delete games | Epic 7 | 7.4 |
| FR31 | Delete confirmation | Epic 7 | 7.4 |
| FR32 | Offline access | Epic 6 | 6.8 |
| FR33 | Empty state | Epic 2 | 2.5 |

### Credit System (FR34-41)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR34 | Track credits | Epic 3, 8 | 3.4, 8.1 |
| FR35 | Consume credit per scan | Epic 5, 8 | 5.7, 8.2 |
| FR36 | View credit packs | Epic 8 | 8.5 |
| FR37 | Purchase credits | Epic 8 | 8.6 |
| FR38 | Paywall when no credits | Epic 8 | 8.3 |
| FR39 | Restore purchases | Epic 8 | 8.8 |
| FR40 | Handle pending purchases | Epic 8 | 8.9 |
| FR41 | Purchase confirmation | Epic 8 | 8.7 |

### Settings (FR42-47)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR42 | Theme selection | Epic 9 | 9.1 |
| FR43 | Haptic toggle | Epic 9 | 9.2 |
| FR44 | Support links | Epic 9 | 9.3 |
| FR45 | Version & legal | Epic 9 | 9.4 |
| FR46 | Clear data | Epic 9 | 9.5 |
| FR47 | Clear confirmation | Epic 9 | 9.5 |

### Platform Integration (FR48-52)

| FR | Description | Epic | Stories |
|----|-------------|------|---------|
| FR48 | App shortcuts | Epic 9 | 9.6 |
| FR49 | Predictive back | Epic 2, 9 | 2.8, 9.7 |
| FR50 | Edge-to-edge | Epic 2, 9 | 2.1, 9.8 |
| FR51 | Permission at use | Epic 4 | 4.9 |
| FR52 | Navigate to settings | Epic 4 | 4.9 |

---

## Quality Validation

### Architecture Integration

| Module | Epic Coverage | Stories |
|--------|---------------|---------|
| `app/` | Epic 1, 2 | 1.1, 1.2, 2.1-2.8 |
| `core/designsystem` | Epic 1 | 1.7, 1.8, 1.9, 1.10, 1.11 |
| `core/data` | Epic 1 | 1.12, 1.13 |
| `core/database` | Epic 1 | 1.3 |
| `core/network` | Epic 1 | 1.5 |
| `core/model` | Epic 1 | 1.12 |
| `core/common` | Epic 1 | 1.13 |
| `core/analytics` | Epic 1 | 1.6 |
| `core/billing` | Epic 8 | 8.4 |
| `core/datastore` | Epic 1 | 1.4 |
| `feature/onboarding` | Epic 3 | 3.1-3.7 |
| `feature/camera` | Epic 4 | 4.1-4.10 |
| `feature/rules` | Epic 5, 6 | 5.1-5.10, 6.1-6.9 |
| `feature/library` | Epic 2, 7 | 2.5, 7.1-7.6 |
| `feature/settings` | Epic 2, 9 | 2.6, 9.1-9.9 |
| `feature/purchase` | Epic 8 | 8.3-8.10 |

### UX Integration

| UX Pattern | Stories Implementing |
|------------|---------------------|
| Brutalist design tokens | 1.7, 1.8 |
| RulebookButton | 1.9 |
| RulebookCard | 1.10 |
| RulebookHeaderBar | 1.11 |
| CollapsibleSection | 6.2-6.6 |
| GameCard | 7.1 |
| ProductCard | 8.5 |
| ProgressPhaseIndicator | 5.2 |
| Empty states | 2.5 |
| Confirmation dialogs | 7.4, 9.5 |
| Bottom navigation + FAB | 2.3, 2.4 |
| Edge-to-edge display | 2.1, 9.8 |
| Haptic feedback | 4.2, 9.2 |

### Story Quality Checklist

- [x] All 52 FRs covered by at least one story
- [x] All stories have clear acceptance criteria in BDD format
- [x] All stories reference technical implementation from Architecture
- [x] All stories are sized for single dev agent completion
- [x] No forward dependencies (stories only depend on previous)
- [x] Epic sequence delivers incremental user value
- [x] Foundation epic properly enables subsequent work
- [x] Analytics events tracked across all major flows

---

## Summary

**Epic Breakdown Complete**

| Metric | Value |
|--------|-------|
| Total Epics | 9 |
| Total Stories | 82 |
| FRs Covered | 52/52 (100%) |
| Architecture Modules | 16/16 (100%) |
| Feature Modules | 6/6 (100%) |
| Core Modules | 9/9 (100%) |

**Implementation Path:**
1. Epic 1: Foundation → All infrastructure in place
2. Epic 2: Shell → App launches and navigates
3. Epic 3: Onboarding → New users get credits
4. Epic 4: Camera → Users can capture photos
5. Epic 5: Recognition → Photos become rules
6. Epic 6: Display → Users read rules
7. Epic 7: Library → Users manage collection
8. Epic 8: Purchases → Users buy credits
9. Epic 9: Polish → App feels native and complete

**Ready for:** Sprint Planning and Development Implementation

---

*For implementation: Use the `create-story` workflow to generate individual story implementation plans from this epic breakdown.*

---

