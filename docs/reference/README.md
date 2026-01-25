---
title: Reference Documentation
description: Technical reference for components, modules, and project structure
---

# Reference

Technical reference documentation for the Rulebook Android project.

## Documents

| Document | Description |
|----------|-------------|
| [project-overview.md](project-overview.md) | High-level project summary and technology stack |
| [source-tree.md](source-tree.md) | Annotated directory structure and module inventory |
| [component-inventory.md](component-inventory.md) | Complete catalog of design system and feature components |

## Quick Links

### Architecture
- 16 modules (1 app + 9 core + 6 feature)
- Clean Architecture (MVVM) with StateFlow
- Brutalist design system on Material 3

### Key Technologies
- Kotlin 2.0.21 + Jetpack Compose
- Koin 4.0 for DI
- Room + DataStore for persistence
- Retrofit + OkHttp for networking

### Design System
- 5 core components (Button, Card, FAB, HeaderBar, CreditsDisplay)
- 0dp corner radius (brutalist aesthetic)
- Custom color/typography/spacing tokens

## Related Sections

- [Architecture](../architecture/) - Detailed design decisions
- [Templates](../templates/) - Component creation patterns
- [Development](../development/) - Setup and build guides
