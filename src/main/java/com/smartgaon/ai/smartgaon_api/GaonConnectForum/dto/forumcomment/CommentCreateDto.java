package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment;

public record CommentCreateDto (
        Long userId,
        Long postId,
        String content	
        ) {}
