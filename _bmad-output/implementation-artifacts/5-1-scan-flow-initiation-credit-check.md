# Story 5.1: Scan Flow Initiation & Credit Check

Status: ready-for-dev

## Story

As a user,
I want the scan to check my credits before proceeding,
So that I don't waste time if I can't complete the scan.

## Acceptance Criteria

1. **Given** the user captures/selects a photo
   **When** initiating the scan flow
   **Then** credit balance is checked

2. **Given** the credit balance check completes
   **When** credits > 0
   **Then** proceed to image analysis

3. **Given** the credit balance check completes
   **When** credits = 0
   **Then** show paywall (FR38, handled in Epic 8)

4. **Given** a scan is initiated
   **When** the scan flow progresses
   **Then** credit is NOT deducted until scan succeeds

## Epic Context

**Epic Goal:** Implement the AI recognition pipeline and rules generation. After this epic, users can capture a photo and receive generated rules with progress feedback and confidence handling.

**This Story's Role:** First story in Epic 5 - establishes the entry point for the scan flow and implements the critical credit gate that determines whether a user can proceed with scanning or needs to purchase credits.

**Dependencies:**
- Epic 4 (photo capture) - COMPLETED
- Epic 1 (DataStore for credits) - COMPLETED
- Story 1-4 (DataStore implementation) - COMPLETED
- Story 4-6 (Gallery picker) - COMPLETED

## Tasks / Subtasks

