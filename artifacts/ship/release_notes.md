## [Unreleased] - 2026-03-19

### Added
- Create ThemePreferencesSource interface and DI binding
- Replace isDarkTheme Boolean with themeMode ThemeMode in SettingsUiState
- Update SettingsViewModel to inject ThemePreferencesSource and persist theme
- Update SettingsModule to inject ThemePreferencesSource into SettingsViewModel
- Create SettingsThemeRow component with brutalist radio button indicator
- Replace dark mode toggle with three theme selection rows in SettingsScreen
- Wire theme preference to app root in MainActivity and RulebookApp
- Update SettingsViewModelTest with FakeThemePreferencesSource and new theme tests

### Fixed
- Add DataStore error handling and accessibility semantics
- Restore 8dp spacing between theme rows in selectableGroup

### Documentation
- Add theme selection feature documentation and update conditional guide
