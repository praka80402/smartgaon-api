package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")

public class JobController {

    private final JobService service;

    public JobController(JobService service) {
        this.service = service;
    }

    @PostMapping
    public Job create(@RequestBody Job job) {
        return service.create(job);
    }

    @PutMapping("/{id}")
    public Job update(@PathVariable Long id, @RequestBody Job job) {
        return service.update(id, job);
    }

    @GetMapping("/{id}")
    public Job get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public List<Job> all() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/employer/{employerId}")
    public List<Job> getJobsByEmployer(@PathVariable Long employerId) {
        return service.getJobsByEmployer(employerId);
    }


    // ================= APPLY JOB =================
    @PostMapping("/{jobId}/apply")
    public ResponseEntity<?> applyJob(
            @PathVariable Long jobId,
            @RequestParam Long applicantId) {

        service.applyJob(jobId, applicantId);
        return ResponseEntity.ok("Applied successfully");
    }

    // ================= MY APPLIED JOBS =================
    @GetMapping("/applied/{applicantId}")
    public List<JobApplication> myAppliedJobs(
            @PathVariable Long applicantId) {

        return service.getAppliedJobs(applicantId);
    }

    // ================= EMPLOYER: VIEW APPLICANTS =================
    @GetMapping("/{jobId}/applicants")
    public List<JobApplicantResponse> getApplicants(
            @PathVariable Long jobId) {

        return service.getApplicants(jobId);
    }

    // ================= ACCEPT / REJECT =================
    @PutMapping("/applications/{applicationId}")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long applicationId,
            @RequestParam String status) {

        service.updateApplicationStatus(applicationId, status);
        return ResponseEntity.ok("Status updated");
    }
}
