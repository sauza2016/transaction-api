package com.spin.transaction.dto.response;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard error response returned by the REST API.
 *
 * @param timestamp date and time when the error occurred
 * @param status HTTP status code
 * @param error HTTP error name
 * @param message general description of the error
 * @param path request path that produced the error
 * @param details specific validation or processing errors
 */
public record ErrorResponse(
        @Schema(
                description = "Timestamp when the error occurred",
                example = "2026-07-24T19:30:00Z"
        )
        Instant timestamp,
        
        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,
        
        @Schema(
                description = "HTTP error name",
                example = "Bad Request"
        )
        String error,
        
        @Schema(
                description = "Human-readable error message",
                example = "Transaction amount must be greater than 1"
        )
        String message,
        
        @Schema(
                description = "Request path that produced the error",
                example = "/transactions"
        )
        String path,
        
        @Schema(
                description = "Individual validation errors, when applicable",
                example = "[\"amount: must be greater than 0\"]"
        )
        List<String> details) {
}