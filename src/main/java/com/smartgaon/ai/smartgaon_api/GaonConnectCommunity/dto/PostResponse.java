package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class PostResponse {
	
	 private UUID id;
	    private String topic;
	    private String description;
	    private String shortDescription;
	    private String pictureUrl;
	    private String postedBy;
	    private String tagVillage;
	    private LocalDateTime datePosted;
		

}
