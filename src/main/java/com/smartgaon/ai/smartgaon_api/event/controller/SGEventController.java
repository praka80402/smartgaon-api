package com.smartgaon.ai.smartgaon_api.event.controller;

import com.smartgaon.ai.smartgaon_api.event.EventSectionType;
import com.smartgaon.ai.smartgaon_api.event.SGEvent;
import com.smartgaon.ai.smartgaon_api.event.service.SGEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class SGEventController {

    private final SGEventService sgEventService;

    // Paginated Events
    @GetMapping
    public Page<SGEvent> getAllEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return sgEventService.getAllEvents(page, size);
    }

    // Event By ID
    @GetMapping("/{id}")
    public SGEvent getEventById(@PathVariable Long id) {
        return sgEventService.getEventById(id);
    }

    // Featured Events
    @GetMapping("/featured")
    public List<SGEvent> getFeaturedEvents() {
        return sgEventService.getFeaturedEvents();
    }

    // Events By Section Type
    @GetMapping("/section/{sectionType}")
    public List<SGEvent> getEventsBySection(
            @PathVariable EventSectionType sectionType) {

        return sgEventService.getEventsBySection(sectionType);
    }

    // Search Events By Title
    @GetMapping("/search")
    public List<SGEvent> searchEvents(
            @RequestParam String title) {

        return sgEventService.searchByTitle(title);
    }

    // All Active Events Without Pagination
    @GetMapping("/all")
    public List<SGEvent> getAllActiveEvents() {
        return sgEventService.getAllActiveEvents();
    }
}