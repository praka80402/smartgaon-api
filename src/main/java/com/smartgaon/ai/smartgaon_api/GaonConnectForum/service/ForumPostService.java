package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;

public interface ForumPostService {
	
	 ForumPostResponse create(ForumPostCreateDto dto);

	 ForumPostResponse getById(Long postId);

	    Page<ForumPostResponse> list(Pageable pageable);

	    Page<ForumPostResponse> search(String query, Pageable pageable);

	    ForumPostResponse update(Long postId, ForumPostUpdateDto dto);

	    ForumPostResponse like(Long postId);

	    ForumPostResponse incrementCommentCount(Long postId);

	    ForumPostResponse moderate(Long postId, String status);

	    void delete(Long postId);

}
