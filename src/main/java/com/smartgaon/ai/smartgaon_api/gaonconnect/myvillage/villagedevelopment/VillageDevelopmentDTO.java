package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;

import lombok.Data;

@Data
public class VillageDevelopmentDTO {
	 private Long id;
	    private Long developmentId;
	    
	    private String title;
	    private String developmentDescription;
	    private String imageUrl;
	    
	    private String workDescription;
	    private String benefit;
	    private Integer progressPercent;

}
