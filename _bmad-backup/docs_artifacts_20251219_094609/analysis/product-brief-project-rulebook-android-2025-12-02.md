---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments:
  - path: 'docs/ios/product-spec.md'
    type: 'ios-migration-spec'
    description: 'Complete iOS product specification for Android migration'
workflowType: 'product-brief'
lastStep: 5
project_name: 'project-rulebook-android'
user_name: 'Ivo'
date: '2025-12-02'
---

# Product Brief: project-rulebook-android

**Date:** 2025-12-02
**Author:** Ivo

---

## Executive Summary

**Rulebook** is a mobile application that transforms how people learn board games. Users simply photograph a game box, and AI-powered image recognition identifies the game and generates comprehensive, progressively-structured rules - enabling immediate gameplay without reading dense rulebooks or watching lengthy tutorials.

The Android version represents a **1:1 feature port** of the proven iOS application, adapted with native Android UX patterns to feel natural on the platform. A key architectural priority is establishing a **robust design system from project inception**, addressing lessons learned from iOS development.

---

## Core Vision

### The Emotional Promise

Rulebook eliminates the social friction of learning new games. We transform the dreaded "who reads the rules?" moment into an instant, shared experience.

> *Four friends gather around a table. Someone pulls out a game still in shrink wrap. The energy shifts. "Who's going to read the rules?" Silence. Eye contact avoidance.*
>
> **With Rulebook, that moment transforms.**
>
> *One person snaps a photo. 60 seconds later, everyone's setting up the board while the phone displays the first steps. No designated "rules person." No 20-minute YouTube detour. Just four friends, already playing, already laughing.*

**From box to playing in 60 seconds** - because game night is about connection, not homework.

---

### Problem Statement

Board game nights are derailed by the friction of learning new games. Rulebooks are dense, intimidating, and often poorly organized. The alternative - YouTube tutorials - requires 10-20 minutes of watching, pausing, and rewinding before anyone can play.

### Problem Impact

- **Social friction:** One person reads rules while others wait, killing the energy
- **Barrier to trying new games:** People stick to games they know rather than face the learning curve
- **Wasted game night time:** 20-30 minutes of "figuring it out" before actual play begins

### Why Existing Solutions Fall Short

| Alternative | Shortcoming |
|-------------|-------------|
| **Physical rulebooks** | Dense, poorly organized, requires dedicated reader |
| **YouTube tutorials** | 10-20 min commitment, can't skim, requires device during play |
| **Google searches** | Scattered results, inconsistent quality, not mobile-optimized |
| **BGG forums** | Information overload, not beginner-friendly |
| **AI chat assistants** (Rulesbot.ai, Boardgamebot.AI, Ludomentor) | Require you to already know the game name - no photo identification |

**No existing app offers the complete journey: photo-based game identification + structured rule generation.**

### Proposed Solution

Rulebook delivers a **3-tap experience**: Open app → Photograph game box → Read structured rules.

The AI-generated rules follow a **progressive disclosure format**:
1. **Game Overview** - What it is, how to win
2. **Setup Instructions** - Checklist-style preparation
3. **First Round Guide** - Turn-by-turn walkthrough to start playing
4. **Advanced Rules** - Deep dive for later reference

### Key Differentiators

| Differentiator | Impact |
|----------------|--------|
| **Photo-to-rules pipeline** | Only app solving the complete journey from unknown game box to playing |
| **Multi-model AI resilience** | Fallback chain ensures recognition even for obscure/international games |
| **First-to-market on Android** | No direct competitors with photo-based identification |
| **Instant vs. passive** | Seconds to read vs. 15+ min video |
| **Progressive structure** | Start playing immediately, reference advanced rules later |
| **Offline library** | Works without wifi - because game nights happen in basements, cabins, and cafes |
| **Credit-based monetization** | 3 free scans to prove value, then affordable IAP |
| **Optimized for the moment of need** | Scores 4.55/5 on weighted user priorities vs. 3.30 for next best alternative |

### Competitive Landscape

