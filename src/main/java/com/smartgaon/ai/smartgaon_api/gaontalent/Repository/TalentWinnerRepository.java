package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentWinner;

public interface TalentWinnerRepository extends JpaRepository<TalentWinner, Long> {

    @Query("""
        SELECT w FROM TalentWinner w
        WHERE (:competitionId IS NULL OR w.competitionId = :competitionId)
        AND (:year IS NULL OR YEAR(w.declaredAt) = :year)
        AND (:month IS NULL OR MONTH(w.declaredAt) = :month)
        ORDER BY w.declaredAt DESC
    """)
    List<TalentWinner> findByCriteria(
        @Param("competitionId") Long competitionId,
        @Param("year") Integer year,
        @Param("month") Integer month
    );
}
