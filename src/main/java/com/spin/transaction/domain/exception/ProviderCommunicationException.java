package com.spin.transaction.domain.exception;

/**
 * Exception thrown when the application cannot communicate correctly
 * with the external transaction provider.
 */
public class ProviderCommunicationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ProviderCommunicationException(String message) {
        super(message);
    }

    public ProviderCommunicationException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}