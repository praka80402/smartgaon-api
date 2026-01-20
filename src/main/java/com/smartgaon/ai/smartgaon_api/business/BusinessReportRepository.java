package com.smartgaon.ai.smartgaon_api.business;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessReportRepository
        extends JpaRepository<BusinessReport, Long> {

    boolean existsByBusinessIdAndReporterId(Long businessId, Long reporterId);
}

