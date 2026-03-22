# Remove Client-Side Billing Consumption (Epic 10 Cleanup)

**Date:** 2026-03-22
**Related Files:** `core/billing/src/main/kotlin/com/rulebook/core/billing/repository/BillingRepository.kt`, `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/PurchaseVerifier.kt`, `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/ServerSidePurchaseVerifier.kt`

## Overview

Removed client-side purchase consumption from the billing module as the final cleanup step for Epic 10 (Server-Side Receipt Validation). The interim `ClientSidePurchaseVerifier` and `BillingRepository.consumePurchase()` method are no longer needed now that server-side validation is fully deployed. This change eliminates dead code paths and enforces the server-side validation architecture.

## What Was Built

- **Removed `consumePurchase()` method** from `BillingRepository` interface and implementation
- **Removed `ClientSidePurchaseVerifier` class and its tests** (interim client-side solution is no longer needed)
- **Renamed `queryUnconsumedPurchases()` → `queryUnacknowledgedPurchases()`** to reflect server-side acknowledgment model
- **Updated `PurchaseVerifier` interface documentation** to remove references to client-side verifier
- **Updated all call sites and test doubles** across the billing and purchase modules (12 modified files + 2 deleted files)

## Technical Implementation

### Key Files Modified

- `core/billing/src/main/kotlin/com/rulebook/core/billing/repository/BillingRepository.kt`: Interface contract changes
- `core/billing/src/main/kotlin/com/rulebook/core/billing/BillingRepositoryImpl.kt`: Implementation removing consumePurchase
- `core/billing/src/main/kotlin/com/rulebook/core/billing/BillingClientWrapper.kt`: Interface cleanup
- `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/PurchaseVerifier.kt`: Updated documentation
- `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/ServerSidePurchaseVerifier.kt`: Now the sole implementation

### Key Files Deleted

- `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/ClientSidePurchaseVerifier.kt` (dead code)
- `core/billing/src/test/kotlin/com/rulebook/core/billing/verification/ClientSidePurchaseVerifierTest.kt`

### Key Patterns

**BillingRepository Interface Simplification**: The interface now focuses solely on query and purchase flow operations, removing consumption logic:

```kotlin
// BEFORE (with consumePurchase):
interface BillingRepository {
    suspend fun queryProducts(): Result<List<ProductInfo>>
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit>
    suspend fun consumePurchase(purchaseToken: String): Result<Unit>  // REMOVED
    suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>>
    // ... other methods
}

// AFTER (server-side validation only):
interface BillingRepository {
    suspend fun queryProducts(): Result<List<ProductInfo>>
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): Result<Unit>
    suspend fun queryUnacknowledgedPurchases(): Result<List<PurchaseInfo>>  // RENAMED
    // ... other methods
}
```

**Purchase Verification Consolidation**: `PurchaseVerifier` now has a single implementation path:

```kotlin
// BEFORE:
// - ClientSidePurchaseVerifier: consumed locally via BillingClient
// - ServerSidePurchaseVerifier: validated via backend API

// AFTER:
// - ServerSidePurchaseVerifier: sole implementation (validates via backend API)
```

**Unacknowledged Purchases Terminology**: The rename from `queryUnconsumedPurchases` to `queryUnacknowledgedPurchases` clarifies the server-side model:
- **Client-side model (old)**: Purchases are "consumed" locally via `BillingClient.consumePurchase()`
- **Server-side model (new)**: Purchases are "acknowledged" by the server after validation

### Code Examples

**Before (Client-Side Consumption)**:
```kotlin
// OLD - No longer used, removed from codebase
val result = billingRepository.consumePurchase(purchaseToken)
if (result.isSuccess) {
    // Purchase was consumed locally, credits awarded
}
```

**After (Server-Side Validation)**:
```kotlin
// NEW - Server-side validation path
val verificationResult = purchaseVerifier.verifyAndConsume(purchaseToken, productId)
if (verificationResult.isSuccess) {
    // Server validated the receipt, credits awarded
}
```

**Querying Unacknowledged Purchases** (for restore flow):
```kotlin
// OLD
val unconsumed = billingRepository.queryUnconsumedPurchases()

// NEW - Renamed for clarity
val unacknowledged = billingRepository.queryUnacknowledgedPurchases()
for (purchase in unacknowledged) {
    // Send to server for validation (Story 8.8: Restore Purchases)
    purchaseVerifier.verifyAndConsume(purchase.token, purchase.productId)
}
```

