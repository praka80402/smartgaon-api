package com.smartgaon.ai.smartgaon_api.fertilizer.repository;

import com.smartgaon.ai.smartgaon_api.fertilizer.entity.CropFertilizerStandard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropFertilizerStandardRepository extends JpaRepository<CropFertilizerStandard, Long> {
    Optional<CropFertilizerStandard> findByCropKey(String cropKey);
    List<CropFertilizerStandard> findAllByCategory(String category);
    List<CropFertilizerStandard> findAllByStateOrState(String state, String defaultState);
}
