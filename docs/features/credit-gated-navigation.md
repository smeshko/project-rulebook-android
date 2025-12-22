# Credit-Gated Navigation Pattern

**Date:** 2025-12-22
**Related Files:**
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/NavigationAction.kt`
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt`

## Overview

The credit-gated navigation pattern implements a pre-check system that validates user credit balance before allowing navigation to premium features. When a user captures or selects a photo, the system checks their credit balance and either proceeds to image processing (if credits available) or redirects to the paywall. This pattern ensures proper resource cleanup and prevents memory leaks by clearing captured images after navigation.

## What Was Built

- Credit balance checking before navigation to premium features
- Sealed class `NavigationAction` for type-safe navigation decisions
- Integration between `CreditRepository`, `CameraViewModel`, and navigation graph
- Memory leak prevention via automatic image URI cleanup after navigation
- Support for both photo capture and gallery selection flows

## Technical Implementation

### Key Files

- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/NavigationAction.kt`: Sealed class defining navigation outcomes based on credit check (ProceedToProcessing or ShowPaywall)
- `feature/camera/src/main/kotlin/com/rulebook/feature/camera/CameraViewModel.kt`: Contains `checkCreditsAndNavigate()` method that evaluates credit balance and returns appropriate navigation action
- `app/src/main/kotlin/com/rulebook/navigation/RulebookNavHost.kt`: Camera route implementation that calls credit check and handles navigation with cleanup

### Key Patterns

- **Credit Check Before Navigation**: Credit validation happens BEFORE navigating to processing, but credit deduction happens LATER after successful scan save (Story 5.7). This prevents charging users for failed processing attempts.

- **Sealed Class for Navigation**: `NavigationAction` uses a sealed class pattern to ensure type-safe exhaustive handling of navigation outcomes in when expressions.

- **Resource Cleanup After Navigation**: After navigating to either Processing or Purchase screens, `clearCapturedImage()` is called to prevent memory leaks by removing the image URI from ViewModel state.

- **Consistent Flow for Multiple Entry Points**: Both `onPhotoCaptured` and `onGalleryImageSelected` callbacks use the same credit check and cleanup pattern, ensuring consistent behavior.

- **Repository-Based Balance Observation**: `CameraViewModel` observes credit balance from `CreditRepository` via Flow, keeping UI state synchronized with the latest balance.

## How to Use

### Implementing Credit-Gated Features

1. **Add Navigation Check in ViewModel**
   ```kotlin
   fun checkCreditsAndNavigate(): NavigationAction {
       val currentBalance = _uiState.value.creditBalance
       return if (currentBalance > 0) {
           NavigationAction.ProceedToProcessing
       } else {
           NavigationAction.ShowPaywall
       }
   }
   ```

2. **Handle Navigation in NavHost**
   ```kotlin
   composable(route = Route.Camera.route) {
       CameraScreen(
           onPhotoCaptured = { imageUri ->
               when (viewModel.checkCreditsAndNavigate()) {
                   NavigationAction.ProceedToProcessing -> {
                       navController.navigate(Route.Processing.createRoute(imageUri))
                       viewModel.clearCapturedImage()
                   }
                   NavigationAction.ShowPaywall -> {
                       navController.navigate(Route.Purchase.route)
                       viewModel.clearCapturedImage()
                   }
               }
           }
       )
   }
   ```

3. **Clean Up Resources**
   Always call cleanup methods after navigation to prevent memory leaks:
   ```kotlin
   viewModel.clearCapturedImage()
   ```

### Observing Credit Balance

In your ViewModel, observe credit balance from the repository:
```kotlin
init {
    creditRepository.creditBalance
        .catch { emit(0) }  // Graceful degradation on errors
        .onEach { balance ->
            _uiState.update { it.copy(creditBalance = balance) }
        }
        .launchIn(viewModelScope)
}
```

## Configuration

No additional configuration required. The pattern relies on:
- `CreditRepository` being available via dependency injection
- Navigation routes defined in your navigation graph
- ViewModel scoping to the appropriate composable lifecycle

## Notes

- **Credit deduction timing**: Credits are checked but NOT deducted during navigation. The actual deduction happens later when the scan is successfully saved (Story 5.7). This prevents charging for failed processing.

- **Error handling**: The credit balance observation includes `.catch { emit(0) }` to handle DataStore IOExceptions gracefully, defaulting to zero credits to keep the UI functional.

- **Memory management**: The `clearCapturedImage()` call after navigation is critical to prevent memory leaks, especially with large image URIs stored in ViewModel state.

- **Reusability**: This pattern can be extended to other premium features that require credit checks (e.g., advanced analysis, export features, etc.) by following the same NavigationAction sealed class approach.

- **Testing considerations**: When testing credit-gated flows, mock the `CreditRepository` to simulate different credit balance scenarios (zero credits, sufficient credits, etc.).
