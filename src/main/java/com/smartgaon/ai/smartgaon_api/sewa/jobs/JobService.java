package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import java.util.List;

import org.springframework.data.domain.Page;

public interface JobService {
    Job create(Job job);
    Job update(Long id, Job job);
    void delete(Long id);
    Job get(Long id);
    List<Job> getAll();
    void applyJob(Long jobId, Long applicantId);

    List<JobApplicantResponse> getApplicants(Long jobId);

    List<JobApplication> getAppliedJobs(Long applicantId);

    void updateApplicationStatus(Long applicationId, String status);

    List<Job> getJobsByEmployer(Long employerId);

    void updateApplicationStatus(
            Long jobId,
            Long applicantId,
            String status
    );
    void closeJob(Long jobId, Long employerId);
    Page<Job> getOpenJobs(int page, int size);

}
