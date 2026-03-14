# Conditional Documentation Guide

This guide helps you find relevant documentation based on what you're working on.

## Instructions

- Review the task you need to perform
- Check the conditions below
- Read the relevant documentation before proceeding
- Only read documentation if conditions match your task

## Documentation Map

- `docs/features/library-sorting-reactive-flow.md` - Library Sorting with Reactive Flow State Management (RULE-217)
  - Conditions:
    - When implementing sorting functionality with reactive updates
    - When designing UI state management with flow-based preferences
    - When implementing multi-sort-order queries with Room database
    - When using flatMapLatest to switch between reactive data sources based on state changes
    - When persisting user preferences with DataStore and applying them reactively

- `docs/features/game-grid-display.md` - Game Grid Display Pattern (RULE-216)
  - Conditions:
    - When implementing grid layouts for game lists or collections
    - When creating reusable composable components for game display
    - When integrating Coil 3 AsyncImage with placeholder/error handling
    - When extending brutalist card styling patterns in design system
    - When working with typography tokens and design system integration

- `docs/features/delete-game-confirmation-dialog.md` - Delete Game with Confirmation Dialog (RULE-219)
  - Conditions:
    - When implementing one-time event channels for notifications in library or other features
    - When adding long-press gesture detection to composable components
    - When creating confirmation dialogs for destructive operations (delete, logout, reset)
    - When integrating snackbar notifications with event-driven architecture
    - When implementing optimistic UI patterns with Room reactive cascading deletes

- `docs/features/game-context-menu-long-press.md` - Game Context Menu with Long-Press (RULE-220)
  - Conditions:
    - When implementing long-press context menus for card-based UI components
    - When adding brutalist-styled dropdown menus to game cards or similar list items
    - When managing temporary UI state (menus, tooltips, popovers) with nullable state pattern
    - When integrating haptic feedback for gesture detection (long-press, double-tap)
    - When extending game card interactions with multiple action options (view, delete, share)

- `docs/features/pull-to-refresh-branded-indicator.md` - Pull-to-Refresh Branded Indicator (RULE-221)
  - Conditions:
    - When customizing Material 3 pull-to-refresh indicators with brand colors
    - When integrating pull-to-refresh gestures with branded visual feedback
    - When styling Material 3 components while preserving platform conventions
    - When implementing refresh mechanisms for future cloud sync integration
    - When using design system color tokens in Material 3 components

- `docs/features/credit-balance-display-component.md` - Credit Balance Display Component (RULE-224)
  - Conditions:
    - When displaying reactive credit balance in app headers or navigation
    - When integrating DataStore Preferences with proper error handling and fallback patterns
    - When implementing multi-variant reusable composables for different screen contexts
    - When using state-based icon tinting and accessibility semantic labels
    - When handling string pluralization and internationalization for count-based UI

- `docs/features/credit-consumption-on-scan.md` - Credit Consumption on Successful Scan (RULE-225)
  - Conditions:
    - When implementing atomic transaction patterns between Room database saves and DataStore updates
    - When designing defensive navigation that proceeds despite secondary operation failures
    - When integrating analytics events that should not block user navigation flows
    - When implementing credit/resource deduction with paywall gating on zero balance
    - When handling DataStore atomic edit blocks for financial or resource-critical state updates

- `docs/features/paywall-screen-mvi-pattern.md` - Paywall Screen MVI Pattern (RULE-226)
  - Conditions:
    - When implementing MVI pattern with StateFlow for state and Channel for one-time events
    - When integrating credit balance with reactive repository flows and error handling
    - When building paywall/purchase UI with Google Play billing repository integration
    - When creating ViewModel that observes multiple repository flows with proper error handling
    - When writing ViewModel unit tests with fake repository implementations

- `docs/features/google-play-billing-integration.md` - Google Play Billing Integration (RULE-227)
  - Conditions:
    - When integrating Google Play Billing Library for in-app purchases
    - When implementing exponential backoff retry logic for transient failures
    - When managing connection lifecycle for external services (SERVICE_DISCONNECTED handling)
    - When creating testable abstractions over Android framework APIs via wrapper interfaces
    - When implementing purchase consumption and unconsumed purchase queries
    - When caching expensive query results (ProductDetails in-memory cache)
