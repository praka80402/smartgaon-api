package com.smartgaon.ai.smartgaon_api.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartgaon.ai.smartgaon_api.config.AnalyticsConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final AnalyticsConfig config;
    private final RestTemplate restTemplate;

 public void sendScreenViewEvent(String clientId,
                                String screenName,
                                String screenClass,
                                String platform,
                                String methodType) {

    String url = "https://www.google-analytics.com/mp/collect"
            + "?measurement_id=" + config.getMeasurementId()
            + "&api_secret=" + config.getApiSecret();

    // Build event parameters
    Map<String, Object> params = new HashMap<>();
    params.put("screen_name", screenName);
    params.put("firebase_screen_class", screenClass);
    params.put("platform", platform);
    params.put("method_type", methodType);


    // Build full payload
    Map<String, Object> event = new HashMap<>();
    event.put("name", "screen_view");
    event.put("params", params);

    Map<String, Object> payload = new HashMap<>();
    payload.put("client_id", clientId);
    payload.put("events", List.of(event));

    // Send request
    restTemplate.postForObject(url, payload, String.class);
}

public void sendPageViewEvent(String clientId,
                              String eventName,
                              String platform,
                              String methodType) {

    String url = "https://www.google-analytics.com/mp/collect"
            + "?measurement_id=" + config.getMeasurementId()
            + "&api_secret=" + config.getApiSecret();

    Map<String, Object> params = new HashMap<>();
    params.put("page_location", eventName);
    params.put("platform", platform);
    params.put("method_type", methodType);

    Map<String, Object> event = new HashMap<>();
    event.put("name", "page_view");
    event.put("params", params);

    Map<String, Object> payload = new HashMap<>();
    payload.put("client_id", clientId);
    payload.put("events", List.of(event));

    restTemplate.postForObject(url, payload, String.class);
}
    
}