package com.smartgaon.ai.smartgaon_api.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessPostRepository
        extends JpaRepository<BusinessPost, Long> {

    List<BusinessPost> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query(value = """
        SELECT * FROM business_post
        WHERE user_id = :userId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<BusinessPost> findMyBusinesses(
            @Param("userId") Long userId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT * FROM business_post
        WHERE status = 'ACTIVE'
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<BusinessPost> findPublicBusinesses(
            @Param("limit") int limit,
            @Param("offset") int offset
    );
}
