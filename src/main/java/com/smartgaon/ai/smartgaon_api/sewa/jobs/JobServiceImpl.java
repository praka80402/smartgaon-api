package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository repo;
    private final JobApplicationRepository applicationRepository;

    public JobServiceImpl(JobRepository repo, final JobApplicationRepository applicationRepository) {
        this.repo = repo;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Job create(Job job) {
        return repo.save(job);
    }

    @Override
    public Job update(Long id, Job job) {
        Job existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        existing.setTitle(job.getTitle());
        existing.setDescription(job.getDescription());
        existing.setRequirements(job.getRequirements());
        existing.setSalaryRange(job.getSalaryRange());
        existing.setEmploymentType(job.getEmploymentType());
        existing.setLocation(job.getLocation());
        existing.setContactNumber(job.getContactNumber());
        existing.setDeadline(job.getDeadline());
        existing.setStatus(job.getStatus());

        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public Job get(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public List<Job> getAll() {
        return repo.findAll();
    }


    @Override
    public void applyJob(Long jobId, Long applicantId) {

        if (applicationRepository.existsByJobIdAndApplicantId(jobId, applicantId)) {
            throw new AlreadyAppliedException("Already applied for this job");
        }

        Job job = repo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        JobApplication app = new JobApplication();
        app.setJobId(jobId);
        app.setApplicantId(applicantId);
        app.setEmployerId(job.getEmployerId());
        app.setStatus("PENDING");
        app.setAppliedAt(LocalDateTime.now());

        applicationRepository.save(app);
    }

    @Override
    public List<JobApplicantResponse> getApplicants(Long jobId) {
        return applicationRepository.findApplicantsByJobId(jobId);
    }

    @Override
    public List<JobApplication> getAppliedJobs(Long applicantId) {
        return applicationRepository.findByApplicantId(applicantId);
    }

    @Override
    public void updateApplicationStatus(Long applicationId, String status) {
        JobApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(status);
        applicationRepository.save(app);
    }

    @Override
    public List<Job> getJobsByEmployer(Long employerId) {
        return repo.findByEmployerId(employerId);
    }
    @Override
    public void updateApplicationStatus(
            Long jobId,
            Long applicantId,
            String status
    ) {
        JobApplication application = applicationRepository
                .findByJobIdAndApplicantId(jobId, applicantId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(status);
        applicationRepository.save(application);
    }

    @Override
    public void closeJob(Long jobId, Long employerId) {

        Job job = repo.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // 🔐 security check
        if (!job.getEmployerId().equals(employerId)) {
            throw new RuntimeException("Unauthorized");
        }

        job.setStatus("CLOSED");
        repo.save(job);
    }





}