Existing AI board game assistants (Rulesbot.ai, Boardgamebot.AI, Ludomentor) validate market demand but solve a different problem: "I forgot a rule mid-game." They require users to already know and select their game.

**Rulebook solves:** "I don't even know what game this is or where to start."

This positions Rulebook not as a feature app, but as a **complete experience** - the only solution covering identification through structured learning.

---

## Target Users

### Primary Users

#### 1. The Game Night Host ("The Collector")

**Profile:** Owns 20+ board games, regularly buys new ones, hosts game nights at their home. Often the most enthusiastic gamer in their friend group.

**Pain Point:** Has games still in shrink wrap because explaining rules kills the momentum. Feels guilty about unplayed games. Ends up rotating the same 3-4 games everyone already knows.

**How They Use Rulebook:** Scans a new game before or during game night. Becomes the "facilitator" rather than the "rules reader" - the app handles the heavy lifting while they set up the board.

**Success Moment:** "We actually played that new game I bought 6 months ago - and it only took 2 minutes to get started!"

---

#### 2. The Game Night Guest ("The Joiner")

**Profile:** Enjoys board games socially but doesn't own many. Joins friends' game nights. Dreads being the one stuck reading the rulebook.

**Pain Point:** Wants to participate and have fun, not sit through a 20-minute rules explanation. Sometimes zones out during rules and feels lost during gameplay.

**How They Use Rulebook:** May have the app themselves to help out the host, or follows along on the host's phone. Uses the progressive format to reference rules mid-game without disrupting flow.

**Success Moment:** "I actually understood what I was doing from the first turn!"

---

#### 3. The Club Member ("The Explorer")

**Profile:** Part of a board game club or meetup group. Enjoys trying new games and meeting fellow enthusiasts. The club often has a library of games to choose from.

**Pain Point:** Club time is limited. Choosing and learning a new game can eat into precious play time. Newcomers to the group need to get up to speed quickly.

**How They Use Rulebook:** Multiple members have the app. Someone scans a few game boxes to help the group decide what to play. Once chosen, the rules summary helps everyone - especially newcomers - start playing immediately.

**Success Moment:** "We tried 2 new games tonight instead of spending the whole session learning one!"

---

### Secondary Users

**Board Game Cafe Staff** *(potential future segment)*

Staff at board game cafes who help customers choose and learn games. Could use Rulebook to quickly onboard customers to unfamiliar games. Not a primary focus for MVP but validates commercial potential.

---

### User Journey

| Stage | The Host | The Guest | The Club Member |
|-------|----------|-----------|-----------------|
| **Discovery** | App Store search, Reddit, BGG forums | Friend shows them at game night | Fellow club member recommends |
| **First Use** | Scans a game from their shelf | Watches host use it, downloads later | Uses at club meetup |
| **Aha Moment** | "We're actually playing in under 5 minutes!" | "I didn't have to pretend I understood" | "We can try more games per session" |
| **Retention** | Uses before every game night with new games | Opens when joining game nights | Brings it to every club meeting |
| **Advocacy** | Recommends to gamer friends | Tells their own friend group | Club adopts as standard tool |

---

## Success Metrics

### North Star Metric

**"Games successfully scanned and rules viewed"**

This measures the core value proposition directly - users are identifying games and consuming the generated rules. Everything else supports this.

---

### User Success Metrics

| Metric | What It Measures | Target |
|--------|------------------|--------|
| **Onboarding completion rate** | Users understanding the value prop | >80% |
| **First scan completion** | Users trying the core feature | >60% of installs |
| **Time to first scan** | App intuitiveness | <2 minutes from install |
| **Scan success rate** | AI reliability | >85% result in usable rules |
| **Return usage (2nd scan)** | Perceived value | >30% within 14 days |
| **Library growth** | Ongoing engagement | Avg 3+ games saved per active user |

---

### Business Objectives

| Timeframe | Objective |
|-----------|-----------|
| **Launch (Month 1)** | Prove core value - users complete scans, view rules, return for more |
| **Growth (Months 2-3)** | Build retention - users come back for new games, library grows |
| **Monetization (Months 3-6)** | Validate revenue - conversion rate supports sustainable business |

