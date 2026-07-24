package com.spin.transaction.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

	private UUID id;

	private String accountId;

	private TransactionType type;

	private BigDecimal amount;

	private Currency currency;

	private String description;

	private TransactionStatus status;

	private String providerTransactionId;

	private BigDecimal balanceAfter;

	private Instant createdAt;
}
