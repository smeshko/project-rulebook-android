# Story 1.3: Room Database Schema & DAOs

Status: ready-for-dev

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

- [ ] Task 1: Add Room dependencies (AC: #1)
  - [ ] Add room-runtime to version catalog
  - [ ] Add room-ktx to version catalog
  - [ ] Add room-compiler for KSP
  - [ ] Add KSP plugin to build configuration
- [ ] Task 2: Create GameEntity (AC: #1, #3, #4)
  - [ ] Create entity class with @Entity annotation
  - [ ] Set tableName = "saved_games"
  - [ ] Define id as @PrimaryKey
  - [ ] Add title, thumbnailUrl, createdAt, lastAccessedAt columns
  - [ ] Use Long for timestamps
- [ ] Task 3: Create RulesEntity (AC: #1, #3, #5)
  - [ ] Create entity class with @Entity annotation
  - [ ] Set tableName = "rules"
  - [ ] Define id as @PrimaryKey
  - [ ] Add gameId with @ForeignKey to GameEntity (CASCADE delete)
  - [ ] Add overview, setup, firstRound, advanced columns as String
  - [ ] Add rawJson column for complete JSON storage
- [ ] Task 4: Create GameDao (AC: #2)
  - [ ] Create @Dao interface
  - [ ] Add @Insert(onConflict = REPLACE) insert method
  - [ ] Add @Update update method
  - [ ] Add @Delete delete method
  - [ ] Add @Query getAll returning Flow<List<GameEntity>>
  - [ ] Add @Query getById returning Flow<GameEntity?>
  - [ ] Add @Query getAllSorted with ORDER BY parameter
- [ ] Task 5: Create RulesDao (AC: #2)
  - [ ] Create @Dao interface
  - [ ] Add @Insert insert method
  - [ ] Add @Query getByGameId returning Flow<RulesEntity?>
  - [ ] Add @Query deleteByGameId
- [ ] Task 6: Create RulebookDatabase (AC: #1)
  - [ ] Create @Database abstract class
  - [ ] Register GameEntity and RulesEntity
  - [ ] Set version = 1
  - [ ] Add abstract gameDao() method
  - [ ] Add abstract rulesDao() method
- [ ] Task 7: Create TypeConverters (AC: #5)
  - [ ] Create Converters class for JSON handling
  - [ ] Add @TypeConverter for any complex types
- [ ] Task 8: Add Database to Koin DI
  - [ ] Create provideDatabase function
  - [ ] Create provideGameDao function
  - [ ] Create provideRulesDao function
  - [ ] Register in DatabaseModule

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

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
