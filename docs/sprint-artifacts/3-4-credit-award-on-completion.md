# Story 3.4: Credit Award on Completion

Status: in-progress

## Story

As a first-time user,
I want to receive 3 free scan credits when I complete onboarding,
So that I can immediately try the app's core feature.

## Acceptance Criteria

1. **Given** the user completes onboarding (taps "Get Started" or "Skip") (FR2)
   **When** onboarding completion is triggered
   **Then** `creditBalance` is set to 3 in DataStore

2. **And** `hasCompletedOnboarding` is set to true (FR4)

3. **And** user is navigated to Library screen

4. **And** credits are only awarded once (idempotent)

## Tasks / Subtasks

- [x] Task 1: Create CreditRepository for credit management (AC: #1, #4)
  - [x] Create `CreditRepository` interface in `core/data`
  - [x] Implement `CreditRepositoryImpl` using DataStore
  - [x] Add `creditBalance: Flow<Int>` property
  - [x] Add `awardInitialCredits(amount: Int)` suspend function
  - [x] Add `deductCredit()` suspend function for future use
  - [x] Register repository in Koin module

- [x] Task 2: Implement idempotent credit award logic (AC: #1, #4)
  - [x] Check if credits already > 0 before awarding
  - [x] Only set to 3 if current balance is 0
  - [x] Use DataStore transaction for atomic update
  - [x] Return Boolean indicating if credits were awarded

- [ ] Task 3: Update OnboardingViewModel with credit award (AC: #1, #2)
  - [ ] Inject `CreditRepository` into OnboardingViewModel
  - [ ] Update `completeOnboarding()` to award credits
  - [ ] Ensure both onboarding flag and credits set together
  - [ ] Handle edge case where user already has credits

- [ ] Task 4: Implement atomic DataStore transaction (AC: #2, #4)
  - [ ] Create `completeOnboardingWithCredits()` in repository
  - [ ] Use DataStore `edit` block for atomic update
  - [ ] Set both `hasCompletedOnboarding` and `creditBalance` together
  - [ ] Ensure no partial state updates possible

- [ ] Task 5: Add navigation to Library after completion (AC: #3)
  - [ ] Emit `NavigateToLibrary` event from ViewModel
  - [ ] Collect event in OnboardingScreen
  - [ ] Call `navController.navigateToLibrary()` with popUpTo
  - [ ] Clear onboarding from back stack

## Dev Notes

### Architecture Context

- **Modules:** `core/data`, `core/datastore`, `feature/onboarding`
- **Pattern:** Repository pattern with atomic DataStore transactions
- **Key Consideration:** Must be idempotent - safe to call multiple times

### Implementation Pattern

```kotlin
// core/data - CreditRepository.kt
interface CreditRepository {
    val creditBalance: Flow<Int>
    suspend fun awardInitialCredits(amount: Int): Boolean
    suspend fun deductCredit(): Boolean
    suspend fun hasCredits(): Boolean
}

// core/data/impl - CreditRepositoryImpl.kt
class CreditRepositoryImpl(
    private val preferencesDataStore: PreferencesDataStore
) : CreditRepository {

    override val creditBalance: Flow<Int> =
        preferencesDataStore.creditBalance

    override suspend fun awardInitialCredits(amount: Int): Boolean {
        return preferencesDataStore.awardInitialCreditsIfNeeded(amount)
    }

    override suspend fun deductCredit(): Boolean {
        return preferencesDataStore.deductCredit()
    }

    override suspend fun hasCredits(): Boolean {
        return creditBalance.first() > 0
    }
}
```

### DataStore Atomic Transaction

```kotlin
// core/datastore - PreferencesDataStore.kt additions
companion object {
    val CREDIT_BALANCE = intPreferencesKey("credit_balance")
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
}

val creditBalance: Flow<Int> = dataStore.data
    .map { preferences -> preferences[CREDIT_BALANCE] ?: 0 }

suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
    return dataStore.edit { preferences ->
        // Only award if not already completed (idempotent)
        val alreadyCompleted = preferences[HAS_COMPLETED_ONBOARDING] ?: false
        if (!alreadyCompleted) {
            preferences[HAS_COMPLETED_ONBOARDING] = true
            preferences[CREDIT_BALANCE] = creditAmount
        }
    }.let { true }
}

suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean {
    var awarded = false
    dataStore.edit { preferences ->
        val currentBalance = preferences[CREDIT_BALANCE] ?: 0
        if (currentBalance == 0) {
            preferences[CREDIT_BALANCE] = amount
            awarded = true
        }
    }
    return awarded
}

suspend fun deductCredit(): Boolean {
    var success = false
    dataStore.edit { preferences ->
        val currentBalance = preferences[CREDIT_BALANCE] ?: 0
        if (currentBalance > 0) {
            preferences[CREDIT_BALANCE] = currentBalance - 1
            success = true
        }
    }
    return success
}
```

### OnboardingViewModel Update

```kotlin
// OnboardingViewModel.kt - updated
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    private val _navigationEvent = Channel<OnboardingNavigationEvent>()
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun completeOnboarding() {
        viewModelScope.launch {
            // Atomic: set onboarding complete AND award credits
            onboardingRepository.completeOnboardingWithCredits(
                creditAmount = INITIAL_CREDITS
            )

            // Navigate to Library
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    // Both Get Started and Skip use same completion
    fun onGetStartedClicked() = completeOnboarding()
    fun onSkipClicked() = completeOnboarding()

    companion object {
        const val INITIAL_CREDITS = 3
    }
}
```

### Navigation After Completion

```kotlin
// OnboardingScreen.kt - event collection
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is OnboardingNavigationEvent.NavigateToLibrary -> {
                    navController.navigate(LibraryRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            }
        }
    }

    // ... rest of screen
}
```

### Koin Module Updates

```kotlin
// core/data/di/DataModule.kt
val dataModule = module {
    single<OnboardingRepository> { OnboardingRepositoryImpl(get()) }
    single<CreditRepository> { CreditRepositoryImpl(get()) }
}

// feature/onboarding/di/OnboardingModule.kt
val onboardingModule = module {
    viewModel { OnboardingViewModel(get(), get()) }
}
```

### Idempotency Flow

```
User taps "Get Started"
         │
         ▼
Check hasCompletedOnboarding
         │
    ┌────┴────┐
    │ false   │ true
    ▼         ▼
Set both    Do nothing
atomically  (idempotent)
    │         │
    └────┬────┘
         ▼
Navigate to Library
```

### Edge Cases

| Scenario | Behavior |
|----------|----------|
| First completion | Awards 3 credits, sets flag |
| Second call (bug/race) | No action (idempotent) |
| Credits already exist (edge case) | Don't overwrite |
| App killed mid-completion | DataStore transaction atomic |

### Testing Notes

- Test completion awards exactly 3 credits
- Test `hasCompletedOnboarding` is true after completion
- Test calling completion twice doesn't double credits
- Test navigation to Library after completion
- Test Skip uses same credit award logic

### Project Structure Notes

```
core/data/
├── CreditRepository.kt
├── OnboardingRepository.kt
└── impl/
    ├── CreditRepositoryImpl.kt
    └── OnboardingRepositoryImpl.kt

core/datastore/
└── PreferencesDataStore.kt  (updated with credit keys)
```

### References

- [Source: docs/architecture.md#Data Architecture]
- [Source: docs/architecture.md#State Management]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.4]
- [Source: docs/sprint-artifacts/1-4-datastore-preferences.md] (DataStore patterns)

## Dev Agent Record

### Context Reference
- Epic 1: DataStore setup and preferences patterns
- Story 3.3: Completion trigger from Get Started button
- Architecture: Repository pattern implementation

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

- Task 1: Created CreditRepository with interface abstraction pattern matching existing OnboardingRepository.
  - CreditPreferencesSource interface added to core/datastore for testing abstraction
  - RulebookPreferences now implements both OnboardingPreferencesSource and CreditPreferencesSource
  - Added awardInitialCreditsIfNeeded() and deductCredit() methods with idempotent behavior
  - Unit tests added for all CreditRepository operations
- Task 2: Idempotent credit award logic was implemented as part of Task 1 in awardInitialCreditsIfNeeded().
  - Checks currentBalance == 0 before awarding
  - Uses DataStore edit block for atomic operation
  - Returns Boolean indicating success

### File List

- core/data/src/main/kotlin/com/rulebook/core/data/repository/CreditRepository.kt (new)
- core/data/src/main/kotlin/com/rulebook/core/data/repository/CreditRepositoryImpl.kt (new)
- core/data/src/main/kotlin/com/rulebook/core/data/di/DataModule.kt (modified)
- core/data/src/test/kotlin/com/rulebook/core/data/repository/CreditRepositoryTest.kt (new)
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/CreditPreferencesSource.kt (new)
- core/datastore/src/main/kotlin/com/rulebook/core/datastore/RulebookPreferences.kt (modified)

## Dependencies

- **Depends On:** Story 3.3 (completion trigger), Epic 1 (DataStore)
- **Blocks:** Story 3.5 (Skip functionality), Story 3.7 (Analytics)
- **Can Parallel With:** None

### Dependency Rationale
- Story 3.3: Get Started button triggers credit award
- Story 3.5: Skip reuses credit award logic from this story
- Story 3.7: Analytics tracks credit award event
