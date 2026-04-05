package com.smartgaon.ai.smartgaon_api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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