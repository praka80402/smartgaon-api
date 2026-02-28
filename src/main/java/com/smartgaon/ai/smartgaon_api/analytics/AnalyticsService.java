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

    public void sendEvent(String clientId,
                      String eventName,
                      Map<String, Object> params) {

    String url = "https://www.google-analytics.com/mp/collect"
            + "?measurement_id=" + config.getMeasurementId()
            + "&api_secret=" + config.getApiSecret();

    Map<String, Object> payload = Map.of(
            "client_id", clientId,
            "events", List.of(
                    Map.of(
                            "name", eventName.toLowerCase(),
                            "params", params
                    )
            )
    );

    restTemplate.postForObject(url, payload, String.class);
}

public void trackScreenView(String clientId,
                            String screenName,
                            String screenClass,
                            String platform,
                            String methodType) {

    Map<String, Object> params = new HashMap<>();
    params.put("screen_name", screenName);
    params.put("firebase_screen_class", screenClass);
    params.put("platform", platform);
    params.put("method_type", methodType);

    sendEvent(clientId, "screen_view", params);
}
    
}