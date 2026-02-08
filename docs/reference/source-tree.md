---
title: Source Tree Analysis
description: Annotated directory structure and module inventory
author: Ivo
date: 2026-01-22
---

# Source Tree Analysis

Annotated directory structure for the Rulebook Android application.

**Architecture:** Multi-Module Clean Architecture (MVVM)

---

## Project Root Structure

```
project-rulebook-android/
├── app/                           # [ENTRY POINT] Main application module
├── core/                          # Shared core modules (9 modules)
│   ├── analytics/                 # Analytics service (TelemetryDeck)
│   ├── billing/                   # Google Play Billing integration
│   ├── common/                    # Common utilities (Result<T>, etc.)
│   ├── data/                      # Repository implementations
│   ├── database/                  # Room database layer
│   ├── datastore/                 # DataStore preferences
│   ├── designsystem/              # Design system components
│   ├── model/                     # Domain models (pure Kotlin)
│   └── network/                   # Retrofit API client
├── feature/                       # Feature modules (7 modules)
│   ├── camera/                    # Photo capture feature
│   ├── generation/                # Scan pipeline & rules generation
│   ├── library/                   # Game library feature
│   ├── onboarding/                # Onboarding flow
│   ├── purchase/                  # In-app purchases
│   ├── rules/                     # Rules display
│   └── settings/                  # Settings screen
├── build-logic/                   # Convention plugins
│   └── convention/                # Gradle convention plugin DSL
├── gradle/                        # Gradle wrapper & version catalog
│   └── libs.versions.toml         # Dependency versions
├── docs/                          # Project documentation
├── build.gradle.kts               # Root build configuration
├── settings.gradle.kts            # Module inclusion
└── gradle.properties              # Build properties
```

---

## App Module (Entry Point)

```
app/
├── build.gradle.kts               # App build config
├── proguard-rules.pro             # ProGuard rules
└── src/main/
    ├── AndroidManifest.xml        # [ENTRY POINT] App manifest
    ├── kotlin/com/rulebook/
    │   ├── RulebookApplication.kt # [ENTRY POINT] Application class
    │   ├── di/
    │   │   └── AppModule.kt       # Root Koin module aggregation
    │   ├── navigation/
    │   │   ├── NavigationDestination.kt    # Route definitions
    │   │   ├── RulebookNavHost.kt          # Navigation graph
    │   │   ├── RulebookBottomBar.kt        # Bottom navigation
    │   │   ├── BottomBarDestination.kt     # Tab destinations
    │   │   ├── DeepLinkConfig.kt           # Deep link handling
    │   │   ├── NavControllerExtensions.kt  # Nav utilities
    │   │   └── PlaceholderScreens.kt       # Temp placeholders
    │   └── startup/
    │       ├── StartupScreen.kt            # Splash/routing screen
    │       └── StartupViewModel.kt         # Onboarding routing logic
    └── res/
        ├── values/strings.xml     # App strings
        ├── drawable/              # App icons/graphics
        └── mipmap-*/              # Launcher icons
```

**Key Entry Points:**
- `RulebookApplication.kt` - Koin initialization, crash reporting setup
- `AndroidManifest.xml` - Permissions, activities, deep links
- `StartupScreen.kt` - Determines initial destination (onboarding vs library)

---

## Core Modules

### core/analytics
```
core/analytics/src/main/kotlin/com/rulebook/core/analytics/
├── di/
│   └── AnalyticsModule.kt         # DI configuration
├── TelemetryDeckAnalyticsManager.kt  # TelemetryDeck implementation
└── AnalyticsManager.kt            # Interface (if exists)
```
**Purpose:** Event tracking for user behavior analytics via TelemetryDeck SDK.

### core/billing
```
core/billing/src/main/kotlin/com/rulebook/core/billing/
└── BillingService.kt              # Google Play Billing wrapper
```
**Purpose:** In-app purchase handling for credits/subscriptions.

### core/common
```
core/common/src/main/kotlin/com/rulebook/core/common/
├── di/
│   └── CommonModule.kt            # Common DI bindings
└── Result.kt                      # [CRITICAL] Result<T> sealed class
```
**Purpose:** Shared utilities including functional error handling pattern.

### core/data
```
core/data/src/main/kotlin/com/rulebook/core/data/
├── di/
│   └── DataModule.kt              # Repository DI bindings
└── repository/
    ├── GameRepository.kt          # Game CRUD interface
    ├── GameRepositoryImpl.kt      # Room-backed implementation
    ├── RulesRepository.kt         # Rules interface
    ├── RulesRepositoryImpl.kt     # Rules implementation
    ├── CreditRepository.kt        # Credit balance interface
    └── CreditRepositoryImpl.kt    # DataStore-backed credits
```
**Purpose:** Repository pattern implementations bridging data sources to domain.

