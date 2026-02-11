---
title: Conditional Documentation Guide
description: Find documentation based on your current task
---

# Conditional Documentation Guide

This guide helps you find relevant documentation based on what you're working on.

## Instructions

- Review the task you need to perform
- Check the conditions below
- Read the relevant documentation before proceeding
- Only read documentation if conditions match your task

## Documentation Map

- docs/features/credit-gated-navigation.md
  - Conditions:
    - When implementing credit checks for premium features
    - When adding navigation that requires credit validation
    - When working with the camera/scan flow
    - When implementing paywall navigation
    - When managing resource cleanup after navigation
    - When preventing memory leaks in ViewModels with navigation

- docs/features/generation-progress-screen.md
  - Conditions:
    - When implementing the scan/generation pipeline (Stories 5.3-5.7)
    - When adding new scan phases or modifying progress ranges
    - When working with the ProgressPhaseIndicator design system component
    - When navigating to or from the generation screen
    - When implementing multi-step progress tracking in other features

- docs/features/image-analysis-api-integration.md
  - Conditions:
    - When implementing image upload or base64 encoding for the Rulebook API
    - When adding new API calls that need user-friendly error messages via NetworkErrorMapper
    - When working with the scan/generation pipeline (Stories 5.4-5.7)
    - When modifying image compression or bitmap processing logic
    - When extending NetworkErrorMapper with new error types

- docs/features/confidence-auto-proceed.md
  - Conditions:
    - When implementing confidence-based branching in the scan/generation pipeline
    - When modifying the auto-proceed threshold or adding remote config for confidence settings
    - When working with the ConfidenceBadge design system component
    - When adding new confirmation/validation screens to the generation flow
    - When implementing analytics for scan confidence events (scan_analysis_complete, scan_confirmed, scan_manual_entry)

- docs/features/manual-game-entry.md
  - Conditions:
    - When implementing manual game name input or text entry in the generation flow
    - When working with the RulebookTextField design system component
    - When modifying the reject/manual entry flow after AI identification
    - When implementing analytics for manual name submission (scan_manual_name_submitted)
    - When adding new text input components to the app

- docs/features/rules-generation-api-integration.md
  - Conditions:
    - When implementing rules generation from identified game titles in the scan/generation pipeline
    - When extending the rules generation API with new fields or response formats
    - When troubleshooting rules display or section mapping issues
    - When adding analytics tracking for generation events (scan_generation_complete, scan_failed)
    - When working with the intelligent section mapper or RulesMapper patterns
    - When implementing similar API response transformation patterns with positional fallback

- docs/features/rules-persistence-and-database-save.md
  - Conditions:
    - When saving generated rules to the local database after generation completes
    - When implementing transaction-based database operations with Room DAOs
    - When integrating credit deduction with database save operations
    - When working with GameRepository or extending game persistence functionality
    - When troubleshooting game save failures or understanding back stack navigation after save
    - When implementing defensive error handling for critical state-changing operations

- docs/features/fallback-ai-model-handling.md
  - Conditions:
    - When implementing fallback/retry logic for API calls or model predictions
    - When handling low-confidence results that need secondary validation
    - When categorizing errors to determine if a failure is retryable
    - When adding multi-model strategies to improve reliability
    - When tracking analytics for fallback attempts and failures
    - When implementing graceful degradation instead of error states

- docs/features/retry-failed-recognition.md
  - Conditions:
    - When implementing error screens or recovery UIs for failed operations
    - When adding retry functionality after errors in the scan/generation pipeline
    - When working with in-screen error states vs navigation-based error handling
    - When implementing user-friendly error messages with NetworkErrorMapper
    - When adding analytics for error recovery actions (retry, manual entry from error)
    - When understanding rendering priority chains in conditional UI state management

- docs/features/scan-analytics-events.md
  - Conditions:
    - When implementing scan funnel tracking or analytics events
    - When modifying the scan/generation pipeline to track user behavior
    - When adding analytics to confidence-based decision points
    - When tracking error recovery paths (retry, fallback, manual entry)
    - When aligning Android and iOS analytics event names and properties

- docs/features/win-condition-display-pattern.md
  - Conditions:
    - When displaying win conditions in the rules overview or other sections
    - When implementing the WinConditionCallout brutalist UI component
    - When extending win condition extraction to other rule sections (Setup, First Round, Advanced)
    - When implementing heuristic parsing for unstructured game rule content
    - When preserving backward compatibility for cached rules without win conditions

- docs/features/setup-checklist-interactive-tracking.md
  - Conditions:
    - When implementing interactive checklists for rule sections (Setup, First Round, Advanced)
    - When working with Material 3 Checkbox component styling and custom colors in Jetpack Compose
    - When implementing session-local state tracking that persists through UI recomposition but resets on navigation
    - When extending CollapsibleRuleSection to new sections or adding user-interactive elements
    - When implementing strikethrough text decoration and reduced opacity visual feedback in Compose

- docs/features/numbered-steps-display-pattern.md
  - Conditions:
    - When implementing step-by-step guides or sequential content in rules sections
    - When adding numbered steps rendering mode to CollapsibleRuleSection or similar components
    - When working with the NumberedStepItem design system component
    - When displaying ordered, sequential procedures that users follow step-by-step
    - When implementing tutorial or onboarding flows with clear sequential ordering
    - When extending CollapsibleRuleSection rendering modes for new section types

- docs/features/advanced-rules-section.md
  - Conditions:
    - When implementing the Advanced Rules section or extending it with new features
    - When working with default bullet point rendering mode in CollapsibleRuleSection
    - When implementing edge case rules or detailed reference content in rules sections
    - When understanding why Advanced section uses bullets instead of checklist or numbered steps
    - When adding future sub-section support for nested content within rule sections
    - When implementing similar reference-style sections that are not sequential or interactive

- docs/features/collapsible-section-shadow-animation.md
  - Conditions:
    - When implementing smooth state-driven animations using `animateDpAsState`
    - When animating shadow offset or other Dp properties in response to state changes
    - When synchronizing multiple animations (shadow, rotation, visibility) to feel coordinated
    - When adding animation effects to existing CollapsibleRuleSection or similar components
    - When integrating design system tokens with animated component properties

- docs/features/offline-rules-access.md
  - Conditions:
    - When implementing offline data access for cached rules and game content
    - When configuring Coil ImageLoader with disk cache for offline thumbnail display
    - When handling placeholder images for uncached content
    - When building offline-first features with Room database as source of truth
    - When verifying no network dependencies exist in read-only data flows
    - When implementing explicit disk/memory caching strategies for image loading

- docs/features/share-rules-functionality.md
  - Conditions:
    - When implementing share functionality for the rules screen or other content
    - When working with Android Intent.ACTION_SEND and system share sheets in Jetpack Compose
    - When formatting game rules or similar domain content for text sharing
    - When handling ActivityNotFoundException for missing share target apps
    - When implementing state-aware UI buttons that show/hide based on data load state
    - When creating pure formatter utilities for shareable content

- docs/features/background-timestamp-update-pattern.md
  - Conditions:
    - When implementing fire-and-forget background operations in ViewModels
    - When performing non-blocking updates that shouldn't affect UI state
    - When implementing timestamp or audit field updates in the repository layer
    - When adding background operations like analytics, logging, or cache updates
    - When optimizing database queries with targeted SQL UPDATE operations
