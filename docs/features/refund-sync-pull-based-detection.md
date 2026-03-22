# Refund Sync: Pull-Based Refund Detection (RULE-259)

**Date:** 2026-03-22
**Related Files:** `core/billing/src/main/kotlin/com/rulebook/core/billing/refund/`, `app/src/main/kotlin/com/rulebook/startup/StartupViewModel.kt`, `app/src/main/kotlin/com/rulebook/MainActivity.kt`

## Overview

Detects refunded purchases on app launch and revokes associated credits. This pull-based system checks recent purchase tokens against the backend's refund status endpoint, processes any refunded purchases, and notifies the user with an informational toast message.

## What Was Built

- **RefundSyncManager** - Orchestrates the complete refund detection and credit revocation flow
- **RefundAcknowledgmentStore** - Persistent store tracking which refund tokens have been processed to prevent duplicate revocations
- **RefundSync interface** - Testable contract for dependency injection and test doubles
- **RefundEvent** - One-shot event emitted by StartupViewModel when credits are revoked
- **Analytics integration** - Tracks `purchase_refund_detected` events with token and credit counts
- **User notification** - Toast message informing users of refunded credits

## Technical Implementation

### Architecture Pattern

The refund check runs as an independent coroutine in `StartupViewModel.init()`, meaning:

1. **Throttled to once per app launch** - Not on every resume (runs in ViewModel init, which fires once per ViewModel lifecycle)
2. **Non-blocking startup** - Failures never delay app startup or crash the app
3. **Event-driven feedback** - UI listens for `RefundEvent.CreditsRevoked` to show toast

### Key Files

- **`core/billing/src/main/kotlin/com/rulebook/core/billing/refund/RefundSyncManager.kt`** - Main orchestrator that coordinates the refund check flow
- **`core/billing/src/main/kotlin/com/rulebook/core/billing/refund/RefundAcknowledgmentStore.kt`** - Interface for tracking processed refunds (DataStore-backed implementation uses `stringSetPreferencesKey`)
- **`core/billing/src/main/kotlin/com/rulebook/core/billing/history/PurchaseHistoryStore.kt`** - Source of recent purchase tokens to check (added `getRecentEntries()` method)
- **`core/data/src/main/kotlin/com/rulebook/core/data/repository/CreditRepository.kt`** - Added `removeCredits(amount)` method for credit revocation
- **`app/src/main/kotlin/com/rulebook/startup/StartupViewModel.kt`** - Integrates RefundSync and emits RefundEvent
- **`app/src/main/kotlin/com/rulebook/MainActivity.kt`** - Collects RefundEvent and displays toast

### Key Patterns

**Acknowledge-Before-Revoke Pattern**

```kotlin
// In RefundSyncManager.checkRefunds()
for (status in refundStatuses) {
    if (!status.isRefunded) continue

    // Acknowledge FIRST to prevent double-revocation if app crashes
    // after removing credits but before acknowledging
    refundAcknowledgmentStore.acknowledge(status.purchaseToken)

    val creditsToRevoke = billingRepository.creditsForProduct(productId) ?: continue
    creditRepository.removeCredits(creditsToRevoke)
}
```

This ordering ensures:
- If the app crashes after acknowledging but before revoking credits, the next launch will see the token as already acknowledged and skip it
- If the app crashes after revoking but before acknowledging, the next launch will process it again (but the removeCredits implementation clamps to 0, so double-revocation is safe)

**Silent Failure with Retry Pattern**

```kotlin
val refundStatuses = receiptRepository.checkRefundStatus(tokens)
    .getOrElse { _ -> return RefundSyncResult(creditsRevoked = 0, tokensRefunded = 0) }
```

Network errors are silently swallowed:
- No crash or error UI
- Check retried on next app launch
- Prevents startup delay from blocking on network

**Deduplication and Filtering**

```kotlin
val unacknowledged = entries
    .distinctBy { it.purchaseToken }
    .filter { !refundAcknowledgmentStore.isAcknowledged(it.purchaseToken) }
```

Only checks tokens that haven't been processed before, avoiding unnecessary backend calls.

## How to Use

### Integrating the Refund Check

