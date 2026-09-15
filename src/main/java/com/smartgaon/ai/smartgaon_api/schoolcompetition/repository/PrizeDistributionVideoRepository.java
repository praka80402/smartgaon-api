package com.smartgaon.ai.smartgaon_api.schoolcompetition.repository;

import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.PrizeDistributionVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrizeDistributionVideoRepository extends JpaRepository<PrizeDistributionVideo, Long> {

    List<PrizeDistributionVideo> findByCategoryIgnoreCase(String category);

    List<PrizeDistributionVideo> findByIsPastCompetitionFalseOrIsPastCompetitionIsNull();
}