//package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;
//
//
//
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.CreatePostRequest;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.PostResponse;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Post;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.PostRepository;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class PostServiceImpl implements PostService {
//
//    private final PostRepository postRepository;
//
//    @Override
//    public PostResponse createPost(CreatePostRequest request) {
//        Post post = Post.builder()
//                .topic(request.getTopic())
//                .description(request.getDescription())
//                .shortDescription(request.getShortDescription())
//                .pictureUrl(request.getPictureUrl())
//                .postedBy(request.getPostedBy())
//                .tagVillage(request.getTagVillage())
//                .build();
//
//        postRepository.save(post);
//        return mapToResponse(post);
//    }
//
//
//     
//    @Override
//    public PostResponse createPostWithImage(
//            String topic,
//            String description,
//            String shortDescription,
//            String postedBy,
//            String tagVillage,
//            MultipartFile image
//    ) {
//        try {
//            // SAVE IMAGE FILE
//            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
//            String uploadPath = "uploads/" + fileName;
//
//            Path path = Paths.get(uploadPath);
//            Files.write(path, image.getBytes());
//
//            // PUBLIC URL
//            String imageUrl = "http://192.168.29.115:8080/uploads/" + fileName;
//
//            // BUILD POST OBJECT
//            Post post = Post.builder()
//                    .topic(topic)
//                    .description(description)
//                    .shortDescription(shortDescription)
//                    .postedBy(postedBy)
//                    .tagVillage(tagVillage)
//                    .pictureUrl(imageUrl)
//                    .build();
//
//            postRepository.save(post);
//            return mapToResponse(post);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error while saving image: " + e.getMessage());
//        }
//    }
//     
//    @Override
//    public List<PostResponse> getAllPosts() {
//        return postRepository.findAll()
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//
//    @Override
//    public PostResponse getPostById(UUID id) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//        return mapToResponse(post);
//    }
//
//    @Override
//    public PostResponse updatePost(UUID id, CreatePostRequest request) {
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//
//        post.setTopic(request.getTopic());
//        post.setDescription(request.getDescription());
//        post.setShortDescription(request.getShortDescription());
//        post.setPictureUrl(request.getPictureUrl());
//        post.setTagVillage(request.getTagVillage());
//        post.setPostedBy(request.getPostedBy());
//
//        postRepository.save(post);
//        return mapToResponse(post);
//    }
//
//    @Override
//    public void deletePost(UUID id) {
//        postRepository.deleteById(id);
//    }
//
//    private PostResponse mapToResponse(Post post) {
//        return PostResponse.builder()
//                .id(post.getId())
//                .topic(post.getTopic())
//                .description(post.getDescription())
//                .shortDescription(post.getShortDescription())
//                .pictureUrl(post.getPictureUrl())
//                .postedBy(post.getPostedBy())
//                .tagVillage(post.getTagVillage())
//                .datePosted(post.getDatePosted())
//                .build();
//    }
//    
//    @Override
//    public List<PostResponse> getPaginatedPosts(int page, int limit) {
//
//        int pageNumber = page - 1; // Page starts from 1 in UI
//
//        Pageable pageable = PageRequest.of(pageNumber, limit);
//
//        return postRepository.findAll(pageable)
//                .stream()
//                .map(this::mapToResponse)
//                .toList();
//    }
//
//}

package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.CreatePostRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.PostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Post;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.PostRepository;
import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CloudinaryService cloudinaryService; // ⬅️ Added

    // Create post without image
    @Override
    public PostResponse createPost(CreatePostRequest request) {

        Post post = Post.builder()
                .topic(request.getTopic())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .pictureUrl(request.getPictureUrl())
                .postedBy(request.getPostedBy())
                .tagVillage(request.getTagVillage())
                .build();

        postRepository.save(post);
        return mapToResponse(post);
    }

    // Create post WITH IMAGE UPLOAD (Cloudinary)
    @Override
    public PostResponse createPostWithImage(
            String topic,
            String description,
            String shortDescription,
            String postedBy,
            String tagVillage,
            MultipartFile image
    ) {
        try {
            // 1️⃣ Upload image to Cloudinary
            String imageUrl = cloudinaryService.uploadFile(image);

            // 2️⃣ Build and save post
            Post post = Post.builder()
                    .topic(topic)
                    .description(description)
                    .shortDescription(shortDescription)
                    .postedBy(postedBy)
                    .tagVillage(tagVillage)
                    .pictureUrl(imageUrl)  // store cloudinary URL
                    .build();

            postRepository.save(post);
            return mapToResponse(post);

        } catch (Exception e) {
            throw new RuntimeException("Error while uploading image: " + e.getMessage());
        }
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PostResponse getPostById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToResponse(post);
    }

    @Override
    public PostResponse updatePost(UUID id, CreatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setTopic(request.getTopic());
        post.setDescription(request.getDescription());
        post.setShortDescription(request.getShortDescription());
        post.setPictureUrl(request.getPictureUrl());
        post.setTagVillage(request.getTagVillage());
        post.setPostedBy(request.getPostedBy());

        postRepository.save(post);
        return mapToResponse(post);
    }

    @Override
    public void deletePost(UUID id) {
        postRepository.deleteById(id);
    }

    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .topic(post.getTopic())
                .description(post.getDescription())
                .shortDescription(post.getShortDescription())
                .pictureUrl(post.getPictureUrl())
                .postedBy(post.getPostedBy())
                .tagVillage(post.getTagVillage())
                .datePosted(post.getDatePosted())
                .build();
    }

    @Override
    public List<PostResponse> getPaginatedPosts(int page, int limit) {
        int pageNumber = page - 1; // UI starts from 1

        Pageable pageable = PageRequest.of(pageNumber, limit);

        return postRepository.findAll(pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}

