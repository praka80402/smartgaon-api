package com.smartgaon.ai.smartgaon_api.donation.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.donation.model.DonationProject;


public interface DonationProjectRepository
        extends JpaRepository<DonationProject, Long> {
}
