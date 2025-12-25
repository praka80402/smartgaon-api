package com.smartgaon.ai.smartgaon_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
