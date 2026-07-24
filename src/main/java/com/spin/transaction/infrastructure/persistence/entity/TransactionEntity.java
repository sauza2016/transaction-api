package com.spin.transaction.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity that represents the TRANSACTIONS table.
 *
 * This class belongs to the infrastructure layer because it contains
 * persistence-specific annotations.
 *
 * The domain model must remain independent from JPA.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {

	/**
	 * Unique identifier of the transaction.
	 */
	@Id
	private UUID id;

	/**
	 * Account that owns the transaction.
	 */
	@Column(nullable = false, length = 50)
	private String accountId;

	/**
	 * Transaction type (CREDIT or DEBIT).
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionType type;

	/**
	 * Transaction amount.
	 */
	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal amount;

	/**
	 * Currency used in the transaction.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 5)
	private Currency currency;

	/**
	 * Optional transaction description.
	 */
	@Column(length = 255)
	private String description;

	/**
	 * Final transaction status.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TransactionStatus status;

	/**
	 * Transaction identifier returned by the external provider.
	 */
	@Column(length = 100)
	private String providerTransactionId;

	/**
	 * Account balance returned by the provider after execution.
	 */
	@Column(precision = 19, scale = 2)
	private BigDecimal balanceAfter;

	/**
	 * Creation timestamp in UTC.
	 */
	@Column(nullable = false, updatable = false)
	private Instant createdAt;

}