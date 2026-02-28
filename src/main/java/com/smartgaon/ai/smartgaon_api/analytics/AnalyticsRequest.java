package com.smartgaon.ai.smartgaon_api.analytics;

import lombok.Data;

@Data
public class AnalyticsRequest {

    private String clientId;     // required
    private String eventName;    // required
    private String methodType;   // email / google / otp
    private String platform;     // mobile / web
}