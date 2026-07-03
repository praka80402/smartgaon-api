package com.smartgaon.ai.smartgaon_api.auth.service;

import com.smartgaon.ai.smartgaon_api.auth.dto.RefreshResult;
import com.smartgaon.ai.smartgaon_api.auth.dto.SessionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Redis-backed refresh-token store with mandatory rotation and reuse detection.
 *
 * Key  : refresh:{userId}:{sessionId}
 * Value: Redis HASH { tokenHash, platform, issuedAt, lastUsedAt, familyId }
 * TTL  : refresh-token expiry (FINAL: 30 days)
 *
 * The refresh token handed to the client is opaque and self-describing:
 *   base64url( userId | sessionId | secret )
 * so the client only ever sends one string back; we decode it to find the
 * Redis key, then compare hashes. Only a SHA-256 hash is stored in Redis,
 * never the raw token, so a Redis leak yields nothing usable.
 *
 * This class is self-contained: it depends only on StringRedisTemplate.
 * The access token is still minted by your existing JwtUtil — this service
 * just returns the userId + sessionId so JwtUtil can put sessionId (sid) in
 * the access-token claims.
 */
@Service
public class RefreshTokenService {

    private static final String KEY_PREFIX   = "refresh:";
    private static final String F_TOKEN_HASH = "tokenHash";
    private static final String F_PLATFORM   = "platform";
    private static final String F_ISSUED_AT  = "issuedAt";
    private static final String F_LAST_USED  = "lastUsedAt";
    private static final String F_FAMILY_ID  = "familyId";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private final StringRedisTemplate redis;
    private final Duration refreshTtl;

