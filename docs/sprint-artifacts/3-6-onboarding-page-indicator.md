# Story 3.6: Onboarding Page Indicator

Status: in-progress

## Story

As a user,
I want to see my progress through onboarding,
So that I know how many screens remain.

## Acceptance Criteria

1. **Given** the user is viewing onboarding
   **When** page indicators are displayed
   **Then** dots show total pages (2) and current position

2. **And** current page dot is highlighted (filled/larger)

3. **And** inactive dots are subtle but visible

4. **And** indicators use brutalist styling (sharp dots, accent color)

5. **And** indicators are positioned at bottom of content area

## Tasks / Subtasks

- [x] Task 1: Create OnboardingPageIndicator composable (AC: #1, #2, #3)
  - [x] Create `OnboardingPageIndicator.kt` in `feature/onboarding/components`
  - [x] Accept `pageCount: Int` and `currentPage: Int` parameters
  - [x] Render row of dots matching page count
  - [x] Highlight current page dot (different size/color)

- [x] Task 2: Apply brutalist styling to indicators (AC: #4)
  - [x] Use sharp-edged dots (square with slight rounding, not circles)
  - [x] Active dot: Primary accent color, larger size
  - [x] Inactive dots: Surface variant color, smaller size
  - [x] High contrast between active and inactive

- [x] Task 3: Position indicators in bottom section (AC: #5)
  - [x] Place indicators in `OnboardingBottomSection`
  - [x] Position above navigation buttons
  - [x] Add appropriate spacing (16-24dp margin)

- [x] Task 4: Add animated transitions (AC: #2)
  - [x] Animate dot size changes on page transition
  - [x] Use `animateDpAsState` for smooth scaling
  - [x] Keep animation duration short (150-200ms)

- [ ] Task 5: Sync with HorizontalPager state (AC: #1)
  - [ ] Pass `pagerState.currentPage` to indicator
  - [ ] Update when user swipes between pages
  - [ ] Update when user taps Next button

## Dev Notes

### Architecture Context

- **Module:** `feature/onboarding`
- **Package:** `com.rulebook.feature.onboarding.components`
- **File:** `OnboardingPageIndicator.kt`
- **Integration:** Used in `OnboardingBottomSection.kt`

### Implementation Pattern

```kotlin
// OnboardingPageIndicator.kt
@Composable
fun OnboardingPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            val isActive = page == currentPage

            // Animated size for smooth transitions
            val size by animateDpAsState(
                targetValue = if (isActive) 12.dp else 8.dp,
                animationSpec = tween(durationMillis = 150),
                label = "indicator_size"
            )

            // Animated color for smooth transitions
            val color by animateColorAsState(
                targetValue = if (isActive) activeColor else inactiveColor,
                animationSpec = tween(durationMillis = 150),
                label = "indicator_color"
            )

            // Brutalist: square with slight rounding
            Box(
                modifier = Modifier
                    .size(size)
                    .background(
                        color = color,
                        shape = RoundedCornerShape(2.dp)  // Slightly rounded square
                    )
            )
        }
    }
}
```

### Brutalist Styling Specifications

| Element | Value | Rationale |
|---------|-------|-----------|
| Shape | Slightly rounded square (2dp radius) | Sharp but not harsh |
| Active size | 12dp | Prominent, clear indication |
| Inactive size | 8dp | Visible but subordinate |
| Spacing | 12dp | Clear separation |
| Active color | `primary` | Accent color |
| Inactive color | `surfaceVariant` | Subtle, not distracting |

### Integration in OnboardingBottomSection

```kotlin
// OnboardingBottomSection.kt
@Composable
fun OnboardingBottomSection(
    pagerState: PagerState,
    onNextClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicator
        OnboardingPageIndicator(
            pageCount = 2,
            currentPage = pagerState.currentPage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation button (conditional)
        when (pagerState.currentPage) {
            0 -> RulebookButton(
                text = "Next",
                onClick = onNextClick,
                modifier = Modifier.fillMaxWidth()
            )
            1 -> RulebookButton(
                text = "Get Started",
                onClick = onGetStartedClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
```

### Alternative: Using PagerState Directly

```kotlin
// If using pagerState directly for more accurate position
@Composable
fun OnboardingPageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { page ->
            // Calculate offset-aware indication (for mid-swipe states)
            val pageOffset = (pagerState.currentPage - page) +
                pagerState.currentPageOffsetFraction

            val scale by animateFloatAsState(
                targetValue = if (abs(pageOffset) < 0.5f) 1.5f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "indicator_scale"
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(
                        color = if (abs(pageOffset) < 0.5f) activeColor else inactiveColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
```

### Visual Layout

```
┌─────────────────────────────────────┐
│                                     │
│           Page Content              │
│                                     │
│                                     │
├─────────────────────────────────────┤
│                                     │
│            ▪️  ▫️                    │ ← Page Indicator
│                                     │
│         ┌───────────────┐           │
│         │     Next      │           │ ← Navigation Button
│         └───────────────┘           │
│                                     │
└─────────────────────────────────────┘
```

### Animation Specifications

| Animation | Duration | Easing |
|-----------|----------|--------|
| Size change | 150ms | `tween` (default easing) |
| Color change | 150ms | `tween` (default easing) |
| Alternative: spring | Medium stiffness | `spring` |

### Accessibility

```kotlin
Row(
    modifier = modifier.semantics {
        contentDescription = "Page ${currentPage + 1} of $pageCount"
    },
    // ...
) {
    // indicators
}
```

### Testing Notes

- Verify 2 dots displayed
- Verify first dot active on page 1
- Verify second dot active on page 2
- Verify animation on page change (swipe and button)
- Verify brutalist styling (square-ish dots)
- Verify colors match design system

### Edge Cases

| Scenario | Behavior |
|----------|----------|
| Fast swipe between pages | Animation completes smoothly |
| Slow swipe mid-transition | Indicator tracks position |
| Accessibility mode | Announces "Page X of Y" |

### Project Structure Notes

```
feature/onboarding/components/
├── OnboardingPage1Content.kt
├── OnboardingPage2Content.kt
├── OnboardingBottomSection.kt  (updated to include indicator)
└── OnboardingPageIndicator.kt  (NEW)
```

### References

- [Source: docs/architecture.md#Composable Structure]
- [Source: docs/epics/epic-3-onboarding-experience.md#Story 3.6]
- [Source: docs/sprint-artifacts/3-2-onboarding-screen-1-value-proposition.md] (HorizontalPager)
- [Source: docs/sprint-artifacts/1-7-design-tokens-theme.md] (Colors)

## Dev Agent Record

### Context Reference
- Story 3.2: HorizontalPager and pager state
- Epic 1: Design system colors and styling
- Architecture: Composable component patterns

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List
- Task 1 Complete: Created OnboardingPageIndicator composable with pageCount/currentPage params, row of animated dots, active/inactive states
- Task 2 Complete: Brutalist styling already applied - RoundedCornerShape(2dp), primary/surfaceVariant colors, 12dp/8dp sizes
- Task 3 Complete: Integrated OnboardingPageIndicator into OnboardingBottomSection with 24dp spacing above button
- Task 4 Complete: Animations already implemented - animateDpAsState/animateColorAsState with 150ms tween duration

### File List
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingPageIndicator.kt (NEW)
- feature/onboarding/src/test/kotlin/com/rulebook/feature/onboarding/components/OnboardingPageIndicatorTest.kt (NEW)
- feature/onboarding/src/main/kotlin/com/rulebook/feature/onboarding/components/OnboardingBottomSection.kt (MODIFIED)

## Dependencies

- **Depends On:** Story 3.2 (HorizontalPager setup)
- **Blocks:** None
- **Can Parallel With:** Story 3.3 (Screen 2)

### Dependency Rationale
- Story 3.2: Page indicator syncs with HorizontalPager state created in 3.2
- Story 3.3: Both can be developed in parallel once HorizontalPager exists
