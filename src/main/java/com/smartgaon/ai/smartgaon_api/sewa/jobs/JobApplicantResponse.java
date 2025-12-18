package com.smartgaon.ai.smartgaon_api.sewa.jobs;
import java.time.LocalDateTime;

public interface JobApplicantResponse {

    Long getApplicationId();
    Long getUserId();
    String getName();
    String getPhone();
    String getStatus();
    LocalDateTime getAppliedAt();
}