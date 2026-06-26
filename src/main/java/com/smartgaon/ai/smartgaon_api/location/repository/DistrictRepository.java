package com.smartgaon.ai.smartgaon_api.location.repository;

import com.smartgaon.ai.smartgaon_api.location.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByStateIdOrderByNameAsc(Long stateId);
}