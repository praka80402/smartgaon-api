package com.smartgaon.ai.smartgaon_api.repository;

import com.smartgaon.ai.smartgaon_api.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
