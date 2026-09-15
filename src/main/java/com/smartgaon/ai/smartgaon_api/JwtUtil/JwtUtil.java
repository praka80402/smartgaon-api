package com.smartgaon.ai.smartgaon_api.JwtUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

import com.smartgaon.ai.smartgaon_api.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // new role-aware generator
    public String generate(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generate(String email) {
        return generate(email, "USER");
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
    public String generateAccessToken(User user, String userType, long expirySeconds) {

        Date now = new Date();

        Date expiry = new Date(now.getTime() + (expirySeconds * 1000L));

        Map<String, Object> claims = new HashMap<>();

        claims.put("tokenType", "ACCESS");
        claims.put("userType", userType);
        claims.put("userId", String.valueOf(user.getId()));

        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            claims.put("phone", user.getPhone());
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            claims.put("email", user.getEmail());
        }

        return Jwts.builder().setClaims(claims).setSubject(String.valueOf(user.getId())).setIssuedAt(now).setExpiration(expiry).setId(UUID.randomUUID().toString()).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    public String generateRefreshToken(User user, String userType, long expirySeconds) {

        Date now = new Date();

        Date expiry = new Date(now.getTime() + (expirySeconds * 1000L));

        Map<String, Object> claims = new HashMap<>();

        claims.put("tokenType", "REFRESH");
        claims.put("userType", userType);
        claims.put("userId", String.valueOf(user.getId()));

        return Jwts.builder().setClaims(claims).setSubject(String.valueOf(user.getId())).setIssuedAt(now).setExpiration(expiry).setId(UUID.randomUUID().toString()).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }
 
public Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
}

public boolean isTokenExpired(String token) {
    try {
        return extractAllClaims(token).getExpiration().before(new Date());
    } catch (ExpiredJwtException e) {
        return true;
    }
}

public boolean validateToken(String token) {
    try {
        extractAllClaims(token);
        return true;
    } catch (Exception e) {
        return false;
    }
}

public String extractTokenType(String token) {
    return extractAllClaims(token).get("tokenType", String.class);
}

public String extractUserId(String token) {
    return extractAllClaims(token).getSubject();
}

public String extractUserType(String token) {
    return extractAllClaims(token).get("userType", String.class);
}
}
