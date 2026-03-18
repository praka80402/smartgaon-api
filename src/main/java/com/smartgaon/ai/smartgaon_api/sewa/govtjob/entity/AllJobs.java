package com.smartgaon.ai.smartgaon_api.sewa.govtjob.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class AllJobs {
	
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String title;

	    @Column(length = 1000)
	    private String description;

	    private String applyUrl;

	    private String jobType; 

	    private String companyName;

	    private String location;

}
