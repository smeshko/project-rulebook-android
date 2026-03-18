## [Unreleased] - 2026-03-18

### Added
- Add SnackbarHost and restore loading state to PurchaseScreen
- Add AnimatedDots composable component
- Implement onRestorePurchases in PurchaseViewModel
- Add restore events to PurchaseEvent
- Add isRestoring field to PurchaseUiState

### Fixed
- Add restore concurrency guard and error safety net
- Handle all-fail restore scenario with error event

### Documentation
- Document restore purchases feature with partial success pattern (RULE-231)

### Other
- Add restore purchases unit tests
- Update FakeBillingRepository and FakePurchaseVerifier for restore testing
