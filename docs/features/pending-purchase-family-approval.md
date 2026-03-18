# Pending Purchase Handling for Family Approval (Ask-to-Buy)

**Date:** 2026-03-18
**Related Issue:** RULE-232 (Story 8.9)
**Related Files:** PurchaseViewModel, BillingRepository, PendingPurchaseChecker, PendingPurchasePreferencesSource, PurchaseScreen, MainActivity

## Overview

This feature implements graceful handling of pending purchases when Google Play's Ask-to-Buy (family sharing approval requirement) is enabled. When a user attempts to purchase but their account requires family approval, the app displays a clear message and automatically checks for resolution when the app is resumed, delivering credits silently once approved.

## What Was Built

- **Pending Purchase Detection** - Captures `Purchase.PurchaseState.PENDING` in the billing listener
- **DataStore Persistence** - Stores pending purchase tokens and product IDs for resume-time checking
- **Resume-Time Resolution Checking** - `PendingPurchaseChecker` queries billing on app resume to detect if pending purchase was approved
- **Automatic Credit Delivery** - Consumes approved purchases and delivers credits without user intervention
- **User-Facing Dialog** - Informs user their purchase is waiting for approval with clear messaging

## Technical Implementation

### Key Files

- **`BillingClientWrapper.kt`** - Detects pending purchases in listener, populates `PurchaseUpdate.pendingPurchaseTokens`
- **`PurchaseUpdate.kt`** - Data class extended with `pendingPurchaseTokens: List<String>` and `pendingProductIds: List<String>`
- **`BillingRepository.kt`** - New `checkPendingPurchases(): Result<PendingPurchaseResolution>` interface method
- **`BillingRepositoryImpl.kt`** - Implements pending check: queries all purchases (PURCHASED + PENDING states), returns resolution state
- **`PendingPurchaseResolution.kt`** - Sealed class: `StillPending`, `Purchased(token, productId)`, `NotFound`
- **`RulebookPreferences.kt`** - Added `pendingPurchaseToken` and `pendingPurchaseProductId` Flow preferences
- **`PendingPurchasePreferencesSource.kt`** - Interface for testable dependency injection (following `CreditPreferencesSource` pattern)
- **`PurchaseViewModel.kt`** - Handles pending detection in `handlePurchaseUpdate()`, stores to DataStore, checks resolution on init
- **`PendingPurchaseChecker.kt`** - App-level utility that reads pending token from DataStore, queries billing, consumes if resolved, delivers credits
- **`MainActivity.kt`** - Calls `PendingPurchaseChecker.checkAndResolve()` in `onResume()` lifecycle
- **`PurchaseScreen.kt`** - New `AlertDialog` for pending state with hourglass icon and "OK" button

### Key Patterns

- **Resume-Time Checking Pattern**: Store minimal state (pending token) in persistent DataStore, check on app foreground to detect resolution without background tasks
- **Preferences Source Interface**: Extracted interface `PendingPurchasePreferencesSource` for testability (mirroring `CreditPreferencesSource`)
- **Sealed Resolution States**: `PendingPurchaseResolution` sealed class clearly represents three possible outcomes (still pending, purchased, not found)
- **Separated Concerns**: App-level resume check (`PendingPurchaseChecker`) vs ViewModel-level detection (`PurchaseViewModel`)

### Code Examples

**Detecting Pending in Listener:**
```kotlin
if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
    pendingTokens.add(purchase.purchaseToken)
    pendingProductIds.add(purchase.products[0])
}
```

**Checking Pending on Resume:**
```kotlin
override fun onResume() {
    super.onResume()
    lifecycleScope.launch {
        val result = pendingPurchaseChecker.checkAndResolve()
        if (result is PendingCheckResult.Resolved) {
            showToast("Purchase approved! ${result.creditsAdded} credits added")
        }
    }
}
```

**Storing and Retrieving Pending Token:**
```kotlin
// Store in ViewModel
pendingPurchasePreferencesSource.setPendingPurchase(token, productId)

// Retrieve in PendingPurchaseChecker
val token = pendingPurchasePreferencesSource.pendingPurchaseToken.first()
```

## How to Use

### For Users

1. User attempts to purchase credits in the app
2. If Ask-to-Buy is enabled on their account, they see "Purchase Pending" dialog
3. Family manager approves the purchase on another device
4. When user opens the app next, credits are automatically added silently
5. Snackbar notification confirms "Purchase approved! X credits added"

### For Developers

**Detecting Pending Purchases:**
- The `PurchasesUpdatedListener` callback in `BillingClientWrapper` automatically detects `PENDING` state
- `PurchaseUpdate` data class includes `pendingPurchaseTokens` and `pendingProductIds` lists
- `PurchaseViewModel` handles the detection and stores pending state in DataStore

**Querying Pending Status:**
```kotlin
val resolution = billingRepository.checkPendingPurchases()
when (resolution) {
    is PendingPurchaseResolution.Purchased -> {
        // Consume and deliver
        purchaseVerifier.verifyAndConsume(token)
        creditRepository.addCredits(productId)
    }
    is PendingPurchaseResolution.StillPending -> {
        // Do nothing, will check again on next resume
    }
    is PendingPurchaseResolution.NotFound -> {
        // Token no longer valid, likely cancelled
        clearPendingToken()
    }
}
```

**Displaying Pending State:**
```kotlin
if (uiState.purchaseState is PurchaseState.Pending) {
    AlertDialog(
        title = "Purchase Pending",
        text = "Your purchase is waiting for approval...",
        icon = Icons.Outlined.HourglassEmpty
    )
}
```

## Configuration

No configuration options. The pending purchase check runs automatically on app resume if a pending token is stored in DataStore.

## Architecture Decisions

- **Resume-Time (Not Background Task)**: We check for resolution when the app comes to foreground rather than using background tasks/workers. This is simpler, faster, and aligns with typical app usage patterns (users open the app to check progress).

- **Separate App-Level Checker**: `PendingPurchaseChecker` lives in `core/billing/` (not in ViewModel) to keep it reusable and testable separate from the purchase flow.

- **Sealed Result States**: Clear enumeration of possible outcomes (still pending, purchased, not found) prevents bugs from missing edge cases.

- **Minimal Persistent State**: Only store the token, not the full purchase object. Tokens are small, immutable, and sufficient to query the current state.

- **DataStore (Not SharedPreferences)**: Uses DataStore for type-safe, atomic operations on purchase state, consistent with app-wide preference architecture.

## Known Limitations & Notes

- Does not handle family members approving on the same device (user would need to restart app or navigate away/back to paywall)
- Toast notification used on app resume (from Activity context) rather than Snackbar (requires Compose context)
- No automatic background checking; resolution is discovered on app foreground only
- If purchase is cancelled by approver, token is cleared on next resume check (`NotFound` result)

## Related Documentation

- See [`docs/features/google-play-billing-integration.md`](google-play-billing-integration.md) for base billing integration details
- See [`docs/features/purchase-verification-client-side-interim.md`](purchase-verification-client-side-interim.md) for purchase consumption and credit delivery
- See [`docs/features/credit-balance-display-component.md`](credit-balance-display-component.md) for credit balance display
