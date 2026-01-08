package com.smartgaon.ai.smartgaon_api.donation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.donation.model.Badge;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    @Query("SELECT b FROM Badge b WHERE b.user.id = :userId")
    List<Badge> findByUserId(@Param("userId") Long userId);
}
