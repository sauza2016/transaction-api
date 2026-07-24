package com.spin.transaction.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * HTTP response returned after processing a transaction.
 */
public record TransactionResponse(
        @Schema(
                description = "Internal transaction identifier",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,
        
        @Schema(
                description = "Account identifier",
                example = "ACC-10001"
        )
        String accountId,
        
        @Schema(
                description = "Transaction type",
                example = "DEBIT"
        )
        TransactionType type,
        
        @Schema(
                description = "Transaction amount",
                example = "1500.00"
        )
        BigDecimal amount,
        
        @Schema(
                description = "Transaction currency",
                example = "MXN"
        )
        Currency currency,
        
        @Schema(
                description = "Transaction description",
                example = "Monthly service payment"
        )
        String description,
        
        @Schema(
                description = "Final processing status",
                example = "APPROVED"
        )
        TransactionStatus status,
        
        @Schema(
                description = "Identifier returned by the external provider",
                example = "PROV-938475"
        )
        String providerTransactionId,
        
        @Schema(
                description = "Account balance after processing",
                example = "18500.00"
        )
        BigDecimal balanceAfter,
        
        @Schema(
                description = "Transaction creation timestamp in UTC",
                example = "2026-07-24T19:30:00Z"
        )
        Instant createdAt) {
} 