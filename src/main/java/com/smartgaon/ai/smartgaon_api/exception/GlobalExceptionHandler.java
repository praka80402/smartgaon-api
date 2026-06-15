package com.smartgaon.ai.smartgaon_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<?> handleRateLimit(RateLimitExceededException ex) {

        // ✅ business warning log
        log.warn("Rate limit exceeded: used={}, limit={}", ex.getUsed(), ex.getLimit());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", "RATE_LIMIT_EXCEEDED");
        body.put("message", "You have reached your daily career guide limit");
        body.put("used", ex.getUsed());
        body.put("limit", ex.getLimit());
        body.put("retry_after", "Tomorrow");
        body.put("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("error", "NOT_FOUND");
    body.put("message", ex.getMessage());
    body.put("timestamp", Instant.now());

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(body);
}

    @ExceptionHandler(com.smartgaon.ai.smartgaon_api.sewa.jobs.AlreadyAppliedException.class)
    public ResponseEntity<?> handleAlreadyApplied(com.smartgaon.ai.smartgaon_api.sewa.jobs.AlreadyAppliedException ex) {

        log.warn("Job application conflict: {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", "ALREADY_APPLIED");
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", ex.getStatusCode().toString());
        body.put("message", ex.getReason() != null ? ex.getReason() : "Request failed");
        body.put("timestamp", Instant.now());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(body);
    }

    // ✅ generic fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {

        // ✅ proper error log with stack trace
        log.error("Unhandled exception occurred", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", "INTERNAL_ERROR");
        body.put("message", "Something went wrong. Please try again later.");
        body.put("timestamp", Instant.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}
