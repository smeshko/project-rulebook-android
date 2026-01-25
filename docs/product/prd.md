---
title: Product Requirements Document
description: Complete product requirements for project-rulebook-android
author: Ivo
date: 2025-12-03
---

# Product Requirements Document - project-rulebook-android

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

**Journey reveals requirements for:**
- Fast image recognition with high confidence display
- Progressive rule structure (Overview → Setup → First Round → Advanced)
- Checklist-style setup instructions
- Quick in-game reference via saved library
- Scan-ahead for future games

### Journey 2: Priya Sharma - The Guest Who Finally Gets It

Priya loves her friends. She does not love being handed a 20-page rulebook while everyone stares at her expectantly. As the designated "reader" at last month's game night (she made the mistake of saying she "reads fast"), she spent 25 minutes stumbling through Gloomhaven rules while her friends checked their phones and got another beer.

Tonight is different. When she arrives at Marcus's apartment and sees an unfamiliar game on the table, she reflexively tenses. But Marcus just says "give me one sec" and pulls out his phone. The rules make sense. They're broken into digestible chunks. Priya actually understands what she's supposed to do on her turn.

**Journey reveals requirements for:**
- Shareable rules (pass phone, no account needed for viewing)
- Clear section navigation for quick reference
- Non-intimidating progressive disclosure
- "Hand-off" friendly UI

### Journey 3: Jordan Williams - The Club Explorer Who Maximizes Game Night

Jordan runs the Tuesday night board game meetup at the local library. Twenty regulars, three hours, one problem: they spend 45 minutes every week debating what to play and then learning the chosen game.

Tonight, Jordan tries something different. They arrive early and scan three games from the library's collection. When members start filtering in, Jordan holds up their phone showing the three game overviews. "Pick one. I can have us playing in five minutes."

**Journey reveals requirements for:**
- Pre-scanning multiple games for comparison
- Quick overview for group decision-making
- Works offline (library basement has spotty wifi)
- Newcomer-friendly progressive rules

### Journey 4: Marcus Returns - When the AI Gets It Wrong

Marcus scans a Kickstarter-exclusive expansion for a niche Japanese game. The app thinks for a moment, then returns "Karuta (Traditional Japanese Card Game)" with 62% confidence. Wrong game entirely. Marcus taps "Not quite right" and the app asks him to enter the correct game name.

**Journey reveals requirements for:**
- Confidence display and user confirmation
- Manual game name entry for low-confidence results
- Multi-model AI fallback for obscure games
- Graceful handling of "close but not quite" scenarios

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
