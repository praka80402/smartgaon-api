package com.smartgaon.ai.smartgaon_api.business;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BusinessInterestResponse {
    private Long id;
    private String name;
    private String phone;
    private String message;
    private LocalDateTime appliedAt;
}
