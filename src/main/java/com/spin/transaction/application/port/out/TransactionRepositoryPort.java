package com.spin.transaction.application.port.out;

import java.util.List;

import com.spin.transaction.domain.model.Transaction;

/**
 * Output port used by the application layer to persist and retrieve
 * transactions.
 *
 * The application layer depends on this abstraction instead of depending
 * directly on Spring Data JPA.
 */
public interface TransactionRepositoryPort {

    /**
     * Persists a transaction.
     *
     * @param transaction transaction to persist
     * @return persisted transaction
     */
    Transaction save(Transaction transaction);

    /**
     * Retrieves all persisted transactions.
     *
     * @return persisted transactions
     */
    List<Transaction> findAll();
}