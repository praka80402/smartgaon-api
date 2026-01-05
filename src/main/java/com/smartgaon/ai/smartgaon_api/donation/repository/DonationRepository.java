package com.smartgaon.ai.smartgaon_api.donation.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartgaon.ai.smartgaon_api.donation.model.Donation;

import org.springframework.data.repository.query.Param;

import java.util.List;


public interface DonationRepository
        extends JpaRepository<Donation, Long> {
	@Query("""
		    SELECT d FROM Donation d
		    JOIN FETCH d.project
		    WHERE d.userId = :userId
		""")
		List<Donation> findByUserId(@Param("userId") Long userId);
}

