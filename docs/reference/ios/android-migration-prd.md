# Product Requirements Document - Android Migration

## Executive Summary

**Product Name**: Rulebook

**Platform**: Android (migrating from iOS)

**Description**: An AI-powered mobile application that helps board game players instantly learn and understand game rules by photographing game boxes.

**Core Value Proposition**: Eliminate the frustration of reading lengthy rulebooks by providing instant, AI-generated rule summaries organized for progressive learning.

---

## Problem Statement

### User Pain Points

1. **Rulebook Complexity**: Board game manuals are often lengthy, poorly organized, and overwhelming for new players
2. **Time to Play**: Groups spend 20-60 minutes reading rules before playing a new game
3. **Rule Lookup**: Finding specific rules mid-game requires flipping through physical manuals
4. **Rule Retention**: Players forget rules between sessions and must re-read portions
5. **Collection Management**: No easy way to organize and reference rules for owned games

### Target Users

**Primary**: Board game enthusiasts (18-45) who:
- Own 10+ board games
- Play games socially (game nights, family gatherings)
- Value efficiency and want to start playing quickly
- Are comfortable with mobile apps and AI tools

**Secondary**: Casual board game players who:
- Occasionally play board games
- Are intimidated by complex rulebooks
- Need quick reference during gameplay

---

## Product Features

### Feature 1: Photo-Based Game Recognition

**Priority**: P0 (Core)

**Description**: Users photograph a game box and the AI identifies the game title with confidence scoring.

**User Story**: As a user, I want to take a photo of a game box so that the app can identify which game it is.

**Acceptance Criteria**:
- Camera opens in full-screen mode
- Flash/torch toggle available
- Tap-to-focus functionality
- Photo can be selected from gallery
- Photo can be rotated before submission
- Clear visual feedback during capture
- Permission handling for camera access

**Technical Requirements**:
- CameraX for camera implementation
- Image compression to max 1024x1024px
- JPEG encoding at 0.8 quality
- Backend API: POST `/api/rules-generation/game-box-analysis`

**Error States**:
- Camera permission denied -> Settings redirect
- Image analysis failed -> Retry option
- No game detected -> "Unable to identify game" message

---

### Feature 2: AI Rule Generation

**Priority**: P0 (Core)

**Description**: Generate structured rule summaries using AI based on identified game title.

**User Story**: As a user, I want to receive easy-to-understand rules so that I can start playing quickly.

**Acceptance Criteria**:
- Progress shown in phases (0-100%)
- Cancellable at any time
- Clear phase messaging:
  - Processing Image (0-15%)
  - Analyzing Image (15-40%)
  - Fetching Rules (40-70%)
  - Saving Rules (70-90%)
  - Complete (90-100%)
- Confidence level displayed (High/Medium/Low)
- Rules organized into progressive sections

**Rule Structure**:
```
Game Title
├── Metadata (players, time, difficulty)
├── Setup Guide (how to prepare)
├── First Round Walkthrough (step-by-step first turn)
├── Advanced Rules (deep dive)
├── Win Condition
└── Additional Notes
```

**Technical Requirements**:
- Backend API for rule generation
- Local caching of generated rules
- Offline access after initial generation

---

### Feature 3: Personal Game Library

**Priority**: P0 (Core)

**Description**: Store and organize scanned games for offline access.

**User Story**: As a user, I want to save games to my library so that I can reference rules anytime.

**Acceptance Criteria**:
- Games displayed in 2-column grid
- Sort options: Recent, Alphabetical, Oldest, Play Time
- Tap game to view full rules
- Long-press to delete
- Pull-to-refresh
- Empty state with onboarding tips
- Offline access to all saved games

**Technical Requirements**:
- SQLite database (Room)
- Persistent storage with migration support
- Grid layout with responsive sizing

**Data Model**:
```kotlin
data class RulesSummary(
    val id: UUID,
    val title: String,
    val playerCount: String,
    val playTime: String,
    val confidence: Confidence,
    val guides: List<Guide>,
    val winCondition: String,
    val notes: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Guide(
    val section: Section, // SETUP, FIRST_ROUND, DEEP_DIVE
    val steps: List<String>
)

enum class Confidence { HIGH, MEDIUM, LOW }
```

