package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentCompetition;

public interface TalentCompetitionRepository extends JpaRepository<TalentCompetition, Long> {

    @Query("SELECT c FROM TalentCompetition c WHERE c.active = true AND (c.endDate IS NULL OR c.endDate >= :now) ORDER BY c.startDate ASC")
    List<TalentCompetition> findActiveAndUpcomingCompetitions(@Param("now") LocalDateTime now);
}




