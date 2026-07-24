package com.spin.transaction.dto.response;

import java.time.Instant;
import java.util.List;

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
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {
}