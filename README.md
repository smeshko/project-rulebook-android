# Rulebook - Android

**From box to playing in 60 seconds** - because game night is about connection, not homework.

## Overview

Rulebook is a mobile application that eliminates the friction of learning board games. By combining AI-powered image recognition with structured rule generation, users can photograph any game box and receive digestible, progressive rules within 60 seconds - transforming the dreaded "who reads the rules?" moment into an instant, shared experience.

The Android version represents a 1:1 feature port of the proven iOS application, adapted with native Android UX patterns (Jetpack Compose, Material Design conventions, Android-specific interactions) to feel natural on the platform.

## Key Features

- **Photo-to-rules pipeline**: Complete journey from unknown game to playing
- **Multi-model AI resilience**: Fallback chain ensures recognition even for obscure/international games
- **Progressive disclosure format**: Setup → First Round → Deep Dive mirrors natural learning
- **Offline-first architecture**: Works in basements, cabins, and cafes where game nights happen

## Documentation

For detailed project information, see:

- [Product Requirements Document](docs/prd.md) - Complete product vision, requirements, and specifications
- [Architecture](docs/architecture.md) - Technical architecture and system design
- [UX Design Specification](docs/ux-design-specification.md) - User experience and interface design
- [Epics](docs/epics.md) - Feature breakdown and development roadmap

## Getting Started

This is an Android project built with modern Android development practices including Jetpack Compose and Material Design 3.

### Prerequisites

- Android Studio (latest stable version)
- JDK 11 or higher
- Android SDK with API level 24+ (Android 7.0+)

### Project Structure

```
docs/
├── analysis/          # Codebase analysis and findings
├── architecture.md    # Project architecture
├── epics.md          # Development epics
├── ios/              # iOS reference implementation
├── planning/         # Implementation plans and roadmaps
├── prd.md           # Product requirements
├── sprint-artifacts/ # Sprint planning artifacts
└── ux-design-specification.md  # UX/UI specifications
```

## Development Workflow

This project follows a structured development workflow with:

- Feature branches for all development work
- Regular commits with conventional commit messages
- Pull requests to staging for review
- Comprehensive documentation in the `docs/` directory

## License

[License information to be added]

---

Hello from Linear
