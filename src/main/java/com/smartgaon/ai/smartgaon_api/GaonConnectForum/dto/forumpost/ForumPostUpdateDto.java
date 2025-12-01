package com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost;

import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;
public record ForumPostUpdateDto(
		  String title,
	        String content,
	        String category,
	        Set<String> tags,
//	        List<MultipartFile> newMediaFiles,
	        List<String> mediaAttachments	
	        ) 
{}
