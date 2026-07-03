package com.smartgaon.ai.smartgaon_api.JwtUtil;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // =====================================================================
    // GENERATORS
    // =====================================================================

    /**
     * Full generator: puts userId (uid) and sessionId (sid) into the access
     * token so JwtAuthFilter can verify the session still exists in Redis.
     * Use this one from every login path.
     */
    public String generate(String email, String role, String userId, String sessionId) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("uid", userId)
                .claim("sid", sessionId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ---- Backward-compatible overloads (old callers keep working) ----
    // NOTE: tokens made without a sid CANNOT be session-revoked. Prefer the
    // 4-arg generator above for anything that logs a user in.

    public String generate(String email, String role) {
        return generate(email, role, null, null);
    }

    public String generate(String email) {
        return generate(email, "USER", null, null);
    }

    // =====================================================================
    // EXTRACTORS
    // =====================================================================

    private Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public String extractRole(String token) {
        return parse(token).get("role", String.class);
    }

    /** userId claim (uid). Null for legacy tokens. */
    public String extractUserId(String token) {
        return parse(token).get("uid", String.class);
    }

    /** sessionId claim (sid). Null for legacy tokens. */
    public String extractSessionId(String token) {
        return parse(token).get("sid", String.class);
    }
}