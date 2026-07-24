package com.spin.transaction.infrastructure.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;
import com.spin.transaction.domain.model.ProviderTransactionResult;
import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionRequest;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionResponse;

@ExtendWith(MockitoExtension.class)
class TransactionProviderAdapterTest {

	@Mock
	private TransactionProviderClient client;

	private TransactionProviderAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new TransactionProviderAdapter(client);
	}

	@Test
	void shouldReturnExecutedResultWhenProviderApprovesTransaction() {

		// Arrange
		Transaction transaction = Transaction.builder().accountId("ACC-100").type(TransactionType.DEBIT)
				.amount(new BigDecimal("1500.00")).currency(Currency.MXN).description("Payment").build();

		ProviderTransactionResponse response = new ProviderTransactionResponse("PROVIDER-TX-001", true,
				new BigDecimal("8500.00"), "Transaction approved");

		when(client.process(any(ProviderTransactionRequest.class))).thenReturn(response);

		// Act
		ProviderTransactionResult result = adapter.process(transaction);

		// Assert
		assertEquals(TransactionStatus.EXECUTED, result.getStatus());

		assertEquals("PROVIDER-TX-001", result.getProviderTransactionId());

		assertEquals(new BigDecimal("8500.00"), result.getBalanceAfter());

		assertEquals("Transaction approved", result.getMessage());

		// Capture the request sent to the client
		ArgumentCaptor<ProviderTransactionRequest> requestCaptor = ArgumentCaptor
				.forClass(ProviderTransactionRequest.class);

		verify(client).process(requestCaptor.capture());

		ProviderTransactionRequest capturedRequest = requestCaptor.getValue();

		assertEquals("ACC-100", capturedRequest.accountId());

		assertEquals(TransactionType.DEBIT, capturedRequest.type());

		assertEquals(new BigDecimal("1500.00"), capturedRequest.amount());

		assertEquals(Currency.MXN, capturedRequest.currency());

		assertEquals("Payment", capturedRequest.description());
	}
}