    public RefreshTokenService(
            StringRedisTemplate redis,
            @Value("${app.jwt.refresh-token-ttl-days:30}") long refreshTtlDays) {
        this.redis = redis;
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    /* ------------------------------------------------------------------ */
    /* Issue: called on a fresh login (mobile OTP or Google).             */
    /* ------------------------------------------------------------------ */
    public RefreshResult issue(String userId, String platform) {
        String sessionId = UUID.randomUUID().toString();
        String familyId  = UUID.randomUUID().toString();
        String rawToken  = buildToken(userId, sessionId);

        String key = key(userId, sessionId);
        long now = Instant.now().getEpochSecond();

        redis.opsForHash().putAll(key, Map.of(
                F_TOKEN_HASH, sha256(rawToken),
                F_PLATFORM,   platform == null ? "unknown" : platform,
                F_ISSUED_AT,  String.valueOf(now),
                F_LAST_USED,  String.valueOf(now),
                F_FAMILY_ID,  familyId
        ));
        redis.expire(key, refreshTtl);

        return new RefreshResult(userId, sessionId, rawToken);
    }

    /* ------------------------------------------------------------------ */
    /* Rotate: called on POST /auth/refresh. Single-use + reuse detection.*/
    /* ------------------------------------------------------------------ */
    public RefreshResult rotate(String rawToken, String platform) {
        String[] parts = decodeToken(rawToken); // [userId, sessionId]
        String userId = parts[0];
        String sessionId = parts[1];
        String key = key(userId, sessionId);

        // Session not in Redis => already revoked / logged out / expired.
        if (Boolean.FALSE.equals(redis.hasKey(key))) {
            throw new InvalidRefreshTokenException("Refresh session not found or expired");
        }

        String storedHash = (String) redis.opsForHash().get(key, F_TOKEN_HASH);
        String presentedHash = sha256(rawToken);

        // REUSE DETECTION: an already-rotated (old) token was replayed.
        // Treat as theft: revoke the whole session (family) and force re-login.
        if (storedHash == null || !constantTimeEquals(storedHash, presentedHash)) {
            redis.delete(key);
            // Optional: publish a security event / notify the user here.
            throw new RefreshTokenReuseException(
                    "Refresh token reuse detected. Session revoked for user " + userId);
        }

        // Valid: rotate to a brand new token, keep the same session + family.
        String familyId = (String) redis.opsForHash().get(key, F_FAMILY_ID);
        String newRaw = buildToken(userId, sessionId);
        long now = Instant.now().getEpochSecond();

        redis.opsForHash().putAll(key, Map.of(
                F_TOKEN_HASH, sha256(newRaw),
                F_LAST_USED,  String.valueOf(now),
                F_PLATFORM,   platform == null ? safe(redis.opsForHash().get(key, F_PLATFORM)) : platform,
                F_FAMILY_ID,  familyId == null ? UUID.randomUUID().toString() : familyId
        ));
        // Sliding expiry: each rotation extends the window by the full TTL.
        // (For a hard absolute cap, compare issuedAt against now and reject.)
        redis.expire(key, refreshTtl);

        return new RefreshResult(userId, sessionId, newRaw);
    }

    /* ------------------------------------------------------------------ */
    /* Logout one device / session.                                       */
    /* ------------------------------------------------------------------ */
    public void revoke(String userId, String sessionId) {
        redis.delete(key(userId, sessionId));
    }

    /** Logout using the opaque refresh token directly (client just sends the token). */
    public void revokeByToken(String rawToken) {
        String[] parts = decodeToken(rawToken); // [userId, sessionId]
        redis.delete(key(parts[0], parts[1]));
    }

    /* ------------------------------------------------------------------ */
    /* Logout all devices for a user.                                     */
    /* ------------------------------------------------------------------ */
    public long revokeAll(String userId) {
        List<String> keys = scanKeys(KEY_PREFIX + userId + ":*");
        if (keys.isEmpty()) return 0;
        Long deleted = redis.delete(keys);
        return deleted == null ? 0 : deleted;
    }

    /* ------------------------------------------------------------------ */
    /* Session check for the access-token filter.                         */
    /* Returns true only if refresh:{userId}:{sessionId} still lives in   */
    /* Redis. logout / logout-all delete that key, so a revoked session   */
    /* returns false and the access token stops being trusted.            */
    /* ------------------------------------------------------------------ */
    public boolean sessionExists(String userId, String sessionId) {
        if (userId == null || sessionId == null) return false;
        return Boolean.TRUE.equals(redis.hasKey(key(userId, sessionId)));
    }

    /* ------------------------------------------------------------------ */
    /* List active sessions (for a "logged-in devices" screen).           */
    /* ------------------------------------------------------------------ */
    public List<SessionInfo> listSessions(String userId) {
        List<SessionInfo> out = new ArrayList<>();
        for (String key : scanKeys(KEY_PREFIX + userId + ":*")) {
            String sessionId = key.substring((KEY_PREFIX + userId + ":").length());
            String platform  = (String) redis.opsForHash().get(key, F_PLATFORM);
            String lastUsed  = (String) redis.opsForHash().get(key, F_LAST_USED);
            long lastUsedAt  = lastUsed == null ? 0L : Long.parseLong(lastUsed);
            out.add(new SessionInfo(sessionId, platform, lastUsedAt));
        }
        return out;
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                            */
    /* ------------------------------------------------------------------ */

    private String key(String userId, String sessionId) {
        return KEY_PREFIX + userId + ":" + sessionId;
    }

    /** token = base64url( userId | sessionId | 32-byte secret ) */
    private String buildToken(String userId, String sessionId) {
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        String raw = userId + "|" + sessionId + "|" + B64.encodeToString(secret);
        return B64.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String[] decodeToken(String rawToken) {
        try {
            String decoded = new String(B64D.decode(rawToken), StandardCharsets.UTF_8);
            String[] p = decoded.split("\\|", 3);
            if (p.length != 3) throw new IllegalArgumentException("bad token shape");
            return new String[]{ p[0], p[1] };
        } catch (RuntimeException e) {
            throw new InvalidRefreshTokenException("Malformed refresh token");
        }
    }

    /** SCAN (not KEYS) so we never block Redis in production. */
    private List<String> scanKeys(String pattern) {
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redis.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private static String safe(Object o) {
        return o == null ? "unknown" : o.toString();
    }

    /* ---- Exceptions (handled by your GlobalExceptionHandler) ---- */

    public static class InvalidRefreshTokenException extends RuntimeException {
        public InvalidRefreshTokenException(String m) { super(m); }
    }

    public static class RefreshTokenReuseException extends RuntimeException {
        public RefreshTokenReuseException(String m) { super(m); }
    }
}