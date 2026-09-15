package com.smartgaon.ai.smartgaon_api.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RedisAuthTokenService {

    private final StringRedisTemplate redisTemplate;

    public RedisAuthTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String buildKey(String userType, String identifier) {
        return "auth:token:" + userType.toUpperCase() + ":" + identifier.trim().toLowerCase();
    }

    public void saveTokens(String userType, String identifier, String accessToken, String refreshToken, long refreshExpirySeconds) {
        String key = buildKey(userType, identifier);
        redisTemplate.opsForHash().put(key, "accessToken", accessToken);
        redisTemplate.opsForHash().put(key, "refreshToken", refreshToken);
        redisTemplate.opsForHash().put(key, "loginIdentifier", identifier);
        redisTemplate.expire(key, Duration.ofSeconds(refreshExpirySeconds));
    }

    public boolean isTokenExists(String userType, String identifier) {
        String key = buildKey(userType, identifier);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Ye method JwtFilter ke liye jaruri hai
    public String getAccessToken(String userType, String identifier) {
        String key = buildKey(userType, identifier);
        Object val = redisTemplate.opsForHash().get(key, "accessToken");
        return val == null ? null : val.toString();
    }

    public boolean deleteToken(String userType, String identifier) {
        String key = buildKey(userType, identifier);
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    public void deleteByKey(String key) {
        redisTemplate.delete(key);
    }
}