package com.smartgaon.ai.smartgaon_api.donation.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DonateResponse {
    private Long transactionId;
    private String utrNumber;
    private String message;
}