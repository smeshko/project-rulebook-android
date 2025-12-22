# Story 5.1: Scan Flow Initiation & Credit Check

Status: ready-for-dev

## Story

As a user,
I want the scan to check my credits before proceeding,
so that I don't waste time if I can't complete the scan.

## Acceptance Criteria

1. **Given** the user captures/selects a photo
   **When** initiating the scan flow
   **Then** credit balance is checked
   **And** if credits > 0, proceed to image analysis
   **And** if credits = 0, show paywall (FR38, handled in Epic 8)

2. **Given** the credit check is performed
   **When** the user has sufficient credits
   **Then** credit is NOT deducted until scan succeeds

## Tasks / Subtasks

- [x] Task 1: Create ScanFlow composable (AC: #1)
  - [x] Create `ScanFlowScreen.kt` in `feature/scan/` module
  - [x] Define `ScanFlowUiState` sealed class with states
  - [x] Set up navigation entry point with captured image parameter

- [x] Task 2: Implement Credit Check Logic in ViewModel (AC: #1, #2)
  - [x] Create `ScanFlowViewModel` injecting `CreditRepository`
  - [x] Check credit balance on initialization
  - [x] Navigate to paywall route if credits == 0
  - [x] Proceed to analysis phase if credits > 0
  - [x] Do NOT deduct credit yet (deduction in Story 5.7)

- [x] Task 3: Handle Navigation from Camera (AC: #1)
  - [x] Update `CameraViewModel` to navigate to scan flow after capture
  - [x] Pass captured/compressed image URI to scan flow
  - [x] Ensure image is compressed before passing (from Story 4.7)

- [x] Task 4: Implement Paywall Navigation (AC: #1)
  - [x] Define paywall route in navigation graph
  - [x] Navigate to paywall route when credits == 0
  - [x] Note: Actual paywall UI is in Epic 8

- [x] Task 5: Add Analytics Event (Related to Story 5.10)
  - [x] Track `scan_started` event with TelemetryDeck
  - [x] Include properties: source (camera/gallery), credit_balance

- [ ] Task 6: Write Unit Tests (AC: #1, #2)
  - [ ] Test credit check logic with various balances
  - [ ] Test navigation to paywall when credits == 0
  - [ ] Test proceeding to analysis when credits > 0
  - [ ] Test that credits are NOT deducted

## Dev Notes

### Critical Implementation Context

This story is the **gateway to the entire scan flow**. It's the first story in Epic 5 and initiates the Photo → Rules → Play pipeline that is the core value proposition of Rulebook.

**Key Decision Points:**
1. **Credit check happens BEFORE any network calls** - prevents wasting user time and server resources
2. **Credits deducted ONLY on success** (Story 5.7) - fair to user, prevents charging for failures
3. **Paywall navigation stub** - route exists but full UI in Epic 8

### Architecture Pattern: Feature Module Setup

Based on existing feature modules (camera, onboarding, rules), create new `feature/scan` module:

```
feature/scan/
├── src/main/kotlin/com/rulebook/feature/scan/
│   ├── ScanFlowScreen.kt
│   ├── ScanFlowViewModel.kt
│   ├── ScanFlowUiState.kt
│   ├── navigation/
│   │   └── ScanFlowNavigation.kt
│   └── di/
│       └── ScanFlowModule.kt
└── build.gradle.kts
```

### ScanFlowUiState Definition

```kotlin
sealed interface ScanFlowUiState {
    data object CheckingCredits : ScanFlowUiState
    data object NavigatingToPaywall : ScanFlowUiState
    data class ReadyToAnalyze(
        val imageUri: Uri,
        val creditBalance: Int
    ) : ScanFlowUiState
    data class Error(val message: String) : ScanFlowUiState
}
```

### ScanFlowViewModel Pattern

```kotlin
@HiltViewModel
class ScanFlowViewModel @Inject constructor(
    private val creditRepository: CreditRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val imageUri: Uri = checkNotNull(savedStateHandle["imageUri"])

    private val _uiState = MutableStateFlow<ScanFlowUiState>(ScanFlowUiState.CheckingCredits)
    val uiState: StateFlow<ScanFlowUiState> = _uiState.asStateFlow()

    init {
        checkCreditsAndProceed()
    }

    private fun checkCreditsAndProceed() {
        viewModelScope.launch {
            creditRepository.creditBalance
                .first() // Get current balance once
                .let { balance ->
                    if (balance > 0) {
                        _uiState.value = ScanFlowUiState.ReadyToAnalyze(
                            imageUri = imageUri,
                            creditBalance = balance
                        )
                    } else {
                        _uiState.value = ScanFlowUiState.NavigatingToPaywall
                    }
                }
        }
    }
}
```

### Navigation Integration

**From Camera to Scan Flow:**
```kotlin
// In CameraViewModel after successful capture
fun onPhotoTaken(imageUri: Uri) {
    viewModelScope.launch {
        // Compression already done in Story 4.7
        navigateToScanFlow(imageUri)
    }
}
```

**Scan Flow Navigation Definition (Type-Safe):**
```kotlin
@Serializable
data class ScanFlowRoute(val imageUri: String)

// In RulebookNavHost
composable<ScanFlowRoute> { backStackEntry ->
    val route = backStackEntry.toRoute<ScanFlowRoute>()
    ScanFlowScreen(
        imageUri = Uri.parse(route.imageUri),
        onNavigateToPaywall = { navController.navigate(PaywallRoute) },
        onProceedToAnalysis = { /* Story 5.2+ */ }
    )
}
```

### Credit Repository Access Pattern

Based on Story 4.8 implementation:

```kotlin
// CreditRepository already exists and works
class CreditRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : CreditRepository {

    override val creditBalance: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[CREDIT_BALANCE_KEY] ?: 0
        }

    override suspend fun decrementCredit() {
        // NOT called in this story - Story 5.7 only
    }
}
```

**Important:** Do NOT implement credit deduction in this story. That happens in Story 5.7 after successful rules generation.

### Paywall Route Stub

Since Epic 8 implements the paywall UI, just define the navigation route:

```kotlin
@Serializable
object PaywallRoute

// In RulebookNavHost (minimal stub for now)
composable<PaywallRoute> {
    // TODO: Epic 8 - Full paywall implementation
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Paywall - Coming in Epic 8")
    }
}
```

### Analytics Integration Pattern

From Story 1.6 (TelemetryDeck integration):

```kotlin
// In ScanFlowViewModel
private fun trackScanStarted(creditBalance: Int) {
    telemetryManager.track(
        event = "scan_started",
        properties = mapOf(
            "source" to "camera", // or "gallery" if from picker
            "credit_balance" to creditBalance.toString()
        )
    )
}
```

### Testing Strategy

**ViewModel Tests:**
```kotlin
@Test
fun `when credits are zero, navigate to paywall`() = runTest {
    // Given
    val creditRepository = FakeCreditRepository(balance = 0)
    val viewModel = ScanFlowViewModel(creditRepository, savedStateHandle)

    // When
    val state = viewModel.uiState.first()

    // Then
    assertThat(state).isInstanceOf(ScanFlowUiState.NavigatingToPaywall::class.java)
}

@Test
fun `when credits are positive, proceed to analyze`() = runTest {
    // Given
    val creditRepository = FakeCreditRepository(balance = 3)
    val viewModel = ScanFlowViewModel(creditRepository, savedStateHandle)

    // When
    val state = viewModel.uiState.first()

    // Then
    assertThat(state).isInstanceOf(ScanFlowUiState.ReadyToAnalyze::class.java)
    assertThat((state as ScanFlowUiState.ReadyToAnalyze).creditBalance).isEqualTo(3)
}

@Test
fun `credits are not deducted during check`() = runTest {
    // Given
    val creditRepository = FakeCreditRepository(balance = 5)
    val viewModel = ScanFlowViewModel(creditRepository, savedStateHandle)

    // When
    viewModel.uiState.first()

    // Then
    assertThat(creditRepository.decrementCallCount).isEqualTo(0)
}
```

### Project Structure Notes

**New Module:** `feature/scan`
- Contains scan flow orchestration
- Coordinates between analysis (5.3), confidence (5.4), manual entry (5.5), rules generation (5.6)
- Lightweight in this story - just credit check and routing

**Modified Files:**
- `feature/camera/CameraViewModel.kt` - Add navigation to scan flow after capture
- `feature/camera/navigation/CameraNavigation.kt` - Export scan flow navigation
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` - Add scan flow route

**Key Dependencies:**
- `core/data` - CreditRepository access
- `core/model` - Domain models (if needed)
- `core/analytics` - TelemetryDeck integration

### References

- [Source: _bmad-output/project-planning-artifacts/epics/epic-5-game-recognition-rules-generation.md#Story 5.1] - Full story definition with acceptance criteria
- [Source: _bmad-output/project-planning-artifacts/architecture.md#Feature Modules] - Feature module structure and patterns
- [Source: _bmad-output/project-planning-artifacts/architecture.md#Navigation] - Type-safe navigation with Jetpack Compose Navigation 2.8+
- [Source: _bmad-output/project-planning-artifacts/architecture.md#State Management] - StateFlow and ViewModel patterns
- [Source: _bmad-output/project-planning-artifacts/prd.md#FR38] - Zero credit handling requirement
- [Source: _bmad-output/implementation-artifacts/4-8-credit-balance-display-camera.md] - CreditRepository implementation pattern
- [Source: _bmad-output/implementation-artifacts/1-6-analytics-telemetrydeck.md] - TelemetryDeck integration

### Latest Technology Context (2025)

**Jetpack Compose Navigation (2.9.6):**
- Use type-safe navigation with `@Serializable` routes
- Define destinations using `composable<T>`
- Navigate with `navigate(route = T(…))`
- Obtain routes with `toRoute<T>()`
- [Type safety in Kotlin DSL and Navigation Compose](https://developer.android.com/guide/navigation/design/type-safety)
- [Type safe navigation for Compose](https://medium.com/androiddevelopers/type-safe-navigation-for-compose-105325a97657)

**Kotlin Coroutines & Flow Best Practices:**
- Use `StateFlow` for UI state management with backing properties pattern
- Create coroutines in ViewModels with `viewModelScope`
- Use `stateIn` with `SharingStarted.WhileSubscribed(5000)` for lifecycle-aware collection
- Expose immutable types with backing `MutableStateFlow`
- [Best practices for coroutines in Android](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)

### Testing Requirements

**Unit Tests:**
- ✅ ScanFlowViewModel credit check logic (0 credits → paywall)
- ✅ ScanFlowViewModel credit check logic (>0 credits → analyze)
- ✅ Verify credits NOT deducted during check
- ✅ Verify correct navigation state emitted
- ✅ Handle error cases (repository failures)

**Integration Tests:**
- ✅ Navigation from camera to scan flow with image URI
- ✅ Navigation from scan flow to paywall when zero credits
- ✅ Analytics event fired with correct properties

**Manual Testing:**
- ✅ Set credits to 0 in DataStore, capture photo, verify paywall navigation
- ✅ Set credits to 3, capture photo, verify scan flow proceeds
- ✅ Verify credit balance unchanged after check

### Dependencies

**Prerequisites:**
- Story 4.1 (Camera Screen) - Source of photo capture
- Story 4.7 (Image Compression) - Image ready for upload
- Story 4.8 (Credit Balance Display) - CreditRepository implementation
- Epic 1 (DataStore, Analytics, DI setup)

**Blocks:**
- Story 5.2 (Progress Screen) - Needs scan flow entry point
- Story 5.3 (Image Analysis API) - Needs scan flow orchestration

**Related:**
- Epic 8 (Credit System) - Full paywall implementation
- Story 5.10 (Scan Analytics) - Analytics event structure

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

✅ Task 1 Completed: Created scan feature module with basic composable, UI state, ViewModel, and navigation structure. All unit tests passing.

✅ Task 2 Completed: Implemented credit check logic in ScanFlowViewModel. ViewModel checks balance on init, navigates to paywall when balance is 0, proceeds to analysis when balance > 0, and does NOT deduct credits (verified by tests).

✅ Task 3 Completed: Wired camera navigation to scan flow. Updated RulebookNavHost to add scan flow and paywall screens, updated camera callbacks to navigate to scan flow with captured/compressed image URI.

✅ Task 4 Completed: Paywall navigation already implemented in Task 3. Paywall route defined in NavigationDestination, PaywallPlaceholder created, and ScanFlowScreen navigates to paywall when credits == 0.

✅ Task 5 Completed: Analytics event already implemented in Task 2. ScanFlowViewModel.trackScanStarted() tracks scan_started event with source and credit_balance properties. Test verifies event is tracked only when credits > 0.

### File List

#### Task 1: Scan Flow Module Setup
- feature/scan/build.gradle.kts (new)
- feature/scan/src/main/kotlin/com/rulebook/feature/scan/ScanFlowUiState.kt (new)
- feature/scan/src/main/kotlin/com/rulebook/feature/scan/ScanFlowViewModel.kt (new)
- feature/scan/src/main/kotlin/com/rulebook/feature/scan/ScanFlowScreen.kt (new)
- feature/scan/src/main/kotlin/com/rulebook/feature/scan/navigation/ScanFlowNavigation.kt (new)
- feature/scan/src/main/kotlin/com/rulebook/feature/scan/di/ScanFlowModule.kt (new)
- feature/scan/src/test/kotlin/com/rulebook/feature/scan/ScanFlowViewModelTest.kt (new)
- settings.gradle.kts (modified - added scan module)
- app/src/main/kotlin/com/rulebook/di/AppModule.kt (modified - registered scan module)

#### Task 3: Camera Navigation Integration
- app/build.gradle.kts (modified - added scan module dependency)
- app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt (modified - added ScanFlow and Paywall routes)
- app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt (modified - added scan flow and paywall screens, updated camera callbacks)
- app/src/main/kotlin/com/rulebook/navigation/PlaceholderScreens.kt (modified - added PaywallPlaceholder)

## Epic Dependencies

- **Depends On:** Story 4.1, Story 4.7, Story 4.8
- **Blocks:** Story 5.2, Story 5.3
- **Can Parallel With:** None (first story in Epic 5)

### Dependency Rationale
- Story 4.1: Requires camera to capture photos
- Story 4.7: Requires compressed image for scan flow
- Story 4.8: Requires CreditRepository for balance checks
