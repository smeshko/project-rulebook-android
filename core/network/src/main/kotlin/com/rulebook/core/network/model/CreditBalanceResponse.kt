package com.rulebook.core.network.model

/**
 * Response DTO for GET /api/v1/credits/balance.
 *
 * @property balance The authoritative credit balance from the server.
 */
data class CreditBalanceResponse(
    val balance: Int,
)
