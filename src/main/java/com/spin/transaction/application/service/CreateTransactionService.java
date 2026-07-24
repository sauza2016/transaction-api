package com.spin.transaction.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.spin.transaction.application.port.in.CreateTransactionUseCase;
import com.spin.transaction.application.port.out.TransactionProviderPort;
import com.spin.transaction.application.port.out.TransactionRepositoryPort;
import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionType;
import com.spin.transaction.domain.exception.BusinessValidationException;
import com.spin.transaction.domain.model.ProviderTransactionResult;
import com.spin.transaction.domain.model.Transaction;

/**
 * Application service responsible for coordinating the transaction creation
 * workflow.
 *
 * <p>
 * This service validates the business rules, sends the transaction to the
 * external provider and persists the processed result.
 * </p>
 */
@Service
public class CreateTransactionService implements CreateTransactionUseCase {

	private static final BigDecimal MINIMUM_AMOUNT = new BigDecimal("1.00");

	private static final BigDecimal MAXIMUM_DEBIT_AMOUNT = new BigDecimal("10000.00");

	private final TransactionProviderPort transactionProviderPort;
	private final TransactionRepositoryPort transactionRepositoryPort;

	public CreateTransactionService(TransactionProviderPort transactionProviderPort,
			TransactionRepositoryPort transactionRepositoryPort) {

		this.transactionProviderPort = transactionProviderPort;
		this.transactionRepositoryPort = transactionRepositoryPort;
	}

	/**
	 * Executes the complete transaction creation workflow.
	 *
	 * @param transaction transaction data received by the application
	 * @return processed transaction persisted in the database
	 * @throws BusinessValidationException when a business rule is violated
	 */
	@Override
	public Transaction create(Transaction transaction) {

		validate(transaction);

		Transaction transactionToProcess = Transaction.builder().id(UUID.randomUUID())
				.accountId(transaction.getAccountId()).type(transaction.getType()).amount(transaction.getAmount())
				.currency(transaction.getCurrency()).description(transaction.getDescription()).createdAt(Instant.now())
				.build();

		ProviderTransactionResult providerResult = transactionProviderPort.process(transactionToProcess);

		Transaction processedTransaction = Transaction.builder().id(transactionToProcess.getId())
				.accountId(transactionToProcess.getAccountId()).type(transactionToProcess.getType())
				.amount(transactionToProcess.getAmount()).currency(transactionToProcess.getCurrency())
				.description(transactionToProcess.getDescription()).status(providerResult.getStatus())
				.providerTransactionId(providerResult.getProviderTransactionId())
				.balanceAfter(providerResult.getBalanceAfter()).createdAt(transactionToProcess.getCreatedAt()).build();

		return transactionRepositoryPort.save(processedTransaction);
	}

	/**
	 * Validates the business rules required before sending the transaction to the
	 * external provider.
	 *
	 * @param transaction transaction to validate
	 * @throws BusinessValidationException when any validation fails
	 */
	private void validate(Transaction transaction) {

		if (transaction == null) {
			throw new BusinessValidationException("Transaction must not be null");
		}

		if (transaction.getAmount() == null) {
			throw new BusinessValidationException("Amount must not be null");
		}

		if (transaction.getAmount().compareTo(MINIMUM_AMOUNT) <= 0) {

			throw new BusinessValidationException("Amount must be greater than 1");
		}

		if (transaction.getCurrency() == null) {
			throw new BusinessValidationException("Currency must not be null");
		}


		if (transaction.getType() == TransactionType.DEBIT
				&& transaction.getAmount().compareTo(MAXIMUM_DEBIT_AMOUNT) > 0) {

			throw new BusinessValidationException("Debit amount must not exceed 10000");
		}
	}
	
	
}