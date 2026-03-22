## [Unreleased] - 2026-03-22

### Added
- Display refund notification Toast in MainActivity
- Integrate RefundSync into StartupViewModel with RefundEvent
- Register RefundAcknowledgmentStore and RefundSyncManager in DI
- Add trackPurchaseRefundDetected analytics event
- Create RefundSyncManager with full refund detection flow
- Create RefundAcknowledgmentStore to prevent duplicate revocations
- Add removeCredits to CreditPreferencesSource and CreditRepository
- Add PurchaseHistoryEntry and getRecentEntries to PurchaseHistoryStore

### Fixed
- Prevent double-revocation and duplicate token processing
- Implement removeCredits in GenerationViewModelTest fake

### Documentation
- Add feature documentation for refund sync detection (RULE-259)
