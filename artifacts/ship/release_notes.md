## [Unreleased] - 2026-03-19

### Added
- Add farewell haptic vibration on toggle-off
- Add hapticsEnabled-guarded overloads to HapticUtils
- Wire HapticsPreferencesSource into SettingsViewModel
- Register HapticsPreferencesSource in DI
- Create HapticsPreferencesSource interface

### Changed
- Update camera haptic callsite to respect haptics preference

### Documentation
- Add haptic feedback toggle feature documentation

### Other
- Add FakeHapticsPreferencesSource and haptics tests
