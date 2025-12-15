package com.rulebook.core.analytics

/**
 * Fake implementation of AnalyticsManager for testing.
 * Records all tracked events for verification in tests.
 */
class FakeAnalyticsManager : AnalyticsManager {

    data class TrackedEvent(
        val name: String,
        val properties: Map<String, String>
    )

    private val _trackedEvents = mutableListOf<TrackedEvent>()
    val trackedEvents: List<TrackedEvent> get() = _trackedEvents.toList()

    private val _trackedScreenViews = mutableListOf<String>()
    val trackedScreenViews: List<String> get() = _trackedScreenViews.toList()

    override fun trackEvent(name: String, properties: Map<String, String>) {
        _trackedEvents.add(TrackedEvent(name, properties))
    }

    override fun trackScreenView(screenName: String) {
        _trackedScreenViews.add(screenName)
    }

    fun clear() {
        _trackedEvents.clear()
        _trackedScreenViews.clear()
    }
}
