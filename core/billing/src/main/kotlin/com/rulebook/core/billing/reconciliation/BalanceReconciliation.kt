package com.rulebook.core.billing.reconciliation

import com.rulebook.core.analytics.AnalyticsManager
import com.rulebook.core.common.getOrElse
import com.rulebook.core.data.repository.CreditRepository
import com.rulebook.core.data.repository.ReceiptRepository
import kotlinx.coroutines.flow.first

/**
 * Contract for running app-launch credit balance reconciliation.
 *
 * Allows [BalanceReconciliationManager] to be swapped with a test double in unit tests.
 */
interface BalanceReconciliation {
    /**
     * Fetches the authoritative balance from the server and syncs local DataStore if different.
     */
    suspend fun reconcile(): ReconciliationResult
}

/**
 * Summary of a [BalanceReconciliationManager.reconcile] run.
 *
 * @property reconciled Whether the local balance was updated to match the server.
 * @property localBalance The local balance before reconciliation.
 * @property serverBalance The authoritative balance from the server.
 * @property delta The difference (serverBalance - localBalance). 0 if not reconciled.
 */
data class ReconciliationResult(
    val reconciled: Boolean,
    val localBalance: Int,
    val serverBalance: Int,
    val delta: Int,
)

/**
 * Orchestrates app-launch credit balance reconciliation.
 *
 * On each app launch, fetches the authoritative credit balance from the server
 * and overwrites the local DataStore balance if they differ. The server is always
 * authoritative — local balance is a cache.
 *
 * Silent on failure — if the server is unreachable, the local balance is preserved
 * as best-effort and reconciliation is skipped silently.
 *
 * Throttled to once per app launch (runs in [StartupViewModel.init]).
 *
 * @param receiptRepository Repository for calling the backend balance endpoint.
 * @param creditRepository Repository for reading and setting the local credit balance.
 * @param analyticsManager Analytics for tracking reconciliation outcomes.
 */
class BalanceReconciliationManager(
    private val receiptRepository: ReceiptRepository,
    private val creditRepository: CreditRepository,
    private val analyticsManager: AnalyticsManager,
) : BalanceReconciliation {

    /**
     * Runs the balance reconciliation flow:
     * 1. Fetches server balance — returns no-op result on error.
     * 2. Reads local balance from DataStore.
     * 3. If equal, returns no-op result.
     * 4. If different: overwrites local balance with server balance and fires analytics.
     *
     * @return [ReconciliationResult] describing the outcome.
     */
    override suspend fun reconcile(): ReconciliationResult {
        // Step 1: Fetch server balance — skip on error
        val serverBalance = receiptRepository.getServerBalance()
            .getOrElse { _ -> return ReconciliationResult(reconciled = false, localBalance = 0, serverBalance = 0, delta = 0) }

        // Step 2: Read local balance
        val localBalance = creditRepository.creditBalance.first()

        // Step 3: No-op if already in sync
        if (localBalance == serverBalance) {
            return ReconciliationResult(reconciled = false, localBalance = localBalance, serverBalance = serverBalance, delta = 0)
        }

        // Step 4: Overwrite local balance and fire analytics
        creditRepository.setCreditBalance(serverBalance)
        val delta = serverBalance - localBalance
        analyticsManager.trackCreditBalanceReconciled(
            localBalance = localBalance,
            serverBalance = serverBalance,
            delta = delta,
        )

        return ReconciliationResult(
            reconciled = true,
            localBalance = localBalance,
            serverBalance = serverBalance,
            delta = delta,
        )
    }
}
