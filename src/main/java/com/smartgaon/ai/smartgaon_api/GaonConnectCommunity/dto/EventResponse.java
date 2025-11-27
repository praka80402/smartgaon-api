package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String contactInfo;
    private String pictureUrl;
    private LocalDateTime createdAt;
}

