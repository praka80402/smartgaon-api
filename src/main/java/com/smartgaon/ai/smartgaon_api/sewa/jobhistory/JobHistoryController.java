package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs/history")
@RequiredArgsConstructor
public class JobHistoryController {

    private final JobHistoryService service;

    @GetMapping("/employer/{employerId}")
    public Page<EmployerJobHistoryResponse> getEmployerHistory(
            @PathVariable Long employerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return service.getEmployerJobHistory(employerId, page, size);
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

    @GetMapping("/reviews/{applicantId}")
public WorkerReviewResponse getWorkerReviews(
        @PathVariable Long applicantId
) {
    return service.getWorkerReviews(applicantId);
}

}