---

### Key Performance Indicators

| Category | KPI | Target |
|----------|-----|--------|
| **Growth** | Monthly downloads | Track trend (no hard target pre-launch) |
| **Engagement** | DAU/MAU ratio | >15% (healthy for utility app) |
| **Monetization** | Free → Paid conversion | >5% of users who exhaust free credits |
| **Monetization** | ARPU (paying users) | Track and optimize |
| **Retention** | Day 1 retention | >40% |
| **Retention** | Day 7 retention | >20% |
| **Retention** | Day 30 retention | >10% |
| **Quality** | App store rating | >4.0 stars |
| **Quality** | Crash-free rate | >99% |

---

### Metrics Philosophy

**Focus on leading indicators early:**
- Scan completion and return usage predict long-term success
- Don't over-optimize monetization before proving value

**Avoid vanity metrics:**
- Downloads alone don't matter if users don't scan
- Focus on activation and engagement over raw installs

---

## MVP Scope

### Core Features (1:1 iOS Parity)

| Module | Features |
|--------|----------|
| **Onboarding** | 2-screen intro flow, awards 3 free credits on completion |
| **Photo Capture** | Custom camera (flash, zoom, tap-to-focus), gallery picker, permission handling |
| **Rules Generation** | 5-phase pipeline (processing → analyzing → manual entry → fetching → saving), confidence-based flow, multi-model AI fallback |
| **Rules Display** | Progressive sections (overview, setup, first round, deep dive), expandable content, share functionality, setup checklist |
| **Library** | Grid view of saved games, sort (recent/alpha/date), delete with confirmation, offline access |
| **Purchase** | 3 credit packs (1, 3, 10 credits), paywall UI, restore purchases, Ask-to-Buy support |
| **Settings** | Theme (light/dark/system), haptics toggle, support links, clear all data |

### Technical Requirements

| Requirement | Details |
|-------------|---------|
| **Design System** | Establish comprehensive design system from day 1 - tokens, components, patterns |
| **Brutalist Visual Style** | Thick borders, bold shadows, bright colors - matching iOS aesthetic |
| **Offline-First** | Rules cached locally, library accessible without network |
| **Analytics** | Firebase Analytics for event tracking (matching iOS TelemetryDeck events) |

### Android-Specific Enhancements (MVP)

| Feature | Rationale |
|---------|-----------|
| **App Shortcuts** | Long-press "Scan Game" action - quick access to core feature |
| **Predictive Back Gesture** | Modern Android navigation pattern |
| **Edge-to-Edge Display** | Full-screen content, modern appearance |

---

### Out of Scope for MVP

| Feature | Rationale |
|---------|-----------|
| **User Accounts / Cloud Sync** | Complexity; local-only is sufficient for MVP |
| **Social Features** | Sharing rules is enough; no in-app social |
| **Subscription Model** | Credit packs proven on iOS; keep monetization simple |
| **Material You / Dynamic Color** | Design system complexity; defer to post-MVP |
| **Share Target (receive images)** | Nice-to-have; camera + gallery is sufficient |
| **Widgets** | Defer to post-MVP based on user demand |
| **Wear OS / Tablet optimization** | Phone-first; optimize later based on usage |

---

### MVP Success Criteria

| Criteria | Threshold |
|----------|-----------|
| **Feature Parity** | All iOS features functional on Android |
| **Performance** | Scan-to-rules in <60 seconds (matching iOS) |
| **Stability** | >99% crash-free sessions |
| **Store Approval** | Published on Google Play Store |
| **User Validation** | First 100 users complete scans successfully |

---

### Future Vision (Post-MVP)

| Phase | Features |
|-------|----------|
| **v1.1** | Material You theming, Share Target, performance optimizations |
| **v1.2** | Widgets, tablet optimization, Wear OS companion |
| **v2.0** | User accounts, cloud sync, cross-platform library |
| **Beyond** | Social features, community rules contributions, board game cafe partnerships |

---

*Product Brief completed on 2025-12-02*
