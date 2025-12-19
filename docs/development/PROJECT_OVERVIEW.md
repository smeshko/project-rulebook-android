# Rulebook Android - Project Overview

**Version:** 1.0.0
**Last Updated:** 2025-12-19
**Target SDK:** Android 15 (API 35)
**Minimum SDK:** Android 14 (API 34)

---

## Executive Summary

**Rulebook** is a native Android application that transforms the board game learning experience. Using AI-powered image recognition, users can photograph any game box and receive digestible, progressive rules within 60 seconds. This Android version is a **1:1 feature port** from the iOS application, adapted with native Android UX patterns using Jetpack Compose and Material Design 3.

### Core Value Proposition

**"From box to playing in 60 seconds"** - Rulebook solves the fundamental problem of "I don't even know what game this is or where to start" through photo-to-rules pipeline, multi-model AI resilience, and progressive disclosure format.

---

## Project Classification

- **Type:** Mobile Application (Android Native)
- **Architecture:** Multi-Module Clean Architecture with MVI
- **Language:** 100% Kotlin
- **UI Framework:** Jetpack Compose (Declarative UI)
- **Dependency Injection:** Koin 4.0
- **Minimum SDK:** API 34 (Android 14)
- **Target SDK:** API 35 (Android 15)

---

## Technical Architecture

### Architecture Pattern: Clean Architecture + MVI

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose)              │
│  - Screens, Components, Navigation      │
└──────────────┬──────────────────────────┘
               │ observes StateFlow
┌──────────────▼──────────────────────────┐
│       ViewModel Layer (MVI)             │
│  - UiState (immutable)                  │
│  - Intent handling                      │
│  - Business logic orchestration         │
└──────────────┬──────────────────────────┘
               │ calls repositories
┌──────────────▼──────────────────────────┐
│       Repository Layer (Interfaces)     │
│  - Data abstraction                     │
│  - Error handling with Result<T>        │
└──────────────┬──────────────────────────┘
               │ implements
