package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import java.util.List;

public interface JobHistoryService {

    List<EmployerJobHistoryResponse>
    getEmployerJobHistory(Long employerId);

    void submitReview(
            Long jobId,
            Long applicantId,
            Integer rating,
            String comment
    );
}
