package com.smartgaon.ai.smartgaon_api.shikshaquiz.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.CompetitionConfig;

@Repository
public interface CompetitionConfigRepository extends JpaRepository<CompetitionConfig, Long> {

    Optional<CompetitionConfig> findByCompetitionType(String competitionType);
}
