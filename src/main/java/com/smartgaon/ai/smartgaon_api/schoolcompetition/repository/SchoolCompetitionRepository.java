package com.smartgaon.ai.smartgaon_api.schoolcompetition.repository;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolCompetitionRepository extends JpaRepository<SchoolCompetition, Long> {
    Optional<SchoolCompetition> findByCompetitionId(String competitionId);
    List<SchoolCompetition> findByIsLiveTrue();
    List<SchoolCompetition> findByStatus(String status);
}
