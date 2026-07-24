package com.spin.transaction.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spin.transaction.infrastructure.persistence.entity.TransactionEntity;

/**
 * Spring Data repository used to access transaction records.
 */
public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    /**
     * Retrieves all transactions ordered from newest to oldest.
     *
     * @return ordered transaction entities
     */
    List<TransactionEntity> findAllByOrderByCreatedAtDesc();
}