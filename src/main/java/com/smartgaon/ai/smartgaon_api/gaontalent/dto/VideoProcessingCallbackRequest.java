package com.smartgaon.ai.smartgaon_api.gaontalent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoProcessingCallbackRequest {

    private Long entryId;

    private String thumbnailUrl;

    private String lowQualityVideoUrl;

    private String status; // READY | FAILED
}
