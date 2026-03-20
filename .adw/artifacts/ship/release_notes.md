## [Unreleased] - 2026-03-20

### Added
- Add Validating state to PurchaseState sealed class
- Add ServerSidePurchaseVerifier and PurchaseValidationException
- Add PurchaseHistoryStore for refund tracking
- Swap DI binding to ServerSidePurchaseVerifier
- Add trackPurchaseValidated analytics method
- Update PurchaseViewModel for server validation flow
- Inject PurchaseHistoryStore into PurchaseViewModel via Koin
- Add Validating UI overlay to PurchaseScreen

### Fixed
- Add missing core:common dependency to core:billing module
- Address code review findings (cycle 1)
- ALREADY_PROCESSED guard and analytics on all paths (cycle 2)

### Other
- Update PurchaseViewModelTest for server validation flow
- Add feature documentation for RULE-256 server-side purchase validation
