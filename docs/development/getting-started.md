---
title: Getting Started
description: Developer setup guide for project-rulebook-android
author: Ivo
date: 2026-01-22
---

# Getting Started

Get up and running with the Rulebook Android project.

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| Android Studio | Latest stable | Arctic Fox or newer |
| JDK | 11+ | Bundled with Android Studio |
| Android SDK | API 24+ | Android 7.0 Nougat minimum |
| Kotlin | 2.0.21 | Managed via Gradle |
| Gradle | 8.9 | Wrapper included |

---

## Environment Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd project-rulebook-android
```

### 2. Android Studio Setup

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to `project-rulebook-android/`
4. Wait for Gradle sync to complete

### 3. Local Properties

Create or update `local.properties` at project root (NOT committed to git):

```properties
sdk.dir=/path/to/your/Android/Sdk
```

The app uses build-variant specific API URLs:
- **Debug/Staging:** `https://api-staging.rulebook.app/v1/`
- **Release/Production:** `https://api.rulebook.app/v1/`

No manual `.env` configuration needed - URLs are baked into build variants.

---

## Build & Run

### Debug Build

```bash
# From command line
./gradlew assembleDebug

# Or in Android Studio: Build > Make Project (Ctrl+F9 / Cmd+F9)
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Run on Device/Emulator

```bash
# Install and launch debug build
./gradlew installDebug

# Or in Android Studio: Run > Run 'app' (Shift+F10)
```

### Run Tests

```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :core:database:test
```

---

## Common Issues

### Gradle Sync Failed

```bash
# Clean Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Invalidate caches in Android Studio
File > Invalidate Caches / Restart
```

### Build Cache Issues

```bash
./gradlew --stop
./gradlew clean build --no-build-cache
```

---

## Next Steps

- See [development-guide.md](development-guide.md) for detailed development reference
- See [../architecture/overview.md](../architecture/overview.md) for architecture decisions
