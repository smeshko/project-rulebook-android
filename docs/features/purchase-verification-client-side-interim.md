# Purchase Verification & Credit Delivery (Client-Side — Interim)

**Date:** 2026-03-17
**Related Files:**
- `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/PurchaseVerifier.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/verification/ClientSidePurchaseVerifier.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseViewModel.kt`
- `core/billing/src/main/kotlin/com/rulebook/core/billing/di/BillingModule.kt`

## Overview

Implemented a swappable purchase verification abstraction that enforces a **consume-before-deliver** contract to prevent fraudulent purchases and double-crediting on retries. The interim client-side implementation uses Google Play's `BillingClient.consumeAsync()` to mark purchases as consumed before delivering credits. Epic 10 will replace this with server-side receipt validation without restructuring the ViewModel.

## What Was Built

- **PurchaseVerifier Interface**: A contract for verifying and consuming purchases before delivering credits. Designed to be swappable between client-side (interim) and server-side (Epic 10) implementations.
- **ClientSidePurchaseVerifier**: Interim implementation that consumes via `BillingClient` and maps SKUs to credit amounts.
- **VerificationResult**: Data class containing the number of credits to deliver after successful consumption.
- **PurchaseViewModel Integration**: Updated to depend on `PurchaseVerifier` abstraction instead of inline consume/credit logic.
- **Analytics Enhancement**: `purchase_completed` event now includes `new_balance` field to track user's credit balance after purchase.
- **Dependency Injection**: Registered `PurchaseVerifier` in DI modules for constructor injection into ViewModel.

## Technical Implementation

### Key Files

- **`PurchaseVerifier.kt`**: Interface defining the verification contract with detailed documentation on the consume-before-deliver requirement. Includes comments on the interim vs. server-side path (Epic 10).
- **`ClientSidePurchaseVerifier.kt`**: Implements the interface, enforcing the contract:
  1. Consume the purchase token first
  2. Only if consumption succeeds, resolve credits for the SKU
  3. Return failure if consumption fails or SKU is unknown
- **`PurchaseViewModel.kt`**: Injected with `PurchaseVerifier`, calls `verifyAndConsume()` instead of inline logic. On success, delivers credits and tracks `purchase_completed` with `new_balance`. On failure, sets error state and tracks `purchase_failed`.
- **`BillingModule.kt`**: Registers `PurchaseVerifier` as `single<PurchaseVerifier> { ClientSidePurchaseVerifier(get()) }`, explicitly noting that Epic 10 will swap this.

### Key Patterns

- **Interface-Based Dependency Injection**: `PurchaseViewModel` depends on the `PurchaseVerifier` interface, not the `ClientSidePurchaseVerifier` implementation. This allows swapping the implementation in the DI module without changing the ViewModel code.

- **Consume-Before-Deliver Contract**: Consumption is called first to acquire a lock on the purchase. Only after consumption succeeds are credits resolved and delivered. This prevents the following scenario:
  - Customer purchases → BillingClient marks as consumed → app crash → customer retries → gets double-credited
  - With consume-first: retry attempt fails at consume step (already consumed), preventing double-credit.

- **Result<T> for Error Handling**: Returns `Result.success(VerificationResult)` on success and `Result.failure(exception)` on any failure. The ViewModel can distinguish between consume failures and unknown product IDs via exception details.

- **Test Double Pattern**: Tests use `FakePurchaseVerifier` (a simple implementation that returns success by default, or failure if configured). This allows ViewModel tests to focus on credit delivery logic without mocking the billing repository.

### Code Examples

**Verifying and delivering credits (in PurchaseViewModel):**
```kotlin
val result = purchaseVerifier.verifyAndConsume(purchaseToken, productId)
result.onSuccess { verification ->
    creditRepository.addCredits(verification.credits)
    val newBalance = creditRepository.creditBalance.first()
    analyticsManager.track("purchase_completed", mapOf(
        "product_id" to productId,
        "credits_added" to verification.credits,
        "new_balance" to newBalance
    ))
    _uiState.update { it.copy(purchaseState = PurchaseState.Success) }
}.onFailure { error ->
    _uiState.update { it.copy(purchaseState = PurchaseState.Error(error.message ?: "Unknown error")) }
    analyticsManager.track("purchase_failed", mapOf("error_code" to "CONSUME_FAILED"))
}
```

**Implementing a server-side verifier (future Epic 10):**
```kotlin
class ServerSidePurchaseVerifier(
    private val billingRepository: BillingRepository,
    private val apiService: ApiService
) : PurchaseVerifier {
    override suspend fun verifyAndConsume(purchaseToken: String, productId: String): Result<VerificationResult> {
        // Send receipt to backend for validation
        val result = apiService.validateReceipt(purchaseToken)
        return if (result.isValid) {
            // Backend marks as consumed, we just resolve credits
            Result.success(VerificationResult(billingRepository.creditsForProduct(productId) ?: 0))
        } else {
            Result.failure(IllegalStateException("Receipt validation failed"))
        }
    }
}

// In BillingModule:
single<PurchaseVerifier> { ServerSidePurchaseVerifier(get(), get()) }
```

## How to Use

### When Building Purchase Features

1. **Inject the interface, not the implementation:**
   ```kotlin
   class MyPurchaseRelatedViewModel(
       private val purchaseVerifier: PurchaseVerifier
   )
   ```

2. **Call verifyAndConsume() to verify and deliver credits:**
   ```kotlin
   val result = purchaseVerifier.verifyAndConsume(purchaseToken, productId)
   result.onSuccess { verification ->
       creditRepository.addCredits(verification.credits)
       // Track analytics with new balance
   }.onFailure { error ->
       // Handle verification failure, don't deliver credits
   }
   ```

3. **Track analytics with new_balance after delivery:**
   Always read the updated balance after `addCredits()` to include `new_balance` in the event.

### When Replacing with Server-Side Verification (Epic 10)

1. Create `ServerSidePurchaseVerifier` implementing `PurchaseVerifier`.
2. Update the `BillingModule` registration:
   ```kotlin
   single<PurchaseVerifier> { ServerSidePurchaseVerifier(get(), get()) }
   ```
3. No changes needed to `PurchaseViewModel` — it depends on the interface.

## Configuration

No configuration options. The implementation is selected via dependency injection in `BillingModule`.

## Notes

- **Interim vs. Server-Side**: This is an explicitly interim solution (Story 8.7). Epic 10 will implement server-side receipt validation. The interface design ensures a clean swap.
- **Consume Idempotency**: Google Play's `consumeAsync()` is idempotent — consuming an already-consumed purchase returns success. This property is relied upon for retry safety.
- **SKU-to-Credits Mapping**: Currently a hard-coded map in `BillingRepository.creditsForProduct()`. Future versions might fetch from remote config or backend.
- **Error Messages**: On consume failure, the error code is tracked as `CONSUME_FAILED` in analytics. Specific error types (network timeout, invalid token, etc.) could be extracted from the exception for more detailed telemetry.
- **Testing Strategy**: Use `FakePurchaseVerifier` in ViewModel tests to control success/failure independently of billing repository behavior.
- **Future Considerations**: If payment methods or credit delivery changes (e.g., server-side credit granting), the `PurchaseVerifier.verifyAndConsume()` contract may need extension (e.g., returning a DTO with more metadata). The interface is designed to accommodate this.
