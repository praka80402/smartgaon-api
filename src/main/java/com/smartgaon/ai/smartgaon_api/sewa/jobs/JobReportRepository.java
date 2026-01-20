package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobReportRepository extends JpaRepository<JobReport, Long> {

    boolean existsByJobIdAndReporterId(Long jobId, Long reporterId);

    long countByJobId(Long jobId);

    @Query("""
        SELECT jr.job.id
        FROM JobReport jr
        WHERE jr.reporterId = :reporterId
    """)
    List<Long> findReportedJobIdsByReporterId(
            @Param("reporterId") Long reporterId
    );
}
