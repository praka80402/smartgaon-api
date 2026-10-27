package com.smartgaon.ai.smartgaon_api.mandi.repository;

import com.smartgaon.ai.smartgaon_api.mandi.entity.MandiRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MandiRateRepository extends JpaRepository<MandiRate, Long> {

    List<MandiRate> findByStateAndDistrict(String state, String district);

    List<MandiRate> findByDistrict(String district);

    List<MandiRate> findByStateAndDistrictOrderByArrivalDateDesc(String state, String district);

    List<MandiRate> findByDistrictOrderByArrivalDateDesc(String district);

    Optional<MandiRate> findByStateAndDistrictAndCommodity(String state, String district, String commodity);

    Optional<MandiRate> findByStateAndDistrictAndCommodityAndArrivalDate(String state, String district, String commodity, String arrivalDate);

    boolean existsByDistrict(String district);

    @Query("SELECT DISTINCT m.state FROM MandiRate m ORDER BY m.state")
    List<String> findDistinctStates();

    @Query("SELECT DISTINCT m.district FROM MandiRate m WHERE LOWER(m.state) = LOWER(:state) ORDER BY m.district")
    List<String> findDistinctDistrictsByState(@Param("state") String state);

    @Query("SELECT m FROM MandiRate m WHERE LOWER(m.district) = LOWER(:district) AND LOWER(m.commodity) LIKE LOWER(CONCAT('%', :commodity, '%')) AND m.arrivalDate >= :cutoffDate ORDER BY m.arrivalDate ASC")
    List<MandiRate> findHistoricalTrend(@Param("district") String district, @Param("commodity") String commodity, @Param("cutoffDate") String cutoffDate);

    @Query("SELECT COUNT(m) FROM MandiRate m WHERE m.arrivalDate < :cutoffDate")
    long countRecordsOlderThan(@Param("cutoffDate") String cutoffDate);

    @Modifying
    @Transactional
    @Query("DELETE FROM MandiRate m WHERE m.arrivalDate < :cutoffDate")
    int deleteRecordsOlderThan(@Param("cutoffDate") String cutoffDate);
}
