package com.smartgaon.ai.smartgaon_api.banners.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartgaon.ai.smartgaon_api.banners.entity.Banner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    @Query("""
        SELECT b FROM Banner b
        WHERE b.startDate <= :today
        AND b.endDate >= :today
    """)
    List<Banner> findActiveBanners(LocalDate today);

    @Query("""
        SELECT b FROM Banner b
        WHERE b.id = :id
        AND b.startDate <= :today
        AND b.endDate >= :today
    """)
    Optional<Banner> findActiveBannerById(Long id, LocalDate today);
}

