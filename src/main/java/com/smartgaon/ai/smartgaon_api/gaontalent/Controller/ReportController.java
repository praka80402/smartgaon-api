package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;
import com.smartgaon.ai.smartgaon_api.gaontalent.Service.ReportService;

@RestController
@RequestMapping("/api/gaon-talent")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @PostMapping("/report/{entryId}")
    public ResponseEntity<?> report(
            @PathVariable Long entryId,
            @RequestParam Long userId,
            @RequestParam TalentReportReason reason,
            @RequestParam(required = false) String customReason
    ) {
        return ResponseEntity.ok(
                service.reportPost(entryId, userId, reason, customReason)
        );
    }

    @GetMapping("/report/status/{entryId}")
    public ResponseEntity<?> reportStatus(
            @PathVariable Long entryId,
            @RequestParam Long userId
    ) {
        return ResponseEntity.ok(
                java.util.Map.of("reported", service.isReportedByUser(entryId, userId))
        );
    }
}
