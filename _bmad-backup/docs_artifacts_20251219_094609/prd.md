---
stepsCompleted: [1, 2, 3, 4, 7, 9, 10]
inputDocuments:
  - path: 'docs/analysis/product-brief-project-rulebook-android-2025-12-02.md'
    type: 'product-brief'
    description: 'Complete Product Brief with vision, users, metrics, and MVP scope'
  - path: 'docs/ios/product-spec.md'
    type: 'ios-migration-spec'
    description: 'Complete iOS product specification for Android migration'
workflowType: 'prd'
lastStep: 2
project_name: 'project-rulebook-android'
user_name: 'Ivo'
date: '2025-12-03'
---

# Product Requirements Document - project-rulebook-android

**Author:** Ivo
**Date:** 2025-12-03

---

## Executive Summary

**Rulebook** is a mobile application that eliminates the friction of learning board games. By combining AI-powered image recognition with structured rule generation, users can photograph any game box and receive digestible, progressive rules within 60 seconds - transforming the dreaded "who reads the rules?" moment into an instant, shared experience.

The Android version represents a **1:1 feature port** of the proven iOS application, adapted with native Android UX patterns (Jetpack Compose, Material Design conventions, Android-specific interactions) to feel natural on the platform.

### Vision Statement

**From box to playing in 60 seconds** - because game night is about connection, not homework.

### What Makes This Special

| Differentiator | Why It Matters |
|----------------|----------------|
| **Photo-to-rules pipeline** | Only solution covering the complete journey from unknown game to playing |
| **Multi-model AI resilience** | Fallback chain ensures recognition even for obscure/international games |
| **Progressive disclosure format** | Setup → First Round → Deep Dive mirrors natural learning |
| **Offline-first architecture** | Works in basements, cabins, and cafes where game nights happen |
| **First-to-market on Android** | No direct competitors with photo-based identification |

### Core Value Proposition

While competitors (Rulesbot.ai, Boardgamebot.AI, Ludomentor) solve "I forgot a rule mid-game" by requiring users to already know their game, **Rulebook solves "I don't even know what game this is or where to start"** - a fundamentally different and underserved problem.

---

## Project Classification

| Attribute | Value |
|-----------|-------|
| **Technical Type** | Mobile Application (Native Android) |
| **Domain** | Consumer Entertainment / Utility |
| **Complexity** | Low (standard patterns, no regulatory requirements) |
| **Platform** | Android (Kotlin, Jetpack Compose) |
| **Architecture** | Feature-scoped Clean Architecture with MVI pattern |

### Technical Context

This project is an **iOS-to-Android migration** with comprehensive source documentation:
- ~29,000 lines of Swift code translated to Kotlin
- 6 feature modules, 10 core services
- 16+ screens with multiple states
- Proven architecture patterns adapted for Android conventions

**Key Technical Requirement:** Establish a robust design system from project inception (lesson learned from iOS development).

---

## Success Criteria

### User Success

| Criteria | Measure | Target |
|----------|---------|--------|
| **Immediate Value** | Time from install to first scan | <2 minutes |
| **Core Feature Works** | Scan completion rate (AI returns usable rules) | >85% |
| **Problem Solved** | User completes setup and starts playing | Qualitative: "aha!" moment |
| **Return Engagement** | Users scan a 2nd game within 14 days | >30% |
| **Library Building** | Average saved games per active user | 3+ games |

**User Success Statement:** A user succeeds when they photograph a game box and are playing within 60 seconds, without reading the physical rulebook.

### Business Success

| Timeframe | Objective | Key Metric |
|-----------|-----------|------------|
| **Launch (Month 1)** | Prove core value | >60% first scan completion |
| **Growth (Months 2-3)** | Build retention | Day 7 retention >20% |
| **Monetization (Months 3-6)** | Validate revenue | >5% conversion (free → paid) |

### Technical Success

| Criteria | Target |
|----------|--------|
| **Performance** | Scan-to-rules complete in <60 seconds |
| **Stability** | >99% crash-free sessions |
| **Compatibility** | Android 8.0+ (API 26+) coverage |
| **Store Quality** | Published on Google Play, no policy violations |

### Measurable Outcomes

**North Star Metric:** Games successfully scanned and rules viewed

