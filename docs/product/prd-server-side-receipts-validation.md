# PRD: Server-Side Receipt Validation (Android)

**Author:** John (PM)
**Date:** 2026-03-04
**Status:** Draft
**Priority:** High — should follow Epic 3 completion
**Platform:** Android

---

## Executive Summary

### Problem

All API endpoints (`game-box-analysis`, `rules-summary`, `config`) are currently unauthenticated. Purchase verification happens entirely on-device using Google Play Billing Library's client-side checks, and credit balances are stored locally. This means:

- Anyone who discovers the API URLs can call them freely
- A rooted device can fake transactions and grant itself unlimited credits
- Refunded purchases are never detected — credits persist
- Transaction records can be wiped by clearing app data or reinstalling

### Solution

Move purchase validation to the server by forwarding the Google Play purchase token to a backend endpoint that verifies directly with the Google Play Developer API (`purchases.products.get`). The server becomes the **single source of truth** for whether a purchase is legitimate, and issues credits only after Google confirms.

### What This Is NOT

- Not a user authentication system (no accounts, no login)
- Not JWT or session tokens (that's a future layer)
- Not Play Integrity API verification (that's a future hardening layer)
- Not a full server-side credit ledger (credits remain on-device for now)

---

## Success Criteria

| Metric | Target |
|--------|--------|
| No purchase grants credits without Google server confirmation | 100% |
| Duplicate purchase token reuse rejected | 100% |
| Refunded transactions detected and flagged | Within 5 minutes of Google RTDN notification |
| API latency increase from validation round-trip | < 2 seconds |
| Offline purchase recovery (validate when back online) | 100% of queued transactions |

---

## Functional Requirements

### FR1: Transaction Validation Endpoint

| ID | Requirement |
|----|-------------|
| FR1.1 | Backend exposes `POST /api/v1/receipts/validate` endpoint |
| FR1.2 | Accepts Google Play purchase token + product ID + package name |
| FR1.3 | Calls Google Play Developer API `purchases.products.get` to verify purchase |
| FR1.4 | Returns validation result: `valid`, `invalid`, `already_processed` |
| FR1.5 | Stores purchase token + order ID + validation timestamp in database |
| FR1.6 | Rejects purchases with previously seen purchase tokens |
| FR1.7 | Calls `purchases.products.acknowledge` on backend after successful validation (purchase must be acknowledged within 3 days or Google auto-refunds) |

### FR2: Rate Limiting

| ID | Requirement |
|----|-------------|
| FR2.1 | Rate limit validation endpoint by purchase token hash (max 10 requests/hour per hash) |
| FR2.2 | Rate limit by IP address as secondary protection (max 30 requests/hour per IP) |
| FR2.3 | Return `429 Too Many Requests` with retry-after header when exceeded |

### FR3: Android Client Changes

| ID | Requirement |
|----|-------------|
| FR3.1 | After `BillingClient` returns a successful `Purchase`, send purchase token + product ID to backend |
| FR3.2 | Only grant credits after backend returns `valid` |
| FR3.3 | Do NOT call `consumeAsync()` or `acknowledgePurchase()` on-device — backend handles acknowledgment via API (FR1.7) |
| FR3.4 | If backend is unreachable, queue transaction for retry (max 3 attempts with exponential backoff) |
| FR3.5 | Store pending validations in EncryptedSharedPreferences so they survive app termination |
| FR3.6 | On app launch, retry any pending validations |
| FR3.7 | On app launch, query `BillingClient.queryPurchasesAsync()` for unacknowledged purchases and send them to backend (handles edge cases like process death mid-purchase) |

### FR4: Refund Detection

| ID | Requirement |
|----|-------------|
| FR4.1 | Register for Google Real-time Developer Notifications (RTDN) via Cloud Pub/Sub |
| FR4.2 | Handle `ONE_TIME_PRODUCT_CANCELED` notification type for refunded one-time purchases |
| FR4.3 | Optionally poll Voided Purchases API as fallback for missed RTDN events |
| FR4.4 | Mark transaction as refunded in database |
| FR4.5 | On next app sync, inform client of revoked credits (mechanism TBD — could be pull-based initially) |

---

## Non-Functional Requirements

| Category | Requirement |
|----------|-------------|
| **Security** | Backend never exposes Google API service account credentials to client |
| **Security** | All communication over HTTPS/TLS 1.3 |
| **Security** | Purchase data validated against app package name before processing |
| **Reliability** | Validation endpoint must handle Google API downtime gracefully (queue and retry) |
| **Reliability** | Offline purchases must eventually be validated (no lost purchases) |
| **Performance** | Validation round-trip should not block UI — show optimistic state with confirmation |
| **Privacy** | No user-identifying data stored — only purchase tokens and order IDs |

---

## User Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    PURCHASE FLOW (Android)                    │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  User taps "Buy 10 Tokens"                                   │
│       │                                                      │
│       ▼                                                      │
│  Google Play Billing processes payment                       │
│       │                                                      │
│       ▼                                                      │
│  App receives Purchase object with purchase token            │
│       │                                                      │
│       ├──── Client-side state check (existing) ─────────┐   │
│       │     Verify purchaseState == PURCHASED             │   │
│       │                                                   │   │
│       ▼                                                   │   │
│  App sends purchase token + productId to                  │   │
│  POST /api/v1/receipts/validate                           │   │
│       │                                                   │   │
│       ▼                                                   │   │
│  Backend validates with Google Play Developer API          │   │
│  + acknowledges purchase via API                          │   │
│       │                                                   │   │
│       ├─── Valid ──► Store transaction, return 200         │   │
│       │                    │                              │   │
│       │                    ▼                              │   │
│       │              App grants credits                   │   │
│       │                                                   │   │
│       ├─── Invalid ──► Return 403                         │   │
│       │                    │                              │   │
│       │                    ▼                              │   │
│       │              App shows error, no credits          │   │
│       │                                                   │   │
│       ├─── Unreachable ──► Queue for retry                │   │
│       │                    │                              │   │
│       │                    ▼                              │   │
│       │              Show "pending" state                 │   │
│       │              Credits granted on confirmation      │   │
│       │                                                   │   │
│       └─── Already processed ──► Return 200 (idempotent)  │   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Android-Specific Considerations

| Topic | Detail |
|-------|--------|
| **Acknowledgment** | Google requires purchases to be acknowledged within 3 days or they are auto-refunded. Moving acknowledgment to the backend (FR1.7, FR3.3) ensures credits and acknowledgment are always in sync. |
| **Consumable products** | For consumable IAPs (credit packs), the backend should call `purchases.products.consume` instead of `acknowledge` — consuming implicitly acknowledges and allows repurchase. |
| **queryPurchasesAsync** | On app launch, the client must query for unacknowledged/unconsumed purchases (FR3.7). This catches purchases made during process death, crashes, or network failures. |
| **Pending purchases** | Google Play supports pending purchases (e.g., slow payment methods). Client must handle `purchaseState == PENDING` and only send to backend when state transitions to `PURCHASED`. |
| **Obfuscated Account ID** | Consider setting `obfuscatedAccountId` on `BillingFlowParams` to help Google detect fraud — does NOT require user accounts, can be a device-derived hash. |

---

## Decisions

| # | Question | Decision |
|---|----------|----------|
| 1 | Optimistic vs confirmed credits? | **Confirmed only** — credits granted only after server validates with Google |
| 2 | Offline handling? | **Out of scope** — will be handled separately |
| 3 | Free $0.00 IAP for initial tokens? | **Deferred** — free users bypass this flow for now |
| 4 | Backend stack? | **Out of scope** — backend team decides |
| 5 | Acknowledge on client or server? | **Server** — backend calls consume/acknowledge via API to keep credits and acknowledgment in sync |
| 6 | RTDN vs Voided Purchases API for refunds? | **Both** — RTDN as primary (real-time), Voided Purchases API as periodic fallback |

## Scope Clarification

- This PRD covers **paid purchases only** — free initial tokens (3 credits) continue to be granted client-side without server validation
- Offline purchase queuing is excluded; to be scoped separately
- This flow applies only when the device is online at time of purchase
- Pending purchases (slow payment methods) are handled client-side by waiting for `PURCHASED` state before sending to backend

---

## Dependencies

- **Network retry + backoff logic** — retry logic for failed validations
- **Google Play Developer API** access — requires Google Cloud service account with `androidpublisher` scope
- **Google Cloud Pub/Sub** — required for Real-time Developer Notifications (RTDN)
- **Google Play Console** — RTDN topic must be configured in app settings
- **Voided Purchases API** — requires Google Play Developer API access (same service account)
