package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.List;
import lombok.Data;

@Data
public class SgAssignmentDTO {
    private Long id;
    private Long developmentId;
    private Integer progressPercent;
    private List<String> images;
    private String videoUrl;
    private String document;
    private SgDevelopmentDTO development;
}
