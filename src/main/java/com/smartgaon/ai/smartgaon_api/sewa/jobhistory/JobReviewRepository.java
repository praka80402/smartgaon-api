package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface JobReviewRepository
        extends JpaRepository<JobReview, Long> {

    Optional<JobReview> findByJobIdAndApplicantId(
            Long jobId,
            Long applicantId

    );

    List<JobReview> findByApplicantId(Long applicantId);

    

    boolean existsByJobIdAndApplicantId(
            Long jobId,
            Long applicantId
    );


     List<JobReview> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    @Query("""
        SELECT AVG(r.rating)
        FROM JobReview r
        WHERE r.applicantId = :applicantId
    """)
    Double getAverageRating(Long applicantId);
}
