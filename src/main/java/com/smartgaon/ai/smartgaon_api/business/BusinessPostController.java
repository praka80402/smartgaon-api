package com.smartgaon.ai.smartgaon_api.business;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessPostController {

    private final BusinessPostService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String budget,

            @RequestParam("images") MultipartFile[] images
    ) {
        try {
            if (images == null || images.length == 0) {
                return ResponseEntity.badRequest().body("At least one image is required");
            }

            if (images.length > 5) {
                return ResponseEntity.badRequest().body("Maximum 5 images allowed");
            }

            return ResponseEntity.ok(
                    service.create(userId, title, description, location,budget, images)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Create failed");
        }
    }


    /* ================= MY BUSINESSES (PAGINATION) ================= */
    @GetMapping("/my")
    public ResponseEntity<?> myBusinesses(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) throws Exception {

        return ResponseEntity.ok(
                service.myBusinesses(userId, limit, offset)
        );
    }

    /* ================= PUBLIC BUSINESSES ================= */
    @GetMapping("/public")
    public ResponseEntity<?> publicBusinesses(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) throws Exception {

        return ResponseEntity.ok(
                service.publicBusinesses(limit, offset)
        );
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{businessId}")
    public ResponseEntity<?> update(
            @PathVariable Long businessId,
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam String budget
    ) {

        return ResponseEntity.ok(
                service.update(businessId, userId, title, description, location, budget)
        );
    }

    /* ================= DELETE ================= */
    @DeleteMapping("/{businessId}")
    public ResponseEntity<?> delete(
            @PathVariable Long businessId,
            @RequestParam Long userId
    ) throws Exception {

        service.delete(businessId, userId);
        return ResponseEntity.ok("Deleted successfully");
    }
}
