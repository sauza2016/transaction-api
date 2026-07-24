package com.spin.transaction.mapper;

import org.springframework.stereotype.Component;

import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.infrastructure.persistence.entity.TransactionEntity;

/**
 * Converts between the transaction domain model and its persistence entity.
 *
 * Mapping is kept outside the application services so that business logic
 * does not depend on JPA-specific classes.
 */
@Component
public class TransactionMapper {

    /**
     * Converts a domain transaction into a persistence entity.
     *
     * @param transaction domain transaction
     * @return persistence entity, or null when the input is null
     */
    public TransactionEntity toEntity(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        TransactionEntity entity = new TransactionEntity();

        entity.setId(transaction.getId());
        entity.setAccountId(transaction.getAccountId());
        entity.setType(transaction.getType());
        entity.setAmount(transaction.getAmount());
        entity.setCurrency(transaction.getCurrency());
        entity.setDescription(transaction.getDescription());
        entity.setStatus(transaction.getStatus());
        entity.setProviderTransactionId(
                transaction.getProviderTransactionId());
        entity.setBalanceAfter(transaction.getBalanceAfter());
        entity.setCreatedAt(transaction.getCreatedAt());

        return entity;
    }

    /**
     * Converts a persistence entity into a domain transaction.
     *
     * @param entity persistence entity
     * @return domain transaction, or null when the input is null
     */
    public Transaction toDomain(TransactionEntity entity) {

        if (entity == null) {
            return null;
        }

        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .type(entity.getType())
                .amount(entity.getAmount())
                .currency(entity.getCurrency())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .providerTransactionId(
                        entity.getProviderTransactionId())
                .balanceAfter(entity.getBalanceAfter())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}