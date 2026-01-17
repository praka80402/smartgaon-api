package com.smartgaon.ai.smartgaon_api.banners.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.banners.entity.Banner;
import com.smartgaon.ai.smartgaon_api.banners.repository.BannerRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    // LIST PAGE
    public List<Banner> getAllActiveBanners() {
        return bannerRepository.findActiveBanners(LocalDate.now());
    }

    // DETAIL PAGE
    public Banner getActiveBannerById(Long id) {
        return bannerRepository.findActiveBannerById(id, LocalDate.now())
                .orElseThrow(() -> new RuntimeException("Banner not found or inactive"));
    }
}