### core/database
```
core/database/src/main/kotlin/com/rulebook/core/database/
├── di/
│   └── DatabaseModule.kt          # Room DI configuration
├── entity/
│   ├── GameEntity.kt              # [TABLE] saved_games
│   └── RulesEntity.kt             # [TABLE] rules (FK → games)
├── mapper/
│   ├── GameMapper.kt              # Entity ↔ Domain mapping
│   └── RulesMapper.kt             # Rules with JSON parsing
├── GameDao.kt                     # [DAO] Game queries
├── RulesDao.kt                    # [DAO] Rules queries
└── RulebookDatabase.kt            # [DATABASE] Room config
```
**Purpose:** SQLite persistence via Room with reactive Flow queries.

### core/datastore
```
core/datastore/src/main/kotlin/com/rulebook/core/datastore/
├── di/
│   └── DataStoreModule.kt         # DataStore DI
├── RulebookPreferences.kt         # Preferences interface
├── RulebookPreferencesImpl.kt     # DataStore implementation
└── ThemeMode.kt                   # Theme enum
```
**Purpose:** Key-value preferences storage (onboarding complete, theme, credits).

### core/designsystem
```
core/designsystem/src/main/kotlin/com/rulebook/core/designsystem/
├── theme/
│   ├── RulebookTheme.kt           # [THEME] CompositionLocal provider
│   ├── RulebookColors.kt          # Color tokens (light/dark)
│   ├── RulebookTypography.kt      # Typography scale
│   ├── RulebookSpacing.kt         # Spacing tokens
│   └── RulebookShapes.kt          # Shape tokens (0dp radius)
├── component/
│   ├── RulebookButton.kt          # [COMPONENT] Primary/Secondary/Destructive
│   ├── RulebookCard.kt            # [COMPONENT] Content container
│   ├── RulebookFAB.kt             # [COMPONENT] Floating action button
│   ├── RulebookHeaderBar.kt       # [COMPONENT] Screen header
│   ├── RulebookTextField.kt       # [COMPONENT] Brutalist text input
│   └── CreditsDisplay.kt          # [COMPONENT] Credit balance pill
└── modifier/
    └── BrutalistModifiers.kt      # Shadow, border modifiers
```
**Purpose:** Brutalist design system with custom components on Material 3.

### core/model
```
core/model/src/main/kotlin/com/rulebook/core/model/
├── Game.kt                        # [DOMAIN] Game entity
├── Rules.kt                       # [DOMAIN] Complete rules
├── RuleSection.kt                 # [DOMAIN] Single rule section
└── ScanResult.kt                  # [DOMAIN] Image scan result
```
**Purpose:** Pure Kotlin domain models (no Android dependencies).

### core/network
```
core/network/src/main/kotlin/com/rulebook/core/network/
├── di/
│   └── NetworkModule.kt           # Retrofit DI
├── api/
│   └── RulebookApi.kt             # [API] Retrofit interface (2 endpoints)
├── model/
│   ├── AnalyzeRequest.kt          # POST /analyze request
│   ├── AnalyzeResponse.kt         # POST /analyze response
│   ├── GenerateRequest.kt         # POST /generate request
│   └── GenerateResponse.kt        # POST /generate response
├── mapper/
│   └── ScanResultMapper.kt        # Response → Domain mapping
└── RulebookApiClient.kt           # OkHttp/Retrofit factory
```
**Purpose:** REST API client with staging/production base URL variants.

---

## Feature Modules

### feature/camera
```
feature/camera/src/main/kotlin/com/rulebook/feature/camera/
├── di/
│   └── CameraModule.kt            # ViewModel DI
├── components/
│   ├── CaptureButton.kt           # Shutter button
│   ├── FlashToggle.kt             # Flash mode toggle
│   ├── ZoomIndicator.kt           # Zoom level overlay
│   ├── FocusIndicator.kt          # Tap-to-focus indicator
│   ├── GalleryButton.kt           # Photo picker
│   ├── CloseButton.kt             # Exit camera
│   ├── PermissionRationale.kt     # Permission explanation
│   └── PermissionDenied.kt        # Denied state
├── util/
│   └── ImageCompressor.kt         # Base64 compression
├── navigation/
│   └── CameraNavigation.kt        # NavGraph integration
├── CameraScreen.kt                # [SCREEN] Main camera UI
├── CameraViewModel.kt             # [VIEWMODEL] Camera state
└── CameraUiState.kt               # [STATE] UI state model
```
**Purpose:** CameraX photo capture with permissions, zoom, focus, flash.

