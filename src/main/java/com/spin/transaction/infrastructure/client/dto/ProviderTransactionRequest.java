package com.spin.transaction.infrastructure.client.dto;

import java.math.BigDecimal;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionType;

public record ProviderTransactionRequest(
        String accountId,
        TransactionType type,
        BigDecimal amount,
        Currency currency,
        String description) {
}