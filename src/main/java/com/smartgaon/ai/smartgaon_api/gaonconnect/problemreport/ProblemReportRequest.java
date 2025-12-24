package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import lombok.Data;

@Data
public class ProblemReportRequest {
    private Long reporterId;      // id of user (FK)
    private String category;
    private String title;
    private String description;
    private String location;      // JSON or "lat,lng"
    private Boolean isPublic = true;
}