# Story 1.4: DataStore Preferences Setup

Status: Done

## Linear Issue

- **ID:** RULE-111
- **URL:** https://linear.app/project-rulebook/issue/RULE-111/story-14-datastore-preferences-setup

## Story

As a developer,
I want DataStore configured for user preferences,
So that settings and state persist across app sessions.

## Acceptance Criteria

1. **Given** the `core/datastore` module
   **When** DataStore is configured
   **Then** `RulebookPreferences.kt` provides access to:
   - `hasCompletedOnboarding: Flow<Boolean>`
   - `creditBalance: Flow<Int>`
   - `themeMode: Flow<ThemeMode>` (LIGHT, DARK, SYSTEM)
   - `hapticsEnabled: Flow<Boolean>`

2. **And** each preference has suspend functions to update

3. **And** default values are sensible:
   - onboarding = false
   - credits = 0
   - theme = SYSTEM
   - haptics = true

## Tasks / Subtasks

- [x] Task 1: Add DataStore dependencies (AC: #1)
  - [x] Add datastore-preferences to version catalog
  - [x] Add dependency to core/datastore module
- [x] Task 2: Create ThemeMode enum (AC: #1)
  - [x] Create ThemeMode enum (LIGHT, DARK, SYSTEM)
  - [x] Place in core/datastore or core/model
- [x] Task 3: Create PreferenceKeys object (AC: #1, #3)
  - [x] Define ONBOARDING_COMPLETED key (Boolean, default false)
  - [x] Define CREDIT_BALANCE key (Int, default 0)
  - [x] Define THEME_MODE key (String/Int, default SYSTEM)
  - [x] Define HAPTICS_ENABLED key (Boolean, default true)
- [x] Task 4: Create RulebookPreferences class (AC: #1, #2)
  - [x] Create class with DataStore<Preferences> dependency
  - [x] Implement hasCompletedOnboarding Flow
  - [x] Implement creditBalance Flow
  - [x] Implement themeMode Flow
  - [x] Implement hapticsEnabled Flow
  - [x] Add suspend fun setOnboardingCompleted(value: Boolean)
  - [x] Add suspend fun setCreditBalance(value: Int)
  - [x] Add suspend fun setThemeMode(mode: ThemeMode)
  - [x] Add suspend fun setHapticsEnabled(enabled: Boolean)
- [x] Task 5: Create DataStore instance (AC: #1)
  - [x] Create Context.rulebookDataStore extension
  - [x] Configure with PreferencesDataStoreFactory
- [x] Task 6: Add DataStore to Koin DI
  - [x] Create provideDataStore function
  - [x] Create provideRulebookPreferences function
  - [x] Register in DataStoreModule

## Dev Notes

### Architecture Patterns

- Use Preferences DataStore (not Proto) for simplicity
- Expose as Flow for reactive updates
- Repository pattern wraps DataStore access

### Implementation Pattern

```kotlin
class RulebookPreferences(
    private val dataStore: DataStore<Preferences>
) {
    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] ?: false
        }

    val creditBalance: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[PreferenceKeys.CREDIT_BALANCE] ?: 0
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setCreditBalance(balance: Int) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.CREDIT_BALANCE] = balance
        }
    }
}

private object PreferenceKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val CREDIT_BALANCE = intPreferencesKey("credit_balance")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
}
```

### iOS Mapping

| iOS Concept | Android Equivalent |
|-------------|-------------------|
| UserDefaults | DataStore |
| @AppStorage | Flow<T> with DataStore |

### PRD Requirements Mapped

- FR4: Remember onboarding completion
- FR34: Track remaining scan credits
- FR42: Theme selection persistence
- FR43: Haptic feedback preference
- NFR9: Credit balance persists across app restarts

### Project Structure Notes

- DataStore module provides reactive preference access
- Use Flow for reactive UI updates
- Suspend functions for writes ensure thread safety

### References

- [Source: docs/architecture.md#Data Architecture]
- [Source: docs/architecture.md#Core Module Responsibilities]
- [Source: docs/prd.md#FR4] - Remember onboarding completion
- [Source: docs/prd.md#FR34] - Track remaining scan credits
- [Source: docs/prd.md#FR42] - Theme selection
- [Source: docs/prd.md#FR43] - Haptic feedback toggle

## Dev Agent Record

### Context Reference
- Story 1.4: DataStore Preferences Setup
- Linear Issue: RULE-111

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
N/A

### Completion Notes List
- Implemented ThemeMode enum with LIGHT, DARK, SYSTEM values and fromString() parsing
- Updated RulebookPreferences with all required Flow properties and suspend setters
- PreferenceKeys object implemented as private nested object in RulebookPreferences
- DataStore instance created via Context extension property (preferencesDataStore delegate)
- Configured Koin DataStoreModule to provide RulebookPreferences singleton
- Added unit tests for ThemeMode enum covering all parsing scenarios
- All acceptance criteria satisfied with sensible defaults

### File List
- core/datastore/build.gradle.kts (modified - added koin-android, test, and androidTest dependencies)
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/ThemeMode.kt (new - with whitespace trimming in fromString())
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/RulebookPreferences.kt (modified - with KDoc, input validation, internal Keys)
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/di/DataStoreModule.kt (modified)
- core/datastore/src/test/kotlin/com/rulebook/core/datastore/ThemeModeTest.kt (new - with whitespace tests)
- core/datastore/src/test/kotlin/com/rulebook/core/datastore/RulebookPreferencesTest.kt (new - unit tests for Keys and validation)
- core/datastore/src/androidTest/kotlin/com/rulebook/core/datastore/RulebookPreferencesIntegrationTest.kt (new - instrumented tests)

### Code Review Fixes Applied
- **H1 Fixed**: Added RulebookPreferencesTest.kt (unit tests) and RulebookPreferencesIntegrationTest.kt (instrumented tests)
- **M1 Fixed**: Added input validation to setCreditBalance() - negative values coerced to 0
- **M3 Fixed**: Created androidTest directory with RulebookPreferencesIntegrationTest.kt
- **M4 Fixed**: Added trim() to ThemeMode.fromString() for whitespace handling
- **L1 Fixed**: Changed PreferenceKeys from private to internal for testing
- **L2 Fixed**: Added comprehensive KDoc documentation to RulebookPreferences class

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.5, Story 1.6, Story 1.7, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: DataStore setup requires datastore module to exist
