# Story 3.5: Skip Onboarding Functionality

Status: done

## Story

As an impatient user,
I want to skip onboarding at any point,
So that I can start using the app immediately.

## Acceptance Criteria

1. **Given** the user is on any onboarding screen (FR3)
   **When** the user taps "Skip"
   **Then** onboarding is marked complete

2. **And** 3 free credits are awarded (same as completing)

3. **And** user is navigated to Library

4. **And** skip button is visible on both screens

5. **And** skip does not require confirmation

## Tasks / Subtasks

- [x] Task 1: Verify Skip button visibility on both pages (AC: #4)
  - [x] Confirm Skip button positioned in OnboardingScreen (parent)
  - [x] Verify button visible when on page 1
  - [x] Verify button visible when on page 2
  - [x] Use consistent positioning (top-right with status bar padding)

- [x] Task 2: Wire Skip button to completion logic (AC: #1, #2, #3)
  - [x] Call `viewModel.onSkipClicked()` from Skip button
  - [x] `onSkipClicked()` calls same `completeOnboarding()` as Get Started
  - [x] Ensure atomic credit award and flag setting
  - [x] Trigger navigation to Library

- [x] Task 3: Verify no confirmation dialog (AC: #5)
  - [x] Skip executes immediately on tap
  - [x] No AlertDialog or BottomSheet confirmation
  - [x] Single tap completes action

- [x] Task 4: Style Skip button appropriately (AC: #4)
  - [x] Use `TextButton` style (secondary/text variant)
  - [x] Color: `onSurfaceVariant` or subtle secondary
  - [x] Typography: `labelLarge`
  - [x] No heavy decoration or background

- [x] Task 5: Add accessibility support (AC: #4)
  - [x] Add contentDescription for Skip button
  - [x] Ensure adequate touch target (48dp minimum)
  - [x] Support TalkBack announcement

## Dev Notes

### Architecture Context

- **Module:** `feature/onboarding`
- **Files:** `OnboardingScreen.kt`, `OnboardingViewModel.kt`
- **Pattern:** Reuses completion logic from Story 3.4

### Implementation Pattern

```kotlin
// OnboardingScreen.kt - Skip button implementation
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = koinViewModel()
) {
    // ... pager setup

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // ... page content
        }

        // Skip button - ALWAYS visible (parent level, not per-page)
        TextButton(
            onClick = { viewModel.onSkipClicked() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .statusBarsPadding()
                .semantics {
                    contentDescription = "Skip onboarding and go to library"
                }
        ) {
            Text(
                text = "Skip",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bottom section
        OnboardingBottomSection(...)
    }
}
```

### ViewModel Skip Logic

```kotlin
// OnboardingViewModel.kt
class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    // Skip uses SAME completion logic as Get Started
    fun onSkipClicked() {
        completeOnboarding()
    }

    fun onGetStartedClicked() {
        completeOnboarding()
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            // Atomic: both operations together
            onboardingRepository.completeOnboardingWithCredits(
                creditAmount = INITIAL_CREDITS
            )
            _navigationEvent.send(OnboardingNavigationEvent.NavigateToLibrary)
        }
    }

    companion object {
        const val INITIAL_CREDITS = 3
    }
}
```

### Skip Button Styling

```kotlin
// Skip button styling requirements
TextButton(
    onClick = { viewModel.onSkipClicked() },
    modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 8.dp, end = 8.dp)
        .statusBarsPadding()
        .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp), // Touch target
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    Text(
        text = "Skip",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
```

### Button Positioning Rationale

| Position | Why |
|----------|-----|
| Top-right | Standard skip button location |
| Parent level | Always visible regardless of page |
| Status bar padding | Not obscured by system UI |
| No background | Subtle, non-competing with main CTA |

### User Flow

```
Page 1                    Page 2
┌─────────────────────┐  ┌─────────────────────┐
│             [Skip]  │  │             [Skip]  │
│                     │  │                     │
│    Scan any game    │  │  3 free scans on us │
│        box          │  │                     │
│                     │  │                     │
│      [Next]         │  │    [Get Started]    │
└─────────────────────┘  └─────────────────────┘
        │                        │
        │     Tap Skip           │
        └───────────┬────────────┘
                    ▼
        ┌───────────────────────┐
        │  completeOnboarding() │
        │  • Set flag = true    │
        │  • Award 3 credits    │
        │  • Navigate Library   │
        └───────────────────────┘
```

### No Confirmation Design Decision

**Rationale for no confirmation:**
- User explicitly chose to skip (intentional action)
- Credits are still awarded (no penalty)
- Can revisit onboarding info in Settings if needed (future)
- Reduces friction for impatient users
- Industry standard for onboarding skip

### Accessibility Requirements

```kotlin
// Accessibility annotations
TextButton(
    onClick = { viewModel.onSkipClicked() },
    modifier = Modifier
        .semantics {
            contentDescription = "Skip onboarding and go to library"
            role = Role.Button
        }
        .defaultMinSize(minHeight = 48.dp, minWidth = 48.dp)
) {
    Text(
        text = "Skip",
        // TalkBack will read "Skip, button"
    )
}
```

### Testing Notes

- Verify Skip visible on page 1
- Verify Skip visible on page 2
- Verify Skip awards 3 credits
- Verify Skip sets `hasCompletedOnboarding = true`
- Verify Skip navigates to Library
- Verify no confirmation dialog appears
- Test Skip accessibility with TalkBack
- Test Skip touch target size (48dp)

### Edge Cases

| Scenario | Behavior |
|----------|----------|
| Double-tap Skip | Idempotent - no double credits |
| Skip during page transition | Still works correctly |
| Back after Skip | Cannot return to onboarding |

### Project Structure Notes

No new files - updates to existing:
- `feature/onboarding/OnboardingScreen.kt` - Skip button positioning
- `feature/onboarding/OnboardingViewModel.kt` - Skip handler (already exists)

### References

- [Source: docs/architecture.md#Composable Structure]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.5]
- [Source: docs/sprint-artifacts/3-4-credit-award-on-completion.md] (completion logic)
- [Android Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)

## Dev Agent Record

### Context Reference
- Story 3.4: Credit award logic to reuse
- Story 3.2/3.3: OnboardingScreen where Skip button lives
- Architecture: ViewModel event handling pattern

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

### Completion Notes List
- Skip button implementation already existed from Story 3.4 foundation work
- Enhanced with 48dp minimum touch target for accessibility compliance
- Added semantic contentDescription for TalkBack support
- Button positioned at parent Box level ensuring visibility on all pager pages
- Uses same atomic completion flow as Get Started button

### File List
- `feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/OnboardingScreen.kt` (modified)

## Dependencies

- **Depends On:** Story 3.4 (credit award logic)
- **Blocks:** None
- **Can Parallel With:** Story 3.7 (Analytics)

### Dependency Rationale
- Story 3.4: Skip reuses `completeOnboarding()` which includes credit award
- Story 3.7: Skip triggers `onboarding_skipped` analytics event
