package com.rulebook.core.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Instrumented tests for RulebookPreferences.
 *
 * These tests verify actual DataStore persistence behavior on a real device/emulator.
 */
@RunWith(AndroidJUnit4::class)
class RulebookPreferencesIntegrationTest {

    private lateinit var context: Context
    private lateinit var preferences: RulebookPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear any existing preferences before each test
        clearDataStore()
        preferences = RulebookPreferences(context)
    }

    @After
    fun tearDown() {
        clearDataStore()
    }

    private fun clearDataStore() {
        // Delete the DataStore file to ensure clean state
        val prefsFile = File(context.filesDir, "datastore/rulebook_preferences.preferences_pb")
        if (prefsFile.exists()) {
            prefsFile.delete()
        }
    }

    // ==================== Default Values Tests ====================

    @Test
    fun hasCompletedOnboarding_defaultsToFalse() = runTest {
        val result = preferences.hasCompletedOnboarding.first()
        assertFalse(result)
    }

    @Test
    fun creditBalance_defaultsToZero() = runTest {
        val result = preferences.creditBalance.first()
        assertEquals(0, result)
    }

    @Test
    fun themeMode_defaultsToSystem() = runTest {
        val result = preferences.themeMode.first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    @Test
    fun hapticsEnabled_defaultsToTrue() = runTest {
        val result = preferences.hapticsEnabled.first()
        assertTrue(result)
    }

    // ==================== Onboarding Tests ====================

    @Test
    fun setOnboardingCompleted_persistsTrue() = runTest {
        preferences.setOnboardingCompleted(true)
        val result = preferences.hasCompletedOnboarding.first()
        assertTrue(result)
    }

    @Test
    fun setOnboardingCompleted_canToggle() = runTest {
        preferences.setOnboardingCompleted(true)
        preferences.setOnboardingCompleted(false)
        val result = preferences.hasCompletedOnboarding.first()
        assertFalse(result)
    }

    // ==================== Credit Balance Tests ====================

    @Test
    fun setCreditBalance_persistsPositiveValue() = runTest {
        preferences.setCreditBalance(100)
        val result = preferences.creditBalance.first()
        assertEquals(100, result)
    }

    @Test
    fun setCreditBalance_coercesNegativeToZero() = runTest {
        preferences.setCreditBalance(-50)
        val result = preferences.creditBalance.first()
        assertEquals(0, result)
    }

    @Test
    fun setCreditBalance_allowsZero() = runTest {
        preferences.setCreditBalance(50)
        preferences.setCreditBalance(0)
        val result = preferences.creditBalance.first()
        assertEquals(0, result)
    }

    // ==================== Theme Mode Tests ====================

    @Test
    fun setThemeMode_persistsLight() = runTest {
        preferences.setThemeMode(ThemeMode.LIGHT)
        val result = preferences.themeMode.first()
        assertEquals(ThemeMode.LIGHT, result)
    }

    @Test
    fun setThemeMode_persistsDark() = runTest {
        preferences.setThemeMode(ThemeMode.DARK)
        val result = preferences.themeMode.first()
        assertEquals(ThemeMode.DARK, result)
    }

    @Test
    fun setThemeMode_persistsSystem() = runTest {
        preferences.setThemeMode(ThemeMode.DARK)
        preferences.setThemeMode(ThemeMode.SYSTEM)
        val result = preferences.themeMode.first()
        assertEquals(ThemeMode.SYSTEM, result)
    }

    // ==================== Haptics Tests ====================

    @Test
    fun setHapticsEnabled_persistsFalse() = runTest {
        preferences.setHapticsEnabled(false)
        val result = preferences.hapticsEnabled.first()
        assertFalse(result)
    }

    @Test
    fun setHapticsEnabled_canToggleBackToTrue() = runTest {
        preferences.setHapticsEnabled(false)
        preferences.setHapticsEnabled(true)
        val result = preferences.hapticsEnabled.first()
        assertTrue(result)
    }

    // ==================== Combined State Tests ====================

    @Test
    fun multiplePreferences_persistIndependently() = runTest {
        preferences.setOnboardingCompleted(true)
        preferences.setCreditBalance(42)
        preferences.setThemeMode(ThemeMode.DARK)
        preferences.setHapticsEnabled(false)

        assertTrue(preferences.hasCompletedOnboarding.first())
        assertEquals(42, preferences.creditBalance.first())
        assertEquals(ThemeMode.DARK, preferences.themeMode.first())
        assertFalse(preferences.hapticsEnabled.first())
    }

    // ==================== Atomic Credit Operations Tests ====================

    @Test
    fun completeOnboardingWithCredits_setsOnboardingAndCreditsAtomically() = runTest {
        val result = preferences.completeOnboardingWithCredits(3)

        assertTrue(result)
        assertTrue(preferences.hasCompletedOnboarding.first())
        assertEquals(3, preferences.creditBalance.first())
    }

    @Test
    fun completeOnboardingWithCredits_isIdempotent() = runTest {
        // First completion should succeed
        val firstResult = preferences.completeOnboardingWithCredits(3)
        assertTrue(firstResult)

        // Second completion should be no-op
        val secondResult = preferences.completeOnboardingWithCredits(3)
        assertFalse(secondResult)

        // Values should remain unchanged
        assertTrue(preferences.hasCompletedOnboarding.first())
        assertEquals(3, preferences.creditBalance.first())
    }

    @Test
    fun completeOnboardingWithCredits_doesNotModifyWhenAlreadyCompleted() = runTest {
        // Manually complete onboarding with different credit value
        preferences.setOnboardingCompleted(true)
        preferences.setCreditBalance(10)

        // Attempt atomic completion should be no-op
        val result = preferences.completeOnboardingWithCredits(3)

        assertFalse(result)
        assertTrue(preferences.hasCompletedOnboarding.first())
        assertEquals(10, preferences.creditBalance.first()) // Original value preserved
    }

    @Test
    fun awardInitialCreditsIfNeeded_awardsWhenBalanceIsZero() = runTest {
        val result = preferences.awardInitialCreditsIfNeeded(3)

        assertTrue(result)
        assertEquals(3, preferences.creditBalance.first())
    }

    @Test
    fun awardInitialCreditsIfNeeded_doesNotAwardWhenBalanceIsPositive() = runTest {
        preferences.setCreditBalance(5)

        val result = preferences.awardInitialCreditsIfNeeded(3)

        assertFalse(result)
        assertEquals(5, preferences.creditBalance.first()) // Original value preserved
    }

    @Test
    fun awardInitialCreditsIfNeeded_isIdempotent() = runTest {
        preferences.awardInitialCreditsIfNeeded(3)
        val result = preferences.awardInitialCreditsIfNeeded(3)

        assertFalse(result)
        assertEquals(3, preferences.creditBalance.first())
    }

    @Test
    fun deductCredit_reducesBalanceByOne() = runTest {
        preferences.setCreditBalance(3)

        val result = preferences.deductCredit()

        assertTrue(result)
        assertEquals(2, preferences.creditBalance.first())
    }

    @Test
    fun deductCredit_returnsFalseWhenBalanceIsZero() = runTest {
        assertEquals(0, preferences.creditBalance.first())

        val result = preferences.deductCredit()

        assertFalse(result)
        assertEquals(0, preferences.creditBalance.first())
    }

    @Test
    fun deductCredit_canReduceBalanceToZero() = runTest {
        preferences.setCreditBalance(1)

        val result = preferences.deductCredit()

        assertTrue(result)
        assertEquals(0, preferences.creditBalance.first())
    }

    @Test
    fun multipleDeductCredit_reducesSequentially() = runTest {
        preferences.setCreditBalance(3)

        assertTrue(preferences.deductCredit())
        assertEquals(2, preferences.creditBalance.first())

        assertTrue(preferences.deductCredit())
        assertEquals(1, preferences.creditBalance.first())

        assertTrue(preferences.deductCredit())
        assertEquals(0, preferences.creditBalance.first())

        // Fourth deduction should fail
        assertFalse(preferences.deductCredit())
        assertEquals(0, preferences.creditBalance.first())
    }
}
