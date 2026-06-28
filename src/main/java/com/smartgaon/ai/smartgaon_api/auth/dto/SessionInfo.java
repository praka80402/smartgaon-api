package com.smartgaon.ai.smartgaon_api.auth.dto;

/**
 * One active session, for a "logged-in devices" screen.
 * lastUsedAt is an epoch-second timestamp (when the session last refreshed).
 */
public record SessionInfo(
        String sessionId,
        String platform,
        long lastUsedAt
) {}