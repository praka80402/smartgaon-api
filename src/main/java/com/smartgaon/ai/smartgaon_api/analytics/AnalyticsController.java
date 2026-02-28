package com.smartgaon.ai.smartgaon_api.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/track")
    public String trackEvent(@RequestBody AnalyticsRequest request) {

        Map<String, Object> params = new HashMap<>();
        params.put("method_type", request.getMethodType());
        params.put("platform", request.getPlatform());

        analyticsService.sendEvent(
                request.getClientId(),
                request.getEventName(),
                params);

        return "Event Sent";
    }
    @PostMapping("/screen")
    public String trackScreen(@RequestBody ScreenViewRequest request) {

        Map<String, Object> params = new HashMap<>();
        params.put("screen_name", request.getScreenName());
        params.put("firebase_screen_class", request.getScreenClass());
        params.put("platform", "mobile");
        params.put("method_type", request.getMethodType());

        analyticsService.sendEvent(
                request.getClientId(),
                "screen_view",
                params
        );

        return "Screen View Event Sent";
    }
}