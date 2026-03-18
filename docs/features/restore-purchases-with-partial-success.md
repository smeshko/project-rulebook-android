# Restore Purchases with Partial Success Handling

**Date:** 2026-03-18
**Related Story:** RULE-231
**Related Files:**
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseViewModel.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/components/AnimatedDots.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseScreen.kt`

## Overview

Implements a complete restore purchases feature that allows users to recover previously purchased credits if they reinstall the app. The implementation handles a critical edge case: **partial success** where some purchases verify successfully and others fail, allowing users to recover whatever credits are available rather than losing everything due to a single verification failure.

## What Was Built

- **onRestorePurchases() flow in PurchaseViewModel**: Complete implementation that queries unconsumed purchases, verifies each, delivers credits, and emits typed result events
- **AnimatedDots composable**: Reusable loading indicator with staggered pulse animation for in-place loading states
- **Snackbar feedback system**: Three-way snackbar messaging for success, no purchases, and error with retry action
- **Analytics tracking**: Detailed purchase_restored analytics with result type and credits count
- **Mixed success handling**: Gracefully degrades when some purchases fail, delivering credits for what succeeds

## Technical Implementation

### Key Files

- **PurchaseViewModel.kt** (onRestorePurchases method): Orchestrates the entire restore flow, handling success/failure paths and analytics
- **AnimatedDots.kt**: Composable loading indicator that replaces button text during restore operation
- **PurchaseScreen.kt**: Handles snackbar host setup and restore event collection
- **PurchaseUiState.kt**: Added `isRestoring: Boolean` field to track operation state
- **PurchaseEvent.kt**: Three new event types for restore outcomes (Success, NoPurchases, Error)

### Key Patterns

#### 1. **Partial Success Pattern**
The critical insight is handling mixed success/failure scenarios gracefully:

```kotlin
var totalCreditsRestored = 0
for (purchase in purchases) {
    val verifyResult = purchaseVerifier.verifyAndConsume(purchase.purchaseToken, purchase.productId)
    verifyResult.onSuccess { verificationResult ->
        creditRepository.addCredits(verificationResult.credits)
        totalCreditsRestored += verificationResult.credits
    }
    verifyResult.onFailure { error ->
        Log.w(TAG, "Failed to verify/consume restored purchase", error)
        // Continue to next purchase instead of failing entire operation
    }
}
```

**Why this matters:** If a user has 3 unconsumed purchases but 1 fails verification, they should recover 2 worth of credits rather than lose all 3. The alternative (fail-fast) would be poor UX because verification failures can be transient.

#### 2. **Three-Level Result Handling**
The restore operation produces three distinct outcomes:
- **Success**: At least one purchase verified and credits delivered → emit `RestoreSuccess(creditsRestored)`
- **No Purchases**: Query returned empty list → emit `RestoreNoPurchases` (informational, not an error)
- **Error**: Query failed OR all verifications failed → emit `RestoreError(message)`

This allows the UI to provide context-appropriate feedback for each scenario.

#### 3. **Analytics with Granular Result Types**
Tracks `purchase_restored` event with:
- `result`: "success" | "none" | "error" (granular tracking of each outcome)
- `credits_count`: Total credits restored (only for success)

This enables distinguishing between "user has no purchases to restore" (none) vs "something broke" (error).

#### 4. **Loading State Indicator Pattern**
Uses `AnimatedDots` to replace button text during operation:
```kotlin
// In PurchaseScreenContent
TextButton(
    onClick = { viewModel.onRestorePurchases() },
    enabled = !isRestoring && !isPurchaseActive
) {
    if (isRestoring) {
        AnimatedDots()
    } else {
        Text("Restore Purchases")
    }
}
```

This replaces text in-place rather than showing a separate spinner, maintaining UI layout stability.

### Code Examples

#### Handling Restore Events in the UI

```kotlin
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) {
            is PurchaseEvent.RestoreSuccess -> {
                snackbarHostState.showSnackbar("${event.creditsRestored} credits restored!")
            }
            is PurchaseEvent.RestoreNoPurchases -> {
                snackbarHostState.showSnackbar("No purchases to restore")
            }
            is PurchaseEvent.RestoreError -> {
                snackbarHostState.showSnackbar(
                    message = event.message,
                    actionLabel = "Retry",
                    duration = SnackbarDuration.Long
                ).let { result ->
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onRestorePurchases()
                    }
                }
            }
        }
    }
}
```

#### Testing Partial Success

```kotlin
@Test
fun `restore with mixed success and failure purchases delivers only successful credits`() = runTest {
    fakeBillingRepository.unconsumedPurchases = listOf(
        PurchaseInfo("token-ok", "credits_3", "order-1"),
        PurchaseInfo("token-fail", "credits_10", "order-2")
    )
    fakePurchaseVerifier.tokenResults["token-fail"] =
        Result.failure(RuntimeException("Consume failed"))

    viewModel.onRestorePurchases()
    advanceUntilIdle()

    // Only successful credits delivered
    assertEquals(3, fakeCreditRepository.addedCreditsTotal)
    // Still reports success with partial credits
    val event = events.filterIsInstance<PurchaseEvent.RestoreSuccess>().first()
    assertEquals(3, event.creditsRestored)
}
```

## How to Use

1. **From UI**: User taps "Restore Purchases" button in paywall
2. **ViewModel triggers**: `viewModel.onRestorePurchases()`
3. **Query phase**: Calls `billingRepository.queryUnconsumedPurchases()` for INAPP products
4. **Verify phase**: Iterates each purchase, calls `purchaseVerifier.verifyAndConsume(token, productId)`
5. **Credit delivery**: For each successful verification, calls `creditRepository.addCredits(credits)`
6. **UI feedback**: Emits one of three event types, UI shows appropriate snackbar with action (retry if error)

### Customization Points

- **AnimatedDots animation**: Adjust `DotAnimationDurationMs` (400ms) and `DotStaggerMs` (150ms) in AnimatedDots.kt for different timing
- **Snackbar messages**: Update in PurchaseScreen.kt event handler
- **Analytics event name**: "purchase_restored" is hardcoded in onRestorePurchases method

## Configuration

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| DotAnimationDurationMs | const | 400 | Duration of fade pulse animation per dot |
| DotStaggerMs | const | 150 | Delay between each dot's animation start |
| Snackbar duration (success) | SnackbarDuration | Short | How long to show success message |
| Snackbar duration (error) | SnackbarDuration | Long | How long to show error (allows user to tap retry) |

## Notes

- **Unconsumed vs Consumed Purchases**: Google Play only returns purchases in "PURCHASED" state, not "CONSUMED" state. This means restore only works for purchases not yet consumed - limited window on user's device after purchase.

- **Partial Success is Intentional**: The implementation allows partial restoration if some purchases fail verification. This is not a bug - it's a feature that prevents complete loss of credits due to transient failures.

- **Verification Responsibility**: The `PurchaseVerifier` abstraction is responsible for both verification and consumption of purchases. Future Epic 10 (RULE-232) will replace this with server-side verification.

- **Analytics Tracing**: The `purchase_restored` event with granular `result` values (success/none/error) enables monitoring user restore attempts, success rates, and failure patterns to detect verification issues.

- **Concurrency Guard**: The `if (_uiState.value.isRestoring) return` check prevents duplicate restore operations if user taps button multiple times while restoring.

- **Cancellation Safety**: The method properly handles `CancellationException` by rethrowing it, preventing suppression of coroutine cancellation.