┌──────────────▼──────────────────────────┐
│         Data Sources                    │
│  - Room (local persistence)             │
│  - Retrofit (network API)               │
│  - DataStore (preferences)              │
└─────────────────────────────────────────┘
```

### Module Structure (16 Modules)

#### App Module
- **Purpose:** Application entry point and navigation orchestration
- **Path:** `/app`
- **Key Files:**
  - `MainActivity.kt` - Single-activity architecture with edge-to-edge display
  - `RulebookApplication.kt` - DI initialization and app lifecycle
  - `RulebookNavHost.kt` - Navigation graph and routing

#### Feature Modules (6)
Feature modules encapsulate complete user-facing features with their own UI, ViewModels, and navigation:

1. **feature:camera** - Photo capture functionality
   - CameraX integration with lifecycle awareness
   - Flash control, zoom gestures, tap-to-focus
   - Gallery picker fallback
   - Credit balance display

2. **feature:library** - Game library management
   - Grid layout with sort options
   - Offline-first access
   - Delete with confirmation
   - Empty state handling

3. **feature:onboarding** - First-time user experience
   - 2-screen introduction flow
   - 3 free credits award
   - Persistent completion tracking
   - Skip capability

4. **feature:purchase** - In-app purchases and credit system
   - Google Play Billing integration
   - Credit pack offerings (1, 3, 10 credits)
   - Purchase restoration
   - Paywall when credits depleted

5. **feature:rules** - Rules generation and display
   - Progressive disclosure format (Overview → Setup → First Round → Advanced)
   - Expandable sections
   - Share functionality
   - Offline access

6. **feature:settings** - User preferences
   - Theme selection (Light/Dark/System)
   - Haptic feedback toggle
   - Support links
   - App version info
   - Clear data functionality

#### Core Modules (9)
Core modules provide shared infrastructure and are dependency-free of feature modules:

1. **core:analytics** - TelemetryDeck analytics integration
2. **core:billing** - Google Play Billing wrapper
3. **core:common** - Shared utilities and extensions
4. **core:data** - Repository pattern implementations
5. **core:database** - Room database with 2 entities (GameEntity, RulesEntity)
6. **core:datastore** - DataStore for preferences and settings
7. **core:designsystem** - Brutalist design system components
8. **core:model** - Domain models (pure Kotlin, no Android dependencies)
9. **core:network** - Retrofit API client with kotlinx.serialization

#### Build Configuration Module
- **build-logic/convention** - Custom Gradle convention plugins for consistent configuration across modules

---

## Technology Stack

### Core Technologies
| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| Language | Kotlin | 2.0.21 | Primary development language |
| Build System | Android Gradle Plugin | 8.7.2 | Build automation |
| UI Framework | Jetpack Compose | 2024.11.00 (BOM) | Declarative UI |
| Navigation | Compose Navigation | 2.8.4 | In-app navigation |
| DI | Koin | 4.0.0 | Dependency injection |
| Architecture | AndroidX Lifecycle | 2.8.7 | ViewModel, StateFlow |

### Data & Persistence
| Library | Version | Purpose |
|---------|---------|---------|
| Room | 2.6.1 | Local database (SQLite wrapper) |
| DataStore | 1.1.1 | Preferences storage |
| kotlinx.serialization | 1.7.3 | JSON serialization |

### Network & API
| Library | Version | Purpose |
|---------|---------|---------|
| Retrofit | 2.11.0 | HTTP client |
| OkHttp | 4.12.0 | Network layer |
| Coil | 3.0.3 | Image loading (Compose-native) |

### Camera & Media
| Library | Version | Purpose |
|---------|---------|---------|
| CameraX | 1.4.1 | Camera API abstraction |
| Accompanist Permissions | 0.36.0 | Permission handling |

### Monetization & Analytics
| Library | Version | Purpose |
|---------|---------|---------|
| Google Play Billing | 7.1.1 | In-app purchases |
| TelemetryDeck | 6.3.0 | Privacy-focused analytics |

### Testing
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit 4 | 4.13.2 | Unit testing |
| AndroidX Test | 1.2.1 | Instrumented testing |
| Espresso | 3.6.1 | UI testing |
| Coroutines Test | 1.9.0 | Async testing |

---

## Key Features (MVP Scope)

### 1. Onboarding Flow
- **Flow:** 2-screen introduction with skip capability
- **Incentive:** 3 free credits awarded on completion
- **Persistence:** Completion status tracked in DataStore
- **Implementation:** `feature:onboarding` module

### 2. Photo Capture System
- **Capture:** CameraX with lifecycle-aware camera management
- **Controls:** Flash (Off/On/Auto), zoom gestures, tap-to-focus
- **Fallback:** Gallery picker for existing photos
- **UX:** Real-time credit balance display
- **Implementation:** `feature:camera` module

### 3. AI Game Recognition
- **Process:** Photo upload → AI identification → Confidence check
- **Fallback:** Multi-model cascade for obscure games
- **Manual Entry:** User can provide game name if AI fails
- **Network:** Retrofit-based API communication
- **Implementation:** `feature:camera` + `core:network`

### 4. Rules Generation & Display
- **Format:** Progressive disclosure (Overview → Setup → First Round → Advanced)
- **Setup:** Interactive checklist format
- **UX:** Expandable sections, share functionality
- **Offline:** Full offline access via Room database
- **Implementation:** `feature:rules` + `core:database`

### 5. Game Library
- **Layout:** Grid display with game thumbnails
- **Sorting:** Recent, alphabetical, date added
- **Management:** Delete with confirmation dialog
- **Offline:** Complete offline functionality
- **Implementation:** `feature:library` + `core:database`

### 6. Credit System & Monetization
- **Economy:** 1 credit per scan
- **Packages:** 1, 3, 10 credit packs
- **Billing:** Google Play Billing integration
- **Recovery:** Purchase restoration
- **Paywall:** Presented when credits depleted
- **Implementation:** `feature:purchase` + `core:billing`

### 7. Settings & Preferences
- **Theme:** Light/Dark/System
- **Haptics:** Toggle haptic feedback
- **Support:** Privacy policy, terms of service links
- **Data:** Clear all data functionality
- **Implementation:** `feature:settings` + `core:datastore`

### 8. Android Platform Integration
- **App Shortcuts:** Long-press launcher icon → "Scan Game"
- **Predictive Back:** Gesture-based navigation support
- **Edge-to-Edge:** Full-screen immersive display
- **Splash Screen:** Android 12+ splash screen API
- **Deep Links:** Intent filter configuration

---

## Design System: "Brutalist" Aesthetic

### Visual Identity
- **Style:** Brutalist design language
- **Characteristics:**
  - Thick borders (2-4dp)
  - Bold shadows for depth
  - Zero corner radius (sharp edges)
  - High contrast
- **Typography:** Custom brutalist text styles
- **Colors:** Extended palette with orange accent (#FF6B35)
- **Spacing:** Consistent 4dp-based spacing scale

### Theme Architecture (3 Layers)

```
┌─────────────────────────────────────────┐
│   Layer 3: Rulebook Components         │
│   (Custom brutalist implementations)   │
├─────────────────────────────────────────┤
│   Layer 2: Rulebook Theme               │
│   (Custom tokens from iOS design)       │
├─────────────────────────────────────────┤
│   Layer 1: Material 3 Foundation        │
│   (Accessibility & Android conventions) │
└─────────────────────────────────────────┘
```

### Custom Components
- `RulebookButton` - Thick borders, bold shadow
- `RulebookCard` - Elevated cards with sharp edges
- `RulebookFAB` - Floating action button with brutalist style
- `RulebookHeaderBar` - Custom top app bar
- `CreditsDisplay` - Credit balance indicator
- `BrutalistModifiers` - Reusable modifier extensions

**Implementation:** `core:designsystem` module

---

## Data Architecture

### Database Schema (Room)

#### GameEntity
```kotlin
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### RulesEntity
```kotlin
@Entity(
    tableName = "rules",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RulesEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val content: String, // JSON
    val generatedAt: Long
)
```

