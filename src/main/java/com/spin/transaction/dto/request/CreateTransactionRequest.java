package com.spin.transaction.dto.request;

import java.math.BigDecimal;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * HTTP request used to create a transaction.
 */
public record CreateTransactionRequest(

        @NotBlank(message = "Account ID is required")
        @Size(max = 100, message = "Account ID must not exceed 100 characters")
        String accountId,

        @NotNull(message = "Transaction type is required")
        TransactionType type,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "1.00",
                inclusive = false,
                message = "Amount must be greater than 1")
        BigDecimal amount,

        @NotNull(message = "Currency is required")
        Currency currency,

        @Size(
                max = 255,
                message = "Description must not exceed 255 characters")
        String description) {
}