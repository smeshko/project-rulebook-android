# Rulebook Android - Documentation Index

**Generated:** 2026-01-22
**Workflow:** BMAD Document Project v2.0.0
**Purpose:** AI-assisted development reference documentation

---

## Project Overview

| Property | Value |
|----------|-------|
| **Type** | Monolith (Multi-Module) |
| **Language** | Kotlin 2.0.21 |
| **Architecture** | Clean Architecture (MVVM) |
| **Modules** | 16 (1 app + 9 core + 6 feature) |
| **Platform** | Android 7.0+ (API 24) |

---

## Quick Reference

### Tech Stack Summary

| Layer | Technology |
|-------|------------|
| UI | Jetpack Compose (BOM 2024.11.00) |
| Navigation | Compose Navigation 2.8.4 |
| DI | Koin 4.0.0 |
| Database | Room 2.6.1 |
| Network | Retrofit 2.11.0 + OkHttp 4.12.0 |
| Local Storage | DataStore 1.1.1 |
| Camera | CameraX 1.4.1 |
| Billing | Play Billing 7.1.1 |

### Key Entry Points

| Purpose | Path |
|---------|------|
| Application | `app/src/main/kotlin/com/rulebook/RulebookApplication.kt` |
| Navigation | `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` |
| Database | `core/database/src/main/kotlin/.../RulebookDatabase.kt` |
| API Client | `core/network/src/main/kotlin/.../api/RulebookApi.kt` |
| Theme | `core/designsystem/src/main/kotlin/.../theme/RulebookTheme.kt` |

### Architecture Patterns

- **State Management:** Unidirectional data flow with StateFlow
- **Repository Pattern:** Abstracts data sources from business logic
- **Convention Plugins:** DRY Gradle configuration via build-logic
- **Design System:** Brutalist aesthetic on Material 3

---

## Generated Documentation

### Core Documentation

| Document | Description | Status |
|----------|-------------|--------|
| [Project Overview](./project-overview.md) | Executive summary and key metrics | ✓ |
| [Architecture](./architecture.md) | Complete architecture documentation | ✓ |
| [Development Guide](./development-guide.md) | Setup, build, and test commands | ✓ |
| [Source Tree Analysis](./source-tree-analysis.md) | Annotated directory structure | ✓ |
| [Component Inventory](./component-inventory.md) | UI component catalog | ✓ |

### Data Files

| File | Description | Status |
|------|-------------|--------|
| [Project Scan Report](./project-scan-report.json) | Raw scan data and workflow state | ✓ |

---

## Existing Documentation

### Planning Artifacts

| Document | Path |
|----------|------|
| Product Brief | [project-planning-artifacts/analysis/product-brief-project-rulebook-android-2025-12-02.md](./project-planning-artifacts/analysis/product-brief-project-rulebook-android-2025-12-02.md) |
| PRD | [project-planning-artifacts/prd.md](./project-planning-artifacts/prd.md) |
| Architecture (Original) | [project-planning-artifacts/architecture.md](./project-planning-artifacts/architecture.md) |
| UX Design | [project-planning-artifacts/ux-design-specification.md](./project-planning-artifacts/ux-design-specification.md) |

### Epics & Stories

| Epic | Description |
|------|-------------|
| [Epic Index](./project-planning-artifacts/epics/index.md) | Master epic navigation |
| [Epic 1](./project-planning-artifacts/epics/epic-1-foundation-design-system.md) | Foundation & Design System |
| [Epic 2](./project-planning-artifacts/epics/epic-2-app-shell-navigation.md) | App Shell & Navigation |
| [Epic 3](./project-planning-artifacts/epics/epic-3-onboarding-experience.md) | Onboarding Experience |
| [Epic 4](./project-planning-artifacts/epics/epic-4-photo-capture-flow.md) | Photo Capture Flow |
| [Epic 5](./project-planning-artifacts/epics/epic-5-game-recognition-rules-generation.md) | Game Recognition & Rules |
| [Epic 6](./project-planning-artifacts/epics/epic-6-rules-display-reference.md) | Rules Display & Reference |
| [Epic 7](./project-planning-artifacts/epics/epic-7-library-management.md) | Library Management |
| [Epic 8](./project-planning-artifacts/epics/epic-8-credit-system-purchases.md) | Credit System & Purchases |
| [Epic 9](./project-planning-artifacts/epics/epic-9-settings-platform-polish.md) | Settings & Platform Polish |

### Implementation Artifacts

Story implementation documentation organized by epic:

**Epic 1 - Foundation:**
- [1-1 Project Initialization](./implementation-artifacts/1-1-project-initialization.md)
- [1-2 Dependency Injection (Koin)](./implementation-artifacts/1-2-dependency-injection-koin.md)
- [1-3 Room Database Schema](./implementation-artifacts/1-3-room-database-schema.md)
- [1-4 DataStore Preferences](./implementation-artifacts/1-4-datastore-preferences.md)
- [1-5 Network Client Configuration](./implementation-artifacts/1-5-network-client-configuration.md)
- [1-6 Analytics (TelemetryDeck)](./implementation-artifacts/1-6-analytics-telemetrydeck.md)
- [1-7 Design Tokens & Theme](./implementation-artifacts/1-7-design-tokens-theme.md)
- [1-8 Brutalist Modifiers](./implementation-artifacts/1-8-brutalist-modifiers.md)
- [1-9 RulebookButton](./implementation-artifacts/1-9-rulebookbutton.md)
- [1-10 RulebookCard](./implementation-artifacts/1-10-rulebookcard.md)
- [1-11 RulebookHeaderBar](./implementation-artifacts/1-11-rulebookheaderbar.md)
- [1-12 Domain Models](./implementation-artifacts/1-12-domain-models.md)
- [1-13 Result Wrapper](./implementation-artifacts/1-13-result-wrapper.md)
- [Epic 1 Retrospective](./implementation-artifacts/epic-1-retro-2025-12-16.md)

