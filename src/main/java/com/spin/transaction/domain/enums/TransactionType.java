package com.spin.transaction.domain.enums;

/**
 * Represents the type of financial transaction to execute.
 *
 * <ul>
 * <li>CREDIT: Adds funds to the account.</li>
 * <li>DEBIT: Withdraws funds from the account.</li>
 * </ul>
 */
public enum TransactionType {
	/**
	 * Adds funds to the account.
	 */
	CREDIT,
	/**
	 * Withdraws funds from the account.
	 */
	DEBIT
}
