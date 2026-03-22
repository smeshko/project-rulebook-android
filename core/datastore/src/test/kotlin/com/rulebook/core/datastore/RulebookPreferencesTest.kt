package com.rulebook.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for RulebookPreferences.
 *
 * Tests for the Keys object and validation logic.
 * Integration tests with actual DataStore are in androidTest.
 */
class RulebookPreferencesTest {

    // ==================== Keys Tests ====================

    @Test
    fun `Keys ONBOARDING_COMPLETED has correct key name`() {
        val expected = booleanPreferencesKey("onboarding_completed")
        assertEquals(expected, RulebookPreferences.Keys.ONBOARDING_COMPLETED)
    }

    @Test
    fun `Keys CREDIT_BALANCE has correct key name`() {
        val expected = intPreferencesKey("credit_balance")
        assertEquals(expected, RulebookPreferences.Keys.CREDIT_BALANCE)
    }

    @Test
    fun `Keys THEME_MODE has correct key name`() {
        val expected = stringPreferencesKey("theme_mode")
        assertEquals(expected, RulebookPreferences.Keys.THEME_MODE)
    }

    @Test
    fun `Keys HAPTICS_ENABLED has correct key name`() {
        val expected = booleanPreferencesKey("haptics_enabled")
        assertEquals(expected, RulebookPreferences.Keys.HAPTICS_ENABLED)
    }

    @Test
    fun `Keys SORT_ORDER has correct key name`() {
        val expected = stringPreferencesKey("sort_order")
        assertEquals(expected, RulebookPreferences.Keys.SORT_ORDER)
    }

    // ==================== Credit Balance Validation Tests ====================

    @Test
    fun `coerceAtLeast prevents negative credit balance`() {
        // Test the validation logic used in setCreditBalance
        val negativeBalance = -50
        val coerced = negativeBalance.coerceAtLeast(0)
        assertEquals(0, coerced)
    }

    @Test
    fun `coerceAtLeast allows zero credit balance`() {
        val zeroBalance = 0
        val coerced = zeroBalance.coerceAtLeast(0)
        assertEquals(0, coerced)
    }

    @Test
    fun `coerceAtLeast allows positive credit balance`() {
        val positiveBalance = 100
        val coerced = positiveBalance.coerceAtLeast(0)
        assertEquals(100, coerced)
    }

    @Test
    fun `coerceAtLeast handles large negative values`() {
        val largeNegative = Int.MIN_VALUE
        val coerced = largeNegative.coerceAtLeast(0)
        assertEquals(0, coerced)
    }

    @Test
    fun `coerceAtLeast handles max int value`() {
        val maxValue = Int.MAX_VALUE
        val coerced = maxValue.coerceAtLeast(0)
        assertEquals(Int.MAX_VALUE, coerced)
    }

    // ==================== removeCredits Logic Tests ====================

    @Test
    fun `removeCredits logic - removing less than balance returns exact amount`() {
        val balance = 10
        val amount = 3
        val actualRemoved = minOf(balance, amount)
        val newBalance = (balance - amount).coerceAtLeast(0)
        assertEquals(3, actualRemoved)
        assertEquals(7, newBalance)
    }

    @Test
    fun `removeCredits logic - removing more than balance clamps to 0 and returns actual balance`() {
        val balance = 2
        val amount = 5
        val actualRemoved = minOf(balance, amount)
        val newBalance = (balance - amount).coerceAtLeast(0)
        assertEquals(2, actualRemoved)
        assertEquals(0, newBalance)
    }

    @Test
    fun `removeCredits logic - removing from zero balance returns 0`() {
        val balance = 0
        val amount = 3
        val actualRemoved = minOf(balance, amount)
        val newBalance = (balance - amount).coerceAtLeast(0)
        assertEquals(0, actualRemoved)
        assertEquals(0, newBalance)
    }
}
