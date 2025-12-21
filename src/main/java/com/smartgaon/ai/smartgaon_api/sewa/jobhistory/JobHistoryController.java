package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/history")
@RequiredArgsConstructor
public class JobHistoryController {

    private final JobHistoryService service;

    // 👔 Employer job history
    @GetMapping("/employer/{employerId}")
    public List<EmployerJobHistoryResponse>
    getEmployerHistory(@PathVariable Long employerId) {
        return service.getEmployerJobHistory(employerId);
    }

    // ⭐ Review candidate
    @PostMapping("/review")
    public ResponseEntity<?> submitReview(
            @RequestParam Long jobId,
            @RequestParam Long applicantId,
            @RequestParam Integer rating,
            @RequestParam String comment
    ) {
        service.submitReview(
                jobId, applicantId, rating, comment);
        return ResponseEntity.ok("Review submitted");
    }
}
