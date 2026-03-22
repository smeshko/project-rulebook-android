# Credit Balance Reconciliation: App-Launch Server Sync (RULE-261)

**Date:** 2026-03-22
**Related Files:** core/billing/src/main/kotlin/com/rulebook/core/billing/reconciliation/, core/network/src/main/kotlin/com/rulebook/core/network/api/ReceiptValidationApi.kt, core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepository.kt, core/datastore/src/main/kotlin/com/rulebook/core/datastore/CreditPreferencesSource.kt, app/src/main/kotlin/com/rulebook/startup/StartupViewModel.kt

## Overview

Implemented app-launch credit balance reconciliation that fetches the authoritative balance from the server and silently syncs the local DataStore if they differ. The server is always authoritative — local balance is treated as a cache that is overwritten on mismatch. Throttled to once per app launch and integrated into StartupViewModel after validation recovery and refund sync complete.

## What Was Built

- **BalanceReconciliation interface & BalanceReconciliationManager**: Manager class orchestrating the reconciliation flow (fetch server balance, compare with local, update if different, fire analytics)
- **GET /api/v1/credits/balance endpoint**: Network layer integration for fetching authoritative server balance
- **ReceiptRepository.getServerBalance()**: Repository abstraction for the balance endpoint with error mapping
- **CreditRepository.setCreditBalance()**: Repository method for updating local balance through the preferences chain
- **Balance reconciliation analytics**: `trackCreditBalanceReconciled()` event with local_balance, server_balance, delta
- **StartupViewModel integration**: Runs balance reconciliation after validation recovery and refund sync, in dedicated Job with proper join semantics
- **Comprehensive unit tests**: BalanceReconciliationManagerTest with happy path, error handling, and no-op scenarios

## Technical Implementation

### Key Files

- `core/billing/src/main/kotlin/com/rulebook/core/billing/reconciliation/BalanceReconciliation.kt`: Interface contract (BalanceReconciliation) and implementation (BalanceReconciliationManager)
- `core/billing/src/main/kotlin/com/rulebook/core/billing/di/BillingModule.kt`: DI registration as singleton
- `core/network/src/main/kotlin/com/rulebook/core/network/api/ReceiptValidationApi.kt`: Network endpoint `GET("api/v1/credits/balance")`
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepository.kt`: `getServerBalance(): Result<Int>` interface
- `core/data/src/main/kotlin/com/rulebook/core/data/repository/ReceiptRepositoryImpl.kt`: Calls `api.getBalance()`, extracts balance field, wraps in safeCall + NetworkErrorMapper
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/CreditPreferencesSource.kt`: `setCreditBalance(balance: Int)` interface
- `core/datastore/src/main/kotlin/com/rulebook/core/datastore/RulebookPreferences.kt`: Implementation writing balance to DataStore
- `app/src/main/kotlin/com/rulebook/startup/StartupViewModel.kt`: Calls `runBalanceReconciliation()` after recovery and refund jobs complete

### Key Patterns

- **Manager Pattern**: Dedicated manager class (BalanceReconciliationManager) for a specific concern, following the pattern of RefundSyncManager and ValidationRecoveryManager. Injected via DI and mockable via interface.

- **Result Type with getOrElse**: Uses `Result<Int>` from network call with `getOrElse { _ -> return ... }` to silently skip reconciliation on network errors without crashing the app.

- **Server-as-Authoritative**: The local balance is always overwritten to match the server. No conflict resolution — server balance wins unconditionally. Handles edge cases like new installs (server has history, local is 0) correctly.

- **Job.join() Coordination**: Reconciliation waits for recovery and refund jobs to complete before running (`recoveryJob.join()` then `refundJob.join()`), ensuring balance changes from those operations are not overwritten.

- **Silent Failure Semantics**: If server is unreachable, reconciliation returns early with `reconciled = false` and local balance is preserved. No UI feedback, no crash — best-effort operation.

- **Throttled to Once Per Launch**: Runs exactly once during app startup in StartupViewModel.init, never re-runs during app lifetime. Prevents repeated network calls if user navigates or resumes app.

### Code Examples

**Usage from StartupViewModel:**
```kotlin
private fun runBalanceReconciliation(recoveryJob: Job, refundJob: Job) {
    viewModelScope.launch {
        // Reconciliation must run after recovery and refund sync to avoid
        // overwriting balance changes made by those operations
        recoveryJob.join()
        refundJob.join()
        try {
            balanceReconciliation.reconcile()
        } catch (_: Exception) {
            // Reconciliation failure must never crash the app — silently swallow and continue
        }
    }
}
```

