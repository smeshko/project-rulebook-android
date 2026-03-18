# Purchase Analytics Events

**Date:** 2026-03-18
**Related Files:**
- `core/analytics/src/main/kotlin/com/rulebook/core/analytics/AnalyticsManager.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseViewModel.kt`
- `feature/purchase/src/main/kotlin/com/rulebook/feature/purchase/PurchaseScreen.kt`
- `core/analytics/src/test/kotlin/com/rulebook/core/analytics/AnalyticsManagerTest.kt`
- `feature/purchase/src/test/kotlin/com/rulebook/feature/purchase/PurchaseViewModelTest.kt`

## Overview

This document describes the purchase analytics event system that tracks user behavior through the paywall and purchase funnel. Five cross-platform events are tracked via `AnalyticsManager` convenience methods, aligning with iOS event names for consistent cross-platform analytics dashboards.

## Event Catalog

| Event Name | Trigger | Properties |
|---|---|---|
| `paywall_displayed` | Paywall screen opens | `source`, `current_balance` |
| `purchase_started` | User taps a product card | `sku`, `credits` |
| `purchase_completed` | Successful purchase with credits delivered | `sku`, `credits_added`, `new_balance` |
| `purchase_failed` | Any purchase error | `sku`, `error_code`, `error_message` |
| `purchase_restored` | Restore purchases completes | `result`, `credits_restored` |

### Event Property Details

#### `paywall_displayed`
Fired in `PurchaseViewModel.init` after collecting the initial credit balance.
- `source`: Navigation entry point — `"scan_gate"` (from camera credit gate), `"settings"` (from Settings screen), `"unknown"` (default)
- `current_balance`: User's credit balance at the time the paywall is shown (integer as string)

#### `purchase_started`
Fired when `onProductSelected()` is called, before the billing flow launches.
- `sku`: Product identifier (e.g., `"credits_1"`, `"credits_3"`, `"credits_10"`)
- `credits`: Number of credits in the selected product pack (integer as string)

#### `purchase_completed`
Fired after successful purchase verification and credit delivery.
- `sku`: Product identifier
- `credits_added`: Number of credits delivered by this purchase
- `new_balance`: User's credit balance after delivery

#### `purchase_failed`
Fired for any purchase failure reason. Error codes:
- `USER_CANCELED` — User dismissed the Google Play billing sheet
- `TOKEN_MISSING` — Purchase returned OK but no token was present
- `CONSUME_FAILED` — Server verification/consumption failed
- `LAUNCH_FAILED` — Billing flow could not be launched
- Numeric codes (e.g., `"6"`) — Raw Google Play billing response codes
- `sku`: Product that failed
- `error_code`: Categorized error identifier
- `error_message`: Human-readable error description

#### `purchase_restored`
Fired when restore purchases completes, regardless of outcome.
- `result`: `"success"` (credits restored), `"none"` (no unconsumed purchases found), `"error"` (query or verification failed)
- `credits_restored`: Total credits restored (0 for `"none"` and `"error"` outcomes)

## Privacy Considerations

No PII is included in these events per the architecture requirements:
- **No purchase price** — prices vary by region/currency and could be PII
- **No payment method** — PCI/PII concern
- **SKU and credit counts are acceptable** — these are internal product identifiers

## Cross-Platform Alignment

These event names and property names match the iOS analytics spec for cross-platform consistency. Notably:
- Android uses `paywall_displayed` (not `paywall_viewed` as in older iOS PRD drafts)
- Android uses `purchase_started` (not `purchase_initiated`)
- `sku` is used instead of `product_id` for alignment

## Supplementary Events (Android-Only)

The following events are tracked in `PurchaseViewModel` but are NOT in the cross-platform spec. They are Android-specific telemetry that remain as raw `trackEvent()` calls:

| Event | Trigger |
|---|---|
| `paywall_dismissed` | User taps "Not now" |
| `paywall_restore_purchases_tapped` | User taps "Restore Purchases" button |
| `purchase_pending` | Purchase enters Ask-to-Buy family approval state |
| `purchase_pending_resolved` | A previously pending purchase was approved and processed |

## Technical Implementation

### Convenience Method Pattern

All five purchase events are implemented as Kotlin interface default methods on `AnalyticsManager`, following the established scan analytics pattern. The default methods delegate to `trackEvent()`:

```kotlin
fun trackPurchaseCompleted(sku: String, creditsAdded: Int, newBalance: Int) {
    trackEvent(
        "purchase_completed",
        mapOf(
            "sku" to sku,
            "credits_added" to creditsAdded.toString(),
            "new_balance" to newBalance.toString()
        )
    )
}
```

`FakeAnalyticsManager` and `TelemetryDeckAnalyticsManager` automatically inherit these methods without modification.

### Navigation Source Propagation

The `source` property for `paywall_displayed` flows through the navigation stack:
1. `Route.Purchase.createRoute(source)` constructs `"purchase?source={value}"`
2. `RulebookNavHost` declares `navArgument(PURCHASE_SOURCE)` with `defaultValue = "unknown"`
3. `PurchaseScreen(source = ...)` receives it from the back stack entry
4. `koinViewModel(parameters = { parametersOf(source) })` passes it to `PurchaseViewModel`
5. `PurchaseViewModel(source = ...)` uses it in the init `trackPaywallDisplayed()` call

### Entry Points

| Caller | Source Value |
|---|---|
| Camera screen credit gate | `"scan_gate"` |
| Settings screen | `"settings"` |
| Direct navigation (no source) | `"unknown"` |
