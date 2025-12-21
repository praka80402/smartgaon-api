package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EmployerJobHistoryResponse {

    private Long jobId;
    private String jobTitle;

    private Long applicantId;
    private String applicantName;
    private String applicantImage;

    private Integer rating;
    private String comment;

    private LocalDateTime appliedAt;
}
