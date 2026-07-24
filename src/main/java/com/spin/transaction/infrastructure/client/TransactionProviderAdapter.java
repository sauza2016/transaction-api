package com.spin.transaction.infrastructure.client;

import org.springframework.stereotype.Component;

import com.spin.transaction.application.port.out.TransactionProviderPort;
import com.spin.transaction.domain.enums.TransactionStatus;
import com.spin.transaction.domain.model.ProviderTransactionResult;
import com.spin.transaction.domain.model.Transaction;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionRequest;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionResponse;

@Component
public class TransactionProviderAdapter
        implements TransactionProviderPort {

    private final TransactionProviderClient client;

    public TransactionProviderAdapter(TransactionProviderClient client) {
        this.client = client;
    }

    @Override
    public ProviderTransactionResult process(Transaction transaction) {

        ProviderTransactionRequest request =
                new ProviderTransactionRequest(
                        transaction.getAccountId(),
                        transaction.getType(),
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getDescription());

        ProviderTransactionResponse response =
                client.process(request);

        TransactionStatus status = response.approved()
                ? TransactionStatus.EXECUTED
                : TransactionStatus.REJECTED;

        return ProviderTransactionResult.builder()
                .status(status)
                .providerTransactionId(response.transactionId())
                .balanceAfter(response.balanceAfter())
                .message(response.message())
                .build();
    }
    
    
}