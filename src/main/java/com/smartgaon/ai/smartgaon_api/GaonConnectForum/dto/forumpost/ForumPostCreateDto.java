package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost;

import java.util.List;
import java.util.Set;

public record ForumPostCreateDto (
	  Long userId,
      String title,
      String content,
      String category,
      String area, 
//      Set<String> tags,
      List<String> mediaAttachments
) {}

