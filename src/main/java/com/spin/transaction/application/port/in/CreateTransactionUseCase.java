package com.spin.transaction.application.port.in;

import com.spin.transaction.domain.model.Transaction;

/**
 * Input port that defines the operation for creating and processing
 * a transaction.
 *
 * <p>The REST adapter depends on this abstraction instead of depending
 * directly on a concrete application service.</p>
 */
public interface CreateTransactionUseCase {

    /**
     * Validates, processes and persists a transaction.
     *
     * @param transaction transaction data to process
     * @return the processed and persisted transaction
     */
    Transaction create(Transaction transaction);
}