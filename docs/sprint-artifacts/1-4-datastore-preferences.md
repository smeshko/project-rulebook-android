# Story 1.4: DataStore Preferences Setup

Status: ready-for-dev

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

- [ ] Task 1: Add DataStore dependencies (AC: #1)
  - [ ] Add datastore-preferences to version catalog
  - [ ] Add dependency to core/datastore module
- [ ] Task 2: Create ThemeMode enum (AC: #1)
  - [ ] Create ThemeMode enum (LIGHT, DARK, SYSTEM)
  - [ ] Place in core/datastore or core/model
- [ ] Task 3: Create PreferenceKeys object (AC: #1, #3)
  - [ ] Define ONBOARDING_COMPLETED key (Boolean, default false)
  - [ ] Define CREDIT_BALANCE key (Int, default 0)
  - [ ] Define THEME_MODE key (String/Int, default SYSTEM)
  - [ ] Define HAPTICS_ENABLED key (Boolean, default true)
- [ ] Task 4: Create RulebookPreferences class (AC: #1, #2)
  - [ ] Create class with DataStore<Preferences> dependency
  - [ ] Implement hasCompletedOnboarding Flow
  - [ ] Implement creditBalance Flow
  - [ ] Implement themeMode Flow
  - [ ] Implement hapticsEnabled Flow
  - [ ] Add suspend fun setOnboardingCompleted(value: Boolean)
  - [ ] Add suspend fun setCreditBalance(value: Int)
  - [ ] Add suspend fun setThemeMode(mode: ThemeMode)
  - [ ] Add suspend fun setHapticsEnabled(enabled: Boolean)
- [ ] Task 5: Create DataStore instance (AC: #1)
  - [ ] Create Context.rulebookDataStore extension
  - [ ] Configure with PreferencesDataStoreFactory
- [ ] Task 6: Add DataStore to Koin DI
  - [ ] Create provideDataStore function
  - [ ] Create provideRulebookPreferences function
  - [ ] Register in DataStoreModule

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

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 1.1
- **Blocks:** None
- **Can Parallel With:** Story 1.2, Story 1.3, Story 1.5, Story 1.6, Story 1.7, Story 1.12, Story 1.13

### Dependency Rationale
- Story 1.1: DataStore setup requires datastore module to exist
