package com.smartgaon.ai.smartgaon_api.business;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBusinessRequest {
    private Long userId;
    private String title;
    private String description;
    private String location;
    private List<String> images; // S3 URLs
}
