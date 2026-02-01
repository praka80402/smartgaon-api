package com.smartgaon.ai.smartgaon_api.gaontalent.dto;

import lombok.Data;

@Data
public class VideoProcessingCallbackRequest {

    private Long entryId;

    private String thumbnailUrl;

    private String lowQualityVideoUrl;

    private String status; // READY | FAILED
}
