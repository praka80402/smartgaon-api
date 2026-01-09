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


    @GetMapping("/my")
    public ResponseEntity<?> myBusinesses(
            @RequestParam Long userId
    ) {
        try {
            return ResponseEntity.ok(service.myBusinesses(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fetch failed");
        }
    }
}
