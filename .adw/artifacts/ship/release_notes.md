## [Unreleased] - 2026-03-19

### Added
- Handle deep link warm start via onNewIntent and singleTop launchMode
- Add deep link routing to camera composable in RulebookNavHost
- Add rulebook:// deep link intent filter to MainActivity
- Add adaptive shortcut icon with pink background and white camera

### Fixed
- Fix warm-start deep link and add haptics tests
- Fix pre-existing build failures in feature:camera from Story 9.2
- Fix shortcut long label capitalization to match spec

### Documentation
- Document MVI pattern for app shortcuts deep link navigation (RULE-239)
