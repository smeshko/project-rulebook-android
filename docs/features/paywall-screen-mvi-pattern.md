# Paywall Screen MVI Pattern (RULE-226)

**Date:** 2026-02-13
**Related Files:**
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseUiState.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseViewModel.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseEvent.kt`
- `feature/purchase/src/test/kotlin/com/rulebook/feature/purchase/PurchaseViewModelTest.kt`

## Overview

The Paywall screen implements a complete MVI (Model-View-Intent) architecture for managing in-app purchases. This foundation establishes the reactive patterns for loading product data from Google Play billing, observing credit balance, and handling purchase state transitions. The implementation provides a blueprint for other purchase-related features.

## What Was Built

- **PurchaseUiState**: Immutable data class holding all UI state (products, balance, loading, purchase state, error)
- **PurchaseEvent**: Sealed class for one-time UI events (Dismiss, PurchaseSuccess) using Kotlin Channel pattern
- **PurchaseViewModel**: Core business logic for paywall, integrating credit repository, billing repository, and analytics
- **Comprehensive testing**: Full test suite with fake repositories demonstrating testable dependency injection

## Technical Implementation

### Key Files

- `PurchaseUiState.kt`: Single source of truth for paywall UI state with strongly-typed fields
  - Products list from Play Store
  - Current credit balance (reactive from DataStore)
  - Loading state for async operations
  - PurchaseState sealed class for transaction tracking
  - Optional error message for user feedback

- `PurchaseViewModel.kt`: Business logic orchestrator following MVI pattern
  - Uses StateFlow for state exposure (one-way data binding)
  - Uses Channel<PurchaseEvent> for one-time events
  - Integrates three repositories: CreditRepository, BillingRepository, AnalyticsManager
  - Handles initialization, product loading, and event emission

- `PurchaseEvent.kt`: One-time events that don't repeat on recomposition
  - Sealed class with specific subtypes (Dismiss, PurchaseSuccess)
  - Consumed by collector, not retained in state

### Key Patterns

#### 1. **MVI Architecture with StateFlow + Channel**
The pattern separates two types of data flows:
- **State (StateFlow)**: Sticky, replayed to new subscribers, represents current UI state
- **Events (Channel)**: One-time emissions, consumed once, not replayed

This prevents duplicate processing of events on orientation changes or recomposition.

```kotlin
private val _uiState = MutableStateFlow(PurchaseUiState())
val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

private val _events = Channel<PurchaseEvent>(Channel.BUFFERED)
val events: Flow<PurchaseEvent> = _events.receiveAsFlow()
```

#### 2. **Reactive Repository Integration**
The ViewModel observes flows from repositories in `init` block, using `.catch { emit() }` to handle errors gracefully:

```kotlin
creditRepository.creditBalance
    .catch { emit(0) }  // Fallback to 0 on error
    .onEach { balance ->
        _uiState.update { it.copy(currentBalance = balance) }
    }
    .launchIn(viewModelScope)
```

This pattern ensures:
- Reactive updates to UI when credit balance changes
- Graceful error handling without crashing
- Proper cancellation when ViewModel is destroyed

#### 3. **Testable Dependency Injection**
All dependencies injected via constructor, enabling easy mocking:

```kotlin
class PurchaseViewModel(
    private val creditRepository: CreditRepository,
    private val billingRepository: BillingRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel()
```

Fake implementations provided for testing without actual Play Store calls.

#### 4. **Analytics Integration**
All user interactions tracked via AnalyticsManager:
- Product selection
- Restore purchases action
- Paywall dismissal

### Code Examples

**Observing UI State in Composable:**
```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
val balance = uiState.currentBalance
val products = uiState.products
val isLoading = uiState.isLoading
```

**Handling One-Time Events:**
```kotlin
LaunchedEffect(viewModel.events) {
    viewModel.events.collect { event ->
        when (event) {
            is PurchaseEvent.Dismiss -> navController.popBackStack()
            is PurchaseEvent.PurchaseSuccess -> {
                showSnackbar("Added ${event.creditsAdded} credits!")
            }
        }
    }
}
```

**User Action Intents:**
```kotlin
Button(
    onClick = { viewModel.onProductSelected(product.productId) }
) {
    Text("Purchase ${product.title}")
}

TextButton(
    onClick = { viewModel.onRestorePurchases() }
) {
    Text("Restore Purchases")
}
```

## How to Use

1. **Inject in Navigation/Screen Layer:**
   ```kotlin
   val viewModel = koinViewModel<PurchaseViewModel>()
   ```

2. **Observe State in Composables:**
   ```kotlin
   val uiState by viewModel.uiState.collectAsStateWithLifecycle()
   ```

3. **Collect Events for Navigation/Toast:**
   ```kotlin
   LaunchedEffect(viewModel.events) {
       viewModel.events.collect { event ->
           // Handle one-time events
       }
   }
   ```

4. **Emit Intents via ViewModel Methods:**
   ```kotlin
   viewModel.onProductSelected(productId)
   viewModel.onRestorePurchases()
   viewModel.onDismiss()
   ```

## Configuration

| Component | Responsibility | Dependencies |
|-----------|-----------------|--------------|
| PurchaseViewModel | Orchestrate state, load products, track analytics | CreditRepository, BillingRepository, AnalyticsManager |
| CreditRepository | Provide reactive credit balance | DataStore, Room database |
| BillingRepository | Query products from Play Store, manage purchases | Google Play Billing Library |
| AnalyticsManager | Track user interactions | Analytics backend |

## Notes

- **Story 8.3 Scope**: Paywall display and navigation only. Purchase flow (actual transaction) is Story 8.4.
- **Stub Methods**: `onProductSelected()` and `onRestorePurchases()` are stubs in 8.3, will be fully implemented in Story 8.4
- **Error Handling**: Uses `.catch { emit() }` pattern to prevent flow termination on repository errors
- **Testing**: Full unit test coverage with fake repositories. Integration tests will be added in Story 8.4.
- **Next Steps**: Implement PaywallScreen composable, ModalBottomSheet UI, feature cards, and product cards
