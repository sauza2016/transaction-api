package com.spin.transaction.infrastructure.client.dto;

import java.math.BigDecimal;

public record ProviderTransactionResponse(
        String transactionId,
        boolean approved,
        BigDecimal balanceAfter,
        String message) {
}