### Data Flow Pattern

```
User Action → Intent
    ↓
ViewModel processes intent
    ↓
Repository called (abstraction)
    ↓
Data Source (Room/Network/DataStore)
    ↓
Result<T> returned
    ↓
UiState updated (StateFlow)
    ↓
UI recomposes (Compose)
```

### Offline-First Strategy
- **Primary Storage:** Room database for games and rules
- **Cache Strategy:** Network-first with local fallback
- **Sync:** No sync required (no user accounts in MVP)
- **Preferences:** DataStore for settings (independent of network)

---

## Build Configuration

### Gradle Setup
- **Build System:** Gradle 8.7.2 with Kotlin DSL
- **Convention Plugins:** Custom plugins in `build-logic` module
- **Version Catalog:** Centralized dependency management in `libs.versions.toml`
- **Configuration Cache:** Enabled for faster builds
- **Parallel Builds:** Enabled

### Build Types
- **Debug:** Development builds with debugging enabled
- **Release:** Optimized with ProGuard/R8 minification and resource shrinking

### Code Generation
- **KSP (Kotlin Symbol Processing):** Used by Room for compile-time verification
- **Benefits:** Faster than kapt, better error messages, Kotlin-first

---

## Security & Privacy

### Security Measures
- **Network Security:** HTTPS enforced via network security config
- **Code Obfuscation:** ProGuard/R8 in release builds
- **Payments:** Google Play Billing (no custom payment processing)
- **No User Accounts:** No password storage, no authentication (MVP)
- **Permissions:** Minimal required permissions with runtime requests

### Privacy Considerations
- **Analytics:** TelemetryDeck (privacy-focused, GDPR-compliant)
- **Data Storage:** Local only (no cloud sync in MVP)
- **Photo Processing:** Photos uploaded for AI processing, not stored on server
- **Transparency:** Privacy policy and terms of service linked in settings

---

## Quality Standards

### Performance Targets
- **Crash-Free Rate:** >99%
- **Scan-to-Rules Time:** <60 seconds (95th percentile)
- **App Startup:** <1.5 seconds cold start
- **Memory:** <100MB typical usage

