package com.smartgaon.ai.smartgaon_api.todaytips.controller;

import com.smartgaon.ai.smartgaon_api.model.TodayTip;
import com.smartgaon.ai.smartgaon_api.service.TodayTipRedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tips")
public class TodayTipController {

    @Autowired
    private TodayTipRedisService redisService;

    // 1. Fetch active tips for a category (last 3 days up to today)
    @GetMapping("/active")
    public ResponseEntity<List<TodayTip>> getActiveTips(@RequestParam(defaultValue = "ALL") String category) {
        List<TodayTip> tips = redisService.fetchActiveTips(category);
        return ResponseEntity.ok(tips);
    }
}


