package com.smartgaon.ai.smartgaon_api.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends RuntimeException {

    private final int limit;
    private final long used;

    public RateLimitExceededException(int limit, long used) {
        super("Daily limit exceeded");
        this.limit = limit;
        this.used = used;
    }

    public int getLimit() {
        return limit;
    }

    public long getUsed() {
        return used;
    }

    public HttpStatus getStatus() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }
}
