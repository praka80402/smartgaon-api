package com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumComment;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;

public interface ForumCommentRepository  extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPost(ForumPost post);
}
