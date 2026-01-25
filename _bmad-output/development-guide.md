# Development Guide: Rulebook Android

**Generated:** 2026-01-22
**Project:** project-rulebook-android

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

### 3. Environment Variables

Create or update `local.properties` at project root (NOT committed to git):

```properties
sdk.dir=/path/to/your/Android/Sdk
```

The app uses build-variant specific API URLs:
- **Debug/Staging:** `https://api-staging.rulebook.app/v1/`
- **Release/Production:** `https://api.rulebook.app/v1/`

No manual `.env` configuration needed - URLs are baked into build variants.

---

## Build Commands

### Debug Build

```bash
# From command line
./gradlew assembleDebug

# Or in Android Studio: Build > Make Project (Ctrl+F9 / Cmd+F9)
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

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

## Run Commands

### Run on Device/Emulator

```bash
# Install and launch debug build
./gradlew installDebug

# Or in Android Studio: Run > Run 'app' (Shift+F10)
```

### Run Specific Module

```bash
# Run tests for specific module
./gradlew :core:database:test
./gradlew :feature:camera:test
```

---

## Test Commands

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

### Kotlin Version Mismatch

Ensure all modules use the same Kotlin version from `libs.versions.toml`.

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
