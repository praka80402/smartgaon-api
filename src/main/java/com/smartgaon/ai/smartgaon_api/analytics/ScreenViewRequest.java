package com.smartgaon.ai.smartgaon_api.analytics;

import lombok.Data;

@Data
public class ScreenViewRequest {

    private String clientId;
    private String screenName;
    private String screenClass;
    private String methodType;
}