**Epic 2 - App Shell:**
- [2-1 MainActivity Edge-to-Edge](./implementation-artifacts/2-1-main-activity-edge-to-edge.md)
- [2-2 Navigation Host & Routes](./implementation-artifacts/2-2-navigation-host-routes.md)
- [2-3 Bottom Navigation Bar](./implementation-artifacts/2-3-bottom-navigation-bar.md)
- [2-4 Camera FAB](./implementation-artifacts/2-4-camera-fab.md)
- [2-5 Library Screen Empty State](./implementation-artifacts/2-5-library-screen-empty-state.md)
- [2-6 Settings Screen Shell](./implementation-artifacts/2-6-settings-screen-shell.md)
- [2-7 Scaffold Integration](./implementation-artifacts/2-7-scaffold-integration.md)
- [2-8 Predictive Back Gesture](./implementation-artifacts/2-8-predictive-back-gesture.md)
- [Epic 2 Retrospective](./implementation-artifacts/epic-2-retro-2025-12-17.md)

**Epic 3 - Onboarding:**
- [3-1 Onboarding Flow Detection](./implementation-artifacts/3-1-onboarding-flow-detection.md)
- [3-2 Onboarding Screen 1 (Value Proposition)](./implementation-artifacts/3-2-onboarding-screen-1-value-proposition.md)
- [3-3 Onboarding Screen 2 (Getting Started)](./implementation-artifacts/3-3-onboarding-screen-2-getting-started.md)
- [3-4 Credit Award on Completion](./implementation-artifacts/3-4-credit-award-on-completion.md)
- [3-5 Skip Onboarding Functionality](./implementation-artifacts/3-5-skip-onboarding-functionality.md)
- [3-6 Onboarding Page Indicator](./implementation-artifacts/3-6-onboarding-page-indicator.md)
- [3-7 Onboarding Analytics Events](./implementation-artifacts/3-7-onboarding-analytics-events.md)
- [Epic 3 Retrospective](./implementation-artifacts/epic-3-retro-2025-12-17.md)

**Epic 4 - Photo Capture:**
- [4-1 Camera Screen (CameraX Preview)](./implementation-artifacts/4-1-camera-screen-camerax-preview.md)
- [4-2 Photo Capture Button & Haptic](./implementation-artifacts/4-2-photo-capture-button-haptic.md)
- [4-3 Flash/Torch Control](./implementation-artifacts/4-3-flash-torch-control.md)
- [4-4 Pinch-to-Zoom Gesture](./implementation-artifacts/4-4-pinch-to-zoom-gesture.md)
- [4-5 Tap-to-Focus](./implementation-artifacts/4-5-tap-to-focus.md)
- [4-6 Gallery Picker Alternative](./implementation-artifacts/4-6-gallery-picker-alternative.md)
- [4-7 Image Compression Before Upload](./implementation-artifacts/4-7-image-compression-before-upload.md)
- [4-8 Credit Balance Display (Camera)](./implementation-artifacts/4-8-credit-balance-display-camera.md)
- [4-9 Camera Permission Handling](./implementation-artifacts/4-9-camera-permission-handling.md)
- [4-10 Camera Close/Back Navigation](./implementation-artifacts/4-10-camera-close-back-navigation.md)

---

## Getting Started

### Quick Start

```bash
# Clone repository
git clone <repository-url>
cd project-rulebook-android

# Build debug APK
./gradlew assembleDebug

# Run on device/emulator
./gradlew installDebug

# Run all tests
./gradlew test
```

### Development Workflow

1. **Read** [Development Guide](./development-guide.md) for environment setup
2. **Review** [Architecture](./architecture.md) for system design
3. **Explore** [Source Tree Analysis](./source-tree-analysis.md) for file locations
4. **Reference** [Component Inventory](./component-inventory.md) for UI patterns

### Module Structure

```
app/                    → Main application entry point
core/                   → Shared infrastructure
├── analytics/          → TelemetryDeck integration
├── billing/            → Google Play Billing
├── common/             → Result<T>, utilities
├── data/               → Repository implementations
├── database/           → Room database
├── datastore/          → DataStore preferences
├── designsystem/       → UI components & theme
├── model/              → Domain models
└── network/            → Retrofit API client
feature/                → Feature modules
├── camera/             → Photo capture
├── library/            → Game library
├── onboarding/         → Onboarding flow
├── purchase/           → Credit purchases
├── rules/              → Rules display
└── settings/           → Settings screen
```

---

## API Quick Reference

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/v1/analyze` | POST | Image recognition |
| `/v1/generate` | POST | Rules generation |

**Base URLs:**
- Debug: `https://api-staging.rulebook.app/v1/`
- Release: `https://api.rulebook.app/v1/`

---

## Database Schema

| Table | Purpose |
|-------|---------|
| `saved_games` | Persisted game entries |
| `rules` | Generated rules (JSON sections) |

**Relationship:** One-to-Many (Game → Rules) with CASCADE delete

---

*Generated by BMAD Document Project Workflow*
