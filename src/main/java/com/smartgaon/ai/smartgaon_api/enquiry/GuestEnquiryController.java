package com.smartgaon.ai.smartgaon_api.enquiry;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
public class GuestEnquiryController {

    private final GuestEnquiryService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody GuestEnquiry enquiry) {

        if (enquiry.getName() == null ||
            enquiry.getPhone() == null ||
            enquiry.getCheckIn() == null ||
            enquiry.getCheckOut() == null ||
            enquiry.getGuests() == null) {

            return ResponseEntity.badRequest().body("Missing required fields");
        }

        GuestEnquiry saved = service.create(enquiry);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Enquiry submitted successfully",
                "id", saved.getId()
        ));
    }

    @GetMapping
    public List<GuestEnquiry> getAll() {
        return service.getAll();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}