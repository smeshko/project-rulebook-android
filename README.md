# Project Rulebook - Android

> AI-powered board game rules companion for Android

**From box to playing in 60 seconds** - because game night is about connection, not homework.

## Overview

Rulebook is a mobile application that eliminates the friction of learning board games. By combining AI-powered image recognition with structured rule generation, users can photograph any game box and receive digestible, progressive rules within 60 seconds - transforming the dreaded "who reads the rules?" moment into an instant, shared experience.

The Android version represents a 1:1 feature port of the proven iOS application, adapted with native Android UX patterns (Jetpack Compose, Material Design conventions, Android-specific interactions) to feel natural on the platform.

## Features

- 📷 **Photo-to-rules pipeline** - Complete journey from unknown game to playing
- 🤖 **Multi-model AI resilience** - Fallback chain ensures recognition even for obscure/international games
- 📚 **Progressive disclosure format** - Setup → First Round → Deep Dive mirrors natural learning
- 💾 **Offline-first architecture** - Works in basements, cabins, and cafes where game nights happen
- ⚙️ **Customizable settings** - Personalize your experience
- 💳 **Credit-based usage system** - Fair and transparent pricing model

## Technology Stack

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVI (Model-View-Intent) + Clean Architecture
- **Build System**: Gradle 8.7.2 (Kotlin DSL)
- **Minimum SDK**: Android 14 (API 34)
- **Target SDK**: Android 15 (API 35)

## Project Structure

Multi-module architecture with 16 modules organized for maximum separation of concerns:

```
project-rulebook-android/
├── app/                    # Main application entry point
├── core/                   # Shared infrastructure (9 modules)
│   ├── common/            # Shared utilities and extensions
│   ├── data/              # Data layer abstractions
│   ├── database/          # Local database implementation
│   ├── datastore/         # Preferences and settings
│   ├── domain/            # Business logic and use cases
│   ├── model/             # Domain models
│   ├── network/           # Network layer
│   ├── testing/           # Testing utilities
│   └── ui/                # Shared UI components
├── feature/                # Feature modules (6 modules)
│   ├── camera/            # Camera and image capture
│   ├── library/           # Game library management
│   ├── onboarding/        # User onboarding flow
│   ├── rules/             # Rules display and interaction
│   ├── settings/          # App settings
│   └── subscription/      # Subscription and credits
└── build-logic/           # Convention plugins
```

## Documentation

Comprehensive project documentation is available in the `docs/` directory:

### Planning & Requirements
- [Product Requirements Document](docs/prd.md) - 52 functional requirements + 24 non-functional requirements
- [UX Design Specification](docs/ux-design-specification.md) - Complete user experience design
- [Epics & Stories](docs/epics.md) - 9 epics with detailed user stories

### Architecture & Design
- [System Architecture](docs/architecture.md) - Technical architecture and design decisions
- [iOS Migration Reference](docs/ios/) - Reference documentation from iOS implementation

### Development Process
- [BMM Workflow Status](docs/bmm-workflow-status.yaml) - Development methodology tracking
- [Sprint Artifacts](docs/sprint-artifacts/) - Story implementation details and progress

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or later
- **JDK**: 17 or later
- **Android SDK**: API 35 (Android 15)
- **Gradle**: 8.7.2 (wrapper included)

### Building the Project

```bash
# Clone the repository
git clone <repository-url>
cd project-rulebook-android

# Build the project
./gradlew build

# Build and install debug APK
./gradlew installDebug
```

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run all instrumented tests
./gradlew connectedAndroidTest

# Run tests for specific module
./gradlew :core:domain:test

# Run tests with coverage
./gradlew testDebugUnitTestCoverage
```

### Code Quality Checks

```bash
# Run Kotlin linter
./gradlew ktlintCheck

# Auto-format code with ktlint
./gradlew ktlintFormat

