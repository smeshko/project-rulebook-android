---
title: Design Documentation
description: UX design specifications and visual design guidelines for project-rulebook-android
---

# Design

UX and visual design documentation for the Rulebook Android application.

## Documents

| Document | Description |
|----------|-------------|
| [ux-design-specification.md](ux-design-specification.md) | Complete UX design specification with user journeys, emotional design, and component strategy |

## Design Summary

**Design Language:** Brutalist

Rulebook uses a distinctive brutalist aesthetic characterized by:
- Thick black borders (3dp)
- Sharp corners (0dp radius)
- Bold offset shadows (4-12dp)
- High-saturation accent colors
- Black weight (900) typography

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| Themed Material 3 | Platform conventions + brand uniqueness |
| Brutalist aesthetic | iOS parity, market differentiation |
| Bottom Nav + FAB | Android convention for primary action |
| Predictive back | Modern Android navigation |
| Edge-to-edge | Immersive brutalist presence |

### Color Palette

| Color | Light | Dark | Usage |
|-------|-------|------|-------|
| Orange | `#FF6B35` | `#FF8C5F` | Game Overview |
| Blue | `#3498DB` | `#5DADE2` | Setup |
| Yellow | `#FFD23F` | `#FFE066` | First Round |
| Purple | `#7209B7` | `#9D4EDD` | Advanced Rules |
| Pink | `#E91E63` | `#F06292` | Primary actions |

### Design Token Sources

- `design-system-android.json` - Tokenized design values
- iOS screenshots (17) - Visual reference

## Related Sections

- [Architecture](../architecture/) - Technical design decisions
- [Reference](../reference/) - Component inventory
- [Templates](../templates/) - Design component patterns
