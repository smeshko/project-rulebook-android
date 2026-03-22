## [Unreleased] - 2026-03-22

### Added
- Integrate balance reconciliation into StartupViewModel
- Register BalanceReconciliation in DI module
- Create BalanceReconciliationManager with tests
- Add trackCreditBalanceReconciled analytics event
- Add setCreditBalance through interface chain
- Add getServerBalance to ReceiptRepository
- Add getBalance endpoint to network layer

### Fixed
- Cycle 1 - sequence reconciliation after recovery and refund sync
- Resolve overload ambiguity in test fakes
- Add override modifier to setCreditBalance in RulebookPreferences

### Documentation
- Add feature documentation for credit balance reconciliation

### Other
- Update StartupViewModelTest for balance reconciliation
