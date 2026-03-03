package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DevelopmentRepository extends JpaRepository<Development, Long> {
	

List<Development> findAllByOrderByPhaseNumberAsc();

List<Development> findByStatus(PhaseStatus status);

List<Development> findByPhaseNumberOrderByIdDesc(Integer phaseNumber);
}
