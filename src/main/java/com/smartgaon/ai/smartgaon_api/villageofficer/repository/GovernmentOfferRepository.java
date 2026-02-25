package com.smartgaon.ai.smartgaon_api.villageofficer.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;

import java.util.List;

public interface GovernmentOfferRepository 
        extends JpaRepository<GovernmentOffer, Long> {
	

    // ✅ SAFE district match (handles case + spaces)
    @Query("SELECT g FROM GovernmentOffer g WHERE LOWER(TRIM(g.district)) = LOWER(TRIM(:district))")
    List<GovernmentOffer> findByDistrict(@Param("district") String district);
//
//    List<GovernmentOffer> findByState(String state);
//    List<GovernmentOffer> findByDistrict(String district);
//    List<GovernmentOffer> findByDistrictIgnoreCase(String district);
}