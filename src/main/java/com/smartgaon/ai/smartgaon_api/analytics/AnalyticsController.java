package com.smartgaon.ai.smartgaon_api.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // ✅ 1️⃣ Page View Tracking (Web)
    @PostMapping("/track")
    public ResponseEntity<?> trackEvent(@RequestBody AnalyticsRequest request) {

        if (request.getClientId() == null || request.getEventName() == null) {
            return ResponseEntity.badRequest()
                    .body("clientId and eventName are required");
        }

        try {

            // If this endpoint is meant for page view
            analyticsService.sendPageViewEvent(
                    ""+request.getClientId(),
                    ""+request.getEventName(),     // page_location
                    ""+request.getClass(),   // page_title
                    ""+request.getPlatform(),
                    ""+request.getMethodType()
            );

            return ResponseEntity.ok("Page View Event Sent Successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Analytics event failed: " + e.getMessage());
        }
    }

    // ✅ 2️⃣ Screen View Tracking (Mobile)
    @PostMapping("/screen")
    public ResponseEntity<?> trackScreen(@RequestBody ScreenViewRequest request) {

        if (request.getClientId() == null || request.getScreenName() == null) {
            return ResponseEntity.badRequest()
                    .body("clientId and screenName are required");
        }

        try {

            analyticsService.sendScreenViewEvent(
                    request.getClientId(),
                    request.getScreenName(),
                    request.getScreenClass(),
                    request.getPlatform(),
                    request.getMethodType()
            );

            return ResponseEntity.ok("Screen View Event Sent Successfully");

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Screen analytics failed: " + e.getMessage());
        }
    }
}