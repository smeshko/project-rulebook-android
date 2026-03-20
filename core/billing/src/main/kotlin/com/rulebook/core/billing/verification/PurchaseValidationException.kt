package com.rulebook.core.billing.verification

/**
 * Exception thrown when a purchase is rejected by server-side validation.
 *
 * This is raised by [ServerSidePurchaseVerifier] when the receipt validation
 * backend returns [com.rulebook.core.model.ValidationStatus.INVALID].
 *
 * @param message Human-readable description of the validation failure.
 */
class PurchaseValidationException(message: String) : Exception(message)