---

### Feature 4: Credit System

**Priority**: P0 (Core)

**Description**: Scan-based credit system with free tier and in-app purchases.

**User Story**: As a user, I want to understand my scan limits so that I can plan my usage.

**Acceptance Criteria**:
- New users receive 3 free credits
- 1 credit consumed per successful scan
- Credit balance visible in settings
- Paywall shown when credits = 0
- Credits not deducted on scan failure
- Purchase packages: 1, 3, 10 credits

**Technical Requirements**:
- Google Play Billing Library
- Server-side purchase verification (optional)
- Local credit balance storage
- Fail-safe: allow scan if credit check fails

**Product IDs**:
- `com.rulebook.scans.1` - 1 Credit
- `com.rulebook.scans.3` - 3 Credits (Popular)
- `com.rulebook.scans.10` - 10 Credits (Best Value)

---

### Feature 5: In-App Purchases

**Priority**: P0 (Core)

**Description**: Purchase additional scan credits through Google Play.

**User Story**: As a user, I want to buy more scans so that I can continue using the app.

**Acceptance Criteria**:
- Paywall displays current balance
- Three product options visible
- Clear pricing displayed
- "Restore Purchases" option
- Loading state during purchase
- Success/error feedback
- Immediate credit application

**Technical Requirements**:
- Google Play Billing Library v6+
- BillingClient connection handling
- Product query and display
- Purchase flow implementation
- Transaction verification

---

### Feature 6: User Preferences

**Priority**: P1 (Important)

**Description**: Customize app behavior and appearance.

**User Story**: As a user, I want to personalize the app so that it matches my preferences.

**Acceptance Criteria**:
- Theme selection: System, Light, Dark
- Haptic feedback toggle
- Links to: Help, Bug Report, Rate App, Terms, Privacy
- App version display
- Clear all data option (with confirmation)

**Technical Requirements**:
- DataStore for preferences
- Theme applied app-wide
- External link handling
- Play Store deep link for rating

---

### Feature 7: First-Time Onboarding

**Priority**: P1 (Important)

**Description**: Introduce app features to new users.

**User Story**: As a new user, I want to understand how the app works so that I can use it effectively.

**Acceptance Criteria**:
- 2-screen onboarding flow
- Feature highlights with icons
- Page indicators
- Skip/Next navigation
- Shown only once (first launch)
- "Get Started" completes onboarding

**Screen 1 Content**:
```
Title: "SCAN & LEARN"
Subtitle: "Point your camera at any game box"
Features:
- Instant Recognition
- Clear Rules
```

**Screen 2 Content**:
```
Title: "BUILD YOUR LIBRARY"
Subtitle: "Save games for quick reference"
Features:
- Offline Access
- Organized Collection
```

---

## Non-Functional Requirements

### Performance

| Metric | Target |
|--------|--------|
| App launch (cold) | < 2 seconds |
| Camera open | < 500ms |
| Image capture | < 200ms |
| Rule generation | < 30 seconds |
| Library load | < 1 second |

### Reliability

- Offline access to saved games
- Graceful degradation on network failure
- Credit system fail-safe (allow scan if check fails)
- Crash-free rate > 99.5%

### Security

- No local storage of payment info
- Secure API communication (HTTPS)
- Purchase verification through Play Store

### Accessibility

- Minimum touch target: 44dp
- Support for TalkBack
- Dynamic text sizing
- Sufficient color contrast (WCAG AA)

---

## API Endpoints

### Image Analysis
```
POST /api/rules-generation/game-box-analysis
Content-Type: multipart/form-data

Request:
- image: Binary image data

Response:
{
  "id": "uuid",
  "title": "Game Name",
  "confidence": "HIGH|MEDIUM|LOW",
  "alternativeTitles": ["Alt1", "Alt2"],
  "keywordsDetected": ["keyword1"],
  "notes": "Additional info"
}
```

### Rules Generation
```
POST /api/rules-generation/summary
Content-Type: application/json

Request:
{
  "gameTitle": "Game Name",
  "analysisId": "uuid"
}

Response:
{
  "id": "uuid",
  "title": "Game Name",
  "playerCount": "2-4 players",
  "playTime": "30-60 minutes",
  "confidence": "HIGH",
  "guides": [
    {
      "section": "SETUP",
      "steps": ["Step 1", "Step 2"]
    }
  ],
  "winCondition": "First to 10 points wins",
  "notes": "Optional notes"
}
```

