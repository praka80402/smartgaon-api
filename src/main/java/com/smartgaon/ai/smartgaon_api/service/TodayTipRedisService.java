package com.smartgaon.ai.smartgaon_api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.model.TodayTip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class TodayTipRedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String getRedisKey(String category) {
        return "today_tips:" + category.toUpperCase();
    }

    // Fetch active tips (last 3 days up to today)
    public List<TodayTip> fetchActiveTips(String category) {
        String key = getRedisKey(category);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(2); // last 3 days (today, yesterday, day before)

        long startScore = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        long endScore = today.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();

        Set<String> jsonSet = redisTemplate.opsForZSet().rangeByScore(key, startScore, endScore);

        if (jsonSet == null || jsonSet.isEmpty()) {
            return Collections.emptyList();
        }

        List<TodayTip> tips = new ArrayList<>();
        for (String json : jsonSet) {
            try {
                TodayTip tip = objectMapper.readValue(json, TodayTip.class);
                tips.add(tip);
            } catch (Exception e) {
                // Ignore parsing errors for individual corrupt elements
            }
        }

        // Sort: targetDate DESC, createdAt DESC
        tips.sort((t1, t2) -> {
            int dateComp = t2.getTargetDate().compareTo(t1.getTargetDate());
            if (dateComp != 0) {
                return dateComp;
            }
            return Long.compare(t2.getCreatedAt(), t1.getCreatedAt());
        });

        return tips;
    }
}