## How to Use

### If Updating Code That Called `consumePurchase()`

1. **Remove calls to `consumePurchase()`** — this method no longer exists
2. **Use `PurchaseVerifier.verifyAndConsume()`** instead — this handles both verification and acknowledgment
3. **Update any fake implementations** in tests to remove the `consumePurchase` override

**Before**:
```kotlin
// OLD - No longer works
val consumeResult = billingRepository.consumePurchase(token)
```

**After**:
```kotlin
// NEW - Verification handles both server validation and acknowledgment
val verifyResult = purchaseVerifier.verifyAndConsume(token, productId)
```

### If Updating Code That Called `queryUnconsumedPurchases()`

Just rename the method call:

```kotlin
// OLD
val purchases = billingRepository.queryUnconsumedPurchases()

// NEW
val purchases = billingRepository.queryUnacknowledgedPurchases()
```

### In Test Doubles

Remove the `consumePurchase` implementation:

```kotlin
// OLD Test Double
class FakeBillingRepository : BillingRepository {
    override suspend fun consumePurchase(purchaseToken: String): Result<Unit> = Result.success(Unit)
    override suspend fun queryUnconsumedPurchases(): Result<List<PurchaseInfo>> = Result.success(emptyList())
}

// NEW Test Double
class FakeBillingRepository : BillingRepository {
    override suspend fun queryUnacknowledgedPurchases(): Result<List<PurchaseInfo>> = Result.success(emptyList())
}
```

## Architecture Impact

### Breaking Changes

**`BillingRepository` Interface**:
- ❌ Removed: `suspend fun consumePurchase(purchaseToken: String): Result<Unit>`
- ✏️ Renamed: `queryUnconsumedPurchases()` → `queryUnacknowledgedPurchases()`

**`BillingClientWrapper` Interface**:
- ❌ Removed: `suspend fun consumePurchase(purchaseToken: String): Result<Unit>`

**All implementations** that implement `BillingRepository` must be updated:
- Update call sites of `queryUnconsumedPurchases` → `queryUnacknowledgedPurchases`
- Remove any `consumePurchase` overrides
- Remove `ConsumeParams` and `consumePurchase` KTX extension imports

### Updated Call Sites

| Component | Change | Impact |
|-----------|--------|--------|
| `PurchaseViewModel.onRestorePurchases()` | Uses `queryUnacknowledgedPurchases()` | Restore flow uses server validation |
| `ValidationRecoveryManager.recover()` | Uses `queryUnacknowledgedPurchases()` | Recovery queries unacknowledged purchases |
| Story 8.8 (Restore Purchases) | Sends each unacknowledged purchase to server | No longer uses local consume |
| Story 8.9 (Pending Purchase Handling) | Server handles acknowledgment | No local consume on pending→purchased |

### Enum Verification Patterns

**PurchaseVerifier is now single-implementation**:
- Previous: Chose between client-side and server-side via Koin configuration
- Now: `ServerSidePurchaseVerifier` is the sole implementation
- All purchase verification flows use the same server-side path

## Configuration

| Item | Before | After | Notes |
|------|--------|-------|-------|
| Purchase Verification | Client-side + Server-side (configurable) | Server-side only | No choice needed — single path |
| Consumption | `BillingRepository.consumePurchase()` | Removed | Server handles after validation |
| Query Method | `queryUnconsumedPurchases()` | `queryUnacknowledgedPurchases()` | Reflects server-side model |
| PurchaseVerifier | Two implementations | One implementation | `ServerSidePurchaseVerifier` |

## Notes

- **Complete Epic 10 cleanup**: This is the final step that removes the interim client-side solution and all supporting code paths
- **No runtime behavior change**: The purchase flow behavior is unchanged — validation still happens server-side (via Stories 10.1-10.5)
- **Interface breaking change**: Any code implementing `BillingRepository` must be updated (see Architecture Impact section)
- **Koin DI impact**: If you had conditional DI for client-side vs server-side verification, that configuration can be simplified (only server-side now)
- **Migration path complete**: The gradual migration from client-side (RULE-230) through server-side interim (RULE-255-259) to full server-side cleanup (RULE-260) is now complete
