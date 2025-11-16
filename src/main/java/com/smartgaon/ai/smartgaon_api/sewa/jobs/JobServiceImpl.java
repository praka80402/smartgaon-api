package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository repo;

    public JobServiceImpl(JobRepository repo) {
        this.repo = repo;
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
}
