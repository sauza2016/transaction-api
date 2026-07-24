package com.spin.transaction.mapper;

import org.springframework.stereotype.Component;

import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.dto.request.CreateTransactionRequest;
import com.spin.transaction.dto.response.TransactionResponse;

/**
 * Maps REST request and response objects to and from the domain model.
 */
@Component
public class TransactionApiMapper {

    /**
     * Converts an HTTP creation request into a domain transaction.
     *
     * @param request HTTP request
     * @return domain transaction
     */
    public Transaction toDomain(CreateTransactionRequest request) {

        return Transaction.builder()
                .accountId(request.accountId())
                .type(request.type())
                .amount(request.amount())
                .currency(request.currency())
                .description(request.description())
                .build();
    }

    /**
     * Converts a domain transaction into an HTTP response.
     *
     * @param transaction domain transaction
     * @return HTTP response
     */
    public TransactionResponse toResponse(Transaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getProviderTransactionId(),
                transaction.getBalanceAfter(),
                transaction.getCreatedAt());
    }
}