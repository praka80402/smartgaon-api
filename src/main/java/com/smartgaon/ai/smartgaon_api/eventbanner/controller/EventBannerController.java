package com.smartgaon.ai.smartgaon_api.eventbanner.controller;

import com.smartgaon.ai.smartgaon_api.eventbanner.BannerSectionType;
import com.smartgaon.ai.smartgaon_api.eventbanner.EventBanner;
import com.smartgaon.ai.smartgaon_api.eventbanner.service.EventBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event-banners")
@RequiredArgsConstructor
public class EventBannerController {

    private final EventBannerService eventBannerService;

    // All Active Banners
    @GetMapping("/all")
    public List<EventBanner> getAllBanners() {
        return eventBannerService.getAllBanners();
    }

    // Banner By Section
    @GetMapping("/section/{sectionType}")
    public List<EventBanner> getBannersBySection(
            @PathVariable BannerSectionType sectionType) {

        return eventBannerService.getBannersBySection(sectionType);
    }
}