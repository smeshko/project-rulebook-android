# Developer Onboarding Guide

> Welcome to Rulebook Android! This guide will help you get up and running quickly.

## Table of Contents
- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [First Build](#first-build)
- [Understanding the Codebase](#understanding-the-codebase)
- [Development Workflow](#development-workflow)
- [Common Tasks](#common-tasks)
- [Troubleshooting](#troubleshooting)
- [Resources](#resources)

## Project Overview

### What is Rulebook?

Rulebook is an AI-powered Android app that helps board game enthusiasts learn games quickly. Users photograph game boxes, and the app identifies the game and generates easy-to-follow rules.

**Key Value Proposition:** "From box to playing in 60 seconds"

### Tech Stack Quick Reference

| What | Technology |
|------|------------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose |
| Architecture | Multi-module Clean Architecture (MVI) |
| DI | Koin 4.0.0 |
| Database | Room 2.6.1 |
| Navigation | Compose Navigation |
| Image Loading | Coil 3.0.3 |
| HTTP | Retrofit + OkHttp |
| Build | Gradle 8.7.2 (Kotlin DSL) |

### Project Context

- **iOS Migration:** This is an Android version of an existing iOS app
- **Documentation:** Extensive planning docs in `/docs/` directory
- **Development Methodology:** BMAD (systematic story-by-story implementation)
- **Current Phase:** Mid-implementation (Phase 4)

## Prerequisites

### Required Software

1. **Android Studio Ladybug (2024.2.1) or later**
   - Download from: https://developer.android.com/studio
   - Includes Android SDK, emulator, and tools

2. **JDK 17 or later**
   - Android Studio includes JDK 17
   - Verify: `java -version`

3. **Git**
   - macOS: Pre-installed or via Homebrew: `brew install git`
   - Verify: `git --version`

### Recommended Software

- **Gradle** (optional - project includes wrapper)
  - macOS: `brew install gradle`

- **Android Command Line Tools** (for CLI builds)
  - Included with Android Studio

### Required Android SDK Components

Install via Android Studio → Settings → Appearance & Behavior → System Settings → Android SDK:

- ✅ Android 15 (API 35) SDK Platform
- ✅ Android 14 (API 34) SDK Platform
- ✅ Android SDK Build-Tools 35.0.0
- ✅ Android Emulator
- ✅ Android SDK Platform-Tools

### Account Setup

1. **GitHub Access** (if private repo)
   - Generate SSH key: `ssh-keygen -t ed25519 -C "your_email@example.com"`
   - Add to GitHub: Settings → SSH and GPG keys

2. **API Keys** (if applicable)
   - Backend API key (for AI features)
   - Google Play Developer account (for billing testing)

## Environment Setup

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone git@github.com:your-org/project-rulebook-android.git
cd project-rulebook-android

# Or if using HTTPS
git clone https://github.com/your-org/project-rulebook-android.git
cd project-rulebook-android
```

### Step 2: Open in Android Studio

1. Launch Android Studio
2. Select **"Open"** from the welcome screen
3. Navigate to the cloned `project-rulebook-android` directory
4. Click **"Open"**
5. Wait for Gradle sync to complete (3-5 minutes first time)

### Step 3: Configure API Keys

Create `local.properties` in the project root:

```bash
# local.properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk

# API Configuration (if applicable)
API_BASE_URL=https://api.rulebook.app/v1/
API_KEY=your_api_key_here
```

**Note:** `local.properties` is gitignored - never commit this file!

### Step 4: Sync Project

```bash
# Command line
./gradlew sync

# Or in Android Studio
File → Sync Project with Gradle Files
```

### Step 5: Configure Emulator

**Create an Android Virtual Device (AVD):**

1. Android Studio → Tools → Device Manager
2. Click **"Create Device"**
3. Select **Pixel 6 Pro** (or similar modern device)
4. Select **API 35 (Android 15)** system image
   - Download if not already installed
5. Name: `Rulebook_Test_Device`
6. Advanced Settings:
   - RAM: 2048 MB (minimum)
   - Internal Storage: 2048 MB
7. Click **"Finish"**

**Or use physical device:**
1. Enable Developer Options on your Android device:
   - Settings → About phone → Tap "Build number" 7 times
2. Enable USB Debugging:
   - Settings → System → Developer options → USB debugging
3. Connect via USB
4. Accept "Allow USB debugging" prompt

## First Build

### Build the Project

```bash
# Clean build
./gradlew clean build

# Or just debug APK
./gradlew :app:assembleDebug
```

**Expected output:**
```
BUILD SUCCESSFUL in 45s
```

### Run the App

#### Option 1: Android Studio
1. Select `app` configuration in toolbar
2. Select emulator or device
3. Click ▶️ Run button (or press Ctrl+R / Cmd+R)

#### Option 2: Command Line
```bash
# Install on connected device/emulator
./gradlew :app:installDebug

# Or install and launch
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.rulebook.debug/.MainActivity
```

### Verify Installation

✅ App launches successfully
✅ Onboarding screens appear
✅ No crash on first launch
✅ Navigation works between screens

## Understanding the Codebase

### Module Overview

The project has **16 modules** organized by feature and layer:

```
📁 app/                    # Main application module
📁 feature/
   ├─ library/             # Game library (grid view)
   ├─ camera/              # Photo capture
   ├─ rules/               # Rules generation & display
   ├─ settings/            # App settings
   ├─ onboarding/          # First-time user experience
   └─ purchase/            # Credit purchases
📁 core/
   ├─ designsystem/        # UI components & theme
   ├─ data/                # Repositories
   ├─ database/            # Room database
   ├─ network/             # API client
   ├─ model/               # Domain models
   ├─ common/              # Utilities
   ├─ analytics/           # TelemetryDeck
   ├─ billing/             # Google Play Billing
   └─ datastore/           # User preferences
📁 build-logic/            # Convention plugins
```

### Key Files to Know

| File | Purpose | Location |
|------|---------|----------|
| `RulebookApplication.kt` | App entry point, Koin setup | `app/src/main/kotlin/com/rulebook/` |
| `RulebookTheme.kt` | Brutalist design system | `core/designsystem/src/main/kotlin/` |
| `RulebookDatabase.kt` | Room database definition | `core/database/src/main/kotlin/` |
| `libs.versions.toml` | Dependency versions | `gradle/libs.versions.toml` |
| `settings.gradle.kts` | Module includes | Root directory |

### Navigation Structure

```
App Launch
    ↓
Has completed onboarding?
    ├─ No → Onboarding Flow
    │          ├─ Welcome Screen
    │          └─ Credits Info Screen
    │                  ↓
    └─ Yes → Library Screen (Main)
                 ├─ Camera Screen (FAB)
                 ├─ Rules Screen (game click)
                 ├─ Settings Screen (menu)
                 └─ Purchase Screen (paywall)
```

### Data Flow Example

**Scenario:** User views their game library

```
1. LibraryScreen.kt
   └─ Observes uiState from ViewModel

2. LibraryViewModel.kt
   └─ Calls GamesRepository.getAllGames()

3. OfflineFirstGamesRepository.kt
   └─ Queries GamesDao.getAllGames()

4. GamesDao.kt (Room)
   └─ SELECT * FROM games

5. Result flows back up:
   Database → Repository → ViewModel → UI State → Screen
```

### Design System

**Brutalist Aesthetic:**
- Thick borders (4-6dp)
- Bold shadows
- High contrast colors
- ExtraBold typography
- Distinctive, non-generic look

**Key Components:**
- `RulebookButton` - Primary action buttons
- `RulebookCard` - Content cards
- `RulebookTextField` - Input fields
- `RulebookTopAppBar` - Screen headers

**Theme Colors:**
```kotlin
// Light mode
primary = Color(0xFF6C63FF)      // Vibrant purple
secondary = Color(0xFFFF6584)    // Coral pink

// Dark mode
primary = Color(0xFF8B84FF)      // Lighter purple
secondary = Color(0xFFFF85A1)    // Lighter coral
```

## Development Workflow

### Git Workflow

```
main (production)
  ↑
  PR from staging
  ↑
staging (integration)
  ↑
  PR from feature branch
  ↑
feature/my-feature (your work)
```

### Creating a Feature Branch

```bash
# Update staging
git checkout staging
git pull origin staging

# Create feature branch
git checkout -b feature/RULE-123-add-zoom-controls

# Work on your feature
# ... make changes ...

# Commit frequently
git add .
git commit -m "feat(camera): add zoom slider component"

# Push to remote
git push -u origin feature/RULE-123-add-zoom-controls
```

### Commit Message Format

Follow **Conventional Commits:**

```
type(scope): description

Types:
- feat: New feature
- fix: Bug fix
- docs: Documentation
- test: Tests
- refactor: Code refactoring
- style: Formatting changes
- chore: Build/config changes

Examples:
feat(camera): add zoom controls
fix(library): resolve grid layout crash
docs(readme): update setup instructions
test(rules): add ViewModel unit tests
```

### Pull Request Process

1. **Ensure code quality:**
   ```bash
   ./gradlew check
   ./gradlew test
   ./gradlew build
   ```

2. **Create PR to `staging` (NOT main)**
   - Title: `[RULE-123] Add zoom controls to camera`
   - Description template:
     ```markdown
     ## Summary
     Brief description of changes

     ## Changes Made
     - Added zoom slider component
     - Updated camera ViewModel
     - Added unit tests

     ## Testing
     - Verified zoom functionality on Pixel 6 emulator
     - All unit tests pass
     - Manual testing completed

     ## Screenshots
     [If UI changes]

     ## Related Issues
     Closes #123
     ```

3. **Request review**

4. **Address feedback**

5. **Merge after approval**

## Common Tasks

### Adding a New Screen

1. **Create navigation route:**
   ```kotlin
   // app/src/main/kotlin/com/rulebook/navigation/RulebookNavigation.kt
   @Serializable
   object MyNewRoute
   ```

2. **Create UI state:**
   ```kotlin
   // feature/mynew/MyNewUiState.kt
   data class MyNewUiState(
       val isLoading: Boolean = false,
       val data: List<Item> = emptyList(),
       val error: String? = null
   )
   ```

3. **Create ViewModel:**
   ```kotlin
   // feature/mynew/MyNewViewModel.kt
   class MyNewViewModel : ViewModel() {
       private val _uiState = MutableStateFlow(MyNewUiState())
       val uiState: StateFlow<MyNewUiState> = _uiState.asStateFlow()
   }
   ```

4. **Create screen:**
   ```kotlin
   // feature/mynew/MyNewScreen.kt
   @Composable
   fun MyNewScreen(
       viewModel: MyNewViewModel = koinViewModel()
   ) {
       val uiState by viewModel.uiState.collectAsStateWithLifecycle()

       MyNewScreenContent(
           data = uiState.data,
           isLoading = uiState.isLoading
       )
   }
   ```

5. **Add to navigation graph:**
   ```kotlin
   // app/src/main/kotlin/com/rulebook/RulebookApp.kt
   composable<MyNewRoute> {
       MyNewScreen()
   }
   ```

6. **Register in Koin:**
   ```kotlin
   // feature/mynew/di/MyNewModule.kt
   val myNewModule = module {
       viewModel { MyNewViewModel() }
   }

   // app/src/main/kotlin/com/rulebook/RulebookApplication.kt
   modules(/* existing modules */, myNewModule)
   ```

### Adding a Database Entity

1. **Create entity:**
   ```kotlin
   // core/database/src/main/kotlin/com/rulebook/core/database/model/MyEntity.kt
   @Entity(tableName = "my_table")
   data class MyEntity(
       @PrimaryKey val id: String,
       val name: String,
       val createdAt: Long
   )
   ```

2. **Create DAO:**
   ```kotlin
   // core/database/src/main/kotlin/com/rulebook/core/database/dao/MyDao.kt
   @Dao
   interface MyDao {
       @Query("SELECT * FROM my_table")
       fun getAll(): List<MyEntity>

       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insert(entity: MyEntity)
   }
   ```

3. **Add to database:**
   ```kotlin
   // core/database/.../RulebookDatabase.kt
   @Database(
       entities = [MyEntity::class, /* existing */],
       version = 2,  // Increment version!
       exportSchema = true
   )
   abstract class RulebookDatabase : RoomDatabase() {
       abstract fun myDao(): MyDao
       // ... existing
   }
   ```

4. **Create migration:**
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL(
               "CREATE TABLE my_table (id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL, createdAt INTEGER NOT NULL)"
           )
       }
   }
   ```

5. **Register DAO in Koin:**
   ```kotlin
   single { get<RulebookDatabase>().myDao() }
   ```

### Running Tests

```bash
# All unit tests
./gradlew test

# Specific module
./gradlew :feature:library:test

# With coverage
./gradlew jacocoTestReport

# Android instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Specific test class
./gradlew :feature:library:testDebugUnitTest --tests "LibraryViewModelTest"
```

### Building Release APK

```bash
# Build release APK
./gradlew :app:assembleRelease

# Output location
# app/build/outputs/apk/release/app-release.apk

# Build app bundle (for Play Store)
./gradlew :app:bundleRelease

# Output location
# app/build/outputs/bundle/release/app-release.aab
```

### Checking Code Style

```bash
# Run lint checks
./gradlew lint

# Run Detekt (if configured)
./gradlew detekt

# Format code (if ktlint configured)
./gradlew ktlintFormat
```

## Troubleshooting

### Build Errors

**Problem:** `Gradle sync failed: Plugin [id: '...'] was not found`

**Solution:**
```bash
./gradlew --refresh-dependencies
File → Invalidate Caches → Invalidate and Restart
```

---

**Problem:** `Execution failed for task ':app:compileDebugKotlin'`

**Solution:**
1. Check Kotlin version matches in `libs.versions.toml`
2. Clean and rebuild:
   ```bash
   ./gradlew clean build
   ```

---

**Problem:** `Could not resolve all dependencies`

**Solution:**
1. Check internet connection
2. Verify `repositories` in `settings.gradle.kts`:
   ```kotlin
   google()
   mavenCentral()
   ```
3. Try with VPN if behind firewall

---

### Runtime Errors

**Problem:** App crashes on launch with `KoinAppAlreadyStartedException`

**Solution:**
- Koin is being initialized multiple times
- Check `RulebookApplication.onCreate()` only calls `startKoin` once
- Verify tests use `stopKoin()` in teardown

---

**Problem:** `java.lang.IllegalStateException: Room cannot verify database schema`

**Solution:**
- Database schema changed but version not incremented
- Either:
  1. Increment database version in `RulebookDatabase.kt`
  2. Add migration
  3. OR for development: Clear app data or uninstall/reinstall

---

**Problem:** Compose preview not showing

**Solution:**
1. Click "Build & Refresh" in preview pane
2. Invalidate caches: File → Invalidate Caches → Restart
3. Ensure preview function has `@Preview` annotation:
   ```kotlin
   @Preview
   @Composable
   private fun MyScreenPreview() {
       RulebookTheme {
           MyScreen()
       }
   }
   ```

---

### Emulator Issues

**Problem:** Emulator won't start

**Solution:**
1. Check system resources (RAM, disk space)
2. Update Android Emulator in SDK Manager
3. Try creating a new AVD
4. macOS: Grant permissions in System Preferences → Security & Privacy

---

**Problem:** App not installing on emulator

**Solution:**
```bash
# Check connected devices
adb devices

# If none listed, restart ADB
adb kill-server
adb start-server

# Uninstall old version
adb uninstall com.rulebook.debug

# Install fresh
./gradlew :app:installDebug
```

---

### Git Issues

**Problem:** Cannot push to remote

**Solution:**
1. Check branch name matches remote:
   ```bash
   git branch -vv
   ```
2. Set upstream if needed:
   ```bash
   git push -u origin feature/my-branch
   ```

---

**Problem:** Merge conflicts

**Solution:**
```bash
# Update your branch with latest staging
git checkout staging
git pull origin staging
git checkout feature/my-branch
git merge staging

# Resolve conflicts in Android Studio
# Then commit the merge
git add .
git commit -m "chore: merge staging into feature branch"
```

## Resources

### Documentation

- **Project Docs:** `/docs/` directory
  - [PRD](../prd.md) - Product requirements
  - [Architecture](../architecture.md) - Technical decisions
  - [UX Design](../ux-design-specification.md) - Design system
  - [Epics & Stories](../epics.md) - Implementation plan

- **Technical Guide:** [TECHNICAL_ARCHITECTURE.md](./TECHNICAL_ARCHITECTURE.md)

### External Resources

**Android Development:**
- [Android Developers](https://developer.android.com/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Now in Android (Reference App)](https://github.com/android/nowinandroid)

**Kotlin:**
- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

**Architecture:**
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Multi-module architecture](https://developer.android.com/topic/modularization)

**Libraries:**
- [Room](https://developer.android.com/training/data-storage/room)
- [Koin](https://insert-koin.io/)
- [Retrofit](https://square.github.io/retrofit/)
- [Coil](https://coil-kt.github.io/coil/)

### Getting Help

1. **Check existing documentation** in `/docs/`
2. **Search codebase** for similar implementations
3. **Ask the team** in Slack/Discord
4. **Review iOS implementation** in `/docs/ios/` for reference
5. **Stack Overflow** with `android`, `jetpack-compose`, `kotlin` tags

### Recommended IDE Plugins

**Android Studio Plugins:**
- **Kotlin Fill Class** - Auto-fill constructor parameters
- **Rainbow Brackets** - Matching bracket colors
- **Key Promoter X** - Learn keyboard shortcuts
- **ADB Idea** - Quick ADB commands

Install: Android Studio → Settings → Plugins → Marketplace

### Useful Commands Cheat Sheet

```bash
# Build
./gradlew clean build                    # Clean and build all
./gradlew :app:assembleDebug             # Build debug APK
./gradlew :app:bundleRelease             # Build release AAB

# Test
./gradlew test                           # All unit tests
./gradlew connectedAndroidTest           # All instrumented tests
./gradlew :feature:library:test          # Module-specific tests

# Install
./gradlew :app:installDebug              # Install debug build
adb uninstall com.rulebook.debug         # Uninstall app

# ADB
adb devices                              # List connected devices
adb logcat                               # View device logs
adb shell am start -n com.rulebook.debug/.MainActivity  # Launch app

# Git
git checkout -b feature/my-feature       # Create feature branch
git commit -m "feat(scope): message"     # Commit changes
git push -u origin feature/my-feature    # Push to remote
git pull origin staging                  # Update from staging

# Gradle
./gradlew --refresh-dependencies         # Refresh dependencies
./gradlew tasks                          # List all tasks
./gradlew dependencies                   # Show dependency tree
```

## Next Steps

Now that you're set up, here's what to do next:

1. ✅ **Explore the codebase**
   - Browse through feature modules
   - Read existing ViewModels and screens
   - Check out the design system components

2. ✅ **Pick a starter task**
   - Look for issues tagged `good-first-issue`
   - Start with a small bug fix or test addition
   - Ask for a simple story assignment

3. ✅ **Read the planning docs**
   - [PRD](../prd.md) - Understand product vision
   - [Architecture](../architecture.md) - Technical context
   - [Epics](../epics.md) - See what's being built

4. ✅ **Set up your IDE**
   - Install recommended plugins
   - Configure code style (Project → Editor → Code Style → Kotlin)
   - Set up live templates for common patterns

5. ✅ **Join the team rituals**
   - Daily standups (if applicable)
   - Code reviews
   - Sprint planning

---

**Welcome aboard! Happy coding! 🚀**

If you have questions, don't hesitate to ask the team. We're here to help!
