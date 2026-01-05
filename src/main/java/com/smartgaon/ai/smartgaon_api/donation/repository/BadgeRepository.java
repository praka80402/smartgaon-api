package com.smartgaon.ai.smartgaon_api.donation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.donation.model.Badge;

import java.util.List;


public interface BadgeRepository
        extends JpaRepository<Badge, Long> {

    List<Badge> findByUserId(Long userId);
}
