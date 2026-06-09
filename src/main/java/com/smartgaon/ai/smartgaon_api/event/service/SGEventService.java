package com.smartgaon.ai.smartgaon_api.event.service;

import com.smartgaon.ai.smartgaon_api.event.EventSectionType;
import com.smartgaon.ai.smartgaon_api.event.SGEvent;
import com.smartgaon.ai.smartgaon_api.event.repository.SGEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SGEventService {

    private final SGEventRepository sgEventRepository;

    // Paginated Active Events
    public Page<SGEvent> getAllEvents(int page, int size) {
        return sgEventRepository.findByActiveTrue(
                PageRequest.of(page, size)
        );
    }

    // Single Event By ID
    public SGEvent getEventById(Long id) {
        return sgEventRepository.findById(id)
                .orElse(null);
    }

    // Featured Events
    public List<SGEvent> getFeaturedEvents() {
        return sgEventRepository
                .findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc();
    }

    // Events By Section
    public List<SGEvent> getEventsBySection(EventSectionType sectionType) {
        return sgEventRepository
                .findBySectionTypeAndActiveTrueOrderByDisplayOrderAsc(sectionType);
    }

    // Search Events
    public List<SGEvent> searchByTitle(String title) {
        return sgEventRepository
                .findByTitleContainingIgnoreCaseAndActiveTrue(title);
    }

    // All Active Events (No Pagination)
    public List<SGEvent> getAllActiveEvents() {
        return sgEventRepository
                .findAllByActiveTrueOrderByDisplayOrderAsc();
    }
}