| KPI | Target | Measurement |
|-----|--------|-------------|
| Onboarding completion | >80% | Analytics event |
| First scan completion | >60% of installs | Analytics funnel |
| Day 1 retention | >40% | Firebase |
| Day 7 retention | >20% | Firebase |
| Day 30 retention | >10% | Firebase |
| Free → Paid conversion | >5% | Play Console |
| App store rating | >4.0 stars | Play Console |
| DAU/MAU ratio | >15% | Analytics |

---

## Product Scope

### MVP - Minimum Viable Product

**Core Features (1:1 iOS Parity):**

| Module | Features |
|--------|----------|
| **Onboarding** | 2-screen intro, award 3 free credits |
| **Photo Capture** | Camera (flash, zoom, focus), gallery picker, permissions |
| **Rules Generation** | 5-phase pipeline, confidence-based flow, multi-model AI fallback |
| **Rules Display** | Progressive sections, expandable content, share, setup checklist |
| **Library** | Grid view, sort options, delete, offline access |
| **Purchase** | 3 credit packs (1, 3, 10), paywall, restore purchases |
| **Settings** | Theme, haptics, support links, clear data |

**Technical Requirements:**
- Design system established from day 1
- Brutalist visual style (thick borders, bold shadows)
- Offline-first architecture
- Firebase Analytics integration

**Android Enhancements:**
- App Shortcuts (long-press "Scan Game")
- Predictive Back Gesture
- Edge-to-Edge Display

**MVP Success Gate:** All iOS features functional, <60s scan-to-rules, >99% crash-free, published on Play Store.

### Growth Features (Post-MVP)

| Version | Features |
|---------|----------|
| **v1.1** | Material You theming, Share Target, performance optimizations |
| **v1.2** | Widgets, tablet optimization, Wear OS companion |

### Vision (Future)

| Phase | Features |
|-------|----------|
| **v2.0** | User accounts, cloud sync, cross-platform library |
| **Beyond** | Social features, community contributions, board game cafe partnerships |

**Out of Scope for MVP:**
- User accounts / cloud sync
- Social features
- Subscription model
- Material You / Dynamic Color
- Widgets
- Wear OS / Tablet optimization

---

## User Journeys

### Journey 1: Marcus Chen - The Collector Who Finally Plays His Games

Marcus is a software engineer with a shelf of 47 board games, 12 still in shrink wrap. He buys games faster than he can learn them - every Black Friday, every birthday, every "this looks amazing" impulse. His friends joke that he collects games, not plays them. Tonight, six friends are coming over for game night, and Marcus is determined to finally crack open Wingspan, the bird-themed engine builder that's been mocking him from the shelf for eight months.

At 6:45 PM, fifteen minutes before guests arrive, Marcus unwraps Wingspan and stares at the 12-page rulebook. His stomach sinks. He grabs his phone, opens Rulebook, and photographs the game box. Within seconds, the app identifies "Wingspan" with 94% confidence and starts generating rules.

By the time his first guest rings the doorbell at 7:00 PM, Marcus is skimming the "First Round Guide" while setting up the bird feeder dice tower. When everyone's seated, he doesn't apologize for "needing a few minutes to explain." Instead, he walks them through setup using the checklist, reads the overview aloud, and they're playing their first round by 7:12 PM.

The breakthrough moment comes when Sarah asks "wait, what do I do with these eggs?" Marcus pulls up the saved rules, expands "Advanced Rules," and finds the answer in three seconds. No rulebook diving. No game-stopping confusion. At 10 PM, as guests leave raving about the game, Marcus realizes something: he actually enjoyed hosting tonight. He didn't dread it. He opens the app and scans Terraforming Mars for next month's game night.

**Journey reveals requirements for:**
- Fast image recognition with high confidence display
- Progressive rule structure (Overview → Setup → First Round → Advanced)
- Checklist-style setup instructions
- Quick in-game reference via saved library
- Scan-ahead for future games

---

### Journey 2: Priya Sharma - The Guest Who Finally Gets It

Priya loves her friends. She does not love being handed a 20-page rulebook while everyone stares at her expectantly. As the designated "reader" at last month's game night (she made the mistake of saying she "reads fast"), she spent 25 minutes stumbling through Gloomhaven rules while her friends checked their phones and got another beer. She didn't understand half of what she read. When the game started, she had no idea what she was doing and spent the whole night feeling one step behind.

