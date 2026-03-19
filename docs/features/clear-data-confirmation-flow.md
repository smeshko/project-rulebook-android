# Clear Data Confirmation Flow

**Date:** 2026-03-19
**Related Story:** RULE-238 (Story 9.5: Clear App Data with Confirmation)
**Related Files:** SettingsViewModel, SettingsScreen, ClearDataConfirmationDialog, RulebookPreferences, RulebookDatabase

## Overview

This document describes the architectural patterns and implementation approach for destructive operations (data clearing) in the Rulebook Android app. The implementation demonstrates how to safely orchestrate multi-step data destruction with confirmation, error handling, and user feedback using Jetpack Compose and Kotlin coroutines.

## What Was Built

The clear data feature enables users to reset the app to a fresh state while preserving their purchased credit balance:

- **Confirmation Dialog** — Brutalist-styled `AlertDialog` warning of consequences ("CLEAR ALL DATA?")
- **Database Clearing** — Room table wipe via `RulebookDatabase.clearAllTables()`
- **Preferences Reset** — DataStore reset via `RulebookPreferences.reset()` (preserves credit balance)
- **Snackbar Feedback** — Success/error messages with 2-second auto-dismiss
- **Back Stack Navigation** — Full-stack clearing back to onboarding (`popUpTo(0) { inclusive = true }`)
- **Error Recovery** — Granular error handling prevents partial data loss (db clear fails → preferences not reset)

## Technical Implementation

### Key Architectural Decision: Lambda Pattern vs Interface

**Why not an interface?**

The initial design used a `ClearableDatabase` interface:

```kotlin
interface ClearableDatabase {
    fun clearAllTables()
}

abstract class RulebookDatabase : RoomDatabase(), ClearableDatabase { ... }
```

**Problem:** Room's KSP annotation processor treats interface methods like `clearAllTables()` as DAO getter methods in `@Database` classes, causing build failures.

**Solution:** Use a suspend lambda instead, wrapped in the Koin module:

```kotlin
class SettingsViewModel(
    ...
    private val clearDatabase: suspend () -> Unit,
    ...
)
```

**Benefits:**
- Avoids Room KSP conflicts
- Still enables testing (easy to mock)
- Cleaner than working around KSP with wrapper interfaces
- Reduces module coupling

**When to apply this pattern:**
- When you need to abstract a blocking Room method
- When an interface would be simpler but causes KSP issues
- When the abstraction is used in only one place (ViewModel, service)

### Selective DataStore Reset Pattern

The `ResettablePreferences` interface preserves credit balance during reset:

```kotlin
interface ResettablePreferences {
    suspend fun reset()
}

class RulebookPreferences(
    ...
) : ResettablePreferences {
    override suspend fun reset() {
        val preservedBalance = prefs[CREDIT_BALANCE].first()
        prefs.clear()
        prefs[CREDIT_BALANCE] = preservedBalance
    }
}
```

**Why this pattern?**
- Allows selective preservation of critical user data
- Separates reset logic from preference access
- Easily testable with `FakeResettablePreferences`
- Future-proof for additional preserve-on-reset fields

### Orchestrated Multi-Step Operation

The `onClearData()` flow in `SettingsViewModel` demonstrates proper error handling:

```kotlin
fun onClearData() {
    _uiState.update { it.copy(showClearConfirmation = false) }
    viewModelScope.launch {
        try {
            clearDatabase()  // Blocking room call via IO dispatcher
            resettablePreferences.reset()  // Async preference reset
            _events.send(SettingsEvent.ShowSnackbar("All data cleared"))
            _events.send(SettingsEvent.NavigateToOnboarding)
        } catch (e: Exception) {
            _events.send(SettingsEvent.ShowSnackbar("Failed to clear data"))
            // Note: Navigation NOT emitted on error
        }
    }
}
```

**Key behaviors:**
1. Dialog dismissed immediately (UX feedback)
2. Database cleared first (most likely to fail)
3. Preferences reset skipped if DB clear fails (prevents partial state)
4. Success → Show snackbar → Navigate
5. Error → Show error snackbar → No navigation

