package com.spin.transaction.infrastructure.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.spin.transaction.domain.exception.ProviderCommunicationException;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionRequest;
import com.spin.transaction.infrastructure.client.dto.ProviderTransactionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP client responsible for sending transactions to the external provider.
 */
@Component
public class TransactionProviderClient {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionProviderClient.class);
    private final RestClient restClient;

    public TransactionProviderClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Sends a transaction request to the external provider.
     *
     * @param request provider-specific transaction request
     * @return provider-specific transaction response
     * @throws ProviderCommunicationException when the provider cannot be reached
     *         or returns an invalid technical response
     */
    public ProviderTransactionResponse process(
            ProviderTransactionRequest request) {

        try {
            ProviderTransactionResponse response = restClient.post()
                    .uri("/transactions")
                    .body(request)
                    .retrieve()
                    .body(ProviderTransactionResponse.class);

            if (response == null) {
                throw new ProviderCommunicationException(
                        "External provider returned an empty response");
            }

            return response;

        } catch (RestClientException exception) {
            LOGGER.error(
                    "Error calling external provider: {}",
                    exception.getMessage(),
                    exception);
            throw new ProviderCommunicationException(
                    "Failed to process transaction with external provider",
                    exception);
        }
    }
}