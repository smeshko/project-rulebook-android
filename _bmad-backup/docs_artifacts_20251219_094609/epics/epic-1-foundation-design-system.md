# Epic 1: Foundation & Design System

**Goal:** Establish the technical foundation and design system that enables all subsequent feature development. After this epic, the project has a working multi-module structure, core infrastructure, and brutalist component library.

---

## Story 1.1: Project Initialization & Multi-Module Structure

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

## Story 1.2: Dependency Injection Setup with Koin

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

## Story 1.3: Room Database Schema & DAOs

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

## Story 1.4: DataStore Preferences Setup

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

## Story 1.5: Network Client Configuration

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

## Story 1.6: Analytics Integration with TelemetryDeck

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

## Story 1.7: Design Tokens & Theme Foundation

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

## Story 1.8: Brutalist Modifier Extensions

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

## Story 1.9: Core UI Components - RulebookButton

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

## Story 1.10: Core UI Components - RulebookCard

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

## Story 1.11: Core UI Components - RulebookHeaderBar

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

## Story 1.12: Domain Models

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

## Story 1.13: Result Wrapper & Error Handling

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
