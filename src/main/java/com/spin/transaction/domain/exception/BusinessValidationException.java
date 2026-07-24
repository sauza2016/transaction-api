package com.spin.transaction.domain.exception;
/**
 * Exception thrown when a transaction violates a business rule.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}