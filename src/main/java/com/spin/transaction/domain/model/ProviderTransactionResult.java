package com.spin.transaction.domain.model;

import java.math.BigDecimal;

import com.spin.transaction.domain.enums.TransactionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProviderTransactionResult {

    private TransactionStatus status;

    private String providerTransactionId;

    private BigDecimal balanceAfter;

    private String message;
}