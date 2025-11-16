package com.smartgaon.ai.smartgaon_api.GaonConnect.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
	 private String topic;
	    private String description;
	    private String shortDescription;
	    private String pictureUrl;
	    private String postedBy;     // ADMIN / USER
	    private String tagVillage;

}
