package com.smartgaon.ai.smartgaon_api.scheme.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InterestedRequest {

    private String name;
    private String village;
    private String pincode;
    private String phoneNumber;
    private LocalDateTime date;
}
