package com.smartgaon.ai.smartgaon_api.banners.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.banners.entity.Banner;
import com.smartgaon.ai.smartgaon_api.banners.service.BannerService;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BannerController {
	
	  private final BannerService bannerService;

    // LIST PAGE API
    @GetMapping
    public List<Banner> getAllBanners() {
        return bannerService.getAllActiveBanners();
    }

    // DETAIL PAGE API
    @GetMapping("/{id}")
    public Banner getBannerById(@PathVariable Long id) {
        return bannerService.getActiveBannerById(id);
    }

  
}