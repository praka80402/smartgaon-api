package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import org.springframework.data.domain.Page;
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


    @GetMapping("/open")
public ResponseEntity<?> getOpenJobs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
) {
    return ResponseEntity.ok(
            service.getOpenJobs(page, size)
    );
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

    @PutMapping("/applications/status")
    public ResponseEntity<?> updateApplicationStatus(
            @RequestParam Long jobId,
            @RequestParam Long applicantId,
            @RequestParam String status
    ) {
        service.updateApplicationStatus(jobId, applicantId, status);
        return ResponseEntity.ok("Application status updated successfully");
    }

    @PutMapping("/employer/{jobId}/close")
    public ResponseEntity<?> closeJob(
            @PathVariable Long jobId,
            @RequestParam Long employerId
    ) {
        service.closeJob(jobId, employerId);
        return ResponseEntity.ok("Job closed successfully");
    }
    @PostMapping("/{jobId}/report")
    public ResponseEntity<?> reportJob(
            @PathVariable Long jobId,
            @RequestParam Long reporterId,
            @RequestParam JobReportReason reason,
            @RequestParam(required = false) String customReason
    ) {
        service.reportJob(jobId, reporterId, reason, customReason);
        return ResponseEntity.ok("Job reported successfully");
    }
    
    @GetMapping("/open/user/{userId}")
    public ResponseEntity<Page<Job>> getOpenJobsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(
                service.getOpenJobsForUser(userId, page, size)
        );
    }



}
