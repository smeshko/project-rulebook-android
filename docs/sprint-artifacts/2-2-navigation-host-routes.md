# Story 2.2: Navigation Host & Route Definitions

Status: in-progress

## Story

As a developer,
I want Compose Navigation configured with route definitions,
So that screens can be navigated to consistently.

## Acceptance Criteria

1. **Given** the app module
   **When** navigation is configured
   **Then** `NavigationDestination.kt` defines sealed class routes:
   ```kotlin
   sealed class Route(val route: String) {
       object Library : Route("library")
       object Settings : Route("settings")
       object Camera : Route("camera")
       object Rules : Route("rules/{gameId}")
       object Onboarding : Route("onboarding")
       object Purchase : Route("purchase")
   }
   ```

2. **And** `RulebookNavHost.kt` configures NavHost with all destinations

3. **And** navigation arguments are type-safe

4. **And** deep links are configured for future use

## Tasks / Subtasks

- [x] Task 1: Create NavigationDestination.kt (AC: #1)
  - [x] Define sealed class Route with route string
  - [x] Create Library, Settings, Camera, Rules, Onboarding, Purchase objects
  - [x] Add gameId argument pattern for Rules route
- [x] Task 2: Create RulebookNavHost composable (AC: #2)
  - [x] Create NavHost with startDestination = Library
  - [x] Add composable() for each route
  - [x] Pass NavController to screen composables
- [x] Task 3: Add type-safe navigation arguments (AC: #3)
  - [x] Create navArgument for gameId in Rules route
  - [x] Use NavType.StringType for arguments
  - [x] Create helper extension functions for navigation
- [x] Task 4: Configure deep links structure (AC: #4)
  - [x] Add deepLinks parameter to Routes that need them
  - [x] Prepare URI patterns for future use
  - [x] Document deep link scheme
- [x] Task 5: Integrate NavHost into RulebookApp
  - [x] Add NavController via rememberNavController()
  - [x] Replace placeholder with RulebookNavHost
  - [x] Create placeholder screens for each destination

## Dev Notes

### Architecture Context

- **Module:** app
- **Package:** com.rulebook.app.navigation
- **Key Files:** NavigationDestination.kt, RulebookNavHost.kt

### Implementation Pattern

```kotlin
// NavigationDestination.kt
sealed class Route(val route: String) {
    object Library : Route("library")
    object Settings : Route("settings")
    object Camera : Route("camera")
    object Onboarding : Route("onboarding")
    object Purchase : Route("purchase")

    // Route with argument
    object Rules : Route("rules/{gameId}") {
        fun createRoute(gameId: String) = "rules/$gameId"
    }
}

// Navigation arguments
object RulebookNavArgs {
    const val GAME_ID = "gameId"
}
```

```kotlin
// RulebookNavHost.kt
@Composable
fun RulebookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Route.Library.route,
        modifier = modifier
    ) {
        composable(Route.Library.route) {
            LibraryScreen(
                onNavigateToCamera = { navController.navigate(Route.Camera.route) },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) },
                onNavigateToRules = { gameId ->
                    navController.navigate(Route.Rules.createRoute(gameId))
                }
            )
        }

        composable(Route.Settings.route) {
            SettingsScreen()
        }

        composable(Route.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRules = { gameId ->
                    navController.navigate(Route.Rules.createRoute(gameId))
                }
            )
        }

        composable(
            route = Route.Rules.route,
            arguments = listOf(
                navArgument(RulebookNavArgs.GAME_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString(RulebookNavArgs.GAME_ID) ?: ""
            RulesScreen(
                gameId = gameId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Route.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Route.Library.route) {
                        popUpTo(Route.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Purchase.route) {
            PurchaseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
```

### Navigation Extension Functions

```kotlin
// NavControllerExtensions.kt
fun NavController.navigateToRules(gameId: String) {
    navigate(Route.Rules.createRoute(gameId))
}

fun NavController.navigateToLibrary() {
    navigate(Route.Library.route) {
        popUpTo(Route.Library.route) { inclusive = true }
    }
}
```

### Deep Link Configuration (for future)

```kotlin
// Deep link scheme: rulebook://
composable(
    route = Route.Rules.route,
    arguments = listOf(navArgument(RulebookNavArgs.GAME_ID) { type = NavType.StringType }),
    deepLinks = listOf(
        navDeepLink { uriPattern = "rulebook://rules/{gameId}" }
    )
) { /* ... */ }
```

### Placeholder Screens

Create temporary placeholder composables for each screen:

```kotlin
@Composable
fun LibraryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Library Screen - Coming in Story 2.5")
    }
}
```

### Dependencies Required

```kotlin
// Already in version catalog from Epic 1
implementation(libs.androidx.navigation.compose)
```

### References

- [Source: docs/architecture.md#Project Setup & Foundation]
- [Source: docs/architecture.md#Core Architectural Decisions]
- [Source: docs/epics/epic-2-app-shell-navigation.md#Story 2.2]

## Dev Agent Record

### Context Reference
- MainActivity from Story 2.1
- Compose Navigation 2.8.x architecture decisions
- Module structure from docs/architecture.md

### Agent Model Used
{{agent_model_name_version}}

### Debug Log References

### Completion Notes List
- Task 1: Created NavigationDestination.kt with sealed class Route defining all app destinations (Library, Settings, Camera, Rules, Onboarding, Purchase). Rules route includes gameId argument pattern and createRoute helper function. Added RulebookNavArgs object for centralized argument keys. Unit tests verify all route strings and argument handling.
- Task 2: Created RulebookNavHost composable with NavHost container. Configured startDestination to Library route. Added composable() entries for all 6 destinations with placeholder content. NavController is accepted as parameter for navigation management.
- Task 3: Added type-safe navigation arguments for Rules route using navArgument with NavType.StringType. Created NavControllerExtensions.kt with helper functions for all navigation destinations (navigateToRules, navigateToLibrary, navigateToSettings, navigateToCamera, navigateToOnboarding, navigateToPurchase, completeOnboarding).
- Task 4: Configured deep links for Rules route with pattern "rulebook://rules/{gameId}". Created DeepLinkConfig.kt with centralized scheme configuration and comprehensive documentation including manifest setup, testing instructions, and future deep link patterns.
- Task 5: Integrated RulebookNavHost into RulebookApp with rememberNavController(). Created PlaceholderScreens.kt with temporary screens for all destinations (LibraryPlaceholder, SettingsPlaceholder, CameraPlaceholder, OnboardingPlaceholder, PurchasePlaceholder, RulesPlaceholder). RulesPlaceholder accepts gameId parameter.

### File List
- app/src/main/kotlin/com/rulebook/navigation/NavigationDestination.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/NavControllerExtensions.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/DeepLinkConfig.kt (new)
- app/src/main/kotlin/com/rulebook/navigation/PlaceholderScreens.kt (new)
- app/src/main/kotlin/com/rulebook/RulebookApp.kt (modified)
- app/src/test/kotlin/com/rulebook/navigation/NavigationDestinationTest.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/RulebookNavHostTest.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/NavControllerExtensionsTest.kt (new)
- app/src/test/kotlin/com/rulebook/navigation/DeepLinkConfigTest.kt (new)

## Dependencies

- **Depends On:** Story 2.1 (MainActivity with edge-to-edge)
- **Blocks:** Story 2.3, Story 2.8
- **Can Parallel With:** None

### Dependency Rationale
- Story 2.1: NavHost must be integrated into MainActivity/RulebookApp
- Story 2.3: Bottom navigation requires route definitions
- Story 2.8: Predictive back needs navigation structure
