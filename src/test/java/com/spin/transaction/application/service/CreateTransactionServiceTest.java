package com.spin.transaction.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spin.transaction.application.port.out.TransactionProviderPort;
import com.spin.transaction.application.port.out.TransactionRepositoryPort;
import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;
import com.spin.transaction.domain.exception.BusinessValidationException;
import com.spin.transaction.domain.model.ProviderTransactionResult;
import com.spin.transaction.domain.model.Transaction;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Instant;
import java.util.UUID;

/**
 * Unit tests for {@link CreateTransactionService}.
 *
 * <p>
 * These tests verify the business rules and the interaction with the provider
 * and repository ports.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class CreateTransactionServiceTest {

	@Mock
	private TransactionProviderPort transactionProviderPort;

	@Mock
	private TransactionRepositoryPort transactionRepositoryPort;

	private CreateTransactionService createTransactionService;

	@BeforeEach
	void setUp() {
		createTransactionService = new CreateTransactionService(transactionProviderPort, transactionRepositoryPort);
	}

	@Test
	void shouldCreateExecutedTransactionWhenProviderApprovesRequest() {

		// Arrange
		Transaction inputTransaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("2500.00")).currency(Currency.MXN).description("Approved transaction test")
				.build();

		ProviderTransactionResult providerResult = ProviderTransactionResult.builder()
				.status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-001")
				.balanceAfter(new BigDecimal("8500.00")).message("Transaction approved").build();

		when(transactionProviderPort.process(any(Transaction.class))).thenReturn(providerResult);

		when(transactionRepositoryPort.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Transaction result = createTransactionService.create(inputTransaction);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertNotNull(result.getCreatedAt());

		assertEquals("ACC-100", result.getAccountId());
		assertEquals(TransactionType.CREDIT, result.getType());
		assertEquals(new BigDecimal("2500.00"), result.getAmount());
		assertEquals(Currency.MXN, result.getCurrency());
		assertEquals("Approved transaction test", result.getDescription());

		assertEquals(TransactionStatus.EXECUTED, result.getStatus());
		assertEquals("PROVIDER-TX-001", result.getProviderTransactionId());
		assertEquals(new BigDecimal("8500.00"), result.getBalanceAfter());

		verify(transactionProviderPort).process(any(Transaction.class));

		verify(transactionRepositoryPort).save(any(Transaction.class));
	}

	@Test
	void shouldCreateRejectedTransactionWhenProviderRejectsRequest() {

		// Arrange
		Transaction inputTransaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("6000.00")).currency(Currency.MXN).description("Rejected transaction test")
				.build();

		ProviderTransactionResult providerResult = ProviderTransactionResult.builder()
				.status(TransactionStatus.REJECTED).providerTransactionId("PROVIDER-TX-REJECTED-001")
				.balanceAfter(new BigDecimal("3000.00")).message("Transaction rejected by external provider").build();

		when(transactionProviderPort.process(any(Transaction.class))).thenReturn(providerResult);

		when(transactionRepositoryPort.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Transaction result = createTransactionService.create(inputTransaction);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertNotNull(result.getCreatedAt());

		assertEquals(TransactionStatus.REJECTED, result.getStatus());

		assertEquals("PROVIDER-TX-REJECTED-001", result.getProviderTransactionId());

		assertEquals(new BigDecimal("3000.00"), result.getBalanceAfter());

		verify(transactionProviderPort).process(any(Transaction.class));

		verify(transactionRepositoryPort).save(any(Transaction.class));
	}

	@Test
	void shouldThrowExceptionWhenTransactionIsNull() {

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(null));

		// Assert
		assertEquals("Transaction must not be null", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldThrowExceptionWhenAmountIsNull() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.currency(Currency.MXN).description("Amount is null").build();

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(transaction));

		// Assert
		assertEquals("Amount must not be null", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldThrowExceptionWhenAmountIsEqualToMinimum() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("1.00")).currency(Currency.MXN).description("Minimum amount validation").build();

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(transaction));

		// Assert
		assertEquals("Amount must be greater than 1", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldThrowExceptionWhenAmountIsLessThanMinimum() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("0.50")).currency(Currency.MXN).description("Amount below minimum").build();

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(transaction));

		// Assert
		assertEquals("Amount must be greater than 1", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldThrowExceptionWhenDebitAmountExceedsMaximum() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.DEBIT)
				.amount(new BigDecimal("10000.01")).currency(Currency.MXN).description("Debit amount above maximum")
				.build();

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(transaction));

		// Assert
		assertEquals("Debit amount must not exceed 10000", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldThrowExceptionWhenCurrencyIsNull() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("2500.00")).currency(null).description("Transaction without currency").build();

		// Act
		BusinessValidationException exception = assertThrows(BusinessValidationException.class,
				() -> createTransactionService.create(transaction));

		// Assert
		assertEquals("Currency must not be null", exception.getMessage());

		verifyNoInteractions(transactionProviderPort);
		verifyNoInteractions(transactionRepositoryPort);
	}

	@Test
	void shouldCreateTransactionWhenDebitAmountEqualsMaximum() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.DEBIT)
				.amount(new BigDecimal("10000.00")).currency(Currency.MXN)
				.description("Debit at maximum allowed amount").build();

		ProviderTransactionResult providerResult = ProviderTransactionResult.builder()
				.status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-002")
				.balanceAfter(new BigDecimal("5000.00")).message("Transaction approved").build();

		when(transactionProviderPort.process(any(Transaction.class))).thenReturn(providerResult);

		when(transactionRepositoryPort.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Transaction result = createTransactionService.create(transaction);

		// Assert
		assertNotNull(result.getId());
		assertNotNull(result.getCreatedAt());

		assertEquals(TransactionStatus.EXECUTED, result.getStatus());

		assertEquals(new BigDecimal("10000.00"), result.getAmount());

		assertEquals("PROVIDER-TX-002", result.getProviderTransactionId());

		assertEquals(new BigDecimal("5000.00"), result.getBalanceAfter());

		verify(transactionProviderPort).process(any(Transaction.class));

		verify(transactionRepositoryPort).save(any(Transaction.class));
	}

	@Test
	void shouldCreateTransactionWhenCreditAmountExceedsDebitMaximum() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("15000.00")).currency(Currency.MXN).description("Credit above debit maximum")
				.build();

		ProviderTransactionResult providerResult = ProviderTransactionResult.builder()
				.status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-003")
				.balanceAfter(new BigDecimal("25000.00")).message("Transaction approved").build();

		when(transactionProviderPort.process(any(Transaction.class))).thenReturn(providerResult);

		when(transactionRepositoryPort.save(any(Transaction.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		Transaction result = createTransactionService.create(transaction);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getId());
		assertNotNull(result.getCreatedAt());

		assertEquals(TransactionType.CREDIT, result.getType());

		assertEquals(new BigDecimal("15000.00"), result.getAmount());

		assertEquals(TransactionStatus.EXECUTED, result.getStatus());

		verify(transactionProviderPort).process(any(Transaction.class));

		verify(transactionRepositoryPort).save(any(Transaction.class));
	}

	@Test
	void shouldReturnTransactionSavedByRepository() {

		// Arrange
		Transaction request = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("2000.00")).currency(Currency.MXN).description("Repository result test").build();

		ProviderTransactionResult providerResult = ProviderTransactionResult.builder()
				.status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-004")
				.balanceAfter(new BigDecimal("12000.00")).message("Approved").build();

		Transaction savedTransaction = Transaction.builder().id(UUID.randomUUID()).accountId("ACC-100")
				.type(TransactionType.CREDIT).amount(new BigDecimal("2000.00")).currency(Currency.MXN)
				.description("Repository result test").status(TransactionStatus.EXECUTED)
				.providerTransactionId("PROVIDER-TX-004").balanceAfter(new BigDecimal("12000.00"))
				.createdAt(Instant.now()).build();

		when(transactionProviderPort.process(any(Transaction.class))).thenReturn(providerResult);

		when(transactionRepositoryPort.save(any(Transaction.class))).thenReturn(savedTransaction);

		// Act
		Transaction result = createTransactionService.create(request);

		// Assert
		assertSame(savedTransaction, result);

		verify(transactionProviderPort).process(any(Transaction.class));

		verify(transactionRepositoryPort).save(any(Transaction.class));
	}
}