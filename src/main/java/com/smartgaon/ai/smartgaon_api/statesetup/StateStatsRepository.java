package com.smartgaon.ai.smartgaon_api.statesetup;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StateStatsRepository extends JpaRepository<StateStats, Long> {
    Optional<StateStats> findByStateNameIgnoreCase(String stateName);
}