# Run all quality checks
./gradlew check
```

## Development Workflow

This project follows a structured development methodology based on the BMM (BMAD Method Methodology):

### Branch Structure

- `main` - Production-ready code only
- `staging` - Development integration branch
- `feature/*` - Feature branches (created from staging)
- `refactoring/*` - Refactoring work
- `hotfix/*` - Emergency fixes

### Commit Standards

All commits follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
type(scope): description

[optional body]

[optional footer]
```

**Common types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `style`: Code formatting (no logic change)
- `refactor`: Code restructuring
- `test`: Adding/updating tests
- `chore`: Build process or tooling

**Examples:**
```bash
feat(camera): add flash toggle for low-light scanning
fix(rules): resolve race condition in AI response parsing
docs(readme): update getting started section
test(library): add unit tests for game search
```

### Pull Request Process

1. **Create feature branch** from `staging`
   ```bash
   git checkout staging
   git pull
   git checkout -b feature/your-feature-name
   ```

2. **Implement changes** with regular commits
   - Commit frequently as checkpoints
   - Ensure project builds before committing
   - All tests must pass before committing

3. **Create PR to staging** (NOT main)
   - Include comprehensive description of changes
   - Reference related issue/story numbers
   - Document testing performed
   - Ensure all CI checks pass

4. **Code review and approval**
   - Request review from appropriate team member
   - Address all feedback
   - Wait for approval before merging

5. **Merge to staging**
   - Squash commits if needed
   - Delete feature branch after merge

### Git Worktree Workflow

This project uses git worktrees for parallel development:

```bash
# Create new worktree for feature work
git worktree add ../trees/<worktree-id> -b feature/branch-name

# Work in isolation
cd ../trees/<worktree-id>

# When done, remove worktree
git worktree remove ../trees/<worktree-id>
```

## Contributing

### Code Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Write self-documenting code
- Add comments only when logic isn't self-evident
- Keep functions small and focused

### Testing Requirements

- Write unit tests for all business logic
- Maintain or improve code coverage
- Test edge cases and error conditions
- Verify integration points
- Use descriptive test names that explain intent

### Security Guidelines

- **NEVER** commit secrets, API keys, or credentials
- Use environment variables for configuration
- Validate all user input
- Follow principle of least privilege
- Review security implications of dependencies

## Architecture Highlights

### MVI Pattern

The app follows Model-View-Intent architecture:
- **Model**: Represents UI state
- **View**: Renders state and emits intents
- **Intent**: User actions that trigger state changes

### Clean Architecture Layers

```
Presentation Layer (feature modules)
    ↓
Domain Layer (core/domain)
    ↓
Data Layer (core/data, core/network, core/database)
```

### Dependency Injection

- Uses Hilt for dependency injection
- Module-scoped dependencies
- ViewModels injected via Hilt

## License

[License information to be added]

---

## ADW 5.1 Integration Test

**Status**: ✅ Tested and Verified
**Test ID**: RULE-121
**Worktree**: 3f99bfe1
**Date**: 2025-12-21
**Version**: 5.1

This project uses **ADW (AI Development Workflow) version 5.1** for automated development workflow orchestration. This README was created as part of validating the ADW 5.1 integration and complete workflow pipeline.

### Validation Checklist

The following aspects of ADW 5.1 were tested and validated:

- ✅ **Planning Phase** - Comprehensive implementation plan created before execution
- ✅ **Execution Phase** - Changes implemented following all project standards
- ✅ **Documentation** - Properly structured in `docs/planning/` directory
- ✅ **Git Workflow** - Feature branch → Staging PR pipeline executed
- ✅ **Commit Standards** - Conventional commit format applied consistently
- ✅ **Quality Assurance** - All standards compliance verified
- ✅ **Integration** - Linear issue tracking and GitHub automation functional
- ✅ **Worktree Isolation** - Git worktree workflow maintained

### Test Objectives

1. **Workflow Validation** - Validate ADW 5.1 can properly plan, execute, and deliver changes
2. **Integration Testing** - Ensure ADW integrates with git worktrees, Linear, and GitHub
3. **Process Verification** - Confirm adherence to development standards and best practices

### Success Criteria

All success criteria were met:
- Implementation plan created following CLAUDE.md standards
- Plan approved through iteration with user
- Implementation executed in logical phases
- Code committed with conventional commit messages
- Pull request created to staging branch
- All documentation properly structured
- No secrets or credentials committed

For detailed implementation planning, see [RULE-121 v5.1 Implementation Plan](docs/planning/RULE-121-v5.1-implementation-plan.md).

---

*This README represents the live documentation for the Project Rulebook Android application. For questions or contributions, please refer to the development workflow section above.*
