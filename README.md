# Rulebook Android

> **From box to playing in 60 seconds** - AI-powered board game rules at your fingertips

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-2024.11.00-orange.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-34-brightgreen.svg)](https://developer.android.com/about/versions)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-35-brightgreen.svg)](https://developer.android.com/about/versions)

---

## Overview

**Rulebook** eliminates the friction of learning board games. Using AI-powered image recognition, users photograph any game box and receive digestible, progressive rules within 60 seconds - transforming the dreaded "who reads the rules?" moment into an instant, shared experience.

This Android version is a **1:1 feature port** from the iOS application, built natively with Jetpack Compose and modern Android architecture patterns.

### Key Features

- 📸 **Smart Game Recognition** - Photo-to-rules pipeline with multi-model AI fallback
- 📖 **Progressive Disclosure** - Setup → First Round → Advanced rules format
- 📚 **Offline Library** - Access your game rules anywhere, anytime
- 💳 **Simple Credit System** - 3 free credits, purchase more as needed
- 🎨 **Brutalist Design** - Bold, distinctive UI with thick borders and sharp edges
- 🔒 **Privacy-First** - No user accounts, local storage, TelemetryDeck analytics

---

## Quick Start

### Prerequisites

- **Android Studio:** Hedgehog (2023.1.1) or later
- **JDK:** 17+ (bundled with Android Studio)
- **Android SDK:** API 34+ installed

### Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url> project-rulebook-android
   cd project-rulebook-android
   ```

2. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **Run the app**
   - Select a device/emulator from the dropdown
   - Click the green play button or press `Shift + F10`

For detailed setup instructions, see [Setup Guide](docs/development/SETUP_GUIDE.md).

---

## Project Architecture

### Architecture Pattern: Clean Architecture + MVI

```
UI Layer (Compose) → ViewModel (MVI) → Repository → Data Sources (Room/Retrofit/DataStore)
```

### Module Structure (16 Modules)

#### 🏗️ App Module
- Single-activity architecture with Compose Navigation
- Koin dependency injection initialization
- Edge-to-edge display with predictive back gestures

#### ⚡ Feature Modules (6)
| Module | Description |
|--------|-------------|
| `feature:camera` | CameraX integration, photo capture, gallery picker |
| `feature:library` | Game library with grid layout and sort options |
| `feature:onboarding` | 2-screen flow with 3 free credits award |
| `feature:purchase` | Google Play Billing integration |
| `feature:rules` | Progressive rule display with offline access |
| `feature:settings` | Theme, haptics, support links, data management |

#### 🔧 Core Modules (9)
| Module | Purpose |
|--------|---------|
| `core:analytics` | TelemetryDeck integration |
| `core:billing` | Google Play Billing wrapper |
| `core:common` | Shared utilities and extensions |
| `core:data` | Repository implementations |
| `core:database` | Room database (2 entities) |
| `core:datastore` | Preferences with DataStore |
| `core:designsystem` | Brutalist UI components |
| `core:model` | Domain models (pure Kotlin) |
| `core:network` | Retrofit API client |

For detailed architecture information, see [Project Overview](docs/development/PROJECT_OVERVIEW.md).

---

## Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI** | Jetpack Compose | 2024.11.00 |
| **DI** | Koin | 4.0.0 |
| **Database** | Room | 2.6.1 |
| **Network** | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| **Serialization** | kotlinx.serialization | 1.7.3 |
| **Image Loading** | Coil | 3.0.3 |
| **Camera** | CameraX | 1.4.1 |
| **Billing** | Google Play Billing | 7.1.1 |
| **Analytics** | TelemetryDeck | 6.3.0 |

---

## Build & Test

### Build Commands

```bash
# Clean build
./gradlew clean

# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Build all modules
./gradlew build
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Run specific module tests
./gradlew :feature:camera:test
```

---

## Design System

### Brutalist Aesthetic

The app features a distinctive **brutalist design system** with:
- **Thick borders** (2-4dp) on all interactive elements
- **Bold shadows** for depth and elevation
- **Zero corner radius** (sharp edges)
- **High contrast** color palette with orange accent (#FF6B35)
- **Custom typography** with brutalist text styles

### Custom Components

All components in `core:designsystem`:
- `RulebookButton` - Primary and secondary button styles
- `RulebookCard` - Elevated cards with sharp brutalist edges
- `RulebookFAB` - Floating action button with thick border
- `RulebookHeaderBar` - Custom top app bar
- `CreditsDisplay` - Real-time credit balance indicator
- `BrutalistModifiers` - Reusable modifier extensions

---

## Project Structure

```
project-rulebook-android/
├── app/                    # Main application module
├── build-logic/           # Gradle convention plugins
├── core/                  # Infrastructure modules
│   ├── analytics/
│   ├── billing/
│   ├── common/
│   ├── data/
│   ├── database/
│   ├── datastore/
│   ├── designsystem/
│   ├── model/
│   └── network/
├── feature/               # Feature modules
│   ├── camera/
│   ├── library/
│   ├── onboarding/
│   ├── purchase/
│   ├── rules/
│   └── settings/
├── docs/                  # Project documentation
│   ├── analysis/
│   ├── architecture.md    # Architecture decisions (17KB)
│   ├── prd.md            # Product requirements (23KB)
│   ├── ux-design-specification.md  # UX patterns (35KB)
│   ├── epics.md          # User stories (87KB)
│   ├── development/      # Setup and development guides
│   └── ios/              # iOS reference documentation
└── gradle/               # Gradle wrapper & version catalog
```

---

## Documentation

### For Developers

| Document | Description |
|----------|-------------|
| [Setup Guide](docs/development/SETUP_GUIDE.md) | Complete development environment setup |
| [Project Overview](docs/development/PROJECT_OVERVIEW.md) | Architecture, tech stack, and design decisions |
| [Architecture](docs/architecture.md) | Detailed architecture decisions and rationale |
| [PRD](docs/prd.md) | 52 functional + 24 non-functional requirements |
| [UX Design Spec](docs/ux-design-specification.md) | Component library and interaction patterns |
| [Epics](docs/epics.md) | User stories and implementation plan |

### iOS Migration Reference

Located in `docs/ios/`:
- iOS product specification
- Screen inventory and layouts
- Component library with Compose examples
- Navigation patterns and user flows
- Design tokens and screenshots

---

## Git Workflow

### Branch Structure

- `main` - Production-ready code
- `staging` - Development integration branch
- `feature/*` - Feature development (branch from staging)
- `hotfix/*` - Emergency fixes

### Commit Standards

We follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): description

Example:
feat(camera): add zoom gesture support
fix(library): resolve sort order bug
docs(readme): update setup instructions
```

**Types:** `feat`, `fix`, `refactor`, `test`, `docs`, `chore`

### Pull Request Process

1. Create feature branch from `staging`
2. Commit regularly with meaningful messages
3. Ensure all tests pass: `./gradlew test`
4. Create PR targeting `staging` (NOT main)
5. Request code review
6. Merge after approval

---

## Development Guidelines

### Code Style

- **Kotlin Official Style Guide** - Use default Android Studio formatting
- **Compose Guidelines** - Follow Jetpack Compose best practices
- **Convention Over Configuration** - Leverage convention plugins in `build-logic`

### Testing Requirements

- **Unit Tests:** >70% coverage for business logic
- **Integration Tests:** Database and API interactions
- **UI Tests:** Critical user flows (onboarding, camera, purchase)

### Quality Checks Before Commit

- [ ] Project builds: `./gradlew build`
- [ ] Tests pass: `./gradlew test`
- [ ] No lint errors: `./gradlew lint`
- [ ] App runs without crashes
- [ ] Conventional commit message format

---

## Troubleshooting

### Common Issues

**Gradle sync failed**
```bash
./gradlew clean --refresh-dependencies
# Or: File → Invalidate Caches → Invalidate and Restart
```

**Emulator won't start**
- Check virtualization enabled in BIOS (Intel VT-x/AMD-V)
- Try different graphics mode in AVD settings

**SDK location not found**
- Create `local.properties` with `sdk.dir=/path/to/android/sdk`

For more troubleshooting help, see [Setup Guide - Troubleshooting](docs/development/SETUP_GUIDE.md#troubleshooting).

---

## Resources

### Official Documentation

- [Android Developer Guide](https://developer.android.com/guide)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)
- [Kotlin Language Guide](https://kotlinlang.org/docs/home.html)
- [Koin Documentation](https://insert-koin.io/)

### Project Resources

- **Linear:** RULE-* issue tracking
- **Workflow Status:** `docs/bmm-workflow-status.yaml`
- **Sprint Artifacts:** `docs/sprint-artifacts/`

---

## License

[License information to be added]

---

## Acknowledgments

- **TelemetryDeck** for privacy-first analytics
- **Jetpack Compose** team for modern Android UI
- **Koin** community for lightweight dependency injection

---

**Project Version:** 1.0.0 (Build 1)
**Last Updated:** 2025-12-19
**Maintained By:** Development Team
