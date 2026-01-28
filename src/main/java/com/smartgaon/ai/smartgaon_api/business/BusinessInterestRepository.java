package com.smartgaon.ai.smartgaon_api.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessInterestRepository
        extends JpaRepository<BusinessInterest, Long> {

    boolean existsByBusinessIdAndUserId(Long businessId, Long userId);

    List<BusinessInterest> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    
    @Query("""
    		SELECT new com.smartgaon.ai.smartgaon_api.business.BusinessInterestResponse(
    		    bi.id,
    		    bi.name,
    		    bi.phone,
    		    bi.message,
    		    u.profileImageUrl,
    		    bi.createdAt
    		)
    		FROM BusinessInterest bi
    		JOIN User u ON bi.userId = u.id
    		WHERE bi.businessId = :businessId
    		ORDER BY bi.createdAt DESC
    		""")
    		List<BusinessInterestResponse> findApplicantsWithProfile(
    		    @Param("businessId") Long businessId
    		);
}