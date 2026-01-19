package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;

public interface TalentEntryRepository extends JpaRepository<TalentEntry, Long> {

    Page<TalentEntry> findByCategory(TalentCategory category, Pageable pageable);

    List<TalentEntry> findByCompetitionId(Long competitionId);
//    
    @Query("""
    		   SELECT e FROM TalentEntry e
    		   WHERE e.category = :category
    		   AND e.id NOT IN (
    		       SELECT r.entryId FROM TalentReport r WHERE r.userId = :userId
    		   )
    		""")
    		Page<TalentEntry> findByCategoryWithoutReported(
    		        @Param("category") TalentCategory category,
    		        @Param("userId") Long userId,
    		        Pageable pageable
    		);


}
