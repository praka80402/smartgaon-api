package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobReviewRepository
        extends JpaRepository<JobReview, Long> {

    Optional<JobReview> findByJobIdAndApplicantId(
            Long jobId,
            Long applicantId

    );

    boolean existsByJobIdAndApplicantId(
            Long jobId,
            Long applicantId
    );
}
