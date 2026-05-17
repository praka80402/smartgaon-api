package com.smartgaon.ai.smartgaon_api.donation.dto;

import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignStatus;
import com.smartgaon.ai.smartgaon_api.donation.enums.CampaignType;
import com.smartgaon.ai.smartgaon_api.donation.enums.State;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CampaignResponse {

	private Long id;
    private String title;
    private String description;
    private CampaignType type;
    private String state;
	private Double targetAmount;
    private Double raisedAmount;
    private String imageUrl;
    private List<String> mediaImages;
}

