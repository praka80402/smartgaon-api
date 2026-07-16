package com.smartgaon.ai.smartgaon_api.shikshaquiz.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Thin wrapper over the shared Redis instance (Section 11).
 * Every method degrades gracefully — if Redis is down or not configured,
 * callers fall back to the database (Section 11.1), so correctness never
 * depends on cache availability.
 *
 * Key patterns:
 *   quiz:active_batch:{segmentKey}
 *   quiz:attempt_count:{userId}:{competitionType}:{quizDay}
 *   quiz:hashes:{segmentKey}:{subject}
 *   quiz:stock:{segmentKey}
 */
@Service
public class QuizRedisService {

    @Autowired(required = false)
    private StringRedisTemplate redis;

    public String get(String key) {
        if (redis == null) return null;
        try {
            return redis.opsForValue().get(key);
        } catch (Exception e) {
            return null; // cache-miss fallback path handles it
        }
    }

    public void set(String key, String value, long ttlSeconds) {
        if (redis == null) return;
        try {
            if (ttlSeconds > 0) {
                redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
            } else {
                redis.opsForValue().set(key, value);
            }
        } catch (Exception ignored) {}
    }

    public void delete(String key) {
        if (redis == null) return;
        try {
            redis.delete(key);
        } catch (Exception ignored) {}
    }

    /** Atomic INCR — prevents double-submit race on the daily attempt counter. Returns null if Redis unavailable. */
    public Long increment(String key, long ttlSeconds) {
        if (redis == null) return null;
        try {
            Long value = redis.opsForValue().increment(key);
            if (value != null && value == 1L && ttlSeconds > 0) {
                redis.expire(key, Duration.ofSeconds(ttlSeconds));
            }
            return value;
        } catch (Exception e) {
            return null;
        }
    }

    public void decrement(String key) {
        if (redis == null) return;
        try {
            redis.opsForValue().decrement(key);
        } catch (Exception ignored) {}
    }

    /** O(1) hash lookup during bulk upload. Returns null if Redis unavailable. */
    public Boolean setContains(String setKey, String member) {
        if (redis == null) return null;
        try {
            return redis.opsForSet().isMember(setKey, member);
        } catch (Exception e) {
            return null;
        }
    }

    public void setAdd(String setKey, String member) {
        if (redis == null) return;
        try {
            redis.opsForSet().add(setKey, member);
        } catch (Exception ignored) {}
    }

    public void setRemove(String setKey, String member) {
        if (redis == null) return;
        try {
            redis.opsForSet().remove(setKey, member);
        } catch (Exception ignored) {}
    }
}
