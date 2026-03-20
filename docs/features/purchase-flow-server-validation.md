# Purchase Flow for Server Validation (RULE-256)

**Date:** 2026-03-20
**Related Story:** Epic 10, Story 10.2 (Server-side receipt validation)
**Related Files:** `core/billing/`, `feature/purchase/`, `core/analytics/`

## Overview

Modified the purchase flow to validate receipts on the server instead of consuming them locally via BillingClient. The app now sends the purchase token to the backend for validation, waits for the response, and delivers credits ONLY after server confirmation. This implements the idempotent ALREADY_PROCESSED pattern and prepares the purchase history for refund tracking (Story 10.5).

## What Was Built

### Core Validation Components

- **`ServerSidePurchaseVerifier`**: Replaces `ClientSidePurchaseVerifier` to validate purchases via `ReceiptRepository.validatePurchase()` instead of local `BillingClient.consumeAsync()`
- **`PurchaseHistoryStore`**: New abstraction for storing validated purchase tokens, limited to 20 recent entries, prepared for future refund sync (Story 10.5)
- **`PurchaseState.Validating`**: New state in the purchase state machine, shown between `Processing` and `Success`/`Error`

### ViewModel & State Machine Updates

- **`PurchaseViewModel`**: Updated to:
  - Transition to `Validating(sku)` state after Google Play returns success
  - Call `purchaseVerifier.verifyAndConsume()` (now server-side)
  - Save validated purchase tokens to `PurchaseHistoryStore` on success
  - Track `purchase_validated` analytics event with status property
  - Handle ALREADY_PROCESSED (idempotent success) and INVALID (failure) cases

### UI & Analytics

- **`PurchaseScreen`**: Added:
  - Validating overlay with `AnimatedDots` centered on paywall
  - Text: "Verifying purchase..." (body.body, 17sp)
  - 5-second timeout showing "This is taking longer than expected..." message
  - Updated `isPurchaseActive` to include `Validating` state

- **`AnalyticsManager`**: Added `trackPurchaseValidated(sku: String, status: String)` convenience method firing `purchase_validated` event

## Technical Implementation

### Key Files

- **`core/model/PurchaseState.kt`**: Added `data class Validating(val sku: String)`
- **`core/billing/verification/ServerSidePurchaseVerifier.kt`**: Main validation orchestrator
- **`core/billing/history/PurchaseHistoryStore.kt`** & **`PurchaseHistoryStoreImpl.kt`**: Purchase token persistence via DataStore
- **`feature/purchase/PurchaseViewModel.kt`**: State machine and DI integration
- **`feature/purchase/PurchaseScreen.kt`**: Validating UI overlay
- **`core/analytics/AnalyticsManager.kt`**: Analytics event tracking

### Key Patterns

#### 1. **Dependency Swapping for Verifier**
```kotlin
// core/billing/di/BillingModule.kt
single<PurchaseVerifier> { ServerSidePurchaseVerifier(get()) }
// ReceiptRepository injected from dataModule
```
The Koin binding swaps the verifier implementation. Tests inject `FakePurchaseVerifier` without changing production code.

#### 2. **Result Bridging Pattern**
`ServerSidePurchaseVerifier` bridges `com.rulebook.core.common.Result` (repository) to `kotlin.Result` (PurchaseVerifier interface):
```kotlin
when (validationResult) {
    is AppResult.Success -> Result.success(...)
    is AppResult.Error -> Result.failure(...)
}
```

#### 3. **Idempotent Success Handling**
Both `VALID` and `ALREADY_PROCESSED` return success with granted credits:
```kotlin
ValidationStatus.ALREADY_PROCESSED ->
    Result.success(VerificationResult(credits = validationResult.data.creditsGranted))
```
This ensures the state machine and UI consistently handle both as success.

#### 4. **Purchase History with Size Limit**
`PurchaseHistoryStoreImpl` uses DataStore with newline-delimited entries:
```
token1::productId1
token2::productId2
...
```
Limited to 20 entries (newest first) for refund tracking without unbounded growth.

#### 5. **Validating State in State Machine**
```
Processing(sku) → Validating(sku) → Success(creditsAdded) | Error(message)
```
The intermediate Validating state allows UI to show "verifying..." feedback.

#### 6. **Timeout Fallback UI**
`LaunchedEffect` 5-second timer shows "This is taking longer than expected..." to manage user expectations:
```kotlin
LaunchedEffect(Unit) {
    delay(5000)
    showTimeoutMessage = true
}
```

