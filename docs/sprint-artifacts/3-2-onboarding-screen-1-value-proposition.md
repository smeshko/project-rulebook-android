# Story 3.2: Onboarding Screen 1 - Value Proposition

Status: Ready for Review

## Story

As a first-time user,
I want to understand what Rulebook does,
So that I know the app's core value before using it.

## Acceptance Criteria

1. **Given** the user is on onboarding screen 1 (FR1)
   **When** the screen is displayed
   **Then** it shows:
   - Bold headline: "Scan any game box"
   - Subtext explaining: "Point your camera at a board game and get the rules instantly"
   - Illustration or graphic representing scanning
   - "Next" button to proceed
   - "Skip" text button to bypass onboarding (FR3)

2. **And** the screen uses brutalist styling (bold typography, accent colors)

3. **And** the screen is full-bleed (edge-to-edge)

## Tasks / Subtasks

- [x] Task 1: Create OnboardingScreen with HorizontalPager (AC: #1, #3)
  - [x] Create `OnboardingScreen.kt` in `feature/onboarding`
  - [x] Implement `HorizontalPager` with 2 pages
  - [x] Create `OnboardingPage` enum for page content
  - [x] Apply edge-to-edge display with proper insets

- [x] Task 2: Implement Page 1 content composable (AC: #1, #2)
  - [x] Create `OnboardingPage1Content.kt` composable
  - [x] Add bold headline "Scan any game box" using `displayLarge` typography
  - [x] Add subtext with `bodyLarge` typography
  - [x] Add placeholder illustration area (Image or Icon)
  - [x] Apply brutalist styling (accent colors, bold fonts)

- [x] Task 3: Create navigation buttons (AC: #1)
  - [x] Add "Next" button using `RulebookButton` (primary variant)
  - [x] Position button at bottom with proper padding
  - [x] Wire button to advance pager to page 2
  - [x] Press feedback via RulebookButton's built-in brutalist modifiers

- [x] Task 4: Add Skip button (AC: #1)
  - [x] Create "Skip" text button (secondary/text style)
  - [x] Position consistently (top-right)
  - [x] Wire to `onSkip` callback parameter
  - [x] Style with secondary color, no heavy decoration

- [x] Task 5: Create OnboardingViewModel (AC: #1)
  - [x] Create `OnboardingViewModel.kt` in `feature/onboarding`
  - [x] Add `currentPage: StateFlow<Int>` for pager state
  - [x] Add `onNextClicked()` method
  - [x] Add `onPageChanged()` method for pager sync
  - [x] Register ViewModel in Koin module

## Dev Notes

### Architecture Context

- **Module:** `feature/onboarding`
- **Package:** `com.rulebook.feature.onboarding`
- **Key Classes:** `OnboardingScreen.kt`, `OnboardingViewModel.kt`
- **Design System:** Uses `core/designsystem` components

### Implementation Pattern

```kotlin
// OnboardingScreen.kt
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingPage1Content()
                1 -> OnboardingPage2Content(onComplete = onComplete)
            }
        }

        // Skip button - top right
        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Text("Skip", style = MaterialTheme.typography.labelLarge)
        }

        // Bottom navigation area
        OnboardingBottomSection(
            pagerState = pagerState,
            onNextClick = { viewModel.onNextClicked() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

### Page 1 Content

```kotlin
// OnboardingPage1Content.kt
@Composable
fun OnboardingPage1Content(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration placeholder
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Headline - brutalist bold
        Text(
            text = "Scan any game box",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subtext
        Text(
            text = "Point your camera at a board game and get the rules instantly",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
```

### Brutalist Styling Requirements

From `core/designsystem` (Epic 1):

| Element | Style |
|---------|-------|
| Headline | `displayLarge` - Bold, high contrast |
| Body | `bodyLarge` - Clear, readable |
| Button | `RulebookButton` - Sharp corners, bold colors |
| Colors | Primary accent, high contrast backgrounds |
| Shadows | Offset shadows (brutalist modifier) |

### HorizontalPager Setup

```kotlin
// Dependency: compose.foundation (already in version catalog)
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { 2 }
)
```

### ViewModel Pattern

```kotlin
// OnboardingViewModel.kt
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository
) : ViewModel() {
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    fun onNextClicked() {
        if (_currentPage.value < 1) {
            _currentPage.value = _currentPage.value + 1
        }
    }

    fun onSkipClicked() {
        viewModelScope.launch {
            // Handled by screen via callback
        }
    }
}
```

### Koin Module

```kotlin
// feature/onboarding/di/OnboardingModule.kt
val onboardingModule = module {
    viewModel { OnboardingViewModel(get()) }
}
```

### Edge-to-Edge Considerations

- Screen is full-bleed (content extends under status bar)
- Skip button needs `statusBarsPadding()` modifier
- Bottom buttons need `navigationBarsPadding()` modifier
- HorizontalPager fills entire screen

### Testing Notes

- Verify page 1 displays correct headline and subtext
- Verify "Next" button advances to page 2
- Verify "Skip" button triggers skip callback
- Verify brutalist styling applied (bold typography, accent colors)
- Test swipe gesture navigation between pages

### Project Structure Notes

```
feature/onboarding/
├── OnboardingScreen.kt
├── OnboardingViewModel.kt
├── OnboardingUiState.kt
├── components/
│   ├── OnboardingPage1Content.kt
│   ├── OnboardingPage2Content.kt
│   ├── OnboardingBottomSection.kt
│   └── OnboardingPageIndicator.kt
├── navigation/
│   └── OnboardingNavigation.kt
└── di/
    └── OnboardingModule.kt
```

### References

- [Source: docs/architecture.md#Feature Module Structure]
- [Source: docs/architecture.md#Implementation Patterns & Consistency Rules]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.2]
- [Source: docs/ux-design-specification.md#Onboarding Flow]
- [Source: docs/sprint-artifacts/1-7-design-tokens-theme.md] (Typography)
- [Source: docs/sprint-artifacts/1-9-rulebookbutton.md] (Button component)

## Dev Agent Record

### Context Reference
- Epic 1: Design system, RulebookButton, typography
- Story 3.1: Onboarding navigation foundation
- Architecture: Feature module structure

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List
- Implemented full onboarding screen with HorizontalPager supporting 2 pages
- Created OnboardingPage enum to define page content (headline, subtext)
- Built OnboardingPage1Content and OnboardingPage2Content composables with brutalist styling
- Used displayLarge typography for headlines and bodyLarge for subtext
- Added camera icon placeholder for Page 1 illustration
- Created OnboardingBottomSection with RulebookButton for navigation
- Implemented Skip button with TextButton in top-right corner with status bar insets
- Created OnboardingViewModel with currentPage state and navigation methods
- Added bidirectional sync between pager state and ViewModel for swipe support
- Registered ViewModel in Koin onboardingModule
- Added kotlin-test dependency to version catalog for unit tests
- All unit tests pass for OnboardingPage enum and OnboardingViewModel

### File List
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingScreen.kt (modified)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingPage.kt (new)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingViewModel.kt (new)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingPage1Content.kt (new)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingPage2Content.kt (new)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingBottomSection.kt (new)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/di/OnboardingModule.kt (modified)
- feature/onboarding/src/test/kotlin/com/rulebook/feature/onboarding/OnboardingScreenTest.kt (new)
- feature/onboarding/src/test/kotlin/com/rulebook/feature/onboarding/OnboardingViewModelTest.kt (new)
- feature/onboarding/build.gradle.kts (modified)
- gradle/libs.versions.toml (modified)

## Dependencies

- **Depends On:** Story 3.1 (navigation setup)
- **Blocks:** Story 3.3 (Screen 2), Story 3.6 (Page Indicator)
- **Can Parallel With:** None

### Dependency Rationale
- Story 3.1: OnboardingScreen needs navigation route established
- Story 3.3: Screen 2 builds on HorizontalPager foundation from this story
- Story 3.6: Page indicator depends on pager state from this story