### Confirmation Dialog Styling

Modeled after `DeleteConfirmationDialog`, uses brutalist design system:

```kotlin
AlertDialog(
    shape = RectangleShape,
    modifier = Modifier
        .clip(RectangleShape)
        .then(brutalistShadow())
        .then(brutalistBorder()),
    title = { Text("CLEAR ALL DATA?", style = BrutalistTypography.headline) },
    text = { Text("This will delete all saved games and reset settings") },
    confirmButton = { Button("Clear Data", destructive=true) },
    dismissButton = { Button("Cancel", secondary=true) }
)
```

### Navigation Back Stack Clearing

Clear all previous screens to prevent navigation back to stale data:

```kotlin
onNavigateToOnboarding = {
    navController.navigate(Route.Onboarding) {
        popUpTo(0) { inclusive = true }  // Clear entire back stack
    }
}
```

## How to Use

### Adding Clear Data to a Feature

1. **Add ViewModel parameters:**
   ```kotlin
   class YourViewModel(
       private val clearDatabase: suspend () -> Unit,
       private val resettablePreferences: ResettablePreferences
   )
   ```

2. **Update Koin module:**
   ```kotlin
   val yourModule = module {
       viewModel {
           val database = get<RulebookDatabase>()
           YourViewModel(
               clearDatabase = { withContext(Dispatchers.IO) { database.clearAllTables() } },
               resettablePreferences = get<ResettablePreferences>()
           )
       }
   }
   ```

3. **Implement orchestration:**
   - Dialog state: `showClearConfirmation: Boolean` in UiState
   - Dialog event: `data object OnClearDataConfirmed : Event`
   - Handler: `onClearData()` with try/catch and event emission

4. **Wire UI:**
   - Show dialog when `uiState.showClearConfirmation == true`
   - Handle `NavigateToOnboarding` event
   - Show snackbar on `ShowSnackbar` event
   - Apply haptic feedback on confirmation

### Testing Clear Data Operations

Use fake implementations:

```kotlin
class FakeResettablePreferences(
    private val shouldThrow: Boolean = false
) : ResettablePreferences {
    var resetCalled = false
    override suspend fun reset() {
        if (shouldThrow) throw RuntimeException("Preferences error")
        resetCalled = true
    }
}

@Test
fun `onClearData emits ShowSnackbar then NavigateToOnboarding on success`() = runTest {
    val viewModel = createViewModel()
    val events = mutableListOf<Event>()

    viewModel.onClearData()
    advanceUntilIdle()

    // Verify event order and content
    assertTrue("ShowSnackbar should be first", events[0] is ShowSnackbar)
    assertTrue("NavigateToOnboarding should be second", events[1] is NavigateToOnboarding)
}
```

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| Clear preserve credits | Boolean | true | Whether credit balance survives reset |
| Back stack pop strategy | PopUpTo | `popUpTo(0) { inclusive = true }` | Clear entire back stack or partial |
| Error snackbar message | String | "Failed to clear data" | User-facing error message |
| Success snackbar duration | Duration | 2 seconds | Auto-dismiss timing |

## Notes

### Limitations

- `clearAllTables()` is a **blocking call** — must be invoked from `Dispatchers.IO` context
- Credit balance preservation is hardcoded — extending to other fields requires modifying `RulebookPreferences.reset()`
- Dialog styling is coupled to brutalist design system — cannot be reused in other design contexts without adaptation

### Future Considerations

- **Incremental clears** — Clear only games or only settings without full reset
- **Selective data preservation** — User-configurable which data to preserve
- **Cloud sync** — Sync clear action to cloud backup before local wipe
- **Audit logging** — Log clear operations for user support / analytics
- **Undo functionality** — Store local backup for 24-hour restore window

### Related Patterns

- **Confirmation before destructive action** — See DeleteConfirmationDialog pattern
- **Snackbar error handling** — See LibraryScreen for SnackbarHost integration
- **Navigation with back stack control** — See RulebookNavHost for `popUpTo` examples
- **Room database migration** — Related to `clearAllTables()` for testing