### Acceptance Criteria Implementation

- ✅ Purchase token sent to backend (NOT `consumeAsync()`)
- ✅ Credits granted ONLY after backend returns `VALID` or `ALREADY_PROCESSED`
- ✅ `INVALID` shows error, does NOT grant credits
- ✅ App does NOT call `consumeAsync()` or `acknowledgePurchase()` locally
- ✅ `Validating` state shown with AnimatedDots overlay
- ✅ 5-second timeout message
- ✅ Analytics event: `purchase_validated` with `status` property
- ✅ Purchase token saved to `PurchaseHistoryStore` for refund tracking (Story 10.5)

## How to Use

### 1. When a Purchase Completes
```
Google Play Sheet Closes
    ↓
BillingRepository emits PurchaseUpdate(responseCode=0, tokens=[...])
    ↓
PurchaseViewModel.handlePurchaseUpdate() called
    ↓
State transitions: Processing → Validating
    ↓
ServerSidePurchaseVerifier.verifyAndConsume(token, productId)
    ↓
ReceiptRepository.validatePurchase() called (network request)
    ↓
Result mapped: VALID/ALREADY_PROCESSED → Success, INVALID → Error
    ↓
State transitions: Validating → Success | Error
    ↓
Analytics: purchase_validated event fired with status
```

### 2. Testing the Validation Flow
```kotlin
// Create FakePurchaseVerifier
val fakePurchaseVerifier = FakePurchaseVerifier()

// Inject into ViewModel
val viewModel = PurchaseViewModel(
    purchaseVerifier = fakePurchaseVerifier,
    ...
)

// Emit purchase update
fakeBillingRepository.emitPurchaseUpdate(
    PurchaseUpdate(responseCode = 0, purchaseTokens = listOf("token"), productIds = listOf("sku"))
)

// Verify token was saved to history
assertTrue(fakePurchaseHistoryStore.savedPurchases.contains("token"))
```

### 3. Restoring Pending Purchases
The restore flow uses the same `ServerSidePurchaseVerifier`:
```kotlin
checkPendingPurchaseResolution() {
    // Queries unconsumed purchases
    // Calls verifier for each
    // Saves tokens to PurchaseHistoryStore
}
```

### 4. Handling Validation Errors
```kotlin
// Network error: shows Error state with message
// INVALID response: shows Error state, no credits granted
// ALREADY_PROCESSED: shows Success state, credits granted
```

## Configuration

| Component | Configuration | Notes |
|-----------|---------------|-------|
| PurchaseHistoryStore | MAX_ENTRIES = 20 | Limit for refund tracking window |
| Validating Timeout | 5 seconds | Shows "longer than expected" message |
| Verifier DI | Koin `single<PurchaseVerifier>` | Swappable for testing |
| Analytics Event | `purchase_validated` | Fired on all paths (valid, invalid, error) |

## Notes

### Server Responsibility
The server now owns:
- Receipt validation via Google Play API
- Idempotent storage of validated receipts
- Credit granting decision
- Acknowledgment/consumption of purchases (client does NOT)

### Refund Tracking (Story 10.5)
`PurchaseHistoryStore` persists validated purchase tokens for the upcoming refund sync implementation. The 20-entry limit maintains a reasonable lookback window without unbounded growth.

### Analytics
The `purchase_validated` event tracks:
- `sku`: Product ID of validated purchase
- `status`: "valid" | "already_processed" | "invalid" | "error"

This helps identify validation failure rates and ALREADY_PROCESSED patterns.

### Idempotency
Treating `ALREADY_PROCESSED` identically to `VALID` ensures:
- Duplicate purchase requests don't fail the user
- Credits are delivered consistently
- The UI doesn't distinguish between first-time and repeat validation

### Testing Without Server
`ServerSidePurchaseVerifier` depends on `ReceiptRepository`, which is easily faked:
```kotlin
fakePurchaseVerifier.tokenResults["token"] = Result.success(VerificationResult(...))
```
No need to mock the entire validation pipeline—the interface abstraction handles it.

## Related Stories

- **Story 10.1**: Prerequisite (server validation endpoint)
- **Epic 8** (Stories 8.4-8.7): Prerequisite (paywall, purchase UI, analytics)
- **Story 10.5**: Refund sync using `PurchaseHistoryStore`
