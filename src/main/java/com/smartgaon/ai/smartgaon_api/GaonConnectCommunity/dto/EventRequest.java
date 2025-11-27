package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto;

import lombok.Data;

@Data
public class EventRequest {
    private String title;
    private String description;
    private String startDateTime;
    private String endDateTime;
    private String location;
    private String contactInfo;
}
