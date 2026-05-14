package com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository;

import java.util.Collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    Page<ForumPost> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    @Query("""
        SELECT p FROM ForumPost p
        WHERE p.deleted = false
        AND p.status IN :statuses
    """)
    Page<ForumPost> findVisiblePosts(@Param("statuses") Collection<ForumPost.Status> statuses, Pageable pageable);

    @Query("""
        SELECT p FROM ForumPost p
        WHERE p.deleted = false
        AND p.status IN :statuses
        AND (
            LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    Page<ForumPost> searchVisiblePosts(
            @Param("query") String query,
            @Param("statuses") Collection<ForumPost.Status> statuses,
            Pageable pageable);

    @Query("""
        SELECT p FROM ForumPost p
        WHERE p.deleted = false
        AND p.status IN :statuses
        AND p.postId NOT IN (
            SELECT r.post.postId FROM ForumPostReport r
            WHERE r.reportedByUserId = :userId
        )
    """)
    Page<ForumPost> findVisiblePostsForUser(
            @Param("userId") Long userId,
            @Param("statuses") Collection<ForumPost.Status> statuses,
            Pageable pageable);
}
