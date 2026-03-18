## [Unreleased] - 2026-03-18

### Added
- Purchase analytics convenience methods to AnalyticsManager
- Source navigation argument to Purchase route
- Fire paywall_displayed event on PurchaseViewModel init
- Replace raw trackEvent calls with purchase analytics convenience methods

### Fixed
- Handle addCredits failure after successful purchase
- Fix failing tests for paywall analytics and purchase route

### Documentation
- Add purchase analytics events feature documentation
- Register purchase analytics events in conditional docs guide

### Other
- Update PurchaseViewModelTest for new analytics property names
- Add unit tests for purchase analytics convenience methods
