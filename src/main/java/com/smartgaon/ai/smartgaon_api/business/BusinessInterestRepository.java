package com.smartgaon.ai.smartgaon_api.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessInterestRepository
        extends JpaRepository<BusinessInterest, Long> {

    boolean existsByBusinessIdAndUserId(Long businessId, Long userId);

    List<BusinessInterest> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
}
