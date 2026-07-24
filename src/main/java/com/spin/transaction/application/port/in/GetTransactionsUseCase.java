package com.spin.transaction.application.port.in;

import java.util.List;

import com.spin.transaction.domain.model.Transaction;

/**
 * Input port that defines the operation for retrieving transactions.
 */
public interface GetTransactionsUseCase {

    /**
     * Retrieves all persisted transactions.
     *
     * @return list of transactions
     */
    List<Transaction> getAll();
}