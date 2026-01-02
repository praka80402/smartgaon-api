package com.smartgaon.ai.smartgaon_api.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RedisCareerGuideService {

    private final StringRedisTemplate redisTemplate;

    public RedisCareerGuideService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /* ===============================
       RATE LIMIT (4 / day)
       =============================== */

    public boolean incrementAndCheckLimit(String key, int limit) {

        Long count = redisTemplate.opsForValue().increment(key);

        // first hit → set expiry till midnight
        if (count != null && count == 1) {
            redisTemplate.expire(key, ttlTillMidnight());
        }

        return count != null && count <= limit;
    }

    public long getCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }

    /* ===============================
       STORE GUIDE HISTORY
       =============================== */

    public void saveGuideResponse(String identifier, String json) {

        String historyKey = historyKey(identifier);

        redisTemplate.opsForList().leftPush(historyKey, json);

        // keep only last 20 guides
        redisTemplate.opsForList().trim(historyKey, 0, 19);

        // history expires in 7 days
        redisTemplate.expire(historyKey, Duration.ofDays(7));
    }

    public List<String> getHistory(String identifier) {
        return redisTemplate.opsForList().range(historyKey(identifier), 0, -1);
    }

    /* ===============================
       HELPERS
       =============================== */

    private String historyKey(String identifier) {
        return "guide:" + identifier + ":history";
    }

    private Duration ttlTillMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIDNIGHT);
        return Duration.between(now, midnight);
    }

    public void resetDailyLimit(String identifier) {
        String todayKey = "guide:" + identifier + ":" + LocalDate.now() + ":count";
        redisTemplate.delete(todayKey);
    }


}
