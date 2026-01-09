package com.smartgaon.ai.smartgaon_api.donation.dto;

import com.smartgaon.ai.smartgaon_api.donation.model.DonationProject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UserProjectDonationDTO {
   
	private DonationProject project;
    private Double totalDonated;
	
}
