package com.smartgaon.ai.smartgaon_api.donation.dto;

import lombok.Data;

@Data
public class DonateRequest {
    private Long campaignId;
    private Double amount;
}