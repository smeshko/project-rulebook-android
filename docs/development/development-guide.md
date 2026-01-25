---
title: Development Guide
description: Detailed development reference for project-rulebook-android
author: Ivo
date: 2026-01-22
---

# Development Guide

Detailed reference for developing on the Rulebook Android project.

---

## Project Structure

```
project-rulebook-android/
├── app/                    # Main application module
├── core/                   # Shared core modules
│   ├── analytics/          # TelemetryDeck analytics
│   ├── billing/            # Google Play Billing
│   ├── common/             # Shared utilities
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
│   ├── purchase/           # In-app purchases
│   ├── rules/              # Rules display
│   └── settings/           # Settings screen
├── build-logic/            # Convention plugins
└── gradle/                 # Gradle wrapper & catalog
```

---

## Module Dependencies

### Adding New Feature Module

1. Create directory: `feature/<feature-name>/`
2. Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.rulebook.android.feature)
}

android {
    namespace = "com.rulebook.feature.<featurename>"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    // Add other dependencies
}
```

3. Register in `settings.gradle.kts`:

```kotlin
include(":feature:<feature-name>")
```

4. Sync Gradle

### Adding New Core Module

Similar process with `rulebook.android.library` plugin.

---

## Dependency Management

All dependencies defined in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.0.21"
composeBom = "2024.11.00"
koin = "4.0.0"
room = "2.6.1"
# ... etc

[libraries]
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
# ... etc
```

### Updating Dependencies

1. Update version in `libs.versions.toml`
2. Sync Gradle
3. Run tests
4. Commit changes

---

## Build Variants

| Variant | API URL | ProGuard | Debuggable |
|---------|---------|----------|------------|
| debug | api-staging.rulebook.app | No | Yes |
| release | api.rulebook.app | Yes | No |

### Release Build

```bash
# Requires signing configuration
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

### Clean Build

```bash
./gradlew clean
./gradlew build
```

---

## Testing

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :core:database:test
./gradlew :core:network:test
./gradlew :core:designsystem:test

# Run with coverage report
./gradlew testDebugUnitTest jacocoTestReport
```

### Instrumented Tests (Android Tests)

```bash
# Requires connected device or emulator
./gradlew connectedAndroidTest

# Specific module instrumented tests
./gradlew :core:database:connectedAndroidTest
```

### Lint Checks

```bash
./gradlew lint
./gradlew lintDebug
```

---

## Permissions

App requires (defined in `AndroidManifest.xml`):

| Permission | Purpose | Required |
|------------|---------|----------|
| `INTERNET` | API communication | Yes |
| `CAMERA` | Photo capture | No (graceful degradation) |
| `VIBRATE` | Haptic feedback | Yes |
| `BILLING` | In-app purchases | Yes |

---

## Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Root build config |
| `settings.gradle.kts` | Module registration |
| `gradle.properties` | Build properties |
| `gradle/libs.versions.toml` | Dependency versions |
| `local.properties` | Local SDK path (gitignored) |
| `proguard-rules.pro` | ProGuard obfuscation rules |

---

## Troubleshooting

### Kotlin Version Mismatch

Ensure all modules use the same Kotlin version from `libs.versions.toml`.

### Gradle Daemon Issues

```bash
./gradlew --stop
```

### Memory Issues

Add to `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError
```

---

## IDE Configuration

### Recommended Android Studio Plugins

- Kotlin
- Compose Preview (bundled)
- EditorConfig
- Git integration

### Code Style

Project uses official Kotlin code style:
- `kotlin.code.style=official` in `gradle.properties`

---

## Additional Resources

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Koin DI Documentation](https://insert-koin.io/)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [CameraX Documentation](https://developer.android.com/training/camerax)
