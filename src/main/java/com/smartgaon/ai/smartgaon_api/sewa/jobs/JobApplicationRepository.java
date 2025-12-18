package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    boolean existsByJobIdAndApplicantId(Long jobId, Long applicantId);

    @Query("""
        SELECT 
            ja.jobId AS applicationId,
            u.id AS userId,
            u.firstName AS name,
            u.phone AS phone,
            ja.status AS status,
            ja.appliedAt AS appliedAt
        FROM JobApplication ja
        JOIN User u ON u.id = ja.applicantId
        WHERE ja.jobId = :jobId
    """)
    List<JobApplicantResponse> findApplicantsByJobId(
            @Param("jobId") Long jobId);

    List<JobApplication> findByJobId(Long jobId);

    List<JobApplication> findByApplicantId(Long applicantId);



    long countByJobId(Long jobId);
}