- [x] Task 1: Create Scan Flow Navigation Route (AC: #1)
  - [x] Add `ScanProcessing` route to NavigationDestination
  - [x] Configure route to accept imageUri parameter
  - [x] Add navigation composable in RulebookNavHost

- [x] Task 2: Implement Credit Check in CameraViewModel (AC: #1, #2, #3)
  - [x] Create `initiateScanFlow(imageUri: Uri)` function in CameraViewModel
  - [x] Inject CreditRepository into CameraViewModel (already done)
  - [x] Check credit balance using `creditRepository.creditBalance.first()`
  - [x] Return Result<Unit> to indicate success or need for paywall

- [ ] Task 3: Add Credit Gate UI State (AC: #3)
  - [ ] Add `showPaywall: Boolean` to CameraUiState
  - [ ] Update UI state when credits = 0
  - [ ] Clear paywall state on dismiss

- [ ] Task 4: Integrate Paywall Navigation (AC: #3)
  - [ ] Navigate to Paywall route when credits = 0
  - [ ] Pass imageUri to paywall for retry after purchase
  - [ ] Handle paywall dismissal (return to camera)

- [ ] Task 5: Navigate to Processing Screen (AC: #2)
  - [ ] Navigate to ScanProcessing route when credits > 0
  - [ ] Pass imageUri as navigation argument
  - [ ] Preserve image in temporary storage during navigation

- [ ] Task 6: Create ScanProcessing Screen Placeholder (AC: #2)
  - [ ] Create `ScanProcessingScreen.kt` in `feature/rules`
  - [ ] Display "Processing..." placeholder
  - [ ] Accept imageUri argument from navigation
  - [ ] Log imageUri for verification

- [ ] Task 7: Add Analytics Event (Best Practice)
  - [ ] Fire `scan_initiated` event with credit balance
  - [ ] Include source (camera vs gallery) in event properties
  - [ ] Track paywall shown events

## Technical Requirements

### Architecture Compliance

**Module Ownership:**
- `feature/camera`: Credit check initiation, navigation trigger
- `feature/rules`: ScanProcessing screen (created in this story)
- `feature/purchase`: Paywall display (Epic 8, just navigate for now)
- `core/data`: CreditRepository (already exists)
- `core/datastore`: Credit preferences (already exists)

**Data Flow Pattern:**
```
CameraScreen (User Action)
    ↓
CameraViewModel.initiateScanFlow(imageUri)
    ↓
CreditRepository.creditBalance.first()
    ↓
if (credits > 0) → Navigate to ScanProcessing
if (credits == 0) → Navigate to Paywall
```

### Library & Framework Requirements

**Kotlin Coroutines Best Practices (2025):**
- Use `viewModelScope.launch` for async operations in ViewModel
- Use `Flow.first()` to get single credit balance value
- Proper exception handling with try/catch or Result wrapper
- Do NOT block main thread - all credit checks are suspend functions

**Navigation Pattern:**
- Type-safe navigation using data classes (Navigation Compose)
- Pass imageUri as String argument (Uri.toString())
- Reconstruct Uri in destination using Uri.parse()

**State Management:**
- Follow MVI pattern - single UiState data class
- Immutable state updates using copy()
- One-time events via Channel for navigation

### File Structure Requirements

**New Files to Create:**
```
feature/rules/src/main/kotlin/com/rulebook/feature/rules/
├── ScanProcessingScreen.kt       # New screen composable
├── ScanProcessingViewModel.kt    # New ViewModel (placeholder for Story 5.2)
└── ScanProcessingUiState.kt      # New UI state (placeholder for Story 5.2)
```

**Files to Modify:**
```
app/src/main/kotlin/com/rulebook/app/navigation/
├── NavigationDestination.kt      # Add ScanProcessing route
└── RulebookNavHost.kt            # Add ScanProcessing composable

feature/camera/src/main/kotlin/com/rulebook/feature/camera/
├── CameraViewModel.kt            # Add initiateScanFlow()
├── CameraScreen.kt               # Trigger credit check on image select
└── CameraUiState.kt              # Add showPaywall flag
```

### Testing Requirements

**Unit Tests:**
- CameraViewModel credit check logic
- Navigation argument passing
- State updates for paywall display

**Integration Tests:**
- Credit check → navigation flow
- Paywall display when credits = 0
- Processing screen display when credits > 0

## Previous Story Intelligence

### Learnings from Story 4.6 (Gallery Picker Alternative)

**Key Implementation Patterns:**
- ActivityResultContracts pattern for photo selection
- Uri handling via callbacks passed to composables
- Navigation via callback parameters to parent
- Brutalist UI styling with 3dp borders

**Code Patterns Established:**
```kotlin
// Gallery picker pattern
val pickMedia = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
) { uri: Uri? ->
    if (uri != null) {
        viewModel.onGalleryImageSelected(uri)
        onImageSelected(uri) // Callback to parent
    }
}
```

**Files Modified in Previous Story:**
- CameraViewModel.kt - Added state management
- CameraScreen.kt - Added UI components
- CameraUiState.kt - Added thumbnail state

**Testing Approach:**
- Unit tests for ViewModel state changes
- Component tests for UI interactions
- No instrumentation tests needed for simple state

## Architecture Intelligence

### Credit System Architecture (from architecture.md)

**DataStore Implementation:**
- Credit balance stored in DataStore (core/datastore)
- Flow-based reactive updates
- Atomic increment/decrement operations

**Repository Pattern:**
```kotlin
interface CreditRepository {
    val creditBalance: Flow<Int>
    suspend fun deductCredit(): Boolean
    suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean
}
```

**Key Constraint:** Credits are ONLY deducted after successful rules generation (Story 5.7), not at scan initiation.

### Navigation Architecture

**Pattern:** Compose Navigation with type-safe routes
- Central NavigationDestination.kt defines all routes
- RulebookNavHost wires destinations to composables
- Arguments passed via data class routes (Navigation 3 pattern)

**Modal Stack Management:**
- Paywall as bottom sheet modal
- Full-screen for camera and processing
- Predictive back gesture support

## Latest Technical Information

### Kotlin Coroutines & Flow (2025 Best Practices)

**ViewModelScope Usage:**
- viewModelScope automatically cancels on ViewModel clear
- Hardcoded to Dispatchers.Main - use flowOn() to switch contexts
- Prefer Flow for streams, suspend functions for single values

**Flow Collection Best Practices:**
- Use `Flow.first()` to get single value (not collect)
- Inject Dispatchers for testing (don't hardcode)
- Use `catch` operator for graceful error handling

**State Management:**
- StateFlow for UI state representation
- Use `stateIn` to convert Flow to StateFlow
- Lifecycle-aware collection with repeatOnLifecycle

**Sources:**
- [Best practices for coroutines in Android](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Kotlin flows on Android](https://developer.android.com/kotlin/flow)
- [Use Kotlin coroutines with lifecycle-aware components](https://developer.android.com/topic/libraries/architecture/coroutines)

### Jetpack Compose Navigation (2025)

**Type-Safe Navigation:**
- Navigation 3 introduced at Google I/O 2025
- Use data classes for routes instead of string-based
- Arguments are type-safe through constructor parameters

**Navigation Pattern:**
```kotlin
@Serializable
data class ScanProcessing(val imageUri: String)

// In ViewModel
navController.navigate(ScanProcessing(uri.toString()))

// In destination
val args = backStackEntry.toRoute<ScanProcessing>()
val uri = Uri.parse(args.imageUri)
```

**SavedStateHandle:**
- Use SavedStateHandle in ViewModel for arguments
- Survives process death
- Type-safe access to navigation arguments

**Sources:**
- [Navigation with Compose](https://developer.android.com/develop/ui/compose/navigation)
- [Type safety in Kotlin DSL and Navigation Compose](https://developer.android.com/guide/navigation/design/type-safety)
- [Announcing Jetpack Navigation 3](https://android-developers.googleblog.com/2025/05/announcing-jetpack-navigation-3-for-compose.html)

## Git Intelligence

### Recent Commit Patterns (from git log)

**Latest Commits:**
- 5385a3d Update sprint status
- 21c7300 docs(sprint): mark story 4-6 and epic-4 as done
- 26c7af2 docs(readme): Add project README with Linear integration

**Commit Message Pattern:**
- Use conventional commits: `type(scope): description`
- Examples: `feat(camera): add credit check`, `docs(sprint): update status`
- Keep commits focused and buildable

**Recent Work Context:**
- Epic 4 just completed (Photo Capture Flow)
- Project uses Linear integration for tracking
- Regular sprint status updates in implementation-artifacts/

## Dev Notes

### Critical Implementation Sequence

1. **Navigation First**: Add route definitions before implementing logic
2. **Credit Check**: Simple Flow.first() call, don't over-complicate
3. **Navigation Trigger**: Use callback pattern from CameraScreen to parent
4. **Paywall Placeholder**: Just navigate to paywall route (Epic 8 will implement)
5. **Processing Placeholder**: Basic screen with "Processing..." text

### Design System Reference

**Brutalist Styling** (from ux-design-specification.md):
- 3dp borders (thick)
- 0dp corner radius (sharp)
- Offset rectangle shadows (4-12dp)
- Black weight (900) for titles
- Material 3 base with Rulebook theme

**Component Library:**
- Use `RulebookButton` for primary actions
- Use `RulebookCard` for containers
- Use `RulebookHeaderBar` for screen titles

### Credit System Logic

**Current Implementation (from CreditPreferencesSource):**
```kotlin
interface CreditPreferencesSource {
    val creditBalance: Flow<Int>
    suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean
    suspend fun deductCredit(): Boolean
}
```

**DO NOT DEDUCT** in this story - only check balance:
```kotlin
suspend fun initiateScanFlow(imageUri: Uri): Result<Unit> {
    val credits = creditRepository.creditBalance.first()
    return if (credits > 0) {
        Result.success(Unit)
    } else {
        Result.failure(InsufficientCreditsException())
    }
}
```

### Network Architecture (Prepared for Future Stories)

**API Client Configuration (from RulebookApiClient.kt):**
- 30-second timeouts (connect, read, write)
- 60-second call timeout (full HTTP operation)
- kotlinx.serialization for JSON
- HTTP logging in debug builds

**API Endpoints (from RulebookApi.kt):**
```kotlin
@POST("analyze")
suspend fun analyzeImage(@Body request: AnalyzeRequest): AnalyzeResponse

@POST("generate")
suspend fun generateRules(@Body request: GenerateRequest): GenerateResponse
```

**Note:** This story does NOT call these endpoints - Story 5.3 will implement image analysis API integration.

### UX Flow Context

**User Journey 1: First Scan Success (from ux-design-specification.md):**
1. TAP FAB → **Credit check (THIS STORY)**
2. CAMERA → Point at game box, capture
3. PROCESSING → 5-phase progress animation (Story 5.2)
4. CONFIDENCE CHECK → Auto-proceed or confirm (Story 5.4)
5. RULES DISPLAY → Overview expanded (Story 5.6)

**Critical Success Moment:**
- User must not encounter surprise "no credits" error mid-scan
- Credit check happens BEFORE any processing starts
- Clear path to paywall if credits = 0

### Performance Requirements (from PRD)

- **NFR1**: Scan-to-rules complete in <60 seconds
- **NFR2**: App cold start in <3 seconds
- **NFR5**: UI interactions respond in <100ms

**Implication:** Credit check must be instantaneous (Flow.first() is fast)

### Analytics Events (from Epic 5.10)

**Events to Track:**
- `scan_initiated` - With credit balance and source (camera/gallery)
- `scan_paywall_shown` - When credits = 0
- `scan_cancelled` - If user backs out

**TelemetryDeck Integration:**
- Use existing analytics module (core/analytics)
- Match iOS event names for cross-platform consistency
- Include relevant properties (balance, source)

## Project Structure Notes

### Alignment with Unified Project Structure

**Module Architecture** (from architecture.md):
```
rulebook/
├── app/                      # Navigation aggregation
├── feature/
│   ├── camera/              # Credit check initiation
│   ├── rules/               # Processing screen (NEW)
│   └── purchase/            # Paywall (Epic 8)
├── core/
│   ├── data/                # CreditRepository
│   ├── datastore/           # Credit preferences
│   ├── analytics/           # Event tracking
│   └── common/              # Result wrapper
```

**Dependency Rules:**
- feature/camera → core/data (CreditRepository)
- feature/rules → app navigation (new route)
- NO direct feature-to-feature dependencies

### Detected Conflicts or Variances

**None** - This story aligns perfectly with established architecture:
- Follows MVI pattern for state management
- Uses existing CreditRepository abstraction
- Respects module boundaries
- Follows established navigation patterns

## References

### Epic & Story References

- **[Source: _bmad-output/project-planning-artifacts/epics/epic-5-game-recognition-rules-generation.md#Story 5.1]**
  - Acceptance criteria
  - Technical notes
  - Prerequisites

### Architecture References

- **[Source: _bmad-output/project-planning-artifacts/architecture.md#Core Architectural Decisions]**
  - State management pattern (MVI)
  - Error handling pattern (Result<T>)
  - Technology stack (Kotlin, Compose, Koin, DataStore)

- **[Source: _bmad-output/project-planning-artifacts/architecture.md#Project Structure & Boundaries]**
  - Module responsibilities
  - File structure conventions
  - Dependency rules

### PRD References

- **[Source: _bmad-output/project-planning-artifacts/prd.md#Functional Requirements]**
  - FR34: System tracks remaining scan credits
  - FR35: System consumes one credit per successful scan
  - FR38: System displays paywall when user has no credits

- **[Source: _bmad-output/project-planning-artifacts/prd.md#Non-Functional Requirements]**
  - NFR1: Scan-to-rules complete in <60 seconds
  - NFR5: UI interactions respond in <100ms

### UX References

- **[Source: _bmad-output/project-planning-artifacts/ux-design-specification.md#User Journey Flows]**
  - Journey 1: First Scan Success
  - Critical success moments
  - Navigation patterns

- **[Source: _bmad-output/project-planning-artifacts/ux-design-specification.md#Design System Foundation]**
  - Brutalist styling specifications
  - Component library patterns
  - Theme implementation

### Code References

- **[Source: core/datastore/src/main/kotlin/com/rulebook/core/datastore/CreditPreferencesSource.kt]**
  - Credit balance Flow interface
  - Deduct credit function signature

- **[Source: feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt]**
  - Existing credit balance observation pattern
  - MVI state management structure

- **[Source: core/network/src/main/kotlin/com/rulebook/core/network/api/RulebookApi.kt]**
  - API endpoint definitions for future stories

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude Sonnet 4.5 (claude-sonnet-4-5-20250929)

### Debug Log References

*None - Task 1 completed without issues*

### Completion Notes List

**Task 1: Create Scan Flow Navigation Route**
- Added `ScanProcessing` route to `NavigationDestination.kt` with imageUri parameter
- Added `IMAGE_URI` argument key to `RulebookNavArgs`
- Created route helper function `createRoute(imageUri: String)` following existing pattern
- Added navigation composable in `RulebookNavHost.kt` with slide transitions
- Created `ScanProcessingPlaceholder` composable in `PlaceholderScreens.kt`
- Added comprehensive unit tests in `NavigationDestinationTest.kt` (4 new tests)
- All tests passing ✅

**Task 2: Implement Credit Check in CameraViewModel**
- Created `initiateScanFlow(imageUri: Uri)` suspend function in `CameraViewModel.kt`
- Made `creditRepository` a private property for access in the new function
- Implemented credit check using `creditBalance.first()` for single value snapshot
- Returns `Result.success(Unit)` when credits > 0, `Result.failure(InsufficientCreditsException())` when credits = 0
- Created `InsufficientCreditsException` class for typed error handling
- Added mockk library to version catalog and camera module dependencies
- Added 3 comprehensive unit tests covering success, failure, and non-deduction cases
- All tests passing ✅

### File List

**Modified:**
- `app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt` - Added ScanProcessing route and IMAGE_URI arg
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt` - Added ScanProcessing composable navigation
- `app/src/main/kotlin/com/rulebook/navigation/PlaceholderScreens.kt` - Added ScanProcessingPlaceholder
- `app/src/test/kotlin/com/rulebook/navigation/NavigationDestinationTest.kt` - Added tests for ScanProcessing route
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt` - Added initiateScanFlow() and InsufficientCreditsException
- `feature/camera/src/test/kotlin/com/rulebook/feature/camera/CameraViewModelTest.kt` - Added 3 new tests for initiateScanFlow
- `gradle/libs.versions.toml` - Added mockk 1.13.13 for testing
- `feature/camera/build.gradle.kts` - Added mockk test dependency

**Created:**
- `local.properties` - Android SDK configuration

## Story Completion Status

**Status**: ready-for-dev
**Created**: 2025-12-21
**Next Story**: 5-2-progress-screen-phase-indicator

**Note to Developer:**
This story is the critical entry point for Epic 5. Focus on clean separation between credit checking and actual processing. The credit check should be instantaneous (Flow.first()), and navigation should be straightforward. Don't over-engineer - this is a simple gate that either allows progression or shows the paywall.

The processing screen created here is a placeholder. Story 5.2 will implement the full 5-phase progress indicator. Just create a basic screen that displays "Processing..." to verify navigation works correctly.

Remember: Credits are NOT deducted here - only checked. Story 5.7 will handle credit deduction after successful rules generation.
