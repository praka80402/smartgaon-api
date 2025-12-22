package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;


import org.springframework.data.domain.Page;

public interface JobHistoryService {
Page<EmployerJobHistoryResponse> getEmployerJobHistory(
            Long employerId,
            int page,
            int size
    );

    void submitReview(
            Long jobId,
            Long applicantId,
            Integer rating,
            String comment
    );
}
