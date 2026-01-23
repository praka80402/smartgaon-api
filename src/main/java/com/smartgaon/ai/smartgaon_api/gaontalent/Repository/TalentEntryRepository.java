//package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;
//
//import java.util.List;
//
//import org.springframework.data.domain.*;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;
//
//public interface TalentEntryRepository extends JpaRepository<TalentEntry, Long> {
//
//    Page<TalentEntry> findByCategory(TalentCategory category, Pageable pageable);
//
//    List<TalentEntry> findByCompetitionId(Long competitionId);
////    
//    @Query("""
//    		   SELECT e FROM TalentEntry e
//    		   WHERE e.category = :category
//    		   AND e.id NOT IN (
//    		       SELECT r.entryId FROM TalentReport r WHERE r.userId = :userId
//    		   )
//    		""")
//    		Page<TalentEntry> findByCategoryWithoutReported(
//    		        @Param("category") TalentCategory category,
//    		        @Param("userId") Long userId,
//    		        Pageable pageable
//    		);
//    
//    @Query("""
//    		SELECT e FROM TalentEntry e
//    		WHERE e.category = :category
//
//    		-- Owner can always see
//    		AND (
//    		    e.userId = :userId
//
//    		    -- OR normal visible posts
//    		    OR (
//    		        e.blocked = false
//
//    		        -- Not reported by this user
//    		        AND e.id NOT IN (
//    		            SELECT r.entryId FROM TalentReport r
//    		            WHERE r.userId = :userId
//    		        )
//    		    )
//    		)
//    		""")
//    		Page<TalentEntry> findFeedWithReportLogic(
//    		    @Param("category") TalentCategory category,
//    		    @Param("userId") Long userId,
//    		    Pageable pageable
//    		);
//    
//    Page<TalentEntry> findByCategoryAndBlockedFalse(
//            TalentCategory category,
//            Pageable pageable
//    );
//
//    @Query("""
//    		SELECT e.category, SUM(e.likes)
//    		FROM TalentEntry e
//    		WHERE e.blocked = false
//    		GROUP BY e.category
//    		ORDER BY SUM(e.likes) DESC
//    		""")
//    		List<Object[]> findTopLikedCategories();
//
//    		@Query("""
//    				SELECT e FROM TalentEntry e
//    				WHERE e.blocked = false
//    				ORDER BY e.createdAt DESC
//    				""")
//    				Page<TalentEntry> findAllVisible(Pageable pageable);
//    		
//    		@Query("""
//    				SELECT e FROM TalentEntry e
//    				WHERE
//    				(
//    				   e.userId = :userId   -- owner can see
//
//    				   OR
//    				   (
//    				     e.blocked = false
//    				     AND e.id NOT IN (
//    				        SELECT r.entryId FROM TalentReport r
//    				        WHERE r.userId = :userId
//    				     )
//    				   )
//    				)
//    				ORDER BY e.createdAt DESC
//    				""")
//    				Page<TalentEntry> findAllForUser(
//    				    @Param("userId") Long userId,
//    				    Pageable pageable
//    				);
//
//
//    		@Query("""
//    				SELECT DISTINCT e.category
//    				FROM TalentEntry e
//    				WHERE
//    				(
//    				   :userId IS NULL
//    				   OR
//    				   (
//    				     e.userId = :userId
//
//    				     OR
//    				     (
//    				        e.blocked = false
//    				        AND e.id NOT IN (
//    				            SELECT r.entryId
//    				            FROM TalentReport r
//    				            WHERE r.userId = :userId
//    				        )
//    				     )
//    				   )
//    				)
//    				""")
//    				List<TalentCategory> findVisibleCategories(
//    				        @Param("userId") Long userId
//    				);
//
//
//}

package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;

public interface TalentEntryRepository extends JpaRepository<TalentEntry, Long> {

    Page<TalentEntry> findByCategory(TalentCategory category, Pageable pageable);

    List<TalentEntry> findByCompetitionId(Long competitionId);


    // Hide posts reported by this user
    @Query("""
        SELECT e FROM TalentEntry e
        WHERE e.category = :category
        AND e.id NOT IN (
            SELECT r.entryId FROM TalentReport r
            WHERE r.userId = :userId
        )
    """)
    Page<TalentEntry> findByCategoryWithoutReported(
        @Param("category") TalentCategory category,
        @Param("userId") Long userId,
        Pageable pageable
    );


    // Feed with owner + report + block logic
    @Query("""
        SELECT e FROM TalentEntry e
        WHERE e.category = :category
        AND (
            e.userId = :userId
            OR
            (
                e.blocked = false
                AND e.id NOT IN (
                    SELECT r.entryId FROM TalentReport r
                    WHERE r.userId = :userId
                )
            )
        )
    """)
    Page<TalentEntry> findFeedWithReportLogic(
        @Param("category") TalentCategory category,
        @Param("userId") Long userId,
        Pageable pageable
    );


    Page<TalentEntry> findByCategoryAndBlockedFalse(
        TalentCategory category,
        Pageable pageable
    );


    // Top liked categories
    @Query("""
        SELECT e.category, SUM(e.likes)
        FROM TalentEntry e
        WHERE e.blocked = false
        GROUP BY e.category
        ORDER BY SUM(e.likes) DESC
    """)
    List<Object[]> findTopLikedCategories();


    // All visible (no login)
    @Query("""
        SELECT e FROM TalentEntry e
        WHERE e.blocked = false
        ORDER BY e.createdAt DESC
    """)
    Page<TalentEntry> findAllVisible(Pageable pageable);


    // All reels for logged user
    @Query("""
        SELECT e FROM TalentEntry e
        WHERE
        (
            e.userId = :userId
            OR
            (
                e.blocked = false
                AND e.id NOT IN (
                    SELECT r.entryId FROM TalentReport r
                    WHERE r.userId = :userId
                )
            )
        )
        ORDER BY e.createdAt DESC
    """)
    Page<TalentEntry> findAllForUser(
        @Param("userId") Long userId,
        Pageable pageable
    );


    // Visible categories
    @Query("""
        SELECT DISTINCT e.category
        FROM TalentEntry e
        WHERE
        (
            :userId IS NULL
            OR
            (
                e.userId = :userId
                OR
                (
                    e.blocked = false
                    AND e.id NOT IN (
                        SELECT r.entryId
                        FROM TalentReport r
                        WHERE r.userId = :userId
                    )
                )
            )
        )
    """)
    List<TalentCategory> findVisibleCategories(
        @Param("userId") Long userId
    );

}

