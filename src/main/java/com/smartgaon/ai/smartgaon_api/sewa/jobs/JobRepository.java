package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDate;
//import java.time.LocalDateTime;


public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
        SELECT j
        FROM Job j
        WHERE j.employerId = :employerId
        ORDER BY j.createdAt DESC
    """)
    List<Job> findByEmployerId(
            @Param("employerId") Long employerId
    );

    Page<Job> findByStatusNot(String status, Pageable pageable);

    // ✅ Active + Not Reported + Not Closed + Not Expired
    @Query("""
        SELECT j
        FROM Job j
        WHERE j.status <> 'CLOSED'
        AND j.deadline > CURRENT_TIMESTAMP
        AND j.id NOT IN (
            SELECT jr.job.id
            FROM JobReport jr
            WHERE jr.reporterId = :userId
        )
    """)
    Page<Job> findActiveJobsExcludingReported(
            @Param("userId") Long userId,
            Pageable pageable
    );
    Page<Job> findByStatusNotAndDeadlineAfter(
            String status,
            LocalDate date,
//            LocalDateTime time,
            Pageable pageable
    );

}
