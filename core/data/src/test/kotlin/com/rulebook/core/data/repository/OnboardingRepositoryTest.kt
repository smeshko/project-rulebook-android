package com.rulebook.core.data.repository

import com.rulebook.core.datastore.OnboardingPreferencesSource
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
 * Unit tests for OnboardingRepository.
 *
 * Tests repository behavior using a fake implementation of OnboardingPreferencesSource.
 */
class OnboardingRepositoryTest {

    private lateinit var fakePreferences: FakeOnboardingPreferencesSource
    private lateinit var repository: OnboardingRepository

    @Before
    fun setup() {
        fakePreferences = FakeOnboardingPreferencesSource()
        repository = OnboardingRepositoryImpl(fakePreferences)
    }

    // ==================== hasCompletedOnboarding Tests ====================

    @Test
    fun `hasCompletedOnboarding returns false by default`() = runTest {
        val result = repository.hasCompletedOnboarding.first()
        assertFalse(result)
    }

    @Test
    fun `hasCompletedOnboarding returns true when onboarding was completed`() = runTest {
        fakePreferences.setOnboardingCompletedValue(true)
        val result = repository.hasCompletedOnboarding.first()
        assertTrue(result)
    }

    @Test
    fun `hasCompletedOnboarding emits updates when value changes`() = runTest {
        assertFalse(repository.hasCompletedOnboarding.first())

        fakePreferences.setOnboardingCompletedValue(true)
        assertTrue(repository.hasCompletedOnboarding.first())

        fakePreferences.setOnboardingCompletedValue(false)
        assertFalse(repository.hasCompletedOnboarding.first())
    }

    // ==================== setOnboardingCompleted Tests ====================

    @Test
    fun `setOnboardingCompleted updates value to true`() = runTest {
        repository.setOnboardingCompleted(true)
        assertEquals(true, fakePreferences.onboardingCompletedValue)
    }

    @Test
    fun `setOnboardingCompleted updates value to false`() = runTest {
        fakePreferences.setOnboardingCompletedValue(true)
        repository.setOnboardingCompleted(false)
        assertEquals(false, fakePreferences.onboardingCompletedValue)
    }

    @Test
    fun `setOnboardingCompleted can be called multiple times`() = runTest {
        repository.setOnboardingCompleted(true)
        assertTrue(fakePreferences.onboardingCompletedValue)

        repository.setOnboardingCompleted(false)
        assertFalse(fakePreferences.onboardingCompletedValue)

        repository.setOnboardingCompleted(true)
        assertTrue(fakePreferences.onboardingCompletedValue)
    }

    // ==================== completeOnboardingWithCredits Tests ====================

    @Test
    fun `completeOnboardingWithCredits sets both onboarding and credits atomically`() = runTest {
        val result = repository.completeOnboardingWithCredits(3)

        assertTrue(result)
        assertTrue(fakePreferences.onboardingCompletedValue)
        assertEquals(3, fakePreferences.creditBalanceValue)
    }

    @Test
    fun `completeOnboardingWithCredits returns false when already completed`() = runTest {
        // Complete once
        repository.completeOnboardingWithCredits(3)

        // Try to complete again
        val result = repository.completeOnboardingWithCredits(3)

        assertFalse(result)
        assertEquals(3, fakePreferences.creditBalanceValue) // Credits unchanged
    }

    @Test
    fun `completeOnboardingWithCredits is idempotent - does not double credits`() = runTest {
        repository.completeOnboardingWithCredits(3)
        assertEquals(3, fakePreferences.creditBalanceValue)

        repository.completeOnboardingWithCredits(3)
        assertEquals(3, fakePreferences.creditBalanceValue) // Still 3, not 6
    }
}

/**
 * Fake implementation of OnboardingPreferencesSource for testing.
 */
class FakeOnboardingPreferencesSource : OnboardingPreferencesSource {
    private val _hasCompletedOnboarding = MutableStateFlow(false)
    override val hasCompletedOnboarding: Flow<Boolean> = _hasCompletedOnboarding

    var onboardingCompletedValue: Boolean = false
        private set

    var creditBalanceValue: Int = 0
        private set

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompletedValue = completed
        _hasCompletedOnboarding.value = completed
    }

    override suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
        return if (!onboardingCompletedValue) {
            onboardingCompletedValue = true
            creditBalanceValue = creditAmount
            _hasCompletedOnboarding.value = true
            true
        } else {
            false
        }
    }

    fun setOnboardingCompletedValue(value: Boolean) {
        onboardingCompletedValue = value
        _hasCompletedOnboarding.value = value
    }
}
