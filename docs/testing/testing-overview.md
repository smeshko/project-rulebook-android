---
title: Testing Overview
description: Testing strategy, patterns, and coverage for project-rulebook-android
author: Ivo
date: 2026-01-25
---

# Testing Overview

Testing strategy and patterns for the Rulebook Android application.

---

## Test Structure

| Type | Location | Runner | Purpose |
|------|----------|--------|---------|
| Unit Tests | `src/test/kotlin/` | `./gradlew test` | JVM tests (fast, no Android) |
| Instrumented | `src/androidTest/kotlin/` | `./gradlew connectedAndroidTest` | Android tests (device/emulator) |

**Total: 45 test files** across all modules.

---

## Coverage by Module

### Core Modules (Well-Tested)

| Module | Unit Tests | Android Tests | Coverage |
|--------|------------|---------------|----------|
| `core/analytics` | 3 | - | Event tracking, screen views |
| `core/database` | 2 | 2 | CRUD, sorting, cascades |
| `core/datastore` | 2 | 1 | Preferences, theme modes |
| `core/network` | 3 | - | API client, DTOs, mappers |
| `core/common` | 1 | - | Result<T> wrapper |
| `core/model` | 4 | - | Domain models |
| `core/designsystem` | 8 | - | Theme tokens, components |

### Feature Modules

| Module | Unit Tests | Android Tests | Coverage |
|--------|------------|---------------|----------|
| `feature/camera` | 2 | 4 | ViewModel (40+ cases), permissions UI |
| `feature/library` | 1 | - | ViewModel states, refresh |
| `feature/settings` | 1 | - | ViewModel, toggles |
| `feature/onboarding` | 3 | - | ViewModel, page indicator |
| `feature/rules` | 0 | - | **Gap** |
| `feature/purchase` | 0 | - | **Gap** |

### App Module

| Area | Tests | Coverage |
|------|-------|----------|
| Navigation | 6 | Routes, deep links, bottom bar |
| Startup | 1 | App initialization |

---

## Testing Patterns

### 1. Fake Implementations (No Mocking Libraries)

Instead of Mockito/MockK, the project uses hand-crafted fakes:

```kotlin
// Example: FakeGameRepository
class FakeGameRepository(
    private var games: List<Game> = emptyList(),
    private var error: String? = null
) : GameRepository {
    override suspend fun getGames(): Result<List<Game>> {
        return error?.let { Result.Error(it) } ?: Result.Success(games)
    }
}
```

**Locations:**
- `FakeAnalyticsManager` - core/analytics tests
- `FakeCreditRepository` - feature/camera tests
- `FakeGameRepository` - feature/library tests

### 2. Coroutine Testing

Two dispatcher patterns used:

**UnconfinedTestDispatcher** (immediate execution):
```kotlin
private val testDispatcher = UnconfinedTestDispatcher()

@Before fun setup() { Dispatchers.setMain(testDispatcher) }
@After fun tearDown() { Dispatchers.resetMain() }

@Test fun test() = runTest { /* state changes immediately */ }
```

**StandardTestDispatcher** (controlled advancement):
```kotlin
@Test fun `loading state observable`() = runTest(testDispatcher) {
    viewModel.refresh()
    testDispatcher.scheduler.runCurrent()  // Observe loading
    assertTrue(viewModel.uiState.value.isRefreshing)
    advanceUntilIdle()  // Complete
}
```

### 3. Room Database Testing

In-memory databases for fast, isolated tests:

```kotlin
database = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(),
    RulebookDatabase::class.java
).allowMainThreadQueries().build()
```

### 4. Compose UI Testing

```kotlin
@get:Rule val composeTestRule = createComposeRule()

@Test fun permissionDenied_showsMessage() {
    composeTestRule.setContent { PermissionDenied() }
    composeTestRule
        .onNodeWithText("Camera Permission Denied")
        .assertIsDisplayed()
}
```

---

## Test Frameworks

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Core test framework |
| kotlin-test | 2.0.21 | Kotlin assertions |
| kotlinx-coroutines-test | 1.9.0 | Coroutine testing |
| androidx.test.core | 1.6.1 | Android test utilities |
| compose-ui-test-junit4 | BOM 2024.11.00 | Compose UI testing |
| room-testing | 2.6.1 | Room test utilities |

**Note:** No mocking libraries (MockK, Mockito) - uses fakes exclusively.

---

## Running Tests

```bash
# All unit tests
./gradlew test

# Specific module unit tests
./gradlew :core:database:test
./gradlew :feature:camera:test

# All instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Specific module instrumented tests
./gradlew :core:database:connectedAndroidTest

# With coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

---

## Test Naming Convention

Uses backtick-quoted descriptive names:

```kotlin
@Test
fun `initial state has camera not ready and no error`() { ... }

@Test
fun `onCameraReady sets isCameraReady to true`() { ... }

@Test
fun `refresh shows loading state then completes`() { ... }
```

---

## Coverage Gaps

| Area | Status | Notes |
|------|--------|-------|
| `feature/rules` | No tests | Rules display logic untested |
| `feature/purchase` | No tests | Billing flow untested |
| Network integration | Partial | No MockWebServer tests |
| End-to-end navigation | Missing | Only structural tests |
| CameraX hardware | Skipped | Requires physical device |

---

## Adding New Tests

### Unit Test (ViewModel)

1. Create `{Feature}ViewModelTest.kt` in `src/test/kotlin/`
2. Create fake repository implementation
3. Use `runTest` with appropriate dispatcher
4. Follow Given-When-Then structure

### Instrumented Test (Room/Compose)

1. Create `{Class}Test.kt` in `src/androidTest/kotlin/`
2. Use `@get:Rule` for test rules
3. Use in-memory database or `createComposeRule()`
4. Clean up in `@After` method
