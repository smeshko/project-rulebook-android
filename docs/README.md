---
title: Project Documentation
description: Documentation hub for project-rulebook-android
---

# Rulebook Android Documentation

Welcome to the documentation for **Rulebook** - an Android app that lets users photograph any board game box and receive AI-generated, digestible rules within 60 seconds.

## Documentation Structure

| Section | Description |
|---------|-------------|
| [Architecture](architecture/) | Technical design decisions, module structure, patterns |
| [Development](development/) | Setup guides and development reference |
| [Design](design/) | UX specifications and visual design guidelines |
| [Product](product/) | Product requirements and specifications |
| [Reference](reference/) | Component inventory and project structure |
| [Templates](templates/) | Code patterns and boilerplate templates |
| [Testing](testing/) | Testing strategy and coverage analysis |

## Quick Start

```bash
# Clone and setup
git clone <repository-url>
cd project-rulebook-android

# Build
./gradlew assembleDebug

# Run tests
./gradlew test
```

See [Development > Getting Started](development/getting-started.md) for detailed setup instructions.

## Project Overview

| Attribute | Value |
|-----------|-------|
| **Platform** | Android (Kotlin, Jetpack Compose) |
| **Min SDK** | API 34 (Android 14) |
| **Architecture** | Clean Architecture (MVVM) with StateFlow |
| **Design System** | Brutalist aesthetic on Material 3 |
| **Modules** | 16 total (1 app + 9 core + 6 feature) |

## Key Documents

| Document | Purpose |
|----------|---------|
| [PRD](product/prd.md) | Complete product requirements (52 FRs, 24 NFRs) |
| [Architecture Overview](architecture/overview.md) | Technical decisions and module structure |
| [UX Design Spec](design/ux-design-specification.md) | User experience and visual design |
| [Testing Overview](testing/testing-overview.md) | Test patterns and coverage |

## Success Metrics

| Metric | Target |
|--------|--------|
| Scan-to-rules | <60 seconds |
| Crash-free sessions | >99% |
| Day 7 retention | >20% |
| Free → Paid conversion | >5% |
