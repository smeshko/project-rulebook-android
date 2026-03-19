package com.rulebook.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rulebook.core.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rulebook_preferences")

/**
 * Manages user preferences using DataStore.
 *
 * Provides reactive [Flow] access to preferences and suspend functions for updates.
 * All preferences have sensible defaults when not yet set.
 *
 * Implements [OnboardingPreferencesSource] to allow OnboardingRepository to
 * access onboarding-related preferences through an abstraction.
 *
 * Implements [CreditPreferencesSource] to allow CreditRepository to
 * access credit balance preferences through an abstraction.
 *
 * @param context Application context for DataStore access. Must be Application context
 *                to avoid memory leaks.
 */
open class RulebookPreferences(private val context: Context) : OnboardingPreferencesSource, CreditPreferencesSource, SortPreferencesSource, PendingPurchasePreferencesSource, ThemePreferencesSource, HapticsPreferencesSource, ResettablePreferences {

    /**
     * Preference keys used for DataStore storage.
     * Internal visibility for testing purposes.
     */
    internal object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val CREDIT_BALANCE = intPreferencesKey("credit_balance")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val PENDING_PURCHASE_TOKEN = stringPreferencesKey("pending_purchase_token")
        val PENDING_PURCHASE_PRODUCT_ID = stringPreferencesKey("pending_purchase_product_id")
    }

    /**
     * Flow of onboarding completion status.
     * Default: `false`
     */
    override val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.ONBOARDING_COMPLETED] ?: false }

    /**
     * Sets the onboarding completion status.
     */
    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    /**
     * Completes onboarding and awards initial credits in a single atomic transaction.
     *
     * This is idempotent - if onboarding was already completed, no changes are made.
     * Both values are set together in a single DataStore edit block to prevent
     * partial state updates (e.g., if app crashes mid-operation).
     *
     * @param creditAmount The number of credits to award.
     * @return true if onboarding was completed and credits were awarded,
     *         false if onboarding was already completed.
     */
    override suspend fun completeOnboardingWithCredits(creditAmount: Int): Boolean {
        var wasCompleted = false
        context.dataStore.edit { preferences ->
            val alreadyCompleted = preferences[Keys.ONBOARDING_COMPLETED] ?: false
            if (!alreadyCompleted) {
                preferences[Keys.ONBOARDING_COMPLETED] = true
                preferences[Keys.CREDIT_BALANCE] = creditAmount
                wasCompleted = true
            }
        }
        return wasCompleted
    }

    /**
     * Flow of current credit balance.
     * Default: `0`
     */
    override val creditBalance: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[Keys.CREDIT_BALANCE] ?: 0 }

    /**
     * Sets the credit balance.
     * @param balance The new balance. Negative values are coerced to 0.
     */
    suspend fun setCreditBalance(balance: Int) {
        context.dataStore.edit { preferences ->
            preferences[Keys.CREDIT_BALANCE] = balance.coerceAtLeast(0)
        }
    }

    /**
     * Awards initial credits if the current balance is 0.
     * This is an idempotent operation - credits are only awarded once.
     *
     * @param amount The number of credits to award.
     * @return true if credits were awarded, false if balance was already positive.
     */
    override suspend fun awardInitialCreditsIfNeeded(amount: Int): Boolean {
        var awarded = false
        context.dataStore.edit { preferences ->
            val currentBalance = preferences[Keys.CREDIT_BALANCE] ?: 0
            if (currentBalance == 0) {
                preferences[Keys.CREDIT_BALANCE] = amount
                awarded = true
            }
        }
        return awarded
    }

    /**
     * Deducts one credit from the balance.
     *
     * @return true if a credit was deducted, false if balance was already 0.
     */
    override suspend fun deductCredit(): Boolean {
        var success = false
        context.dataStore.edit { preferences ->
            val currentBalance = preferences[Keys.CREDIT_BALANCE] ?: 0
            if (currentBalance > 0) {
                preferences[Keys.CREDIT_BALANCE] = currentBalance - 1
                success = true
            }
        }
        return success
    }

    /**
     * Adds credits to the current balance atomically.
     *
     * @param amount The number of credits to add.
     * @return true if credits were added successfully.
     */
    override suspend fun addCredits(amount: Int): Boolean {
        if (amount <= 0) return false
        context.dataStore.edit { preferences ->
            val currentBalance = preferences[Keys.CREDIT_BALANCE] ?: 0
            preferences[Keys.CREDIT_BALANCE] = currentBalance + amount
        }
        return true
    }

    /**
     * Flow of the pending purchase token, or null if no purchase is pending.
     */
    override val pendingPurchaseToken: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.PENDING_PURCHASE_TOKEN] }

    /**
     * Flow of the pending purchase product ID, or null if no purchase is pending.
     */
    override val pendingPurchaseProductId: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[Keys.PENDING_PURCHASE_PRODUCT_ID] }

    /**
     * Stores the pending purchase token and product ID atomically.
     */
    override suspend fun setPendingPurchase(token: String, productId: String) {
        context.dataStore.edit { preferences ->
            preferences[Keys.PENDING_PURCHASE_TOKEN] = token
            preferences[Keys.PENDING_PURCHASE_PRODUCT_ID] = productId
        }
    }

    /**
     * Clears the pending purchase token and product ID atomically.
     */
    override suspend fun clearPendingPurchase() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.PENDING_PURCHASE_TOKEN)
            preferences.remove(Keys.PENDING_PURCHASE_PRODUCT_ID)
        }
    }

    /**
     * Flow of current theme mode setting.
     * Default: [ThemeMode.SYSTEM]
     */
    override val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            ThemeMode.fromString(preferences[Keys.THEME_MODE])
        }

    /**
     * Sets the theme mode.
     */
    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    /**
     * Flow of haptic feedback enabled status.
     * Default: `true`
     */
    override val hapticsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[Keys.HAPTICS_ENABLED] ?: true }

    /**
     * Sets whether haptic feedback is enabled.
     */
    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    /**
     * Flow of current library sort order.
     * Default: [SortOrder.RECENT]
     */
    override val sortOrder: Flow<SortOrder> = context.dataStore.data
        .map { preferences ->
            val storedValue = preferences[Keys.SORT_ORDER]
            when (storedValue) {
                SortOrder.RECENT.name -> SortOrder.RECENT
                SortOrder.ALPHABETICAL.name -> SortOrder.ALPHABETICAL
                SortOrder.DATE_ADDED.name -> SortOrder.DATE_ADDED
                else -> SortOrder.RECENT // Default for null or unrecognized values
            }
        }

    /**
     * Sets the library sort order.
     */
    override suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SORT_ORDER] = order.name
        }
    }

    /**
     * Resets all preferences to defaults while preserving the credit balance.
     *
     * Reads the current credit balance, clears all preference keys, then
     * re-writes the preserved balance — all in a single atomic DataStore edit.
     */
    override suspend fun reset() {
        context.dataStore.edit { preferences ->
            val preservedBalance = preferences[Keys.CREDIT_BALANCE] ?: 0
            preferences.clear()
            preferences[Keys.CREDIT_BALANCE] = preservedBalance
        }
    }
}
