## [Unreleased] - 2026-03-19

### Added
- Instrument settings analytics events in SettingsViewModel
- Add GameRepository and AnalyticsManager dependencies to SettingsViewModel
- Add settings analytics convenience methods to AnalyticsManager

### Fixed
- Guard analytics calls with try-catch, add failure resilience tests

### Documentation
- Add settings analytics events feature documentation and update conditional docs guide

### Other
- Add analytics unit tests to SettingsViewModelTest
