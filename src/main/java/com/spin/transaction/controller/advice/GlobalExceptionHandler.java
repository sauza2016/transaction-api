package com.spin.transaction.controller.advice;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import com.spin.transaction.domain.exception.BusinessValidationException;
import com.spin.transaction.domain.exception.ProviderCommunicationException;
import com.spin.transaction.dto.response.ErrorResponse;

/**
 * Global REST exception handler responsible for converting application
 * exceptions into consistent HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles violations of business rules.
     *
     * @param exception business validation exception
     * @param request current HTTP request
     * @return HTTP 400 response with the business error
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ErrorResponse> handleBusinessValidation(
            BusinessValidationException exception,
            ServletWebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response = buildErrorResponse(
                status,
                exception.getMessage(),
                request.getRequest().getRequestURI(),
                List.of());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handles validation errors produced by Jakarta Bean Validation.
     *
     * @param exception request validation exception
     * @param request current HTTP request
     * @return HTTP 400 response containing all invalid fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleRequestValidation(
            MethodArgumentNotValidException exception,
            ServletWebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        ErrorResponse response = buildErrorResponse(
                status,
                "Request validation failed",
                request.getRequest().getRequestURI(),
                details);

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handles technical communication errors with the external provider.
     *
     * @param exception provider communication exception
     * @param request current HTTP request
     * @return HTTP 502 response
     */
    @ExceptionHandler(ProviderCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleProviderCommunication(
            ProviderCommunicationException exception,
            ServletWebRequest request) {

        HttpStatus status = HttpStatus.BAD_GATEWAY;

        ErrorResponse response = buildErrorResponse(
                status,
                exception.getMessage(),
                request.getRequest().getRequestURI(),
                List.of());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Handles unexpected errors without exposing internal implementation
     * details to API clients.
     *
     * @param exception unexpected exception
     * @param request current HTTP request
     * @return HTTP 500 response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(
            Exception exception,
            ServletWebRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = buildErrorResponse(
                status,
                "An unexpected error occurred",
                request.getRequest().getRequestURI(),
                List.of());

        return ResponseEntity
                .status(status)
                .body(response);
    }

    /**
     * Creates the common error response used by all handlers.
     */
    private ErrorResponse buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            List<String> details) {

        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details);
    }

    /**
     * Formats a field validation error into a readable message.
     */
    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField()
                + ": "
                + fieldError.getDefaultMessage();
    }
}