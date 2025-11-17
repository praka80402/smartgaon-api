package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.CreatePostRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.PostResponse;

public interface PostService {
	
	PostResponse createPost(CreatePostRequest request);
	
	  PostResponse createPostWithImage(
	            String topic,
	            String description,
	            String shortDescription,
	            String postedBy,
	            String tagVillage,
	            MultipartFile image
	    );

    List<PostResponse> getAllPosts();

    PostResponse getPostById(UUID id);

    PostResponse updatePost(UUID id, CreatePostRequest request);

    void deletePost(UUID id);
    List<PostResponse> getPaginatedPosts(int page, int limit);

    
    

}
