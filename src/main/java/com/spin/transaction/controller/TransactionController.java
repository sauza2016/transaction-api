package com.spin.transaction.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spin.transaction.application.port.in.CreateTransactionUseCase;
import com.spin.transaction.application.port.in.GetTransactionsUseCase;
import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.dto.request.CreateTransactionRequest;
import com.spin.transaction.dto.response.ErrorResponse;
import com.spin.transaction.dto.response.TransactionResponse;
import com.spin.transaction.mapper.TransactionApiMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST adapter that exposes transaction operations over HTTP.
 */
@RestController
@RequestMapping("/transactions")
@Tag(
        name = "Transactions",
        description = "Operations for creating and retrieving transactions"
)
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final TransactionApiMapper mapper;
    private final GetTransactionsUseCase getTransactionsUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            GetTransactionsUseCase getTransactionsUseCase,
            TransactionApiMapper mapper) {

        this.createTransactionUseCase = createTransactionUseCase;
        this.mapper = mapper;
        this.getTransactionsUseCase = getTransactionsUseCase;
    }

    /**
     * Creates and processes a new transaction.
     *
     * @param request transaction information received through HTTP
     * @return the processed transaction
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "Create a transaction",
            description = """
                    Validates and processes a new transaction through the external
                    transaction provider and persists the resulting transaction.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transaction created and processed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = TransactionResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or business validation failure",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "External transaction provider is unavailable",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {

        Transaction transaction = mapper.toDomain(request);

        Transaction createdTransaction =
                createTransactionUseCase.create(transaction);

        TransactionResponse response =
                mapper.toResponse(createdTransaction);

        URI location = URI.create(
                "/transactions/" + createdTransaction.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    /**
     * Retrieves all persisted transactions.
     *
     * @return list of transaction responses
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Retrieve transactions",
            description = """
                    Retrieves all persisted transactions ordered from newest
                    to oldest.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = TransactionResponse.class
                                    )
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<List<TransactionResponse>> getTransactions() {

        List<TransactionResponse> response =
                getTransactionsUseCase.getAll()
                        .stream()
                        .map(mapper::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }
}