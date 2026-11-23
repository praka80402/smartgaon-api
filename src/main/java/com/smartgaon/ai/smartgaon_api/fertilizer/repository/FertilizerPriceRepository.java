package com.smartgaon.ai.smartgaon_api.fertilizer.repository;

import com.smartgaon.ai.smartgaon_api.fertilizer.entity.FertilizerPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FertilizerPriceRepository extends JpaRepository<FertilizerPrice, Long> {
    Optional<FertilizerPrice> findByItemKey(String itemKey);
    List<FertilizerPrice> findAllByCategory(String category);
}
