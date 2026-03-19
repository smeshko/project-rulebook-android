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

- `docs/features/product-cards-badges.md` - Product Cards with Badges System (RULE-228)
  - Conditions:
    - When implementing product card displays in the purchase flow
    - When extending the badge system with new badge types
    - When customizing product card styling or animations
    - When adding new product SKU types that need badges
    - When troubleshooting product card layout or shimmer animation behavior

- `docs/features/purchase-verification-client-side-interim.md` - Purchase Verification & Credit Delivery (Client-Side — Interim) (RULE-230)
  - Conditions:
    - When implementing purchase verification logic with the consume-before-deliver pattern
    - When replacing the interim client-side verifier with a server-side implementation (Epic 10)
    - When injecting PurchaseVerifier into ViewModels or other components
    - When designing swappable dependencies for purchase-related features
    - When adding new SKU types and mapping them to credit amounts

- `docs/features/restore-purchases-with-partial-success.md` - Restore Purchases with Partial Success Handling (RULE-231)
  - Conditions:
    - When implementing restore purchases functionality for in-app purchases
    - When handling mixed success/failure scenarios where some operations succeed and others fail
    - When querying unconsumed purchases from Google Play Billing API
    - When designing loading indicators that replace button text (AnimatedDots pattern)
    - When implementing snackbar feedback with retry actions for purchase-related errors

- `docs/features/pending-purchase-family-approval.md` - Pending Purchase Handling for Family Approval (Ask-to-Buy) (RULE-232)
  - Conditions:
    - When implementing Ask-to-Buy family approval handling for in-app purchases
    - When storing persistent state for pending operations in DataStore
    - When checking for asynchronous approval resolution on app resume
    - When implementing pause-and-retry patterns for user approval workflows
    - When designing lifecycle-aware checks that run on app foreground

- `docs/features/purchase-analytics-events.md` - Purchase Analytics Events (RULE-233)
  - Conditions:
    - When tracking purchase funnel metrics through paywall and purchase flow
    - When implementing cross-platform analytics with iOS event name alignment
    - When adding convenience methods to AnalyticsManager for domain-specific events
    - When propagating user context (source) through navigation arguments to ViewModels
    - When implementing privacy-compliant analytics that excludes PII (prices, payment methods)

- `docs/features/theme-selection-light-dark-system.md` - Theme Selection (Light/Dark/System Modes) (RULE-234)
  - Conditions:
    - When implementing theme selection with Light/Dark/System modes
    - When creating preference source abstractions for reactive setting persistence
    - When designing settings UI with radio-button selection rows
    - When applying theme changes at app root without activity restart
    - When integrating system dark mode detection with user preference override

- `docs/features/haptic-feedback-toggle.md` - Haptic Feedback Toggle (RULE-235)
  - Conditions:
    - When adding new preferences to the Settings screen
    - When implementing preference-aware feature toggles in the app
    - When extending haptic feedback to new actions or modules
    - When creating new preference sources following the established pattern
    - When modifying camera or gesture-based interactions that trigger haptics

- `docs/features/about-section-legal-links.md` - About Section: Version Display and Legal Links (RULE-237)
  - Conditions:
    - When extending the About section with new information rows or legal links
    - When implementing safe external link navigation via Intent.ACTION_VIEW
    - When handling ActivityNotFoundException for unavailable browsers or email clients
    - When creating reusable settings UI components with icon boxes
    - When adding dynamic version display to your app

- `docs/features/clear-data-confirmation-flow.md` - Clear Data Confirmation Flow (RULE-238)
  - Conditions:
    - When implementing destructive operations with confirmation dialogs in Jetpack Compose
    - When clearing database tables and resetting user preferences in Android apps
    - When deciding between interface abstractions vs lambda patterns for Room dependencies
    - When implementing dialogs that preserve certain data during reset operations

- `docs/features/app-shortcuts-deep-link-mvi-pattern.md` - App Shortcuts: Deep Link Navigation with MVI Preference Pattern (RULE-239)
  - Conditions:
    - When implementing app shortcuts with deep link navigation to screens
    - When observing user preferences (haptics, theme, language) in ViewModels for MVI pattern compliance
    - When handling warm-start deep link navigation with singleTop launch mode
    - When creating preference source implementations with DataStore error handling patterns
    - When writing unit tests for ViewModels that observe multiple preference flows
    - When designing lifecycle-safe state management for preference-dependent UI features

- `docs/features/predictive-back-gesture-refinement.md` - Predictive Back Gesture Refinement (RULE-240)
  - Conditions:
    - When implementing back gesture handling with predictive back preview on Android 14+
    - When adding or modifying BackHandler in Compose — understand when NOT to use it
    - When designing in-screen pagination (like Onboarding pager) that needs back navigation
    - When removing or refactoring back gesture handling to enable predictive back animation
    - When troubleshooting missing predictive back preview or back gesture conflicts

- `docs/features/edge-to-edge-display-cutout-handling.md` - Edge-to-Edge Display Cutout Handling in Immersive Mode (RULE-241)
  - Conditions:
    - When building full-screen immersive UIs that hide system bars (e.g., Camera, fullscreen video)
    - When adding overlay controls (buttons, indicators) to screens with display cutouts (notches, punch holes)
    - When choosing between `statusBarsPadding()` and `safeDrawingPadding()` in immersive mode
    - When handling display cutout insets separately from system bar insets in Compose
    - When testing full-screen UIs on notched devices (Pixel 3+, etc.) with gesture navigation
