package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import java.util.List;

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



}
