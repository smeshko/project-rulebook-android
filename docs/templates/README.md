---
title: Component Templates
description: Templates for creating new components in project-rulebook-android
---

# Templates

This section contains templates for creating new components in the project. Each template documents existing patterns in the codebase with code examples and integration checklists.

## Available Templates

| Template | Description |
|----------|-------------|
| [feature-screen-template.md](feature-screen-template.md) | Screen composable with ViewModel injection and state separation |
| [viewmodel-template.md](viewmodel-template.md) | Feature ViewModel with StateFlow and MVI pattern |
| [uistate-template.md](uistate-template.md) | Immutable UI state data class with computed properties |
| [repository-template.md](repository-template.md) | Repository interface and implementation with Result error handling |
| [room-entity-dao-template.md](room-entity-dao-template.md) | Room database entity and DAO pair |
| [koin-module-template.md](koin-module-template.md) | Koin dependency injection module |
| [design-component-template.md](design-component-template.md) | Brutalist-styled reusable Compose component |

## How to Use

1. Find the template matching your component type
2. Copy the code template section
3. Replace `{Placeholders}` with your specific values
4. Follow the integration checklist
5. Reference existing patterns linked in each template

## Template Groups

### New Feature Screen (use together)
1. `feature-screen-template.md` - The screen composable
2. `viewmodel-template.md` - State management
3. `uistate-template.md` - State data class
4. `koin-module-template.md` - DI registration

### New Data Entity (use together)
1. `room-entity-dao-template.md` - Database layer
2. `repository-template.md` - Data abstraction
3. `koin-module-template.md` - DI registration

### New UI Component
1. `design-component-template.md` - Reusable composable

## Related Sections

- [Architecture](../architecture/) - System patterns and decisions
- [Development](../development/) - Setup and development guides
