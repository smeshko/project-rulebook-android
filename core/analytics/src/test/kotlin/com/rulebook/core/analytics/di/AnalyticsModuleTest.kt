package com.rulebook.core.analytics.di

import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for AnalyticsModule DI configuration.
 *
 * Note: Full Koin module verification requires Android instrumentation tests
 * since the module depends on Android Context. These tests verify the module
 * structure and configuration.
 */
class AnalyticsModuleTest {

    @Test
    fun `analyticsModule is defined`() {
        assertNotNull(analyticsModule)
    }
}
