# Story 1.12: Domain Models

Status: ready-for-dev

## Linear Issue

- **ID:** RULE-119
- **URL:** https://linear.app/project-rulebook/issue/RULE-119/story-112-domain-models

## Story

As a developer,
I want domain models defined in core/model,
So that data structures are shared across modules.

## Acceptance Criteria

1. **Given** the `core/model` module
   **When** domain models are defined
   **Then** the following data classes exist:

   ```kotlin
   data class Game(
       val id: String,
       val title: String,
       val thumbnailUrl: String?,
       val createdAt: Long,
       val lastAccessedAt: Long
   )

   data class Rules(
       val gameId: String,
       val overview: RuleSection,
       val setup: RuleSection,
       val firstRound: RuleSection,
       val advanced: RuleSection
   )

   data class RuleSection(
       val title: String,
       val content: String,
       val items: List<String>? = null // For checklist items
   )

   data class ScanResult(
       val gameTitle: String,
       val confidence: Float,
       val thumbnailUrl: String?
   )
   ```

2. **And** models are pure Kotlin (no Android dependencies)

3. **And** models are immutable data classes

## Tasks / Subtasks

- [ ] Task 1: Create Game data class (AC: #1, #2, #3)
  - [ ] Define id: String
  - [ ] Define title: String
  - [ ] Define thumbnailUrl: String?
  - [ ] Define createdAt: Long
  - [ ] Define lastAccessedAt: Long
  - [ ] Ensure no Android dependencies
- [ ] Task 2: Create RuleSection data class (AC: #1, #2, #3)
  - [ ] Define title: String
  - [ ] Define content: String
  - [ ] Define items: List<String>? for checklists
- [ ] Task 3: Create Rules data class (AC: #1, #2, #3)
  - [ ] Define gameId: String
  - [ ] Define overview: RuleSection
  - [ ] Define setup: RuleSection
  - [ ] Define firstRound: RuleSection
  - [ ] Define advanced: RuleSection
- [ ] Task 4: Create ScanResult data class (AC: #1, #2, #3)
  - [ ] Define gameTitle: String
  - [ ] Define confidence: Float
  - [ ] Define thumbnailUrl: String?
- [ ] Task 5: Create mapper functions
  - [ ] GameEntity.toDomain() -> Game
  - [ ] Game.toEntity() -> GameEntity
  - [ ] RulesEntity.toDomain() -> Rules
  - [ ] Rules.toEntity() -> RulesEntity
  - [ ] AnalyzeResponse.toDomain() -> ScanResult

## Dev Notes

### Architecture Patterns

- core/model has no dependencies
- Used by both data layer and UI layer
- Mappers convert between Entity and Domain models

### Module Dependency Rules

```
core/model <- core/database
core/model <- core/network
core/model <- core/data
core/model <- feature/*
```

The model module is a leaf node - it depends on nothing.

### Data Classes

All models are:
- Immutable (val only)
- Data classes (automatic equals, hashCode, copy)
- Pure Kotlin (no Android imports)

### Entity vs Domain Model

| Entity (database) | Domain Model |
|-------------------|--------------|
| GameEntity | Game |
| RulesEntity | Rules |
| - | RuleSection |
| - | ScanResult |

Entities have Room annotations; domain models are clean data classes.

### Mapper Pattern

```kotlin
// In core/data or core/database
fun GameEntity.toDomain(): Game = Game(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    title = title,
    thumbnailUrl = thumbnailUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)
```

### PRD Requirements Mapped

These models support:
- FR27-32: Game library management
- FR18-24: Rules generation and display
- FR12-17: Game recognition

### References

- [Source: docs/architecture.md#Data Architecture]
- [Source: docs/architecture.md#Core Module Responsibilities]

## Dev Agent Record

### Context Reference

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
