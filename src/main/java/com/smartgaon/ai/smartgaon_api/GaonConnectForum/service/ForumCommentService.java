package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service;

import java.util.List;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentResponse;

public interface ForumCommentService {

    CommentResponse addComment(CommentCreateDto dto);

    List<CommentResponse> getCommentsForPost(Long postId);

    CommentResponse likeComment(Long commentId);

    void deleteComment(Long commentId);

}
