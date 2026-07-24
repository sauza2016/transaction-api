package com.spin.transaction.domain.enums;
/**
 * Represents the final processing status of a transaction.
 */
public enum TransactionStatus {
	/**
     * The transaction was successfully executed by the external provider.
     */
    EXECUTED,

    /**
     * The external provider processed the request but rejected it
     * (e.g., insufficient funds).
     */
    REJECTED,

    /**
     * The transaction could not be completed due to a technical failure,
     * such as a timeout, network issue, or unexpected server error.
     */
    FAILED
}
