package com.smartgaon.ai.smartgaon_api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "analytics")
@Getter
@Setter
public class AnalyticsConfig {

    private String measurementId;
    private String apiSecret;
}