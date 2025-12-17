# Story 3.7: Onboarding Analytics Events

Status: ready-for-dev

## Story

As a product owner,
I want to track onboarding completion and skip rates,
So that I can optimize the first-time experience.

## Acceptance Criteria

1. **Given** TelemetryDeck is configured
   **When** onboarding events occur
   **Then** the following events are tracked:
   - `onboarding_started` - When onboarding screen appears
   - `onboarding_page_viewed` - With page number (1 or 2)
   - `onboarding_completed` - When "Get Started" tapped
   - `onboarding_skipped` - When "Skip" tapped, with page number

2. **And** events match iOS event names for cross-platform consistency

## Tasks / Subtasks

- [ ] Task 1: Track onboarding_started event (AC: #1, #2)
  - [x] Add `trackOnboardingStarted()` to AnalyticsManager interface
  - [x] Implement using `trackEvent("onboarding_started")`
  - [ ] Fire event when OnboardingScreen first appears
  - [ ] Use LaunchedEffect(Unit) for one-time tracking

- [ ] Task 2: Track onboarding_page_viewed event (AC: #1, #2)
  - [x] Add `trackOnboardingPageViewed(pageNumber: Int)` to interface
  - [x] Implement using `trackEvent("onboarding_page_viewed", mapOf("page" to pageNumber))`
  - [ ] Fire when user views each page (including swipe)
  - [ ] Track page 1 on start, page 2 on transition

- [ ] Task 3: Track onboarding_completed event (AC: #1, #2)
  - [x] Add `trackOnboardingCompleted()` to interface
  - [x] Implement using `trackEvent("onboarding_completed")`
  - [ ] Fire when "Get Started" is tapped (in completeOnboarding)
  - [x] Include completion method: "get_started"

- [ ] Task 4: Track onboarding_skipped event (AC: #1, #2)
  - [x] Add `trackOnboardingSkipped(pageNumber: Int)` to interface
  - [x] Implement using `trackEvent("onboarding_skipped", mapOf("page" to pageNumber))`
  - [ ] Fire when "Skip" is tapped
  - [x] Include which page user was on when skipping

- [ ] Task 5: Wire analytics in OnboardingViewModel (AC: #1)
  - [ ] Inject `AnalyticsManager` into OnboardingViewModel
  - [ ] Call appropriate tracking methods at each event point
  - [ ] Ensure events fire before navigation

## Dev Notes

### Architecture Context

- **Modules:** `core/analytics`, `feature/onboarding`
- **Pattern:** AnalyticsManager interface from Epic 1 (Story 1.6)
- **SDK:** TelemetryDeck 6.3.0

### AnalyticsManager Extensions

```kotlin
// core/analytics - AnalyticsManager.kt additions
interface AnalyticsManager {
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())
    fun trackScreenView(screenName: String)

    // Onboarding-specific events (convenience methods)
    fun trackOnboardingStarted() {
        trackEvent("onboarding_started")
    }

    fun trackOnboardingPageViewed(pageNumber: Int) {
        trackEvent("onboarding_page_viewed", mapOf("page" to pageNumber.toString()))
    }

    fun trackOnboardingCompleted() {
        trackEvent("onboarding_completed", mapOf("method" to "get_started"))
    }

    fun trackOnboardingSkipped(pageNumber: Int) {
        trackEvent("onboarding_skipped", mapOf("page" to pageNumber.toString()))
    }
}
```

### OnboardingViewModel Integration

```kotlin
// OnboardingViewModel.kt - updated with analytics
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val creditRepository: CreditRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    init {
        // Track onboarding started on ViewModel creation
        analyticsManager.trackOnboardingStarted()
        // Track initial page view
        analyticsManager.trackOnboardingPageViewed(pageNumber = 1)
    }

    fun onPageChanged(page: Int) {
        _currentPage.value = page
        // Track page view (1-indexed for analytics)
        analyticsManager.trackOnboardingPageViewed(pageNumber = page + 1)
    }

    fun onGetStartedClicked() {
        analyticsManager.trackOnboardingCompleted()
        completeOnboarding()
    }

    fun onSkipClicked() {
        // Track skip with current page (1-indexed)
        analyticsManager.trackOnboardingSkipped(pageNumber = _currentPage.value + 1)
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.completeOnboardingWithCredits(INITIAL_CREDITS)
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }
}
```

### OnboardingScreen Page Tracking

```kotlin
// OnboardingScreen.kt - page change tracking
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    // Track page changes from swipe
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    // ... rest of screen
}
```

### iOS Event Name Consistency

| Event | Android | iOS | Parameters |
|-------|---------|-----|------------|
| Started | `onboarding_started` | `onboarding_started` | None |
| Page View | `onboarding_page_viewed` | `onboarding_page_viewed` | `page: Int` |
| Completed | `onboarding_completed` | `onboarding_completed` | `method: String` |
| Skipped | `onboarding_skipped` | `onboarding_skipped` | `page: Int` |

### Event Timing Diagram

```
App Launch
    │
    ▼
OnboardingScreen appears
    │
    ├──► onboarding_started
    │
    ├──► onboarding_page_viewed (page=1)
    │
    ▼
User on Page 1
    │
    ├─── Swipe/Next ───►┐
    │                   │
    │                   ▼
    │          onboarding_page_viewed (page=2)
    │                   │
    │                   ▼
    │             User on Page 2
    │                   │
    │    ┌──────────────┼──────────────┐
    │    │              │              │
    │   Skip        Get Started     Swipe Back
    │    │              │              │
    │    ▼              ▼              ▼
    │ onboarding_    onboarding_   onboarding_
    │ skipped(2)     completed     page_viewed(1)
    │    │              │
    │    └──────┬───────┘
    │           ▼
    │    Navigate to Library
    │
    ├─── Skip (from page 1) ───►┐
    │                           │
    │                           ▼
    │                   onboarding_skipped(1)
    │                           │
    │                           ▼
    │                   Navigate to Library
    │
    └───────────────────────────►
```

### Koin Module Update

```kotlin
// feature/onboarding/di/OnboardingModule.kt
val onboardingModule = module {
    viewModel {
        OnboardingViewModel(
            onboardingRepository = get(),
            creditRepository = get(),
            analyticsManager = get()  // Added
        )
    }
}
```

### Privacy Considerations

From Epic 1 Story 1.6:
- No PII transmitted
- No user identifiers stored
- GDPR compliant by design
- Page numbers are non-identifying aggregate data

### Testing Notes

- Verify `onboarding_started` fires once on screen appear
- Verify `onboarding_page_viewed` fires for page 1 on start
- Verify `onboarding_page_viewed` fires for page 2 on transition
- Verify `onboarding_completed` fires on "Get Started" tap
- Verify `onboarding_skipped` fires on "Skip" with correct page
- Verify no duplicate events on configuration changes

### Test Implementation

```kotlin
// Use FakeAnalyticsManager from Story 1.6
@Test
fun `onboarding started event fires on screen appear`() {
    val fakeAnalytics = FakeAnalyticsManager()
    val viewModel = OnboardingViewModel(
        onboardingRepository = fakeOnboardingRepo,
        creditRepository = fakeCreditRepo,
        analyticsManager = fakeAnalytics
    )

    // ViewModel init should track started
    assertThat(fakeAnalytics.events).contains("onboarding_started")
}

@Test
fun `skip event includes page number`() {
    val fakeAnalytics = FakeAnalyticsManager()
    val viewModel = OnboardingViewModel(...)

    viewModel.onPageChanged(1) // Move to page 2
    viewModel.onSkipClicked()

    val skipEvent = fakeAnalytics.eventsWithProperties
        .first { it.first == "onboarding_skipped" }
    assertThat(skipEvent.second["page"]).isEqualTo("2")
}
```

### Project Structure Notes

Updates to existing files:
- `core/analytics/AnalyticsManager.kt` - Add convenience methods
- `feature/onboarding/OnboardingViewModel.kt` - Add analytics calls
- `feature/onboarding/OnboardingScreen.kt` - Track page changes
- `feature/onboarding/di/OnboardingModule.kt` - Inject analytics

### References

- [Source: docs/architecture.md#Platform Integration]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.7]
- [Source: docs/sprint-artifacts/1-6-analytics-telemetrydeck.md] (Analytics setup)

## Dev Agent Record

### Context Reference
- Epic 1 Story 1.6: TelemetryDeck analytics setup
- Story 3.4: Completion logic where completed event fires
- Story 3.5: Skip logic where skipped event fires

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List

### File List

## Dependencies

- **Depends On:** Story 3.4 (completion logic), Epic 1 (Analytics)
- **Blocks:** None
- **Can Parallel With:** Story 3.5 (Skip functionality)

### Dependency Rationale
- Epic 1: Requires AnalyticsManager interface from Story 1.6
- Story 3.4: Analytics events integrated with completion flow
- Story 3.5: Skip analytics integrated with skip flow
