package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployerId(Long employerId);
      Page<Job> findByStatusNot(String status, Pageable pageable);
      
//      @Query("""
//      	    SELECT j FROM Job j
//      	    WHERE j.status <> 'CLOSED'
//      	      AND j.id NOT IN (
//      	          SELECT r.jobId FROM JobReport r
//      	          WHERE r.reportedBy = :userId
//      	      )
//      	""")
//      	Page<Job> findOpenJobsExcludingReported(
//      	        @Param("userId") Long userId,
//      	        Pageable pageable
//      	);
      
      @Query("""
    	        SELECT j
    	        FROM Job j
    	        WHERE j.status <> 'CLOSED'
    	        AND j.id NOT IN (
    	            SELECT jr.job.id
    	            FROM JobReport jr
    	            WHERE jr.reporterId = :userId
    	        )
    	    """)
    	    Page<Job> findOpenJobsExcludingReported(
    	            @Param("userId") Long userId,
    	            Pageable pageable
    	    );
      


}
