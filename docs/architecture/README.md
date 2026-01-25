---
title: Architecture Documentation
description: System architecture and design documentation for project-rulebook-android
---

# Architecture

This section contains architecture and design documentation.

## Documents

| Document | Description |
|----------|-------------|
| [overview.md](overview.md) | Complete architecture decision document with technology stack, patterns, and module structure |

## Key Decisions

- **Architecture Pattern:** Multi-module with MVI state management
- **DI Framework:** Koin 4.x
- **Database:** Room with offline-first approach
- **UI:** Jetpack Compose with Material 3
- **Analytics:** TelemetryDeck (cross-platform with iOS)

## Related Sections

- [Development](../development/) - Implementation guides
- [Reference](../reference/) - Technical specifications