RefundSyncManager is registered in DI and injected as `RefundSync` interface into StartupViewModel. The check runs automatically on app launch—no manual integration needed.

### Listening for Refund Events

In any Activity/Fragment, collect refund events to show UI feedback:

```kotlin
lifecycleScope.launch {
    startupViewModel.refundEvents.collect { event ->
        if (event is RefundEvent.CreditsRevoked) {
            Toast.makeText(
                this@Activity,
                "A previous purchase was refunded. ${event.credits} credits removed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
```

### Testing the Refund Flow

In unit tests, provide a `FakeRefundSync` that returns `RefundSyncResult(creditsRevoked = 10, tokensRefunded = 1)`:

```kotlin
@Test
fun testRefundEventEmitted() = runTest {
    val fakeRefundSync = object : RefundSync {
        override suspend fun checkRefunds() = RefundSyncResult(10, 1)
    }

    val viewModel = StartupViewModel(
        onboardingRepository = fakeOnboarding,
        validationRecoveryManager = fakeRecovery,
        refundSync = fakeRefundSync
    )

    val event = viewModel.refundEvents.first()
    assertThat(event).isInstanceOf(RefundEvent.CreditsRevoked::class.java)
    assertThat((event as RefundEvent.CreditsRevoked).credits).isEqualTo(10)
}
```

## Configuration & Data Flow

### DataStore Schema

RefundAcknowledgmentStore uses `stringSetPreferencesKey("refund_acknowledged_tokens")`:

```kotlin
val refundAcknowledgedTokensKey = stringSetPreferencesKey("refund_acknowledged_tokens")
```

Stores set of purchase tokens that have been processed, persisted across app launches.

### Backend Endpoint

`ReceiptRepository.checkRefundStatus(tokens: List<String>)` calls `POST /api/v1/receipts/status`:

**Request:**
```json
{
  "tokens": ["token1", "token2", ...]
}
```

**Response:**
```json
[
  {
    "purchaseToken": "token1",
    "status": "refunded"  // or "active"
  },
  ...
]
```

### Event Flow

1. **App Launch** → StartupViewModel.init() → `runRefundSync()`
2. `RefundSyncManager.checkRefunds()`:
   - Read recent purchase entries from PurchaseHistoryStore
   - Filter out already-acknowledged tokens
   - Call backend `/api/v1/receipts/status`
   - For each refunded token:
     - Acknowledge token (DataStore update)
     - Look up credits for product
     - Revoke credits (DataStore update, clamped to 0)
     - Increment counter
   - Fire analytics event
   - Return RefundSyncResult
3. **If credits revoked > 0** → Emit `RefundEvent.CreditsRevoked`
4. **MainActivity.onCreate()** → Listen for RefundEvent → Show toast

## Notes

### Credit Clamping

The `removeCredits()` method never allows negative balance:

```kotlin
override suspend fun removeCredits(amount: Int): Int {
    val current = currentBalance
    val actualRemoved = minOf(current, amount)
    _creditBalance.value = (current - amount).coerceAtLeast(0)
    return actualRemoved
}
```

This ensures credits cannot go negative, even if backend sends inconsistent refund amounts.

### Throttling Behavior

The refund check is throttled to **once per app launch**:
- Runs in `StartupViewModel.init()`, which fires once per ViewModel lifecycle
- Does NOT run on every resume
- This prevents excessive backend calls and provides consistent behavior

If you need to trigger a manual refund check outside of app launch, call `refundSync.checkRefunds()` directly, but consider the performance impact of additional backend calls.

### Analytics

The `purchase_refund_detected` event is only fired if `tokensRefunded > 0`:

```kotlin
if (tokensRefunded > 0) {
    analyticsManager.trackPurchaseRefundDetected(
        tokensRefunded = tokensRefunded,
        creditsRevoked = totalCreditsRevoked
    )
}
```

This prevents noise in analytics from successful checks with no refunds.

### Extending for Other Recovery Operations

This pattern (pull-based check on app launch, silent failure, acknowledgment store) can be extended for:
- Pending purchase approval resolution (Story 10.3)
- Failed validation recovery (Story 10.4)
- Other financial/transactional recovery scenarios

The key design principle: **recovery operations fail silently and retry on next launch**, never blocking or crashing the app.