**Testing with fake:**
```kotlin
class FakeBalanceReconciliation : BalanceReconciliation {
    var reconcileResult = ReconciliationResult(
        reconciled = false,
        localBalance = 100,
        serverBalance = 100,
        delta = 0
    )

    override suspend fun reconcile(): ReconciliationResult = reconcileResult
}

// In test
val fakeReconciliation = FakeBalanceReconciliation()
fakeReconciliation.reconcileResult = ReconciliationResult(
    reconciled = true,
    localBalance = 50,
    serverBalance = 75,
    delta = 25
)
val viewModel = StartupViewModel(..., balanceReconciliation = fakeReconciliation)
```

## How to Use

1. **Inject BalanceReconciliation** into any component that needs to run reconciliation:
   ```kotlin
   class MyComponent(
       private val balanceReconciliation: BalanceReconciliation
   ) {
       // Use it
   }
   ```

2. **Call reconcile()** to run the synchronization:
   ```kotlin
   val result = balanceReconciliation.reconcile()
   if (result.reconciled) {
       // Local balance was updated from server
       println("Local: ${result.localBalance}, Server: ${result.serverBalance}, Delta: ${result.delta}")
   }
   ```

3. **For testing**: Use FakeBalanceReconciliation to control the reconciliation outcome without network calls:
   ```kotlin
   val fake = FakeBalanceReconciliation().apply {
       reconcileResult = ReconciliationResult(reconciled = true, ...)
   }
   val viewModel = StartupViewModel(..., fakeBalanceReconciliation)
   ```

## Architecture

### Flow Diagram
```
App Launch
  ↓
StartupViewModel.init
  ├─ determineStartupDestination() [parallel]
  ├─ runValidationRecovery() → recoveryJob [parallel]
  ├─ runRefundSync() → refundJob [parallel]
  └─ runBalanceReconciliation()
       ├─ Wait for recoveryJob.join()
       ├─ Wait for refundJob.join()
       └─ balanceReconciliation.reconcile()
            ├─ Fetch server balance (network)
            ├─ Read local balance (DataStore)
            ├─ Compare
            └─ If different: Write server value → DataStore, Fire analytics
```

### Dependencies

- **ReceiptRepository**: For `getServerBalance()` network call
- **CreditRepository**: For reading/writing local balance via DataStore
- **AnalyticsManager**: For `trackCreditBalanceReconciled()` event

## Decisions & Rationale

| Decision | Rationale |
|----------|-----------|
| **Server-as-authoritative** | Prevents drift between app and server. App cache (local balance) must always match source of truth (server). No merge logic — simple, deterministic, correct. |
| **Silent on failure** | Network errors are transient and expected offline. Failing silently with best-effort local balance is better than crashing or blocking startup. |
| **Throttled to once per launch** | Prevents repeated network calls during app lifetime. Reconciliation is not real-time sync; once-per-launch is sufficient for correctness. |
| **Job.join() coordination** | Prevents race conditions where balance is overwritten after recovery/refund operations set it. Explicit ordering ensures consistency. |
| **Analytics event even on no-op** | Not implemented — reconciled flag is false when no update occurs. Could be changed to always fire analytics with reconciled=true/false if needed. |

## Configuration

| Setting | Default | Description |
|---------|---------|-------------|
| Endpoint | `GET /api/v1/credits/balance` | Backend balance endpoint. Must return `{ balance: Int }` |
| Throttle | Once per launch | Hardcoded. Change requires modifying StartupViewModel.init |
| Timeout | Inherited from Retrofit client | HTTP timeout for balance fetch |
| Fallback on error | Local balance | If server unreachable, local balance is preserved |

## Notes

- **Financial correctness**: Server balance always wins. If there's a sync bug, the server value is what matters for the user's account.
- **New installs**: If local balance is 0 but server has purchase history, server balance is correctly restored to the local app on first launch.
- **Edge case — simultaneous operations**: If user makes a purchase while reconciliation runs, purchase writes balance, then reconciliation fetches old server value and overwrites it. Mitigation: Reconciliation runs early in startup (after recovery/refund only), so race window is minimal. Real-time sync is out of scope.
- **Analytics**: `credit_balance_reconciled` event fires only if reconciliation updates the balance (reconciled=true).
- **Testing**: All network errors are mapped via NetworkErrorMapper using the same pattern as validatePurchase and checkRefundStatus.

## Related Features

- **Story 10.4 (Validation Recovery)**: Recovers credits from failed validations before reconciliation runs
- **Story 10.5 (Refund Sync)**: Detects refunded purchases and revokes credits before reconciliation runs
- **Story 10.2 (Purchase Verification)**: Creates initial purchase history on the server that reconciliation syncs down

## Future Enhancements

- Real-time balance sync via push notifications or polling (not in scope for Story 10.7)
- Conflict resolution if user purchases offline and server has different balance (currently: server wins)
- Reconciliation result exposed to UI (currently: silent operation, no notification)
