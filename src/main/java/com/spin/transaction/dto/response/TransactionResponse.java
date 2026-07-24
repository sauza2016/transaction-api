package com.spin.transaction.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;

/**
 * HTTP response returned after processing a transaction.
 */
public record TransactionResponse(
        UUID id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        String description,
        TransactionStatus status,
        String providerTransactionId,
        BigDecimal balanceAfter,
        Instant createdAt) {
} 