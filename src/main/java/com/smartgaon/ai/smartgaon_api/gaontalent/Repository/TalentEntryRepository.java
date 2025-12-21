package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;

public interface TalentEntryRepository extends JpaRepository<TalentEntry, Long> {

    Page<TalentEntry> findByCategory(TalentCategory category, Pageable pageable);

    List<TalentEntry> findByCompetitionId(Long competitionId);
}
