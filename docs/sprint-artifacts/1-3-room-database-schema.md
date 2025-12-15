# Story 1.3: Room Database Schema & DAOs

Status: Done

## Linear Issue

- **ID:** RULE-110
- **URL:** https://linear.app/project-rulebook/issue/RULE-110/story-13-room-database-schema-and-daos

## Story

As a developer,
I want the Room database configured with entity schemas,
So that game and rules data can be persisted offline.

## Acceptance Criteria

1. **Given** the `core/database` module
   **When** Room is configured
   **Then** `RulebookDatabase.kt` defines the database with entities:
   - `GameEntity` (id, title, thumbnailUrl, createdAt, lastAccessedAt)
   - `RulesEntity` (id, gameId, overview, setup, firstRound, advanced, rawJson)

2. **And** DAOs exist for each entity:
   - `GameDao` with insert, update, delete, getAll, getById, getAllSorted
   - `RulesDao` with insert, getByGameId, deleteByGameId

3. **And** table names use `snake_case` (e.g., `saved_games`)

4. **And** timestamps stored as `Long` (milliseconds)

5. **And** JSON data stored as `String` with TypeConverters

## Tasks / Subtasks

- [x] Task 1: Add Room dependencies (AC: #1)
  - [x] Add room-runtime to version catalog
  - [x] Add room-ktx to version catalog
  - [x] Add room-compiler for KSP
  - [x] Add KSP plugin to build configuration
- [x] Task 2: Create GameEntity (AC: #1, #3, #4)
  - [x] Create entity class with @Entity annotation
  - [x] Set tableName = "saved_games"
  - [x] Define id as @PrimaryKey
  - [x] Add title, thumbnailUrl, createdAt, lastAccessedAt columns
  - [x] Use Long for timestamps
- [x] Task 3: Create RulesEntity (AC: #1, #3, #5)
  - [x] Create entity class with @Entity annotation
  - [x] Set tableName = "rules"
  - [x] Define id as @PrimaryKey
  - [x] Add gameId with @ForeignKey to GameEntity (CASCADE delete)
  - [x] Add overview, setup, firstRound, advanced columns as String
  - [x] Add rawJson column for complete JSON storage
- [x] Task 4: Create GameDao (AC: #2)
  - [x] Create @Dao interface
  - [x] Add @Insert(onConflict = REPLACE) insert method
  - [x] Add @Update update method
  - [x] Add @Delete delete method
  - [x] Add @Query getAll returning Flow<List<GameEntity>>
  - [x] Add @Query getById returning Flow<GameEntity?>
  - [x] Add sorted query methods (by title, createdAt, lastAccessedAt) - Note: Uses separate type-safe methods instead of dynamic ORDER BY for compile-time query validation
- [x] Task 5: Create RulesDao (AC: #2)
  - [x] Create @Dao interface
  - [x] Add @Insert insert method
  - [x] Add @Query getByGameId returning Flow<RulesEntity?>
  - [x] Add @Query deleteByGameId
- [x] Task 6: Create RulebookDatabase (AC: #1)
  - [x] Create @Database abstract class
  - [x] Register GameEntity and RulesEntity
  - [x] Set version = 1
  - [x] Add abstract gameDao() method
  - [x] Add abstract rulesDao() method
- [x] Task 7: TypeConverters Assessment (AC: #5)
  - [x] Assessed TypeConverter needs - JSON stored as raw String, timestamps as Long
  - [x] No TypeConverters required - all types are natively supported by Room
  - [x] Documented decision in RulebookDatabase KDoc
- [x] Task 8: Add Database to Koin DI
  - [x] Create provideDatabase function with fallbackToDestructiveMigration
  - [x] Create provideGameDao function
  - [x] Create provideRulesDao function
  - [x] Register in DatabaseModule

## Dev Notes

### Architecture Patterns

- Room 2.6.x with KSP for annotation processing
- Use `@Transaction` for complex queries
- Primary keys named `id`
- Foreign key from rules to games with CASCADE delete

### Database Conventions

| Convention | Value |
|------------|-------|
| Table names | `lowercase_plural` (saved_games) |
| Column names | `snake_case` |
| Primary key | Always named `id` |
| Timestamps | `Long` (milliseconds) |
| JSON data | Stored as `String` |

### Entity Schema

```kotlin
@Entity(tableName = "saved_games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "last_accessed_at") val lastAccessedAt: Long
)

@Entity(
    tableName = "rules",
    foreignKeys = [ForeignKey(
        entity = GameEntity::class,
        parentColumns = ["id"],
        childColumns = ["game_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RulesEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    val overview: String,
    val setup: String,
    @ColumnInfo(name = "first_round") val firstRound: String,
    val advanced: String,
    @ColumnInfo(name = "raw_json") val rawJson: String
)
```

### Project Structure Notes

- Database module has no UI dependencies
- Entities are internal to database module
- Only expose DAOs and Database class
- Mapper functions convert between Entity and Domain models

### References

- [Source: docs/architecture.md#Data Architecture]
- [Source: docs/architecture.md#Database Conventions]
- [Source: docs/prd.md#NFR11] - No data loss on app termination

## Dev Agent Record

### Context Reference
- Story 1.3: Room Database Schema & DAOs (RULE-110)

### Agent Model Used
- Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Build warnings resolved: Added koin-android dependency for androidContext() usage
- Schema export configured: Added KSP arg for room.schemaLocation
- Fixed app module dependencies: Added core:database, core:datastore, core:network

### Completion Notes List
- Implemented GameEntity with snake_case column names (saved_games table)
- Implemented RulesEntity with foreign key to GameEntity (CASCADE delete) and index on game_id
- Created GameDao with CRUD operations and multiple sorted query methods (Flow-based)
- Created RulesDao with insert, update, getByGameId, deleteByGameId operations
- Configured RulebookDatabase with both entities (no TypeConverters needed)
- Configured Koin DI module with database and DAO singletons
- Added unit tests for GameEntity and RulesEntity
- Added instrumented tests for GameDao and RulesDao (including CASCADE delete verification)
- All acceptance criteria satisfied

### Code Review Fixes (2025-12-15)
- Removed unused Converters.kt (Date converters were never used)
- Removed @TypeConverters annotation from RulebookDatabase
- Added @Update method to RulesDao for explicit update semantics
- Added fallbackToDestructiveMigration() for dev build safety
- Added KDoc documentation to all DAO methods
- Added Room testing dependencies for instrumented tests
- Created comprehensive DAO instrumented tests (GameDaoTest, RulesDaoTest)

### File List
- gradle/libs.versions.toml (modified - added room-testing, test dependencies)
- core/database/build.gradle.kts (modified - added koin-android, testing dependencies, schema location)
- core/database/src/main/kotlin/com/rulebook/core/database/entity/GameEntity.kt (modified)
- core/database/src/main/kotlin/com/rulebook/core/database/entity/RulesEntity.kt (created)
- core/database/src/main/kotlin/com/rulebook/core/database/GameDao.kt (modified - added KDoc)
- core/database/src/main/kotlin/com/rulebook/core/database/RulesDao.kt (created - with @Update method)
- core/database/src/main/kotlin/com/rulebook/core/database/RulebookDatabase.kt (modified - removed TypeConverters)
- core/database/src/main/kotlin/com/rulebook/core/database/di/DatabaseModule.kt (modified - added fallbackToDestructiveMigration)
- core/database/src/test/kotlin/com/rulebook/core/database/entity/GameEntityTest.kt (created)
- core/database/src/test/kotlin/com/rulebook/core/database/entity/RulesEntityTest.kt (created)
- core/database/src/androidTest/kotlin/com/rulebook/core/database/GameDaoTest.kt (created)
- core/database/src/androidTest/kotlin/com/rulebook/core/database/RulesDaoTest.kt (created)
- core/database/schemas/com.rulebook.core.database.RulebookDatabase/1.json (created - schema export)
- app/build.gradle.kts (modified - added database, datastore, network dependencies)

## Change Log
- 2025-12-05: Implemented Room database schema with GameEntity, RulesEntity, DAOs, and Koin DI integration
- 2025-12-15: Code review fixes - removed unused TypeConverters, added RulesDao.update(), migration strategy, KDoc, and proper DAO instrumented tests
