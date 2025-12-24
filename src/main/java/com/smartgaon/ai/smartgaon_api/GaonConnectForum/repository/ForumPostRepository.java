package com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
	

    Page<ForumPost> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<ForumPost> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);
    Page<ForumPost> findByDeletedFalse(Pageable pageable);

}
