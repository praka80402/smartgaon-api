package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByEmployerId(Long employerId);
      Page<Job> findByStatusNot(String status, Pageable pageable);

}
