package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.controller;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/community/events")

@RequiredArgsConstructor
public class EventController {

    private final EventService service;

    @GetMapping
    public ResponseEntity<?> getEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getEvents(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable String id) {
        return ResponseEntity.ok(service.getEventById(UUID.fromString(id)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EventRequest request) {
        return ResponseEntity.ok(service.createEvent(request));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> createWithImage(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String startDateTime,
            @RequestParam String endDateTime,
            @RequestParam String location,
            @RequestParam String contactInfo,
            @RequestParam MultipartFile image
    ) {
        return ResponseEntity.ok(
                service.createEventWithImage(
                        title, description, startDateTime, endDateTime, location, contactInfo, image
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        service.deleteEvent(UUID.fromString(id));
        return ResponseEntity.ok("Deleted");
    }
}
