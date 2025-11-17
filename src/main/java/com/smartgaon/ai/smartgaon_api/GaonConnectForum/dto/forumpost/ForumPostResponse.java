package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record ForumPostResponse(
		 Long postId,
	        Long authorId,
	        String authorName,
	        String title,
	        String content,
	        String category,
	        String area,   

	        String profileImage,
//	        Set<String> tags,
	        List<String> mediaAttachments,
	        Long likeCount,
	        Long commentCount,
	        String status,
	        Instant createdAt,
	        Instant updatedAt) {

}
