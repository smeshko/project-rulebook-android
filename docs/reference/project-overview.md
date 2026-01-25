---
title: Project Overview
description: High-level project summary and technology stack
author: Ivo
date: 2026-01-22
---

# Project Overview

High-level summary of the Rulebook Android application.

---

## Project Identity

| Property | Value |
|----------|-------|
| **Name** | Rulebook |
| **Package** | com.rulebook |
| **Type** | Android Mobile Application |
| **Platform** | Android 7.0+ (API 24) |
| **Language** | Kotlin 2.0.21 |

---

## Executive Summary

Rulebook is a consumer Android application that transforms the board game learning experience. Users photograph any board game box and receive AI-generated, digestible rules within 60 seconds. The app follows Clean Architecture principles with a multi-module structure (16 modules), using Jetpack Compose for UI and a distinctive brutalist design aesthetic.

### Value Proposition

> **"From box to playing in 60 seconds"** - because game night is about connection, not homework.

### Key Features

- Photo-to-rules pipeline with multi-model AI fallback
- Progressive disclosure format: Setup → First Round → Deep Dive
- Offline-first architecture for basement/cabin game nights
- Credit-based monetization with Google Play Billing

---

## Technology Stack Summary

### Core Technologies

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose | BOM 2024.11.00 |
| Navigation | Compose Navigation | 2.8.4 |
| DI | Koin | 4.0.0 |
| Database | Room | 2.6.1 |
| Network | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Local Storage | DataStore | 1.1.1 |
| Camera | CameraX | 1.4.1 |
| Billing | Play Billing | 7.1.1 |
| Analytics | TelemetryDeck | 6.3.0 |

### Build System

| Tool | Version |
|------|---------|
| Gradle | 8.9 |
| Android Gradle Plugin | 8.7.2 |
| KSP | 2.0.21-1.0.27 |

---

## Architecture Classification

| Aspect | Classification |
|--------|----------------|
| Repository Type | Monolith |
| Architecture Type | Multi-Module |
| Architecture Pattern | Clean Architecture (MVVM) |
| State Management | StateFlow (Unidirectional) |
| Module Count | 16 (1 app + 9 core + 6 feature) |

---

## Repository Structure

```
project-rulebook-android/
├── app/                    # Main application module (entry point)
├── core/                   # Shared core modules
│   ├── analytics/          # TelemetryDeck analytics
│   ├── billing/            # Google Play Billing
│   ├── common/             # Result<T>, utilities
│   ├── data/               # Repository implementations
│   ├── database/           # Room database
│   ├── datastore/          # DataStore preferences
│   ├── designsystem/       # UI components & theme
│   ├── model/              # Domain models
│   └── network/            # Retrofit API client
├── feature/                # Feature modules
│   ├── camera/             # Photo capture
│   ├── library/            # Game library
│   ├── onboarding/         # Onboarding flow
│   ├── purchase/           # Credit purchases
│   ├── rules/              # Rules display
│   └── settings/           # Settings screen
├── build-logic/            # Gradle convention plugins
├── gradle/                 # Gradle wrapper & version catalog
└── docs/                   # Project documentation
```

---

## Key Metrics

| Metric | Value |
|--------|-------|
| Total Modules | 16 |
| API Endpoints | 2 |
| Database Tables | 2 |
| ViewModels | 5 |
| Design System Components | 5 |
| Kotlin Files | 100+ |

---

## Quick Start

```bash
# Clone and open in Android Studio
git clone <repository-url>
cd project-rulebook-android

# Build debug APK
./gradlew assembleDebug

# Run on device/emulator
./gradlew installDebug

# Run all tests
./gradlew test
```

---

## Project Status

| Aspect | Status |
|--------|--------|
| Implementation | In Progress (Epic 4+ of 9) |
| CI/CD | Not Configured |
| Localization | English Only |
| Test Coverage | Partial |

---

## Contact

- **Project Owner:** Ivo
