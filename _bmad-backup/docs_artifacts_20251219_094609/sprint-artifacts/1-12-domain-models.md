# Story 1.12: Domain Models

Status: done

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

- [x] Task 1: Create Game data class (AC: #1, #2, #3)
  - [x] Define id: String
  - [x] Define title: String
  - [x] Define thumbnailUrl: String?
  - [x] Define createdAt: Long
  - [x] Define lastAccessedAt: Long
  - [x] Ensure no Android dependencies
- [x] Task 2: Create RuleSection data class (AC: #1, #2, #3)
  - [x] Define title: String
  - [x] Define content: String
  - [x] Define items: List<String>? for checklists
- [x] Task 3: Create Rules data class (AC: #1, #2, #3)
  - [x] Define gameId: String
  - [x] Define overview: RuleSection
  - [x] Define setup: RuleSection
  - [x] Define firstRound: RuleSection
  - [x] Define advanced: RuleSection
- [x] Task 4: Create ScanResult data class (AC: #1, #2, #3)
  - [x] Define gameTitle: String
  - [x] Define confidence: Float
  - [x] Define thumbnailUrl: String?
- [x] Task 5: Create mapper functions
  - [x] GameEntity.toDomain() -> Game
  - [x] Game.toEntity() -> GameEntity
  - [x] RulesEntity.toDomain() -> Rules
  - [x] Rules.toEntity(id) -> RulesEntity (requires entity ID)
  - [x] AnalyzeResponse.toDomain() -> ScanResult

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

- Architecture: `docs/architecture.md`
- Room Entities: `core/database/src/main/kotlin/.../entity/`

### Agent Model Used
Claude Opus 4.5

claude-opus-4-5-20251101

### Debug Log References

N/A

### Completion Notes List
- Task 1: Updated Game.kt to match story specification (id, title, thumbnailUrl, createdAt, lastAccessedAt). Removed kotlinx.serialization dependency to keep domain models pure Kotlin. Added GameTest.kt with unit tests for data class functionality.
- Task 2: Created RuleSection.kt with title, content, and optional items list with null default. Added RuleSectionTest.kt with comprehensive tests.
- Task 3: Created Rules.kt with gameId and four RuleSection properties (overview, setup, firstRound, advanced). Added RulesTest.kt with tests for composition and equality.
- Task 4: Created ScanResult.kt with gameTitle, confidence, and optional thumbnailUrl. Added ScanResultTest.kt with tests for float confidence and nullable thumbnail.
- Task 5: Created mapper extension functions in core/database (GameMapper, RulesMapper) and core/network (ScanResultMapper). Mappers convert between entities/responses and domain models. Added kotlin.serialization plugin to database module for JSON serialization of RuleSection.

- All 4 domain models implemented as pure Kotlin data classes
- Mappers created for Entity ↔ Domain conversion
- JSON serialization for RuleSection storage in database
- Comprehensive unit tests for all models and mappers

### File List
- core/model/src/main/kotlin/com/rulebook/core/model/Game.kt (modified)
- core/model/src/main/kotlin/com/rulebook/core/model/RuleSection.kt (new)
- core/model/src/main/kotlin/com/rulebook/core/model/Rules.kt (new)
- core/model/src/main/kotlin/com/rulebook/core/model/ScanResult.kt (new)
- core/model/src/test/kotlin/com/rulebook/core/model/GameTest.kt (new)
- core/model/src/test/kotlin/com/rulebook/core/model/RuleSectionTest.kt (new)
- core/model/src/test/kotlin/com/rulebook/core/model/RulesTest.kt (new)
- core/model/src/test/kotlin/com/rulebook/core/model/ScanResultTest.kt (new)
- core/model/build.gradle.kts (modified)
- core/database/src/main/kotlin/com/rulebook/core/database/mapper/GameMapper.kt (new)
- core/database/src/main/kotlin/com/rulebook/core/database/mapper/RulesMapper.kt (new)
- core/database/src/test/kotlin/com/rulebook/core/database/mapper/GameMapperTest.kt (new)
- core/database/src/test/kotlin/com/rulebook/core/database/mapper/RulesMapperTest.kt (new)
- core/database/build.gradle.kts (modified)
- core/network/src/main/kotlin/com/rulebook/core/network/mapper/ScanResultMapper.kt (new)
- core/network/src/test/kotlin/com/rulebook/core/network/mapper/ScanResultMapperTest.kt (new)

**Domain Models (core/model):**
- `core/model/src/main/kotlin/com/rulebook/core/model/Game.kt`
- `core/model/src/main/kotlin/com/rulebook/core/model/RuleSection.kt`
- `core/model/src/main/kotlin/com/rulebook/core/model/Rules.kt`
- `core/model/src/main/kotlin/com/rulebook/core/model/ScanResult.kt`

**Model Tests:**
- `core/model/src/test/kotlin/com/rulebook/core/model/GameTest.kt`
- `core/model/src/test/kotlin/com/rulebook/core/model/RuleSectionTest.kt`
- `core/model/src/test/kotlin/com/rulebook/core/model/RulesTest.kt`
- `core/model/src/test/kotlin/com/rulebook/core/model/ScanResultTest.kt`

**Database Mappers:**
- `core/database/src/main/kotlin/com/rulebook/core/database/mapper/GameMapper.kt`
- `core/database/src/main/kotlin/com/rulebook/core/database/mapper/RulesMapper.kt`

**Mapper Tests:**
- `core/database/src/test/kotlin/com/rulebook/core/database/mapper/GameMapperTest.kt`
- `core/database/src/test/kotlin/com/rulebook/core/database/mapper/RulesMapperTest.kt`

**Network Mapper:**
- `core/network/src/main/kotlin/com/rulebook/core/network/mapper/ScanResultMapper.kt`
- `core/network/src/test/kotlin/com/rulebook/core/network/mapper/ScanResultMapperTest.kt`

**Build Config:**
- `core/model/build.gradle.kts`
- `core/database/build.gradle.kts`

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.4, Story 1.5, Story 1.6, Story 1.7, Story 1.13

### Dependency Rationale
- Story 1.1: Domain models require model module to exist
