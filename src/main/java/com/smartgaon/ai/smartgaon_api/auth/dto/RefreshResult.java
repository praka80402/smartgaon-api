package com.smartgaon.ai.smartgaon_api.auth.dto;

/**
 * Returned by RefreshTokenService on issue() and rotate().
 *
 * - userId / sessionId : feed these into your JwtUtil so the access token
 *   carries the sessionId (sid) claim.
 * - refreshToken       : the raw opaque token to hand back to the client
 *   (httpOnly cookie for web, expo-secure-store for React Native).
 */
public record RefreshResult(
        String userId,
        String sessionId,
        String refreshToken
) {}