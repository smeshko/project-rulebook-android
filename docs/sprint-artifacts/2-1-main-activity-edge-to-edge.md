# Story 2.1: MainActivity with Edge-to-Edge Display

Status: Done

## Story

As a user,
I want the app to display content edge-to-edge,
So that the experience feels modern and immersive.

## Acceptance Criteria

1. **Given** the app is launched
   **When** MainActivity starts
   **Then** content extends under the status bar and navigation bar (FR50)

2. **And** the status bar is transparent with appropriate icon colors (light/dark)

3. **And** the navigation bar is transparent or matches the bottom nav color

4. **And** content is properly inset to avoid overlap with system UI

## Tasks / Subtasks

- [x] Task 1: Create MainActivity with edge-to-edge setup (AC: #1, #2, #3)
  - [x] Create MainActivity.kt in app module
  - [x] Call `enableEdgeToEdge()` in onCreate before setContent
  - [x] Set transparent status bar with system icon colors
  - [x] Configure navigation bar appearance
- [x] Task 2: Apply WindowInsets to content (AC: #4)
  - [x] Use `WindowInsets.systemBars` for padding
  - [x] Create root composable that applies insets
  - [x] Ensure content doesn't overlap with status/nav bars
- [x] Task 3: Configure theme-aware status bar icons (AC: #2)
  - [x] Light icons on dark backgrounds
  - [x] Dark icons on light backgrounds
  - [x] Test with both light and dark themes
- [x] Task 4: Create RulebookApp root composable
  - [x] Create RulebookApp.kt as main entry composable
  - [x] Apply RulebookTheme wrapper
  - [x] Prepare scaffold structure for navigation (Story 2.7)

## Dev Notes

### Architecture Context

- **Module:** app
- **Package:** com.rulebook.app
- **Key Classes:** MainActivity.kt, RulebookApp.kt

### Implementation Pattern

```kotlin
// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge BEFORE setContent
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RulebookTheme {
                RulebookApp()
            }
        }
    }
}

// RulebookApp.kt
@Composable
fun RulebookApp() {
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()

    // Set status bar icons based on theme
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !isDarkTheme
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // NavHost will be added in Story 2.2
    }
}
```

### Edge-to-Edge Requirements

| Element | Configuration |
|---------|---------------|
| Status bar | Transparent, icons adapt to theme |
| Navigation bar | Transparent or surface color |
| Content | Uses WindowInsets for padding |
| API | AndroidX Activity `enableEdgeToEdge()` |

### Dependencies Required

```kotlin
// Already in version catalog from Epic 1
implementation(libs.androidx.activity.compose) // enableEdgeToEdge()
implementation(libs.accompanist.systemuicontroller) // Status bar control
```

### WindowInsets Application

```kotlin
Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.systemBars
) { innerPadding ->
    // Content with padding
    Box(modifier = Modifier.padding(innerPadding)) {
        // Screen content
    }
}
```

### Testing Notes

- Verify status bar icons change with theme toggle
- Verify content doesn't overlap status bar
- Verify navigation bar handling
- Test on Android 14+ devices

### References

- [Source: docs/architecture.md#Project Structure & Boundaries]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.1]
- [Source: docs/ux-design-specification.md#Platform Integration]

## Dev Agent Record

### Context Reference
- RulebookTheme from Epic 1 (Story 1.7)
- Core design system components
- Architecture module structure

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Build successful with Android Studio JDK 21
- No compilation warnings after removing deprecated Window.statusBarColor/navigationBarColor calls

### Completion Notes List
- Updated MainActivity.kt to use RulebookTheme from core/designsystem module
- Created RulebookApp.kt as root composable with edge-to-edge support
- enableEdgeToEdge() called before super.onCreate() for proper system bar handling
- WindowInsets.systemBars applied via Scaffold's contentWindowInsets
- ConfigureSystemBars composable sets light/dark status bar icons based on theme
- Removed unused local theme (app/src/main/kotlin/com/rulebook/ui/theme/Theme.kt)
- All acceptance criteria satisfied

### File List
- app/src/main/kotlin/com/rulebook/MainActivity.kt (modified)
- app/src/main/kotlin/com/rulebook/RulebookApp.kt (created)
- app/src/main/kotlin/com/rulebook/ui/theme/Theme.kt (deleted)

## Dependencies

- **Depends On:** Epic 1 (theme, design system)
- **Blocks:** Story 2.2
- **Can Parallel With:** None (first story in epic)

### Dependency Rationale
- Epic 1: Requires RulebookTheme for theming wrapper
- Story 2.2: Navigation Host needs MainActivity as foundation
