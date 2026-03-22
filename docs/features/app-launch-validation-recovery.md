# App Launch Validation Recovery

**Story:** RULE-258 / Story 10.4
**Epic:** Epic 10: Server-Side Receipt Validation
**Status:** Implemented

---

## Overview

On every app launch, the validation recovery system silently processes purchases that previously failed server-side validation. This ensures users always receive their credits, even after network failures, process deaths, or reinstallation.

Recovery runs non-blocking on `Dispatchers.IO` — app startup is never delayed.

---

## Architecture

### Core Component: `ValidationRecovery` Interface

```
core/billing/src/main/kotlin/com/rulebook/core/billing/recovery/
├── ValidationRecoveryManager.kt  — implementation + interface + RecoveryResult
```

**Interface:** `ValidationRecovery`
- Single method: `suspend fun recover(): RecoveryResult`
- Injected into `StartupViewModel` via Koin as a singleton

**Implementation:** `ValidationRecoveryManager`
- Dependencies: `PendingValidationStore`, `BillingRepository`, `PurchaseVerifier`, `CreditRepository`, `PurchaseHistoryStore`, `AnalyticsManager`

### Recovery Flow

```
App Launch
    │
    └── StartupViewModel.init()
            ├── determineStartupDestination()  [concurrent, independent]
            └── runValidationRecovery()         [Dispatchers.IO, non-blocking]
                    │
                    └── ValidationRecoveryManager.recover()
                            │
                            ├── 1. PendingValidationStore.getAll()
                            │        └── Returns non-expired entries (auto-prunes expired)
                            │
                            ├── 2. For each pending entry:
                            │        ├── PurchaseVerifier.verifyAndConsume()
                            │        │       ├── VALID → addCredits + savePurchase + remove from pending
                            │        │       ├── ALREADY_PROCESSED + in history → remove from pending only
                            │        │       ├── ALREADY_PROCESSED + NOT in history → addCredits + remove
                            │        │       ├── PurchaseValidationException (INVALID) → remove from pending
                            │        │       └── Transient error → increment retryCount, keep in pending
                            │
                            ├── 3. BillingRepository.queryUnconsumedPurchases()
                            │        └── For each orphaned purchase (not in pending or history):
                            │                └── verifyAndConsume → addCredits + savePurchase
                            │
                            └── 4. Fire analytics: purchase_recovery_attempted
```

### Startup Integration

```kotlin
// StartupViewModel.kt
class StartupViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val validationRecoveryManager: ValidationRecovery
) : ViewModel() {

    val recoveryEvents = _recoveryEvents.receiveAsFlow() // Channel<RecoveryEvent>

    init {
        determineStartupDestination()  // independent coroutine
        runValidationRecovery()        // Dispatchers.IO, won't block startup
    }
}
```

### UI Notification

When credits are recovered, `MainActivity` shows a Toast (consistent with `PendingPurchaseChecker`):

```
"X credits from a previous purchase have been added!"
```

Collection happens in `MainActivity.onCreate` via `lifecycleScope.launch`, before `setContent`.

---

## Two Recovery Scenarios

### Scenario 1: Pending Validation Store Entries

Purchases that exhausted all retry attempts in `PurchaseViewModel` (Story 10.3) land in `PendingValidationStore`. On the next app launch, `ValidationRecoveryManager` retries each entry.

**Entry lifecycle:**
| Validation Result | Action |
|---|---|
| `VALID` | Deliver credits, save to history, remove from pending |
| `ALREADY_PROCESSED` + in history | Remove from pending (idempotent cleanup) |
| `ALREADY_PROCESSED` + not in history | Deliver credits, save to history, remove from pending |
| `PurchaseValidationException` (INVALID) | Remove from pending (unrecoverable) |
| Transient error | Increment `retryCount`, keep in pending for next launch |

**Expiry:** `PendingValidationStore.getAll()` auto-prunes entries older than 72 hours.

### Scenario 2: Orphaned Unconsumed Purchases

`BillingRepository.queryUnconsumedPurchases()` queries Google Play for purchases in PURCHASED state that haven't been consumed. This catches:
- Process deaths during purchase flow
- App crashes before credits were saved
- Reinstallation (EncryptedSharedPreferences is wiped, pending store is lost)

**Deduplication:** Orphaned purchases are skipped if their token appears in:
- `PendingValidationStore` (being processed in Scenario 1)
- `PurchaseHistoryStore` (credits already delivered in a previous session)

---

## Analytics Events

| Event | When fired | Properties |
|---|---|---|
| `purchase_recovery_attempted` | Every app launch | `pending_count`, `recovered_count`, `expired_count` |
| `purchase_recovery_expired` | Available via `AnalyticsManager` | `expired_count` |

---

## DI Registration

```kotlin
// BillingModule.kt
single<ValidationRecovery> { ValidationRecoveryManager(get(), get(), get(), get(), get(), get()) }

// AppModule.kt
viewModel { StartupViewModel(get(), get()) }  // OnboardingRepository + ValidationRecovery
```

---

## Edge Cases

| Scenario | Handling |
|---|---|
| No pending entries, no orphans | `recover()` returns zero counts, analytics still fire |
| `queryUnconsumedPurchases()` fails | Silently logged; pending store processing still completes |
| `addCredits()` returns false | Entry stays in pending store; counted as `failedCount` |
| `ValidationRecoveryManager.recover()` throws | Caught in `StartupViewModel.runValidationRecovery()`; app startup unaffected |
| User reinstalls app | Pending store is wiped; `queryUnconsumedPurchases()` recovers any outstanding purchases |
| Duplicate delivery attempt | `PurchaseHistoryStore` check prevents double-granting for `ALREADY_PROCESSED` entries |

---

## Files Modified

**New:**
- `core/billing/src/main/kotlin/com/rulebook/core/billing/recovery/ValidationRecoveryManager.kt`
- `core/billing/src/test/kotlin/com/rulebook/core/billing/recovery/ValidationRecoveryManagerTest.kt`
- `docs/features/app-launch-validation-recovery.md`

**Modified:**
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt` — added `trackPurchaseRecoveryAttempted()` and `trackPurchaseRecoveryExpired()`
- `core/billing/build.gradle.kts` — added `core:analytics` dependency
- `core/billing/src/main/kotlin/com/rulebook/core/billing/di/BillingModule.kt` — registered `ValidationRecovery` singleton
- `app/src/main/kotlin/com/rulebook/startup/StartupViewModel.kt` — injected recovery, launched non-blocking, exposed `recoveryEvents`
- `app/src/main/kotlin/com/rulebook/di/AppModule.kt` — updated `StartupViewModel` DI to include second `get()`
- `app/src/main/kotlin/com/rulebook/MainActivity.kt` — collect recovery events, show Toast
- `app/src/test/kotlin/com/rulebook/startup/StartupViewModelTest.kt` — updated tests for new constructor

---

## Related Stories

- **Story 10.1** (RULE-255): `ReceiptRepository` network layer
- **Story 10.2** (RULE-256): `ServerSidePurchaseVerifier`, `PurchaseHistoryStore`
- **Story 10.3** (RULE-257): `PendingValidationStore` — where entries originate
- **Story 10.5**: Refund Sync — will consume `PurchaseHistoryStore.getRecentTokens()`
