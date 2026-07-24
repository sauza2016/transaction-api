package com.spin.transaction.integration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;
import com.spin.transaction.infrastructure.persistence.entity.TransactionEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionIntegrationTest extends AbstractIntegrationTest {

	@Test
	void shouldCreateExecutedTransactionWhenProviderApprovesRequest() throws Exception {

		WIRE_MOCK_SERVER.stubFor(
				com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/transactions")).willReturn(aResponse()
						.withStatus(200).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("""
								{
								  "transactionId": "provider-tx-001",
								  "approved": true,
								  "balanceAfter": 15000.00,
								  "message": "Transaction approved"
								}
								""")));

		String requestBody = """
				{
				  "accountId": "account-001",
				  "type": "CREDIT",
				  "amount": 500.00,
				  "currency": "MXN",
				  "description": "Integration test transaction"
				}
				""";

		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isCreated())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").isNotEmpty()).andExpect(jsonPath("$.accountId").value("account-001"))
				.andExpect(jsonPath("$.type").value("CREDIT")).andExpect(jsonPath("$.amount").value(500.00))
				.andExpect(jsonPath("$.currency").value("MXN"))
				.andExpect(jsonPath("$.description").value("Integration test transaction"))
				.andExpect(jsonPath("$.status").value("EXECUTED"))
				.andExpect(jsonPath("$.providerTransactionId").value("provider-tx-001"))
				.andExpect(jsonPath("$.balanceAfter").value(15000.00)).andExpect(jsonPath("$.createdAt").isNotEmpty());

		List<TransactionEntity> transactions = repository.findAll();

		assertThat(transactions).hasSize(1);

		TransactionEntity persistedTransaction = transactions.getFirst();

		assertThat(persistedTransaction.getId()).isNotNull();
		assertThat(persistedTransaction.getAccountId()).isEqualTo("account-001");
		assertThat(persistedTransaction.getType()).isEqualTo(TransactionType.CREDIT);
		assertThat(persistedTransaction.getAmount()).isEqualByComparingTo("500.00");
		assertThat(persistedTransaction.getCurrency()).isEqualTo(Currency.MXN);
		assertThat(persistedTransaction.getDescription()).isEqualTo("Integration test transaction");
		assertThat(persistedTransaction.getStatus()).isEqualTo(TransactionStatus.EXECUTED);
		assertThat(persistedTransaction.getProviderTransactionId()).isEqualTo("provider-tx-001");
		assertThat(persistedTransaction.getBalanceAfter()).isEqualByComparingTo("15000.00");
		assertThat(persistedTransaction.getCreatedAt()).isNotNull();

		WIRE_MOCK_SERVER.verify(1, postRequestedFor(urlEqualTo("/transactions")));
	}

	@Test
	void shouldCreateRejectedTransactionWhenProviderRejectsRequest() throws Exception {

		WIRE_MOCK_SERVER.stubFor(
				com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/transactions")).willReturn(aResponse()
						.withStatus(200).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("""
								{
								  "transactionId": "provider-tx-002",
								  "approved": false,
								  "balanceAfter": 7500.00,
								  "message": "Transaction rejected"
								}
								""")));

		String requestBody = """
				{
				  "accountId": "account-002",
				  "type": "DEBIT",
				  "amount": 800.00,
				  "currency": "MXN",
				  "description": "Rejected transaction"
				}
				""";

		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNotEmpty())
				.andExpect(jsonPath("$.accountId").value("account-002"))
				.andExpect(jsonPath("$.status").value("REJECTED"))
				.andExpect(jsonPath("$.providerTransactionId").value("provider-tx-002"))
				.andExpect(jsonPath("$.balanceAfter").value(7500.00));

		List<TransactionEntity> transactions = repository.findAll();

		assertThat(transactions).hasSize(1);

		TransactionEntity persistedTransaction = transactions.getFirst();

		assertThat(persistedTransaction.getStatus()).isEqualTo(TransactionStatus.REJECTED);
		assertThat(persistedTransaction.getProviderTransactionId()).isEqualTo("provider-tx-002");
		assertThat(persistedTransaction.getBalanceAfter()).isEqualByComparingTo("7500.00");

		WIRE_MOCK_SERVER.verify(1, postRequestedFor(urlEqualTo("/transactions")));
	}

	@Test
	void shouldReturnBadGatewayAndNotPersistTransactionWhenProviderFails() throws Exception {

		WIRE_MOCK_SERVER.stubFor(
				com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo("/transactions")).willReturn(aResponse()
						.withStatus(500).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("""
								{
								  "message": "Provider unavailable"
								}
								""")));

		String requestBody = """
				{
				  "accountId": "account-003",
				  "type": "DEBIT",
				  "amount": 300.00,
				  "currency": "MXN",
				  "description": "Provider failure transaction"
				}
				""";

		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadGateway())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(502)).andExpect(jsonPath("$.error").value("Bad Gateway"))
				.andExpect(jsonPath("$.message").value("Failed to process transaction with external provider"))
				.andExpect(jsonPath("$.path").value("/transactions")).andExpect(jsonPath("$.timestamp").isNotEmpty());

		assertThat(repository.count()).isZero();

		WIRE_MOCK_SERVER.verify(1, postRequestedFor(urlEqualTo("/transactions")));
	}

	@Test
	void shouldReturnPersistedTransactionsOrderedByCreationDate() throws Exception {

		TransactionEntity olderTransaction = TransactionEntity.builder().id(UUID.randomUUID()).accountId("account-old")
				.type(TransactionType.CREDIT).amount(new BigDecimal("100.00")).currency(Currency.MXN)
				.description("Older transaction").status(TransactionStatus.EXECUTED)
				.providerTransactionId("provider-old").balanceAfter(new BigDecimal("1000.00"))
				.createdAt(Instant.parse("2026-07-20T10:00:00Z")).build();

		TransactionEntity newerTransaction = TransactionEntity.builder().id(UUID.randomUUID()).accountId("account-new")
				.type(TransactionType.DEBIT).amount(new BigDecimal("200.00")).currency(Currency.MXN)
				.description("Newer transaction").status(TransactionStatus.EXECUTED)
				.providerTransactionId("provider-new").balanceAfter(new BigDecimal("800.00"))
				.createdAt(Instant.parse("2026-07-21T10:00:00Z")).build();

		repository.saveAll(List.of(olderTransaction, newerTransaction));

		mockMvc.perform(get("/transactions").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].accountId").value("account-new"))
				.andExpect(jsonPath("$[0].createdAt").value("2026-07-21T10:00:00Z"))
				.andExpect(jsonPath("$[1].accountId").value("account-old"))
				.andExpect(jsonPath("$[1].createdAt").value("2026-07-20T10:00:00Z"));
	}
}