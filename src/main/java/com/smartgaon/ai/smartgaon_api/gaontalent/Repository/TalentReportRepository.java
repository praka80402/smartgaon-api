package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.util.List;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentReport;

public interface TalentReportRepository extends JpaRepository<TalentReport, Long> {

    boolean existsByEntryIdAndUserId(Long entryId, Long userId);

    Optional<TalentReport> findByEntryIdAndUserId(Long entryId, Long userId);
    
    @Query("""
    	    select r.entryId
    	    from TalentReport r
    	    where r.userId = :userId
    	""")
    	List<Long> findReportedEntryIdsByUserId(Long userId);

}