### feature/library
```
feature/library/src/main/kotlin/com/rulebook/feature/library/
├── di/
│   └── LibraryModule.kt           # ViewModel DI
├── components/
│   └── LibraryEmptyState.kt       # Empty state card
├── LibraryScreen.kt               # [SCREEN] Game list
├── LibraryViewModel.kt            # [VIEWMODEL] List state
└── LibraryUiState.kt              # [STATE] Loading/error/games
```
**Purpose:** Saved games list with pull-to-refresh and empty state CTA.

### feature/onboarding
```
feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/
├── di/
│   └── OnboardingModule.kt        # ViewModel DI
├── components/
│   ├── OnboardingPageIndicator.kt # Page dots
│   ├── OnboardingPage1Content.kt  # Value proposition
│   ├── OnboardingPage2Content.kt  # Getting started
│   └── OnboardingBottomSection.kt # Navigation buttons
├── OnboardingScreen.kt            # [SCREEN] Pager flow
├── OnboardingViewModel.kt         # [VIEWMODEL] Page navigation
└── OnboardingNavigationEvent.kt   # Completion event
```
**Purpose:** 2-page onboarding with analytics and credit award on completion.

### feature/purchase
```
feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/
├── di/
│   └── PurchaseModule.kt          # ViewModel DI
└── PurchaseScreen.kt              # [SCREEN] Credit purchase
```
**Purpose:** In-app purchase flow for buying credits.

### feature/rules
```
feature/rules/src/main/kotlin/com/rulebook/feature/rules/
├── di/
│   └── RulesModule.kt             # ViewModel DI
└── RulesScreen.kt                 # [SCREEN] Rules display
```
**Purpose:** Display generated rules for a selected game.

### feature/settings
```
feature/settings/src/main/kotlin/com/rulebook/feature/settings/
├── di/
│   └── SettingsModule.kt          # ViewModel DI
├── components/
│   ├── SettingsToggleRow.kt       # Switch setting row
│   ├── SettingsLinkRow.kt         # Tappable link row
│   ├── SettingsSectionHeader.kt   # Section title
│   └── SettingsInfoRow.kt         # Read-only info row
├── SettingsScreen.kt              # [SCREEN] Settings list
├── SettingsViewModel.kt           # [VIEWMODEL] Settings state
└── SettingsUiState.kt             # [STATE] Theme/haptics
```
**Purpose:** App settings (theme, haptics, support links, data management).

---

## Module Dependency Graph

```
                    ┌─────────────────────────────────────┐
                    │               app                    │
                    │  (RulebookApplication, Navigation)   │
                    └─────────────────────────────────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           │                        │                        │
           ▼                        ▼                        ▼
    ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
    │  feature:   │          │  feature:   │          │  feature:   │
    │   camera    │          │   library   │          │  settings   │
    └─────────────┘          └─────────────┘          └─────────────┘
           │                        │                        │
           └────────────────────────┼────────────────────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────────────┐
                    │            core:data                 │
                    │       (Repository Pattern)           │
                    └─────────────────────────────────────┘
                                    │
           ┌────────────────────────┼────────────────────────┐
           │                        │                        │
           ▼                        ▼                        ▼
    ┌─────────────┐          ┌─────────────┐          ┌─────────────┐
    │   core:     │          │   core:     │          │   core:     │
    │  database   │          │  network    │          │  datastore  │
    └─────────────┘          └─────────────┘          └─────────────┘
           │                        │                        │
           └────────────────────────┼────────────────────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────────────┐
                    │            core:model                │
                    │       (Domain Models)                │
                    └─────────────────────────────────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────────────┐
                    │           core:common                │
                    │     (Result<T>, Utilities)           │
                    └─────────────────────────────────────┘
```

---

## Key File Locations Summary

| Category | Key File | Location |
|----------|----------|----------|
| **Entry Point** | Application | `app/.../RulebookApplication.kt` |
| **Entry Point** | Manifest | `app/src/main/AndroidManifest.xml` |
| **Navigation** | Nav Graph | `app/.../navigation/RulebookNavHost.kt` |
| **Database** | Room Config | `core/database/.../RulebookDatabase.kt` |
| **API** | Endpoints | `core/network/.../api/RulebookApi.kt` |
| **Theme** | Design System | `core/designsystem/.../theme/RulebookTheme.kt` |
| **State** | Result Type | `core/common/.../Result.kt` |
| **Build** | Version Catalog | `gradle/libs.versions.toml` |

---

## Annotations Legend

- `[ENTRY POINT]` - Application startup location
- `[SCREEN]` - User-facing Composable screen
- `[VIEWMODEL]` - UI state holder
- `[STATE]` - UiState data class
- `[TABLE]` - Room entity (database table)
- `[DAO]` - Data Access Object
- `[DATABASE]` - Room database class
- `[API]` - Retrofit service interface
- `[DOMAIN]` - Pure Kotlin model
- `[COMPONENT]` - Reusable UI component
- `[THEME]` - Design token provider
- `[CRITICAL]` - Architecture-critical file