### Environment Configuration

| Environment | Base URL | Timeout |
|-------------|----------|---------|
| Development | `d5b30da7cd03.ngrok-free.app` | 60s |
| Staging | `project-rulebook-staging.up.railway.app` | 30s |
| Production | `api.rulebook.app` | 15s |

---

## Analytics Events

Track the following events for product insights:

| Event | Properties | Purpose |
|-------|------------|---------|
| `app_launch` | `is_first_launch`, `theme` | Usage tracking |
| `onboarding_started` | - | Funnel analysis |
| `onboarding_completed` | - | Conversion |
| `scan_initiated` | `credits_remaining` | Feature usage |
| `scan_completed` | `confidence`, `game_title` | Success rate |
| `scan_failed` | `error_type` | Error tracking |
| `game_saved` | `game_id` | Library growth |
| `game_deleted` | `game_id` | Churn |
| `paywall_viewed` | `trigger_source` | Monetization |
| `purchase_initiated` | `product_id` | Revenue |
| `purchase_completed` | `product_id`, `credits` | Revenue |
| `purchase_failed` | `error_type` | Drop-off |
| `settings_changed` | `setting_name`, `value` | Preferences |

---

## Release Phases

### Phase 1: MVP (Core Features)
- Photo capture and game recognition
- AI rule generation with progress
- Basic library with grid view
- Credit system and paywall
- Essential settings

### Phase 2: Polish
- Onboarding flow
- Sort and filter options
- Improved error handling
- Analytics integration
- Haptic feedback

### Phase 3: Enhancement
- Share functionality
- Search within library
- Widget for quick access
- Deep linking support

---

## Success Metrics

### Engagement
- DAU/MAU ratio > 20%
- Average session length > 3 minutes
- Games scanned per user > 5

### Retention
- Day 1 retention > 40%
- Day 7 retention > 20%
- Day 30 retention > 10%

### Monetization
- Free-to-paid conversion > 5%
- Average revenue per user (ARPU) tracking
- Credit purchase frequency

### Quality
- App store rating > 4.0
- Crash-free rate > 99.5%
- Scan success rate > 85%

---

## Technical Architecture Recommendations

### Android Tech Stack

```
UI Layer:
- Jetpack Compose
- Material 3 with custom theme
- Compose Navigation

Data Layer:
- Room for local database
- DataStore for preferences
- Retrofit for networking
- Kotlin Serialization

Architecture:
- MVVM with clean architecture
- Kotlin Coroutines & Flow
- Hilt for dependency injection

Camera:
- CameraX

Payments:
- Google Play Billing Library v6+

Analytics:
- TelemetryDeck or equivalent
```

### Module Structure

```
app/
├── core/
│   ├── ui/          # Design system, components
│   ├── data/        # Database, preferences
│   └── network/     # API client
├── feature/
│   ├── library/     # Library screen
│   ├── camera/      # Photo capture
│   ├── rules/       # Rule display & generation
│   ├── purchase/    # Paywall & IAP
│   ├── settings/    # Preferences
│   └── onboarding/  # First-time flow
└── app/             # Application class, navigation
```

---

## Appendix: iOS Feature Parity Checklist

| Feature | iOS | Android Target |
|---------|-----|----------------|
| Camera capture | Yes | Yes |
| Photo library picker | Yes | Yes |
| Flash toggle | Yes | Yes |
| Image analysis API | Yes | Yes |
| Rule generation | Yes | Yes |
| Progress phases | Yes | Yes |
| Library grid | Yes | Yes |
| Sort options | Yes | Yes |
| Delete game | Yes | Yes |
| Credit system | Yes | Yes |
| IAP (3 products) | Yes | Yes |
| Restore purchases | Yes | Yes |
| Theme selection | Yes | Yes |
| Haptics toggle | Yes | Yes |
| Onboarding (2 screens) | Yes | Yes |
| Offline access | Yes | Yes |
| Collapsible sections | Yes | Yes |
| Confidence display | Yes | Yes |
