package com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.*;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    Page<ForumPost> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    Page<ForumPost> findByDeletedFalse(Pageable pageable);

    @Query("""
        SELECT p FROM ForumPost p
        WHERE p.deleted = false
        AND p.postId NOT IN (
            SELECT r.post.postId FROM ForumPostReport r
            WHERE r.reportedByUserId = :userId
        )
    """)
    Page<ForumPost> findVisiblePostsForUser(Long userId, Pageable pageable);
}
