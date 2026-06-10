package com.smartgaon.ai.smartgaon_api.eventbanner.repository;

import com.smartgaon.ai.smartgaon_api.eventbanner.BannerSectionType;
import com.smartgaon.ai.smartgaon_api.eventbanner.EventBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventBannerRepository extends JpaRepository<EventBanner, Long> {

    List<EventBanner> findByActiveTrueOrderByDisplayOrderAsc();

    List<EventBanner> findBySectionTypeAndActiveTrueOrderByDisplayOrderAsc(
            BannerSectionType sectionType
    );
}