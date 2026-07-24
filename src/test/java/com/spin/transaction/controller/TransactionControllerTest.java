package com.spin.transaction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;
import com.spin.transaction.application.port.in.CreateTransactionUseCase;
import com.spin.transaction.application.port.in.GetTransactionsUseCase;
import com.spin.transaction.controller.advice.GlobalExceptionHandler;
import com.spin.transaction.domain.enums.Currency;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.enums.TransactionType;
import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.dto.request.CreateTransactionRequest;
import com.spin.transaction.dto.response.TransactionResponse;
import com.spin.transaction.mapper.TransactionApiMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;

import static org.mockito.Mockito.never;

import com.spin.transaction.domain.exception.BusinessValidationException;
import com.spin.transaction.domain.exception.ProviderCommunicationException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JsonMapper jsonMapper;

	@MockitoBean
	private CreateTransactionUseCase createTransactionUseCase;

	@MockitoBean
	private GetTransactionsUseCase getTransactionsUseCase;

	@MockitoBean
	private TransactionApiMapper mapper;

	@Test
	void shouldReturnCreatedWhenTransactionIsCreatedSuccessfully() throws Exception {

		// Arrange
		CreateTransactionRequest request = new CreateTransactionRequest("ACC-100", TransactionType.CREDIT,
				new BigDecimal("2500.00"), Currency.MXN, "Deposit");

		UUID transactionId = UUID.randomUUID();

		Transaction transactionToCreate = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("2500.00")).currency(Currency.MXN).description("Deposit").build();

		Transaction createdTransaction = Transaction.builder().id(transactionId).accountId("ACC-100")
				.type(TransactionType.CREDIT).amount(new BigDecimal("2500.00")).currency(Currency.MXN)
				.description("Deposit").status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-001")
				.balanceAfter(new BigDecimal("12500.00")).createdAt(Instant.now()).build();

		TransactionResponse response = new TransactionResponse(transactionId, "ACC-100", TransactionType.CREDIT,
				new BigDecimal("2500.00"), Currency.MXN, "Deposit", TransactionStatus.EXECUTED, "PROVIDER-TX-001",
				new BigDecimal("12500.00"), createdTransaction.getCreatedAt());

		when(mapper.toDomain(any(CreateTransactionRequest.class))).thenReturn(transactionToCreate);

		when(createTransactionUseCase.create(transactionToCreate)).thenReturn(createdTransaction);

		when(mapper.toResponse(createdTransaction)).thenReturn(response);

		// Act and Assert
		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(header().string("Location", "/transactions/" + transactionId))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(transactionId.toString()))
				.andExpect(jsonPath("$.status").value("EXECUTED"))
				.andExpect(jsonPath("$.providerTransactionId").value("PROVIDER-TX-001"));

		verify(mapper).toDomain(any(CreateTransactionRequest.class));
		verify(createTransactionUseCase).create(transactionToCreate);
		verify(mapper).toResponse(createdTransaction);
	}

	@Test
	void shouldCreateTransactionSuccessfully() throws Exception {

		// Arrange
		UUID transactionId = UUID.randomUUID();
		Instant createdAt = Instant.now();

		CreateTransactionRequest request = new CreateTransactionRequest("ACC-100", TransactionType.CREDIT,
				new BigDecimal("2500.00"), Currency.MXN, "Deposit");

		Transaction domainRequest = Transaction.builder().accountId("ACC-100").type(TransactionType.CREDIT)
				.amount(new BigDecimal("2500.00")).currency(Currency.MXN).description("Deposit").build();

		Transaction createdTransaction = Transaction.builder().id(transactionId).accountId("ACC-100")
				.type(TransactionType.CREDIT).amount(new BigDecimal("2500.00")).currency(Currency.MXN)
				.description("Deposit").status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-001")
				.balanceAfter(new BigDecimal("12500.00")).createdAt(createdAt).build();

		TransactionResponse response = new TransactionResponse(transactionId, "ACC-100", TransactionType.CREDIT,
				new BigDecimal("2500.00"), Currency.MXN, "Deposit", TransactionStatus.EXECUTED, "PROVIDER-TX-001",
				new BigDecimal("12500.00"), createdAt);

		when(mapper.toDomain(any(CreateTransactionRequest.class))).thenReturn(domainRequest);

		when(createTransactionUseCase.create(domainRequest)).thenReturn(createdTransaction);

		when(mapper.toResponse(createdTransaction)).thenReturn(response);

		// Act & Assert
		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(header().string("Location", "/transactions/" + transactionId))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(transactionId.toString()))
				.andExpect(jsonPath("$.accountId").value("ACC-100")).andExpect(jsonPath("$.status").value("EXECUTED"))
				.andExpect(jsonPath("$.providerTransactionId").value("PROVIDER-TX-001"));

		verify(mapper).toDomain(any(CreateTransactionRequest.class));
		verify(createTransactionUseCase).create(domainRequest);
		verify(mapper).toResponse(createdTransaction);
	}

	@Test
	void shouldReturnBadRequestWhenAmountIsNull() throws Exception {

		// Arrange
		CreateTransactionRequest request = new CreateTransactionRequest("ACC-100", TransactionType.CREDIT, null,
				Currency.MXN, "Deposit");

		// Act & Assert
		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verifyNoInteractions(createTransactionUseCase, mapper);
	}

	@Test
	void shouldReturnBadRequestWhenAmountIsEqualToMinimum() throws Exception {

		// Arrange
		CreateTransactionRequest request = new CreateTransactionRequest("ACC-100", TransactionType.CREDIT,
				new BigDecimal("1.00"), Currency.MXN, "Deposit");

		// Act & Assert
		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verifyNoInteractions(createTransactionUseCase, mapper);
	}

	@Test
	void shouldReturnBadRequestWhenAccountIdIsBlank() throws Exception {

		// Arrange
		CreateTransactionRequest request = new CreateTransactionRequest(" ", TransactionType.CREDIT,
				new BigDecimal("100.00"), Currency.MXN, "Deposit");

		// Act & Assert
		mockMvc.perform(post("/transactions").contentType(MediaType.APPLICATION_JSON)
				.content(jsonMapper.writeValueAsString(request))).andExpect(status().isBadRequest());

		verifyNoInteractions(createTransactionUseCase, mapper);
	}

	@Test
	void shouldReturnTransactionsSuccessfully() throws Exception {

		// Arrange
		UUID transactionId = UUID.randomUUID();
		Instant createdAt = Instant.now();

		Transaction transaction = Transaction.builder().id(transactionId).accountId("ACC-100")
				.type(TransactionType.CREDIT).amount(new BigDecimal("2500.00")).currency(Currency.MXN)
				.description("Deposit").status(TransactionStatus.EXECUTED).providerTransactionId("PROVIDER-TX-001")
				.balanceAfter(new BigDecimal("12500.00")).createdAt(createdAt).build();

		TransactionResponse response = new TransactionResponse(transactionId, "ACC-100", TransactionType.CREDIT,
				new BigDecimal("2500.00"), Currency.MXN, "Deposit", TransactionStatus.EXECUTED, "PROVIDER-TX-001",
				new BigDecimal("12500.00"), createdAt);

		when(getTransactionsUseCase.getAll()).thenReturn(List.of(transaction));

		when(mapper.toResponse(transaction)).thenReturn(response);

		// Act & Assert
		mockMvc.perform(get("/transactions").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(transactionId.toString()))
				.andExpect(jsonPath("$[0].accountId").value("ACC-100"))
				.andExpect(jsonPath("$[0].status").value("EXECUTED"))
				.andExpect(jsonPath("$[0].providerTransactionId").value("PROVIDER-TX-001"));

		verify(getTransactionsUseCase).getAll();
		verify(mapper).toResponse(transaction);
	}

	@Test
	void shouldReturnEmptyListWhenNoTransactionsExist() throws Exception {

		// Arrange
		when(getTransactionsUseCase.getAll()).thenReturn(List.of());

		// Act & Assert
		mockMvc.perform(get("/transactions").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$.length()").value(0));

		verify(getTransactionsUseCase).getAll();
		verifyNoInteractions(mapper);
	}
	
	@Test
	void shouldReturnBadRequestWhenBusinessValidationFails()
	        throws Exception {

	    // Arrange
	    CreateTransactionRequest request =
	            new CreateTransactionRequest(
	                    "ACC-100",
	                    TransactionType.DEBIT,
	                    new BigDecimal("15000.00"),
	                    Currency.MXN,
	                    "Debit transaction"
	            );

	    Transaction domainRequest =
	            Transaction.builder()
	                    .accountId("ACC-100")
	                    .type(TransactionType.DEBIT)
	                    .amount(new BigDecimal("15000.00"))
	                    .currency(Currency.MXN)
	                    .description("Debit transaction")
	                    .build();

	    when(mapper.toDomain(any(CreateTransactionRequest.class)))
	            .thenReturn(domainRequest);

	    when(createTransactionUseCase.create(domainRequest))
	            .thenThrow(new BusinessValidationException(
	                    "Debit amount must not exceed 10000"
	            ));

	    // Act & Assert
	    mockMvc.perform(
	                    post("/transactions")
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .content(jsonMapper.writeValueAsString(request))
	            )
	            .andExpect(status().isBadRequest())
	            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
	            .andExpect(jsonPath("$.status").value(400))
	            .andExpect(jsonPath("$.error").value("Bad Request"))
	            .andExpect(jsonPath("$.message")
	                    .value("Debit amount must not exceed 10000"))
	            .andExpect(jsonPath("$.path")
	                    .value("/transactions"))
	            .andExpect(jsonPath("$.details").isArray())
	            .andExpect(jsonPath("$.details").isEmpty())
	            .andExpect(jsonPath("$.timestamp").exists());

	    verify(mapper).toDomain(any(CreateTransactionRequest.class));
	    verify(createTransactionUseCase).create(domainRequest);
	    verify(mapper, never()).toResponse(any(Transaction.class));
	}
	
	@Test
	void shouldReturnBadGatewayWhenProviderCommunicationFails()
	        throws Exception {

	    // Arrange
	    CreateTransactionRequest request =
	            new CreateTransactionRequest(
	                    "ACC-100",
	                    TransactionType.CREDIT,
	                    new BigDecimal("2500.00"),
	                    Currency.MXN,
	                    "Deposit"
	            );

	    Transaction domainRequest =
	            Transaction.builder()
	                    .accountId("ACC-100")
	                    .type(TransactionType.CREDIT)
	                    .amount(new BigDecimal("2500.00"))
	                    .currency(Currency.MXN)
	                    .description("Deposit")
	                    .build();

	    when(mapper.toDomain(any(CreateTransactionRequest.class)))
	            .thenReturn(domainRequest);

	    when(createTransactionUseCase.create(domainRequest))
	            .thenThrow(new ProviderCommunicationException(
	                    "Failed to process transaction with external provider"
	            ));

	    // Act & Assert
	    mockMvc.perform(
	                    post("/transactions")
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .content(jsonMapper.writeValueAsString(request))
	            )
	            .andExpect(status().isBadGateway())
	            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
	            .andExpect(jsonPath("$.status").value(502))
	            .andExpect(jsonPath("$.error").value("Bad Gateway"))
	            .andExpect(jsonPath("$.message").value(
	                    "Failed to process transaction with external provider"
	            ))
	            .andExpect(jsonPath("$.path")
	                    .value("/transactions"))
	            .andExpect(jsonPath("$.details").isEmpty())
	            .andExpect(jsonPath("$.timestamp").exists());

	    verify(mapper).toDomain(any(CreateTransactionRequest.class));
	    verify(createTransactionUseCase).create(domainRequest);
	    verify(mapper, never()).toResponse(any(Transaction.class));
	}
	
	@Test
	void shouldReturnInternalServerErrorWhenUnexpectedErrorOccurs()
	        throws Exception {

	    // Arrange
	    CreateTransactionRequest request =
	            new CreateTransactionRequest(
	                    "ACC-100",
	                    TransactionType.CREDIT,
	                    new BigDecimal("2500.00"),
	                    Currency.MXN,
	                    "Deposit"
	            );

	    Transaction domainRequest =
	            Transaction.builder()
	                    .accountId("ACC-100")
	                    .type(TransactionType.CREDIT)
	                    .amount(new BigDecimal("2500.00"))
	                    .currency(Currency.MXN)
	                    .description("Deposit")
	                    .build();

	    when(mapper.toDomain(any(CreateTransactionRequest.class)))
	            .thenReturn(domainRequest);

	    when(createTransactionUseCase.create(domainRequest))
	            .thenThrow(new RuntimeException(
	                    "Database connection failed"
	            ));

	    // Act & Assert
	    mockMvc.perform(
	                    post("/transactions")
	                            .contentType(MediaType.APPLICATION_JSON)
	                            .content(jsonMapper.writeValueAsString(request))
	            )
	            .andExpect(status().isInternalServerError())
	            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
	            .andExpect(jsonPath("$.status").value(500))
	            .andExpect(jsonPath("$.error")
	                    .value("Internal Server Error"))
	            .andExpect(jsonPath("$.message")
	                    .value("An unexpected error occurred"))
	            .andExpect(jsonPath("$.path")
	                    .value("/transactions"))
	            .andExpect(jsonPath("$.details").isEmpty())
	            .andExpect(jsonPath("$.timestamp").exists());

	    verify(mapper).toDomain(any(CreateTransactionRequest.class));
	    verify(createTransactionUseCase).create(domainRequest);
	    verify(mapper, never()).toResponse(any(Transaction.class));
	}
}