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
