# Rulebook Android

**From box to playing in 60 seconds** - because game night is about connection, not homework.

## Overview

Rulebook is a mobile application that eliminates the friction of learning board games. By combining AI-powered image recognition with structured rule generation, users can photograph any game box and receive digestible, progressive rules within 60 seconds.

This is the Android version - a 1:1 feature port of the proven iOS application, adapted with native Android UX patterns (Jetpack Compose, Material Design conventions, Android-specific interactions) to feel natural on the platform.

## Key Features

- **Photo-to-rules pipeline**: Complete journey from unknown game to playing
- **Multi-model AI resilience**: Fallback chain ensures recognition even for obscure/international games
- **Progressive disclosure format**: Setup → First Round → Deep Dive mirrors natural learning
- **Offline-first architecture**: Works in basements, cabins, and cafes where game nights happen

## Project Structure

```
docs/
├── analysis/          # Codebase analysis and findings
├── architecture/      # Project architecture documents
├── design/            # Project design documents
├── development/       # Project setup and dev-related docs
├── planning/          # Roadmaps, work phases, task lists
├── product/           # Product related files like PRDs, requirements
└── testing/           # Testing strategies and documentation
```

## Documentation

- [Product Requirements Document](docs/prd.md)
- [Architecture Document](docs/architecture.md)
- [UX Design Specification](docs/ux-design-specification.md)
- [Epics](docs/epics.md)

## Development

This project follows strict development workflow standards:
- Feature branches created from `staging`
- Regular commits at checkpoints
- Pull requests to `staging` (not `main`)
- Comprehensive PR descriptions required

## Getting Started

[Development setup instructions coming soon]

---

Hello from Linear