### Testing Strategy
- **Unit Tests:** ViewModel logic, repository implementations
- **Integration Tests:** Database queries, API calls
- **UI Tests:** Critical user flows (onboarding, camera, purchase)
- **Coverage Target:** >70% for core business logic

### Code Quality
- **Static Analysis:** Android Lint, ktlint
- **Code Review:** Required for all feature branches
- **Conventional Commits:** Enforced commit message format
- **Branch Protection:** Staging and main branches protected

---

## Development Context

### iOS to Android Migration
This Android application is a **1:1 feature port** from an existing iOS application. Key migration considerations:

- **Design Parity:** Brutalist design system matches iOS version
- **Feature Parity:** All iOS features implemented with Android equivalents
- **UX Adaptation:** Android patterns (bottom navigation, FAB, material transitions)
- **Platform Integration:** Android-specific features (app shortcuts, predictive back)

### Reference Documentation
Located in `/docs/ios/`:
- `product-spec.md` - iOS product specification
- `android-migration-*.md` - Migration guides and component mappings
- Screenshots and design tokens for reference

### Developer Profile
- **Background:** iOS developer (Swift/SwiftUI) transitioning to Android
- **Learning Approach:** Modern Android with Google's recommended patterns
- **Architecture:** Clean Architecture familiar from iOS

---

## Git Workflow

### Branch Structure
- **main** - Production-ready code only
- **staging** - Development integration branch
- **feature/** - Feature branches (created from staging)
- **refactoring/** - Code improvements
- **hotfix/** - Emergency fixes

### Commit Standards
- **Format:** Conventional Commits (`type(scope): description`)
- **Types:** feat, fix, refactor, test, docs, chore
- **Requirements:**
  - Project must build before committing
  - All tests must pass
  - Regular commits as checkpoints

### Phase Completion Protocol
1. Complete all tasks in current phase
2. Commit all changes
3. Create PR from feature branch to `staging` (NOT main)
4. Include comprehensive PR description
5. Request code review
6. Merge after approval
7. Continue to next phase

---

## Development Environment

### Prerequisites
- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** 17 or later (bundled with Android Studio)
- **Android SDK:** API 34+ installed via SDK Manager
- **Git:** For version control
- **Koin:** 4.0+ for dependency injection

### IDE Configuration
- **Kotlin Plugin:** Auto-updated with Android Studio
- **Compose Plugin:** Enable Compose multipreview support
- **Code Style:** Kotlin official style guide
- **Live Templates:** Compose templates recommended

---

## Project Documentation

### Documentation Structure
```
docs/
├── analysis/               # Codebase analysis
├── architecture.md         # Architecture decisions (17KB)
├── prd.md                 # Product requirements (23KB, 52 FRs, 24 NFRs)
├── ux-design-specification.md  # UX patterns (35KB)
├── epics.md               # Story breakdown (87KB)
├── ios/                   # iOS reference documentation
│   ├── product-spec.md
│   ├── android-migration-*.md
│   └── design-tokens/
├── planning/              # Roadmaps and work phases
├── sprint-artifacts/      # Sprint documentation
└── development/           # This file and setup guides
```

### Key Documents
- **PRD:** 52 functional requirements, 24 non-functional requirements
- **Architecture:** Complete technical decisions and rationale
- **UX Spec:** Component library, user flows, interaction patterns
- **Epics:** User stories organized by value

---

## Support & Resources

### Documentation Links
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Koin Documentation](https://insert-koin.io/)
- [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- [CameraX](https://developer.android.com/training/camerax)

### Project-Specific Resources
- Linear Issue Tracker: RULE-* issues
- Design Tokens: `docs/ios/design-tokens/`
- iOS Screenshots: `docs/ios/screenshots/`
- Workflow Status: `docs/bmm-workflow-status.yaml`

---

## License

[License information to be added]

---

**Document Version:** 1.0.0
**Last Updated:** 2025-12-19
**Maintained By:** Development Team
