package com.spin.transaction.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spin.transaction.application.port.in.GetTransactionsUseCase;
import com.spin.transaction.application.port.out.TransactionRepositoryPort;
import com.spin.transaction.domain.model.Transaction;

/**
 * Application service responsible for retrieving persisted transactions.
 */
@Service
public class GetTransactionsService implements GetTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;

    public GetTransactionsService(
            TransactionRepositoryPort transactionRepositoryPort) {
        this.transactionRepositoryPort = transactionRepositoryPort;
    }

    /**
     * Retrieves all transactions without modifying database state.
     *
     * @return persisted transactions
     */
    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAll() {
        return transactionRepositoryPort.findAll();
    }
}