package com.smartgaon.ai.smartgaon_api.eventbanner.service;

import com.smartgaon.ai.smartgaon_api.eventbanner.BannerSectionType;
import com.smartgaon.ai.smartgaon_api.eventbanner.EventBanner;
import com.smartgaon.ai.smartgaon_api.eventbanner.repository.EventBannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventBannerService {

    private final EventBannerRepository eventBannerRepository;

    // All Active Banners
    public List<EventBanner> getAllBanners() {
        return eventBannerRepository
                .findByActiveTrueOrderByDisplayOrderAsc();
    }

    // Banner By Section
    public List<EventBanner> getBannersBySection(
            BannerSectionType sectionType) {

        return eventBannerRepository
                .findBySectionTypeAndActiveTrueOrderByDisplayOrderAsc(sectionType);
    }
}