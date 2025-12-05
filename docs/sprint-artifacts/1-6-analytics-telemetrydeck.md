# Story 1.6: Analytics Integration with TelemetryDeck

Status: ready-for-dev

## Linear Issue

- **ID:** RULE-113
- **URL:** https://linear.app/project-rulebook/issue/RULE-113/story-16-analytics-integration-with-telemetrydeck

## Story

As a developer,
I want TelemetryDeck analytics configured,
So that usage events can be tracked consistently with iOS.

## Acceptance Criteria

1. **Given** the `core/analytics` module
   **When** TelemetryDeck is configured
   **Then** `AnalyticsManager.kt` provides:
   - `trackEvent(name: String, properties: Map<String, String>)`
   - `trackScreenView(screenName: String)`

2. **And** TelemetryDeck SDK 6.0.1 is initialized in Application class

3. **And** App ID is configured via BuildConfig

4. **And** no PII is transmitted

## Tasks / Subtasks

- [ ] Task 1: Add TelemetryDeck dependency (AC: #2)
  - [ ] Add TelemetryDeck SDK 6.0.1 to version catalog
  - [ ] Add dependency to core/analytics module
- [ ] Task 2: Configure App ID in BuildConfig (AC: #3)
  - [ ] Add TELEMETRY_APP_ID to build.gradle.kts
  - [ ] Use different IDs for debug/release if needed
- [ ] Task 3: Create AnalyticsManager interface (AC: #1)
  - [ ] Define trackEvent(name, properties) method
  - [ ] Define trackScreenView(screenName) method
- [ ] Task 4: Create TelemetryDeckAnalyticsManager (AC: #1, #4)
  - [ ] Implement AnalyticsManager interface
  - [ ] Initialize TelemetryDeck client
  - [ ] Implement trackEvent using TelemetryDeck.send()
  - [ ] Implement trackScreenView
  - [ ] Ensure no PII in event data
- [ ] Task 5: Initialize TelemetryDeck in Application (AC: #2)
  - [ ] Initialize in RulebookApplication.onCreate()
  - [ ] Configure with App ID from BuildConfig
- [ ] Task 6: Add Analytics to Koin DI
  - [ ] Create provideAnalyticsManager function
  - [ ] Register in AnalyticsModule

## Dev Notes

### Architecture Patterns

- TelemetryDeck Kotlin SDK 6.0.1
- Same event names as iOS for cross-platform consistency
- Privacy-focused, GDPR compliant

### Implementation Pattern

```kotlin
interface AnalyticsManager {
    fun trackEvent(name: String, properties: Map<String, String> = emptyMap())
    fun trackScreenView(screenName: String)
}

class TelemetryDeckAnalyticsManager(
    private val context: Context
) : AnalyticsManager {

    init {
        TelemetryDeck.builder()
            .appID(BuildConfig.TELEMETRY_APP_ID)
            .build(context)
    }

    override fun trackEvent(name: String, properties: Map<String, String>) {
        TelemetryDeck.signal(name, properties)
    }

    override fun trackScreenView(screenName: String) {
        TelemetryDeck.signal("screen_view", mapOf("screen_name" to screenName))
    }
}
```

### iOS Event Name Consistency

Use same event names as iOS for cross-platform analytics:
- `onboarding_started`
- `onboarding_completed`
- `onboarding_skipped`
- `scan_started`
- `scan_completed`
- `scan_failed`
- `purchase_started`
- `purchase_completed`

### PRD Requirements Mapped

- NFR18: Firebase Analytics event tracking (TelemetryDeck for this project)

### Privacy Compliance

- No PII transmitted
- No user identifiers stored
- GDPR compliant by design
- Privacy-focused alternative to Firebase Analytics

### Project Structure Notes

- AnalyticsManager is an interface for testability
- Implementation uses TelemetryDeck
- Easy to swap implementations if needed

### References

- [Source: docs/architecture.md#Platform Integration]
- [Source: docs/architecture.md#Core Architectural Decisions]

## Dev Agent Record

### Context Reference

### Agent Model Used

### Debug Log References

### Completion Notes List

### File List
