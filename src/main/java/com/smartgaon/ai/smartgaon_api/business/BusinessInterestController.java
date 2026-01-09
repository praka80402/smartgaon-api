package com.smartgaon.ai.smartgaon_api.business;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business-interest")
@RequiredArgsConstructor
public class BusinessInterestController {

    private final BusinessInterestService service;

    @PostMapping("/apply")
    public ResponseEntity<?> apply(
            @RequestBody ApplyBusinessInterestRequest req
    ) {
        try {
            service.apply(req);
            return ResponseEntity.ok("Interest submitted");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<?> applicants(
            @PathVariable Long businessId
    ) {
        return ResponseEntity.ok(service.applicants(businessId));
    }
}
