# Rulebook App - Android Migration Product Overview

**Document Version**: 1.0
**Date**: December 2024
**Purpose**: Complete product specification for Android platform migration

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [App Overview](#app-overview)
3. [Architecture Overview](#architecture-overview)
4. [Feature Modules](#feature-modules)
5. [Screen Inventory](#screen-inventory)
6. [Navigation Architecture](#navigation-architecture)
7. [Data Models](#data-models)
8. [API Specification](#api-specification)
9. [Core Services](#core-services)
10. [Design System](#design-system)
11. [Permission Requirements](#permission-requirements)
12. [Platform Mapping Guide](#platform-mapping-guide)

---

## Executive Summary

Rulebook is a mobile application that allows users to scan board game boxes using their device camera, identifies the game using AI-powered image analysis, and generates comprehensive game rules. The app uses a credit-based system where users receive 3 free scans and can purchase additional credits via in-app purchases.

**Core Value Proposition**: Instantly learn any board game's rules by simply taking a photo of the game box.

**Key Statistics**:
- ~28,966 lines of Swift code
- 6 main feature modules
- 10 core services
- 2 API endpoints
- 16+ screens with multiple states

---

## App Overview

### Core User Journey

```
1. First Launch → Onboarding (2 screens) → Main App
2. User taps Camera → Check credits → If credits available: Camera opens
3. User captures game box photo → Image processed & compressed
4. Image sent to AI backend → Game identified with confidence score
5. If high confidence: Auto-fetch rules | If low: User confirms/enters game name
6. Rules generated → Saved to library → Displayed to user
7. User can browse saved games in Library anytime
```

### Key Features Summary

| Feature | Description |
|---------|-------------|
| Photo Capture | Custom camera with flash, zoom, tap-to-focus, gallery picker |
| AI Game Detection | Backend service identifies game from box photo |
| Rules Generation | AI generates progressive rules (setup, first round, advanced) |
| Game Library | Persistent storage of scanned games with sort/filter |
| Credit System | 3 free scans, purchasable credit packs (1, 3, 10) |
| Settings | Theme mode, haptics, support links, data management |

---

## Architecture Overview

### Technology Stack (iOS)

| Component | iOS Technology | Android Equivalent |
|-----------|---------------|-------------------|
| Language | Swift 6.0 | Kotlin |
| UI Framework | SwiftUI | Jetpack Compose |
| State Management | TCA (Composable Architecture) | MVI/MVVM + StateFlow |
| Dependency Injection | Swift Dependencies | Hilt/Dagger |
| Local Storage | GRDB (SQLite) | Room |
| Networking | URLSession + async/await | Retrofit/OkHttp + Coroutines |
| IAP | StoreKit 2 | Google Play Billing |
| Camera | AVFoundation | CameraX |
| Analytics | TelemetryDeck | Firebase Analytics |

### Module Structure

```
app/
├── features/
│   ├── photo/           # Camera capture & gallery
│   ├── rules-generation/ # AI analysis & rules workflow
│   ├── rules/           # Rules display & management
│   ├── library/         # Saved games browsing
│   ├── purchase/        # IAP & paywall
│   ├── settings/        # User preferences
│   └── onboarding/      # First-time user flow
├── services/
│   ├── network/         # HTTP client
│   ├── persistence/     # Local database
│   ├── storage/         # Preferences/settings
│   ├── camera/          # Camera session management
│   ├── image-processing/ # Image compression
│   ├── storekit/        # In-app purchases
│   ├── credit-manager/  # Credit balance
│   ├── analytics/       # Event tracking
│   └── haptics/         # Vibration feedback
├── core-ui/             # Design system components
├── entities/            # Shared domain models
└── framework/           # App configuration, errors
```

### Architectural Pattern

**Feature-Scoped Clean Architecture**:
- Each feature is self-contained with 4 layers:
  - **Presentation**: UI + State management (Reducer/ViewModel)
  - **Domain**: Business logic, protocols, error types
  - **Data**: Repositories, data sources (local/remote)
  - **Infrastructure**: Mappers, analytics, utilities

---

## Feature Modules

### 1. Photo Capture Feature

**Purpose**: Capture or select game box images

**Capabilities**:
- Custom camera interface with live preview
- Flash/torch toggle
- Tap-to-focus
- Pinch-to-zoom (1.0x to device max)
- Photo library picker (PHPicker)
- Real-time credit balance display
- Permission handling with graceful fallbacks

**State Machine**:
```
Idle → Preparing → Capturing → Processing → Completed/Failed
```

**Key Outputs**:
- Captured image data (JPEG, compressed)
- Scan ID (UUID for credit tracking)

**Dependencies**: CustomCameraService, ImageProcessingService, CameraPermissionService, CreditManager

---

### 2. Rules Generation Feature

**Purpose**: Orchestrate the complete image-to-rules workflow

**5-Phase Pipeline**:

| Phase | Progress | Description |
|-------|----------|-------------|
| Processing Image | 15% | Compress and validate image |
| Analyzing Image | 40% | Send to AI backend for game identification |
| Manual Entry (conditional) | 40% | User confirms/enters game name if confidence low |
| Fetching Rules | 70% | Generate rules from game title via API |
| Saving Rules | 90% | Persist to local storage |
| Completed | 100% | Display generated rules |

**Confidence-Based Flow**:
- **High confidence (81-100%)**: Auto-proceed to rules fetching
- **Medium confidence (61-80%)**: Show suggested title, ask for confirmation
- **Low confidence (0-60%)**: Require manual game name entry

**Error Handling**:
- Retryable: Network failures, analysis timeouts
- Non-retryable: Invalid image, cancelled by user

**Dependencies**: NetworkService, ImageProcessingService, PersistenceService, CreditManager

---

### 3. Rules Display Feature

**Purpose**: Present generated rules with expandable sections

**Content Sections**:

| Section | Description | UI Component |
|---------|-------------|--------------|
| Game Overview | Summary + "How to Win" highlight | Text + highlighted box |
| Setup Instructions | Step-by-step preparation | Checklist with toggles |
| First Round Guide | Turn-by-turn walkthrough | Numbered steps |
| Advanced Rules | Deep dive strategies | Expandable text |

**Capabilities**:
- Expand/collapse individual sections
- Share rules via system share sheet
- Track setup progress with checkboxes
- Load from local storage or fresh generation

**Dependencies**: RulesRepository, PersistenceService

---

### 4. Library Feature

**Purpose**: Browse and manage saved games

**Capabilities**:
- Grid display of saved game cards
- Sort by: Recent, Alphabetical, Date Added
- Delete games with confirmation
- Navigate to rules detail view
- Empty state handling
- Pull-to-refresh

**State Machine**:
```
Loading → Empty | Loaded | Error
```

**Dependencies**: LibraryRepository, PersistenceService

---

### 5. Purchase Feature

**Purpose**: In-app purchase system for scan credits

**Products**:

| Product ID | Credits | Display | Notes |
|------------|---------|---------|-------|
| com.rulebook.scans.1 | 1 | "$X.XX" | Single scan |
| com.rulebook.scans.3 | 3 | "$X.XX" | Most popular |
| com.rulebook.scans.10 | 10 | "$X.XX" | Best value |

**Purchase Flow**:
```
Load Products → Display Paywall → User Selects → Purchase → Verify → Deliver Credits → Finish Transaction
```

**State Machine**:
```
Initial → LoadingProducts → Ready → Purchasing → Processing → Success/Error/Cancelled/Pending
```

**Special Handling**:
- **Ask to Buy**: Support pending transactions for family sharing
- **Restore Purchases**: Re-deliver previously purchased credits
- **Background Transactions**: Monitor for delayed approvals

**Dependencies**: StoreKitService, CreditManager

---

### 6. Settings Feature

**Purpose**: User preferences and app management

**Sections**:

| Section | Color | Contents |
|---------|-------|----------|
| Preferences | Blue | Haptics toggle |
| Appearance | Purple | Theme picker (Light/Dark/System) |
| Scan Credits | Green | Balance display + Purchase button |
| Support | Yellow | Report bug, Rate app |
| About | Pink | Terms, Privacy, Version |
| Danger Zone | Red | Clear all data |
| Developer | Gray | Debug options (DEBUG only) |

**Data Deletion** (Clear All):
- Reset all preferences
- Clear all saved games
- Reset credit balance (optional)
- Reset environment to production

**Dependencies**: StorageService, PersistenceService, CreditManager

---

### 7. Onboarding Feature

**Purpose**: First-time user introduction

**Flow**:
- 2 screens with page indicators
- Progress via Next button or Close (skip)
- Marks completion in storage
- Awards initial 3 free credits

**Screen Content**:
1. Camera & Rules explanation
2. Library & Offline access explanation

**Dependencies**: StorageService, CreditManager

---

## Screen Inventory

### Complete Screen List

| Screen | Type | Parent | Key States |
|--------|------|--------|------------|
| AppView | Root | - | Onboarding modal |
| MainNavigationView | Tab Container | AppView | Tab selection, modal destinations |
| LibraryView | Tab Content | MainNav | Loading, Empty, Loaded |
| SettingsView | Tab Content | MainNav | Default, Paywall sheet |
| PhotoCaptureView | Full Modal | MainNav | Permission, Camera, Loading, Error |
| RulesGenerationProgressView | Full Modal | MainNav | 7 phase states |
| RulesView | Sheet/Stack | MainNav/Library | Loading, NoRules, Loaded |
| PaywallView | Sheet | Settings/Photo | 8 purchase states |
| OnboardingView | Full Modal | AppView | Screen 1, Screen 2 |

### Screen State Details

#### LibraryView States
```
Loading     → Spinner centered
Empty       → "NO GAMES YET!" + empty state graphic
Loaded      → Grid of game cards + sort controls
Error       → Alert with retry option (deletion failures)
```

#### PhotoCaptureView States
```
Permission Not Granted → CameraPermissionView with explanation
Camera Authorized      → Live preview + overlay controls
Loading               → Black overlay + spinner + message
Error Alert           → Alert dialog with retry/settings options
```

#### RulesGenerationProgressView States
```
Idle               → "Preparing to analyze..."
Processing (15%)   → "CAPTURING..."
Analyzing (40%)    → "ANALYZING..."
Manual Entry (40%) → Text field for game name
Fetching (70%)     → "GENERATING..."
Saving (90%)       → "SAVING..."
Completed (100%)   → Success → transition to RulesView
Failed             → Error message + retry option
```

#### PaywallView States
```
Initial         → Initial setup
LoadingProducts → Skeleton cards + spinner
Ready           → Product cards + purchase button
Purchasing      → "PROCESSING PURCHASE..."
Processing      → "DELIVERING CREDITS..."
Success         → Checkmark + credits added count
Error           → Error icon + message + retry
Cancelled       → Cancelled icon + message
Pending         → Hourglass + "Awaiting approval"
```

---

## Navigation Architecture

### Tab-Based Structure

```
MainNavigationView (Tab Container)
├── Tab 1: LibraryView
│   └── NavigationStack → RulesView (detail)
│
└── Tab 2: SettingsView
    └── Sheet → PaywallView (purchase)
```

### Modal Overlay Stack

```
MainNavigationView
└── Destination (mutually exclusive modals)
    ├── cameraModal (fullScreenCover)
    │   └── PhotoCaptureView
    │       └── Sheet → PaywallView (no credits)
    │
    ├── rulesGenerationModal (fullScreenCover)
    │   └── RulesGenerationProgressView
    │
    ├── rulesModal (sheet)
    │   └── RulesView
    │
    └── paywallModal (sheet)
        └── PaywallView
```

### Navigation Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         AppView                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              MainNavigationView                          │   │
│  │  ┌───────────────────┬───────────────────────────────┐  │   │
│  │  │   LibraryView     │       SettingsView            │  │   │
│  │  │   (Tab 1)         │       (Tab 2)                 │  │   │
│  │  │                   │                               │  │   │
│  │  │  ┌─────────┐      │   ┌───────────────────┐      │  │   │
│  │  │  │ Game    │──────┼───│ PaywallView       │      │  │   │
│  │  │  │ Card    │      │   │ (sheet)           │      │  │   │
│  │  │  └────┬────┘      │   └───────────────────┘      │  │   │
│  │  │       │           │                               │  │   │
│  │  │       ▼           │                               │  │   │
│  │  │  ┌─────────┐      │                               │  │   │
│  │  │  │RulesView│      │                               │  │   │
│  │  │  │(detail) │      │                               │  │   │
│  │  │  └─────────┘      │                               │  │   │
│  │  └───────────────────┴───────────────────────────────┘  │   │
│  │                                                          │   │
│  │  ┌─────────────── MODAL LAYER ──────────────────────┐   │   │
│  │  │                                                    │   │   │
│  │  │  Camera Button                                     │   │   │
│  │  │      │                                             │   │   │
│  │  │      ▼                                             │   │   │
│  │  │  ┌──────────────┐    ┌──────────────────────┐    │   │   │
│  │  │  │PhotoCapture  │───▶│RulesGenerationProgress│    │   │   │
│  │  │  │View          │    │View                   │    │   │   │
│  │  │  └──────────────┘    └──────────┬───────────┘    │   │   │
│  │  │                                  │                 │   │   │
│  │  │                                  ▼                 │   │   │
│  │  │                           ┌───────────┐           │   │   │
│  │  │                           │ RulesView │           │   │   │
│  │  │                           │ (modal)   │           │   │   │
│  │  │                           └───────────┘           │   │   │
│  │  └────────────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │           OnboardingView (first launch only)             │   │
│  │           (fullScreenCover modal)                        │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Data Models

### Core Domain Entities

#### RulesSummary (Primary Entity)

```kotlin
data class RulesSummary(
    val id: UUID,
    val title: String,
    val playerCount: String,        // "2-8 players"
    val playTime: String,           // "60-180 minutes"
    val summary: String,
    val guides: List<Guide>,
    val winCondition: String,
    val confidence: ResponseConfidence,
    val notes: String,
    val createdAt: Date,
    val updatedAt: Date,
    val isSaved: Boolean,
    val version: String
)

data class Guide(
    val section: Section,           // SETUP, FIRST_ROUND, DEEP_DIVE
    val steps: List<String>
)

enum class Section {
    SETUP,
    FIRST_ROUND,
    DEEP_DIVE
}
```

#### ImageAnalysis

```kotlin
data class ImageAnalysis(
    val id: UUID,
    val title: String,
    val confidence: ResponseConfidence,
    val alternativeTitles: List<String>,
    val keywordsDetected: List<String>,
    val notes: String,
    val analyzedAt: Date
)
```

#### ScanCredit

```kotlin
data class ScanCredit(
    val remainingCredits: Int,
    val totalEarned: Int,
    val totalUsed: Int,
    val lastUpdated: Date,
    val source: CreditSource
) {
    companion object {
        val NEW_USER = ScanCredit(
            remainingCredits = 3,
            totalEarned = 3,
            totalUsed = 0,
            lastUpdated = Date(),
            source = CreditSource.FREE
        )
    }
}

sealed class CreditSource {
    object Free : CreditSource()
    data class Purchased(val transactionId: String) : CreditSource()
    object Promotional : CreditSource()
}
```

#### ResponseConfidence

```kotlin
enum class ResponseConfidence(val range: IntRange) {
    LOW(0..60),
    MEDIUM(61..80),
    HIGH(81..100);

    companion object {
        fun fromPercentage(value: Int): ResponseConfidence = when {
            value <= 60 -> LOW
            value <= 80 -> MEDIUM
            else -> HIGH
        }
    }
}
```

### API DTOs

#### ImageAnalysisResponseDTO

```kotlin
@Serializable
data class ImageAnalysisResponseDTO(
    val guessedTitle: String,
    val confidence: Int,            // 0-100
    val alternativeTitles: List<String>,
    val keywordsDetected: List<String>,
    val notes: String
)
```

#### RulesSummaryDTO

```kotlin
@Serializable
data class RulesSummaryDTO(
    val title: String,
    val playerCount: String,
    val playTime: String,
    val summary: String,
    val initialSetup: List<String>,
    val firstRoundGuide: List<String>,
    val winCondition: String,
    val deepDive: List<String>,
    val resources: GameResourcesDTO,
    val confidence: Int,            // 0-100
    val notes: String
)

@Serializable
data class GameResourcesDTO(
    val videoLinks: List<String>,
    val webLinks: List<String>
)

@Serializable
data class RulesRequestDTO(
    val gameTitle: String
)
```

### Purchase Models

#### ProductOffering

```kotlin
data class ProductOffering(
    val id: String,                 // "com.rulebook.scans.3"
    val creditAmount: Int,          // 3
    val displayName: String,
    val displayPrice: String,       // "$2.99"
    val description: String,
    val priceValue: BigDecimal
) {
    val isPopular: Boolean = creditAmount == 3
    val pricePerCredit: BigDecimal = priceValue / creditAmount.toBigDecimal()
}
```

### State Enums

#### GenerationPhase

```kotlin
sealed class GenerationPhase {
    object Idle : GenerationPhase()
    object ProcessingImage : GenerationPhase()
    object AnalyzingImage : GenerationPhase()
    data class AwaitingManualGameName(
        val suggestedTitle: String?,
        val confidence: ResponseConfidence
    ) : GenerationPhase()
    data class FetchingRules(val gameTitle: String) : GenerationPhase()
    object SavingRules : GenerationPhase()
    data class Completed(val rules: RulesSummary) : GenerationPhase()
    data class Failed(val error: RulesGenerationError) : GenerationPhase()

    val progressPercentage: Int
        get() = when (this) {
            is Idle -> 0
            is ProcessingImage -> 15
            is AnalyzingImage -> 40
            is AwaitingManualGameName -> 40
            is FetchingRules -> 70
            is SavingRules -> 90
            is Completed -> 100
            is Failed -> 0
        }
}
```

#### PurchaseState

```kotlin
sealed class PurchaseState {
    object Initial : PurchaseState()
    object LoadingProducts : PurchaseState()
    object Ready : PurchaseState()
    object Purchasing : PurchaseState()
    object Processing : PurchaseState()
    data class Success(val creditsAdded: Int) : PurchaseState()
    data class Error(val message: String, val isRecoverable: Boolean) : PurchaseState()
    object Cancelled : PurchaseState()
    object Pending : PurchaseState()   // Ask to Buy
}
```

### Complete Model Summary

| Model | Persisted | API | Purpose |
|-------|-----------|-----|---------|
| RulesSummary | Local DB | Response | Primary game rules entity |
| ImageAnalysis | No | Response | Game recognition result |
| ScanCredit | Preferences | No | User credit balance |
| CreditSource | Nested | No | Credit origin tracking |
| RulesSummaryDTO | No | Yes | Rules API response |
| ImageAnalysisResponseDTO | No | Yes | Analysis API response |
| ProductOffering | No | No | Play Store product |
| GenerationPhase | No | No | Workflow state machine |
| PurchaseState | No | No | Purchase state machine |
| ThemeMode | Preferences | No | App appearance |
| LibrarySortOption | Preferences | No | Library sorting |

---

## API Specification

### Base Configuration

| Environment | Base URL | Timeout |
|-------------|----------|---------|
| Production | `https://api.rulebook.app` | 30s |
| Staging | `https://project-rulebook-staging.up.railway.app` | 30s |
| Development | `https://d5b30da7cd03.ngrok-free.app` | 60s |

### Endpoint 1: Image Analysis

**URL**: `POST /api/rules-generation/game-box-analysis`

**Request**:
- Content-Type: `application/octet-stream` (binary image data)
- Supported formats: JPEG, PNG, GIF, WebP
- Max recommended size: 1024x1024 (production)

**Response**:
```json
{
  "guessedTitle": "Monopoly",
  "confidence": 85,
  "alternativeTitles": ["Monopoly Classic", "Monopoly Original"],
  "keywordsDetected": ["board", "dice", "money", "properties"],
  "notes": "Classic edition detected"
}
```

**Error Codes**:
| Status | Meaning | Retryable |
|--------|---------|-----------|
| 400 | Invalid image | No |
| 413 | Image too large | No |
| 503 | Service unavailable | Yes |
| Timeout | Analysis timed out | Yes |

### Endpoint 2: Rules Generation

**URL**: `POST /api/rules-generation/rules-summary`

**Request**:
```json
{
  "gameTitle": "Monopoly"
}
```

**Response**:
```json
{
  "title": "Monopoly",
  "playerCount": "2-8 players",
  "playTime": "60-180 minutes",
  "summary": "Classic property trading game...",
  "initialSetup": [
    "Choose a banker",
    "Each player selects a token",
    "Distribute starting money ($1500)"
  ],
  "firstRoundGuide": [
    "Roll dice to determine play order",
    "Highest roll goes first",
    "Move clockwise around the board"
  ],
  "winCondition": "Be the last player with money",
  "deepDive": [
    "Properties can be mortgaged...",
    "House building rules..."
  ],
  "resources": {
    "videoLinks": [],
    "webLinks": []
  },
  "confidence": 90,
  "notes": "Standard rules"
}
```

**Error Codes**:
| Status | Meaning | Retryable |
|--------|---------|-----------|
| 400 | Invalid game title | No |
| 404 | Game not found | No |
| 500 | Server error | Yes |
| Timeout | Request timed out | Yes |

### Authentication

**Current**: No authentication required (public endpoints)

### Network Configuration

```kotlin
// Recommended Android configuration
val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(/* logging, retry */)
    .build()
```

---

## Core Services

### Service Architecture Pattern

Each service follows Interface/Implementation pattern:

```kotlin
// Interface
interface NetworkService {
    suspend fun <T> sendRequest(endpoint: Endpoint): T
}

// Live implementation
class NetworkServiceImpl : NetworkService { ... }

// Mock implementation
class MockNetworkService : NetworkService { ... }
```

### Service Inventory

| Service | Purpose | Android Library |
|---------|---------|-----------------|
| NetworkService | HTTP client | Retrofit + OkHttp |
| PersistenceService | Local database | Room |
| StorageService | User preferences | DataStore |
| CameraService | Camera session | CameraX |
| ImageProcessingService | Image compression | Bitmap utilities |
| StoreKitService | IAP operations | Google Play Billing |
| CreditManagerService | Credit balance | Custom + DataStore |
| AnalyticsService | Event tracking | Firebase Analytics |
| HapticService | Vibration | Vibrator/VibrationEffect |
| CameraPermissionService | Permission handling | ActivityResultContracts |

### Key Service Details

#### CreditManagerService

**Methods**:
```kotlin
interface CreditManager {
    fun hasAvailableCredits(): Boolean
    fun getCreditBalance(): ScanCredit
    suspend fun consumeCredit(scanId: UUID)
    suspend fun addCredits(amount: Int, source: CreditSource)
}
```

**Business Rules**:
- New users start with 3 free credits
- 1 credit consumed per successful scan (after rules saved)
- Credits are idempotent (same scanId won't double-deduct)
- Fail-safe: Allow scan if storage fails (don't block user)

#### ImageProcessingService

**Methods**:
```kotlin
interface ImageProcessingService {
    fun optimizeForAnalysis(image: Bitmap): ProcessedImage?
    fun optimizeForUpload(image: Bitmap, targetSizeKb: Int): ProcessedImage?
    fun createThumbnail(image: Bitmap, maxDimension: Int): Bitmap?
}

data class ProcessedImage(
    val data: ByteArray,
    val originalSize: Size,
    val processedSize: Size,
    val compressionQuality: Float
)
```

**Configuration**:
- Production: 1024x1024 max, 0.9 quality
- Development: 512x512 max, 0.6 quality

---

## Design System

### Visual Style

**Design Language**: "Brutalist" - Bold, chunky, playful

**Characteristics**:
- Thick black borders (3-4dp)
- Bold shadows (offset 4-6dp)
- Bright, saturated colors
- Large, readable typography
- Playful icons and illustrations

### Color Palette

| Token | Light Mode | Dark Mode | Usage |
|-------|------------|-----------|-------|
| Primary | Orange #FF6B35 | Orange #FF6B35 | CTAs, highlights |
| Secondary | Yellow #FFD23F | Yellow #FFD23F | Accents |
| Background | White #FFFFFF | Black #1A1A1A | App background |
| Surface | Light Gray #F5F5F5 | Dark Gray #2A2A2A | Cards, sections |
| Text Primary | Black #1A1A1A | White #FFFFFF | Main text |
| Text Secondary | Gray #666666 | Gray #AAAAAA | Supporting text |
| Success | Green #4CAF50 | Green #4CAF50 | Success states |
| Error | Red #F44336 | Red #F44336 | Error states |
| Border | Black #1A1A1A | White #FFFFFF | Borders |

### Section Colors

| Section | Background Color |
|---------|-----------------|
| Preferences | Blue #2196F3 |
| Appearance | Purple #9C27B0 |
| Scan Credits | Green #4CAF50 |
| Support | Yellow #FFD23F |
| About | Pink #E91E63 |
| Danger Zone | Red #F44336 |

### Typography

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| Display | 32sp | Bold | Screen titles |
| Headline | 24sp | Bold | Section headers |
| Title | 20sp | SemiBold | Card titles |
| Body | 16sp | Regular | Main content |
| Caption | 14sp | Regular | Supporting text |
| Button | 16sp | Bold | Button labels |

### Core UI Components

| Component | Description |
|-----------|-------------|
| RulebookButton | Primary action button with icon support |
| RulebookIconButton | Icon-only circular button |
| RulebookCard | Game card with patterns and shadows |
| RulebookHeaderBar | Navigation header with back/actions |
| RulebookEmptyState | Empty state with icon + message |
| RulebookBadge | Inline metadata badge |
| RulebookCollapsibleSection | Expandable content section |
| RulebookProgressIndicator | Loading spinner variations |
| RulebookTextField | Text input with brutalist styling |
| RulebookCheckbox | Checkbox with thick borders |
| RulebookPicker | Dropdown selection |
| RulebookListRow | Settings row (navigation/toggle/value) |
| RulebookTabView | Custom tab bar |

---

## Permission Requirements

### Required Permissions

| Permission | Purpose | Fallback |
|------------|---------|----------|
| Camera | Photo capture | Gallery picker only |
| Read External Storage | Photo selection | Camera only |

### Permission Flow

```
Check Permission Status
    │
    ├── Authorized → Proceed to feature
    │
    ├── Not Determined → Request permission
    │   ├── Granted → Proceed
    │   └── Denied → Show explanation + Settings link
    │
    ├── Denied → Show explanation + Settings link
    │
    └── Restricted → Show explanation (cannot change)
```

### Android Manifest Entries

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

## Platform Mapping Guide

### iOS to Android Equivalents

| iOS Component | Android Equivalent |
|---------------|-------------------|
| SwiftUI View | @Composable |
| @State | remember { mutableStateOf() } |
| @ObservableState | StateFlow / MutableStateFlow |
| @Binding | Callback functions |
| NavigationStack | NavHost + NavController |
| TabView | BottomNavigation + NavHost |
| Sheet | ModalBottomSheet |
| FullScreenCover | Dialog(fullscreen) / new Activity |
| Alert | AlertDialog |
| PhotosPicker | PhotoPicker / Intent |
| AVCaptureSession | CameraX |
| StoreKit 2 | Google Play Billing 5.x |
| UserDefaults | DataStore Preferences |
| GRDB/CoreData | Room Database |
| TCA Reducer | ViewModel + MVI pattern |
| @Dependency | Hilt @Inject |
| async/await | suspend functions + Coroutines |

### Architecture Mapping

| TCA Concept | Android Equivalent |
|-------------|-------------------|
| State | UiState data class |
| Action | UiEvent sealed class |
| Reducer | ViewModel + when(event) |
| Effect | Flow / suspend function |
| Store | ViewModel instance |
| @Shared | SharedFlow / StateFlow |

### Example State Mapping

**iOS (TCA)**:
```swift
@ObservableState
struct LibraryFeature.State {
    var games: IdentifiedArrayOf<RulesSummary> = []
    var isLoading = false
    var sortOption: LibrarySortOption = .recent
}
```

**Android (MVI)**:
```kotlin
data class LibraryUiState(
    val games: List<RulesSummary> = emptyList(),
    val isLoading: Boolean = false,
    val sortOption: LibrarySortOption = LibrarySortOption.RECENT
)

sealed class LibraryEvent {
    object LoadGames : LibraryEvent()
    data class SortOptionChanged(val option: LibrarySortOption) : LibraryEvent()
    data class DeleteGame(val game: RulesSummary) : LibraryEvent()
}
```

---

## Appendix: Key Implementation Notes

### Credit System Rules

1. New users receive 3 free credits on first launch
2. 1 credit is consumed only after successful rules generation and save
3. Credit consumption is idempotent (uses scanId as key)
4. If storage fails, still allow scan (fail-safe)
5. Credits persist across app reinstalls (via account sync in future)

### Image Processing Pipeline

1. Validate image format (JPEG/PNG/GIF/WebP)
2. Check minimum size (100 bytes)
3. Resize to max dimensions (1024x1024)
4. Compress to JPEG with target quality (0.9)
5. Validate output size for upload

### Error Recovery Patterns

1. **Network Errors**: Show retry button, exponential backoff
2. **Camera Errors**: Fall back to gallery, link to settings
3. **Purchase Errors**: Show specific message, retry or cancel
4. **Storage Errors**: Log error, continue operation (fail-safe)

### Analytics Events to Track

- App launch / session start
- Screen views (all screens)
- Camera opened / photo captured
- Generation started / phase transitions / completed
- Rules viewed / shared
- Purchase flow (paywall shown, product selected, success/failure)
- Settings changes
- Errors (with context)

---

**Document End**

*This document serves as the complete product specification for migrating the Rulebook iOS app to Android. All features, screens, models, and APIs have been documented based on the implemented iOS codebase.*
