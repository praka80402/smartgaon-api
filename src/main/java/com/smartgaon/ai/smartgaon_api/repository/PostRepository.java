package com.smartgaon.ai.smartgaon_api.repository;

import com.smartgaon.ai.smartgaon_api.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
}
