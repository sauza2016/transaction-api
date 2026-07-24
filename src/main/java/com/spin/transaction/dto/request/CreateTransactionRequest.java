package com.spin.transaction.dto.request;

import java.math.BigDecimal;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * HTTP request used to create a transaction.
 */
public record CreateTransactionRequest(

        @Schema(
                description = "Account identifier associated with the transaction",
                example = "ACC-10001"
        )
        @NotBlank(message = "Account ID is required")
        @Size(max = 100, message = "Account ID must not exceed 100 characters")
        String accountId,

        @Schema(
                description = "Transaction type",
                example = "DEBIT"
        )
        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @Schema(
                description = "Transaction amount",
                example = "1500.00",
                minimum = "1.01"
        )
        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "1.00",
                inclusive = false,
                message = "Amount must be greater than 1")
        BigDecimal amount,

        @Schema(
                description = "Currency used by the transaction",
                example = "MXN"
        )
        @NotNull(message = "Currency is required")
        Currency currency,

        @Schema(
                description = "Optional description of the transaction",
                example = "Monthly service payment"
        )
        @Size(
                max = 255,
                message = "Description must not exceed 255 characters")
        String description) {
}