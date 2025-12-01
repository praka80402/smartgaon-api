package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment;

import java.time.Instant;

public record CommentResponse(
        Long commentId,
        Long postId,
        Long authorId,
        String authorName,
        String content,
        Long likeCount,
        String status,
        Instant createdAt,
        String profileImageUrl
) {}

