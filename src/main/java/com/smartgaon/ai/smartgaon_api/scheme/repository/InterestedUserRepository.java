package com.smartgaon.ai.smartgaon_api.scheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.scheme.entity.InterestedUser;

public interface InterestedUserRepository extends JpaRepository<InterestedUser, Long> {
	boolean existsBySchemeIdAndPhoneNumber(Long schemeId, String phoneNumber);

}