Tonight is different. When she arrives at Marcus's apartment and sees an unfamiliar game on the table, she reflexively tenses. But Marcus just says "give me one sec" and pulls out his phone. Priya watches the app work its magic - the box gets scanned, rules appear in neat sections, and suddenly Marcus is walking everyone through setup like he's known the game for years.

The rules make sense. They're broken into digestible chunks. Priya actually understands what she's supposed to do on her turn. When she forgets something mid-game, she leans over and whispers "can I see that setup section again?" Marcus hands her the phone. No judgment. No "weren't you listening?" No thumbing through a massive rulebook.

At the end of the night, Priya downloads Rulebook herself. Not because she owns games, but because next time someone asks her to "read the rules real quick," she has an answer.

**Journey reveals requirements for:**
- Shareable rules (pass phone, no account needed for viewing)
- Clear section navigation for quick reference
- Non-intimidating progressive disclosure
- Works for non-owners who just want to participate
- "Hand-off" friendly UI (someone else using owner's phone briefly)

---

### Journey 3: Jordan Williams - The Club Explorer Who Maximizes Game Night

Jordan runs the Tuesday night board game meetup at the local library. Twenty regulars, three hours, one problem: they spend 45 minutes every week debating what to play and then learning the chosen game. By the time everyone understands the rules, they've got maybe 90 minutes of actual gaming.

Tonight, Jordan tries something different. They arrive early and scan three games from the library's collection: Azul, Ticket to Ride, and Codenames. When members start filtering in, Jordan holds up their phone showing the three game overviews. "Pick one. I can have us playing in five minutes."

The group chooses Azul. Jordan reads the two-paragraph overview aloud, then walks through setup with the checklist while others help arrange tiles. The "First Round Guide" gets everyone through their first turns without confusion. A newcomer named Derek, attending his first meetup, keeps up with everyone else instead of feeling lost.

They finish Azul by 7:45 PM. Jordan scans Codenames. By 8:00 PM they're playing again. By the end of the night, the group has played three complete games - a meetup record.

The following week, half the regulars have Rulebook on their phones. Derek brings a friend. The meetup grows to 28 members. Jordan starts a tradition: "Scanner of the Week" - whoever arrives first picks and scans the opening game.

**Journey reveals requirements for:**
- Pre-scanning multiple games for comparison
- Quick overview for group decision-making
- Works offline (library basement has spotty wifi)
- Newcomer-friendly progressive rules
- Library persistence across sessions
- Social/word-of-mouth viral loop

---

### Journey 4: Marcus Returns - When the AI Gets It Wrong

It's been two months since Marcus discovered Rulebook. He's scanned 23 games and converted his entire friend group. Tonight, he's pulling out his prized possession: a Kickstarter-exclusive expansion for a niche Japanese game called "Karuta Kings: Tournament Edition." Only 2,000 copies exist worldwide.

He scans the box. The app thinks for a moment, then returns "Karuta (Traditional Japanese Card Game)" with 62% confidence. Wrong game entirely. Marcus taps "Not quite right" and the app asks him to enter the correct game name. He types "Karuta Kings Tournament Edition" and the app generates rules for the actual game - drawing on its broader AI knowledge.

The rules are good, but miss one expansion-specific mechanic. Marcus uses the game anyway, consulting the physical rulebook just for that one section. After the session, he wishes there was a way to add notes or corrections, but understands - the app is for quick starts, not comprehensive rule databases.

**Journey reveals requirements for:**
- Confidence display and user confirmation
- Manual game name entry for low-confidence results
- Multi-model AI fallback for obscure games
- Graceful handling of "close but not quite" scenarios
- Clear user control over AI suggestions

---

### Journey Requirements Summary

| Journey | Key Capabilities Required |
|---------|--------------------------|
| **Marcus (Host - Success)** | Fast scan, progressive rules, setup checklist, in-game reference, library persistence |
| **Priya (Guest - Participation)** | Shareable viewing, quick navigation, non-intimidating UI, hand-off friendly |
| **Jordan (Club - Efficiency)** | Multi-game scanning, offline access, overview comparison, newcomer support |
| **Marcus (Error Recovery)** | Confidence display, manual entry fallback, multi-model AI, user correction flow |

**Core Capabilities Revealed:**
1. **Photo Capture & AI Recognition** - Fast, confidence-scored, fallback-enabled
2. **Progressive Rule Display** - Overview → Setup → First Round → Advanced
3. **Offline Library** - Saved games accessible without network
4. **Quick Reference** - Section navigation, checklist toggles, share capability
5. **Error Handling** - Manual entry, confirmation prompts, graceful degradation

---

## Mobile App Specific Requirements

### Platform Requirements

| Requirement | Specification |
|-------------|---------------|
| **Platform** | Android |
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Min SDK** | API 26 (Android 8.0) |
| **Target SDK** | Latest stable (API 34+) |
| **Architecture** | Feature-scoped Clean Architecture with MVI |

### Device Permissions

| Permission | Purpose | Required |
|------------|---------|----------|
| `CAMERA` | Photo capture for game box scanning | Yes |
| `READ_MEDIA_IMAGES` | Gallery picker for existing photos | Yes |
| `VIBRATE` | Haptic feedback for interactions | Optional |
| `INTERNET` | API calls for AI analysis and rules generation | Yes |

**Permission Handling:**
- Request camera permission on first scan attempt (not at install)
- Graceful fallback to gallery-only if camera denied
- Link to system settings for permission re-grant

### Offline Mode

| Capability | Behavior |
|------------|----------|
| **Library Access** | Full offline access to saved games and rules |
| **Rules Viewing** | All saved rules viewable without network |
| **New Scans** | Requires network (AI processing is server-side) |
| **Credit Display** | Cached locally, synced when online |

**Offline Architecture:**
- Room database for persistent rule storage
- DataStore for preferences and credit balance
- Network-first with local cache fallback for rules
- Clear offline/online state indicators in UI

### Push Notifications

| Status | Details |
|--------|---------|
| **MVP** | No push notifications |
| **Future (v1.1+)** | Background rules generation with completion notification |

**Future Implementation Notes:**
- Firebase Cloud Messaging (FCM) integration
- Background WorkManager task for rules generation
- Local notification on completion: "Your rules for [Game] are ready!"
- User preference to enable/disable notifications

### Store Compliance

| Requirement | Approach |
|-------------|----------|
| **Google Play Policies** | Standard consumer app, no special review requirements |
| **In-App Purchases** | Google Play Billing Library 6.x for credit packs |
| **Content Rating** | Everyone (no objectionable content) |
| **Data Safety** | Camera usage, analytics collection disclosed |
| **Target Audience** | General audience (not children-directed) |

### Android-Specific Enhancements

| Feature | Implementation |
|---------|----------------|
| **App Shortcuts** | Long-press launcher icon → "Scan Game" quick action |
| **Predictive Back** | Modern gesture navigation with preview animations |
| **Edge-to-Edge** | Content extends under status/nav bars |
| **Splash Screen** | Android 12+ SplashScreen API |

---

## Functional Requirements

### Onboarding & First-Time Experience

- **FR1:** First-time users can view a 2-screen introduction explaining core app functionality
- **FR2:** First-time users receive 3 free scan credits upon completing onboarding
- **FR3:** Users can skip onboarding at any point
- **FR4:** System remembers onboarding completion and does not show it again

### Photo Capture & Image Processing

- **FR5:** Users can capture photos using the device camera
- **FR6:** Users can control camera flash/torch during capture
- **FR7:** Users can zoom the camera view (pinch gesture)
- **FR8:** Users can tap to focus the camera on a specific area
- **FR9:** Users can select existing photos from device gallery
- **FR10:** System compresses and optimizes images before upload
- **FR11:** Users can see their current credit balance while in camera view

### Game Recognition & AI Analysis

- **FR12:** System can identify board games from box photos using AI
- **FR13:** System displays confidence level for game identification
- **FR14:** Users can confirm or reject AI-suggested game identification
- **FR15:** Users can manually enter game name when AI confidence is low
- **FR16:** System uses fallback AI model for obscure/unrecognized games
- **FR17:** Users can retry failed recognition attempts

### Rules Generation & Display

- **FR18:** System generates structured rules for identified games
- **FR19:** Users can view game overview with summary and win condition
- **FR20:** Users can view step-by-step setup instructions
- **FR21:** Users can mark setup steps as complete (checklist)
- **FR22:** Users can view first-round gameplay guide
- **FR23:** Users can view advanced rules and deep-dive content
- **FR24:** Users can expand/collapse individual rule sections
- **FR25:** Users can share generated rules via system share sheet
- **FR26:** System displays progress during rules generation phases

### Game Library Management

- **FR27:** Users can view all saved games in a grid layout
- **FR28:** Users can sort library by recent, alphabetical, or date added
- **FR29:** Users can select a saved game to view its rules
- **FR30:** Users can delete saved games from library
- **FR31:** System prompts for confirmation before deleting games
- **FR32:** Users can access saved games and rules while offline
- **FR33:** System displays empty state when library has no games

### Credit System & Monetization

- **FR34:** System tracks remaining scan credits
- **FR35:** System consumes one credit per successful scan
- **FR36:** Users can view available credit packs for purchase
- **FR37:** Users can purchase credit packs (1, 3, or 10 credits)
- **FR38:** System displays paywall when user has no credits
- **FR39:** Users can restore previous purchases
- **FR40:** System handles pending purchases (Ask-to-Buy)
- **FR41:** Users can see purchase confirmation and credit delivery status

### User Preferences & Settings

- **FR42:** Users can switch between light, dark, and system themes
- **FR43:** Users can enable/disable haptic feedback
- **FR44:** Users can access support/bug reporting links
- **FR45:** Users can view app version and legal information
- **FR46:** Users can clear all app data (reset to fresh state)
- **FR47:** System prompts for confirmation before clearing data

### Android Platform Integration

- **FR48:** Users can access "Scan Game" action via app shortcut (long-press icon)
- **FR49:** System supports predictive back gesture navigation
- **FR50:** App displays content edge-to-edge under system bars
- **FR51:** System requests camera permission at point of use (not install)
- **FR52:** Users can navigate to system settings to grant permissions

---

## Non-Functional Requirements

### Performance

| NFR | Requirement | Measurement |
|-----|-------------|-------------|
| **NFR1** | Scan-to-rules complete in <60 seconds | End-to-end timing from photo capture to rules displayed |
| **NFR2** | App cold start in <3 seconds | Time from launch to interactive state |
| **NFR3** | Camera preview starts in <1 second | Time from camera open to live preview |
| **NFR4** | Library loads in <500ms | Time to display saved games grid |
| **NFR5** | UI interactions respond in <100ms | Touch feedback, animations |
| **NFR6** | Image compression completes in <2 seconds | Photo processing before upload |

### Reliability

| NFR | Requirement | Measurement |
|-----|-------------|-------------|
| **NFR7** | Crash-free sessions >99% | Firebase Crashlytics |
| **NFR8** | Offline library access 100% available | Saved rules viewable without network |
| **NFR9** | Credit balance persists across app restarts | DataStore reliability |
| **NFR10** | Graceful degradation on network failure | Clear error states, retry options |
| **NFR11** | No data loss on app termination | Room database integrity |

### Security

| NFR | Requirement | Measurement |
|-----|-------------|-------------|
| **NFR12** | All API communication over HTTPS | Network security config enforced |
| **NFR13** | No sensitive data in logs | ProGuard/R8 log stripping |
| **NFR14** | Payment processing via Google Play Billing only | No custom payment handling |
| **NFR15** | No user credentials stored (no accounts for MVP) | N/A - no authentication |

### Integration

| NFR | Requirement | Measurement |
|-----|-------------|-------------|
| **NFR16** | Backend API timeout handling (30s default) | Graceful timeout with retry |
| **NFR17** | Google Play Billing Library 6.x integration | Standard implementation |
| **NFR18** | Firebase Analytics event tracking | All key events captured |
| **NFR19** | CameraX for camera functionality | Standard Android camera API |

### Compatibility

| NFR | Requirement | Measurement |
|-----|-------------|-------------|
| **NFR20** | Minimum SDK: API 34 (Android 14) | Gradle minSdk configuration |
| **NFR21** | Target SDK: API 35 (Android 15) | Gradle targetSdk configuration |
| **NFR22** | Compile SDK: API 35 | Gradle compileSdk configuration |
| **NFR23** | Support both ARM and x86 architectures | APK/AAB includes all ABIs |
| **NFR24** | Portrait orientation only | No landscape support required |

---

*Product Requirements Document completed on 2025-12-03*