package com.smartgaon.ai.smartgaon_api.event.repository;

import com.smartgaon.ai.smartgaon_api.event.EventSectionType;
import com.smartgaon.ai.smartgaon_api.event.SGEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SGEventRepository extends JpaRepository<SGEvent, Long> {

    // All Active Events with Pagination
    Page<SGEvent> findByActiveTrue(Pageable pageable);

    // Featured Events
    List<SGEvent> findByFeaturedTrueAndActiveTrue();

    // Search Events by Title
    List<SGEvent> findByTitleContainingIgnoreCaseAndActiveTrue(String title);

    // Events by Section Type
    List<SGEvent> findBySectionTypeAndActiveTrue(EventSectionType sectionType);

    // All Active Events without Pagination
    List<SGEvent> findAllByActiveTrueOrderByDisplayOrderAsc();

    // Featured Events Sorted
    List<SGEvent> findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc();

    // Section Wise Sorted Events
    List<SGEvent> findBySectionTypeAndActiveTrueOrderByDisplayOrderAsc(
            EventSectionType sectionType
    );
}