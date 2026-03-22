package com.rulebook.core.data.repository

import com.rulebook.core.datastore.CreditPreferencesSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreditRepository.
 *
 * Tests repository behavior using a fake implementation of CreditPreferencesSource.
 */
class CreditRepositoryTest {

    private lateinit var fakePreferences: FakeCreditPreferencesSource
    private lateinit var repository: CreditRepository

    @Before
    fun setup() {
        fakePreferences = FakeCreditPreferencesSource()
        repository = CreditRepositoryImpl(fakePreferences)
    }

    // ==================== creditBalance Tests ====================

    @Test
    fun `creditBalance returns 0 by default`() = runTest {
        val result = repository.creditBalance.first()
        assertEquals(0, result)
    }

    @Test
    fun `creditBalance returns current balance value`() = runTest {
        fakePreferences.setBalance(5)
        val result = repository.creditBalance.first()
        assertEquals(5, result)
    }

    @Test
    fun `creditBalance emits updates when value changes`() = runTest {
        assertEquals(0, repository.creditBalance.first())

        fakePreferences.setBalance(3)
        assertEquals(3, repository.creditBalance.first())

        fakePreferences.setBalance(10)
        assertEquals(10, repository.creditBalance.first())
    }

    // ==================== awardInitialCredits Tests ====================

    @Test
    fun `awardInitialCredits sets balance when current balance is 0`() = runTest {
        val result = repository.awardInitialCredits(3)

        assertTrue(result)
        assertEquals(3, repository.creditBalance.first())
    }

    @Test
    fun `awardInitialCredits does not change balance when current balance is positive`() = runTest {
        fakePreferences.setBalance(5)

        val result = repository.awardInitialCredits(3)

        assertFalse(result)
        assertEquals(5, repository.creditBalance.first())
    }

    @Test
    fun `awardInitialCredits is idempotent - second call does nothing`() = runTest {
        repository.awardInitialCredits(3)
        assertEquals(3, repository.creditBalance.first())

        val secondResult = repository.awardInitialCredits(3)

        assertFalse(secondResult)
        assertEquals(3, repository.creditBalance.first())
    }

    // ==================== deductCredit Tests ====================

    @Test
    fun `deductCredit reduces balance by 1 when balance is positive`() = runTest {
        fakePreferences.setBalance(3)

        val result = repository.deductCredit()

        assertTrue(result)
        assertEquals(2, repository.creditBalance.first())
    }

    @Test
    fun `deductCredit returns false when balance is 0`() = runTest {
        val result = repository.deductCredit()

        assertFalse(result)
        assertEquals(0, repository.creditBalance.first())
    }

    @Test
    fun `deductCredit can reduce balance to 0`() = runTest {
        fakePreferences.setBalance(1)

        val result = repository.deductCredit()

        assertTrue(result)
        assertEquals(0, repository.creditBalance.first())
    }

    @Test
    fun `multiple deductCredit calls reduce balance sequentially`() = runTest {
        fakePreferences.setBalance(3)

        assertTrue(repository.deductCredit())
        assertEquals(2, repository.creditBalance.first())

        assertTrue(repository.deductCredit())
        assertEquals(1, repository.creditBalance.first())

        assertTrue(repository.deductCredit())
        assertEquals(0, repository.creditBalance.first())

        assertFalse(repository.deductCredit())
        assertEquals(0, repository.creditBalance.first())
    }

    @Test
    fun `deductCredit from balance of 1 results in 0 and hasCredits returns false`() = runTest {
        fakePreferences.setBalance(1)

        val deductResult = repository.deductCredit()
        assertTrue(deductResult)
        assertEquals(0, repository.creditBalance.first())

        val hasCreditsResult = repository.hasCredits()
        assertFalse(hasCreditsResult)
    }

    // ==================== hasCredits Tests ====================

    @Test
    fun `hasCredits returns false when balance is 0`() = runTest {
        val result = repository.hasCredits()
        assertFalse(result)
    }

    @Test
    fun `hasCredits returns true when balance is positive`() = runTest {
        fakePreferences.setBalance(3)
        val result = repository.hasCredits()
        assertTrue(result)
    }

    // ==================== removeCredits Tests ====================

    @Test
    fun `removeCredits reduces balance by specified amount`() = runTest {
        fakePreferences.setBalance(10)

        val removed = repository.removeCredits(3)

        assertEquals(3, removed)
        assertEquals(7, repository.creditBalance.first())
    }

    @Test
    fun `removeCredits returns actual removed when balance less than amount`() = runTest {
        fakePreferences.setBalance(2)

        val removed = repository.removeCredits(5)

        assertEquals(2, removed)
        assertEquals(0, repository.creditBalance.first())
    }

    @Test
    fun `removeCredits from zero balance returns 0 and leaves balance at 0`() = runTest {
        val removed = repository.removeCredits(3)

        assertEquals(0, removed)
        assertEquals(0, repository.creditBalance.first())
    }

    @Test
    fun `removeCredits exact balance returns exact amount and leaves 0`() = runTest {
        fakePreferences.setBalance(5)

        val removed = repository.removeCredits(5)

        assertEquals(5, removed)
        assertEquals(0, repository.creditBalance.first())
    }
}

/**
 * Fake implementation of CreditPreferencesSource for testing.
 */
class FakeCreditPreferencesSource : CreditPreferencesSource {
    private val _creditBalance = MutableStateFlow(0)
    override val creditBalance: Flow<Int> = _creditBalance

    override suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean {
        return if (_creditBalance.value == 0) {
            _creditBalance.value = amount
            true
        } else {
            false
        }
    }

    override suspend fun deductCredit(): Boolean {
        return if (_creditBalance.value > 0) {
            _creditBalance.value = _creditBalance.value - 1
            true
        } else {
            false
        }
    }

    override suspend fun addCredits(amount: Int): Boolean {
        _creditBalance.value = _creditBalance.value + amount
        return true
    }

    override suspend fun removeCredits(amount: Int): Int {
        val current = _creditBalance.value
        val actualRemoved = minOf(current, amount)
        _creditBalance.value = (current - amount).coerceAtLeast(0)
        return actualRemoved
    }

    fun setBalance(balance: Int) {
        _creditBalance.value = balance
    }
}
